package dev.controlparental.parent

import dev.controlparental.parent.data.*
import dev.controlparental.parent.recovery.*
import dev.controlparental.parent.security.CifradoMetadatos
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import javax.crypto.KeyGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class OfflineChallengeSignerTest {
    private class Entorno(scope: CoroutineScope) {
        var tiempo = 0L
        var autoridad = true
        var duranteFirma: () -> Unit = {}
        var firmas = 0
        val padre = par(); val hijo = par()
        val archivo = object : ArchivoMetadatos {
            var bytes: ByteArray? = null
            override fun existe() = bytes != null
            override fun leer() = bytes!!.copyOf()
            override fun escribir(bytes: ByteArray) { this.bytes = bytes.copyOf() }
        }
        val aes = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val store = ParentStore(archivo, CifradoMetadatos({}, { aes }), scope)
        val datos = MetadatosPadre(parentPublicKeyB64 = b64(padre.public.encoded), pairId = ID,
            childPublicKeyB64 = b64(hijo.public.encoded), relayOrigin = "https://prueba.invalid",
            vinculo = VinculoPersistido.PREPARADO)
        val reto = RetoOffline(ID, BOOT, AccionOffline.MAINTENANCE, "A".repeat(43))
        val flujo = OfflineChallengeSigner(store, { tiempo }, { check(autoridad) }, {
            firmas++; duranteFirma(); firmar(it, Rol.PARENT)
        })
        fun firmar(mensaje: Mensaje, rol: Rol, proposito: Proposito = Proposito.OFFLINE): SobreFirmado {
            val par = if (rol == Rol.PARENT) padre else hijo
            return CriptografiaProtocolo.firmarSobre(mensaje, proposito,
                ClaveVerificacion.desdeSpki(rol, b64(par.public.encoded))) { bytes ->
                Signature.getInstance("SHA256withECDSA").run { initSign(par.private); update(bytes); sign() }
            }
        }
        fun frame(mensaje: Mensaje = reto) = CodecProtocolo.codificarFrame(firmar(mensaje, Rol.CHILD))
        suspend fun preparar(mensaje: Mensaje = reto): PreparacionOffline {
            if (!store.existe()) store.inicializar(datos)
            return flujo.preparar(frame(mensaje))
        }
        fun verificar(frame: ByteArray) = CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.PARENT, datos.parentPublicKeyB64), Proposito.OFFLINE).mensaje as RespuestaOffline
    }

    @Test fun respuestaExactaOfflineSinContadorNiPromocionDeVinculo() = runTest {
        val e = Entorno(backgroundScope); val p = e.preparar(); val antes = e.store.leer()
        val respuesta = e.verificar(e.flujo.responder(p, false))
        assertEquals(RespuestaOffline(ID, BOOT, AccionOffline.MAINTENANCE, e.reto.nonce), respuesta)
        assertEquals(antes, e.store.leer()); assertEquals(1, e.firmas)
        assertTrue(runCatching { e.flujo.responder(p, false) }.isFailure)
        e.store.cerrar()
    }

    @Test fun retiroExigeAdvertenciaYNoConvierteMantenimientoEnRetiro() = runTest {
        val e = Entorno(backgroundScope); val p = e.preparar(e.reto.copy(action = AccionOffline.RETIRE))
        assertTrue(runCatching { e.flujo.responder(p, false) }.isFailure); assertEquals(0, e.firmas)
        assertEquals(AccionOffline.RETIRE, e.verificar(e.flujo.responder(p, true)).action)
        assertTrue(runCatching { e.firmar(OrdenPolitica(ID, BOOT, BOOT, ID, e.reto.nonce, "1", Accion.ALLOW, null), Rol.PARENT) }.isFailure)
        e.store.cerrar()
    }

    @Test fun parejaClaveTipoYPropositoAjenosSeRechazanAntesDeFirmar() = runTest {
        val e = Entorno(backgroundScope); e.preparar()
        assertTrue(runCatching { e.flujo.preparar(e.frame(e.reto.copy(pairId = BOOT))) }.isFailure)
        val intruso = Entorno(backgroundScope)
        assertTrue(runCatching { e.flujo.preparar(intruso.frame()) }.exceptionOrNull() is FirmaInvalida)
        val respuesta = CodecProtocolo.codificarFrame(e.firmar(RespuestaOffline(ID, BOOT, AccionOffline.MAINTENANCE, e.reto.nonce), Rol.PARENT))
        assertTrue(runCatching { e.flujo.preparar(respuesta) }.isFailure)
        val auth = CodecProtocolo.codificarFrame(e.firmar(PruebaAutenticacion(ID, Rol.CHILD, BOOT, e.reto.nonce), Rol.CHILD, Proposito.AUTH))
        assertTrue(runCatching { e.flujo.preparar(auth) }.isFailure)
        assertEquals(0, e.firmas); e.store.cerrar(); intruso.store.cerrar()
    }

    @Test fun descartarCambiarEntradaOCaducarImpideResponderPreparacionAnterior() = runTest {
        val e = Entorno(backgroundScope); val antigua = e.preparar()
        e.flujo.descartar(); assertTrue(runCatching { e.flujo.responder(antigua, false) }.isFailure)
        val nueva = e.preparar(); e.preparar(e.reto.copy(nonce = "E".repeat(43)))
        assertTrue(runCatching { e.flujo.responder(nueva, false) }.isFailure)
        val caducada = e.preparar(); e.tiempo = 240_000
        assertEquals(0, e.flujo.tiempoRestante(caducada))
        assertTrue(runCatching { e.flujo.responder(caducada, false) }.isFailure)
        assertEquals(0, e.firmas); e.store.cerrar()
    }

    @Test fun faltaDeAutoridadOCancelacionDuranteFirmaNoPublicanToken() = runTest {
        for (durante in listOf(false, true)) {
            val e = Entorno(backgroundScope); val p = e.preparar()
            if (durante) e.duranteFirma = { e.flujo.descartar() } else e.autoridad = false
            assertTrue(runCatching { e.flujo.responder(p, false) }.isFailure)
            e.store.cerrar()
        }
    }

    @Test fun ventanaDePresentacionNoPermiteQrConAutoridadCaducada() = runTest {
        val e = Entorno(backgroundScope); val p = e.preparar()
        e.flujo.responder(p, false); e.flujo.exigirPresentacion()
        e.tiempo = 230_000; assertEquals(10_000, e.flujo.tiempoRestante(p))
        e.autoridad = false; assertTrue(runCatching { e.flujo.exigirPresentacion() }.isFailure)
        e.store.cerrar()
    }

    @Test fun hijoSimuladoRechazaRepeticionArranqueDistintoYUsoCruzado() = runTest {
        val e = Entorno(backgroundScope); val p = e.preparar(); val token = e.flujo.responder(p, false)
        var consumido = false
        fun aceptar(frame: ByteArray, boot: String, action: AccionOffline): Boolean {
            val r = e.verificar(frame)
            if (consumido || r.bootId != boot || r.action != action || r.nonce != e.reto.nonce || r.pairId != ID) return false
            consumido = true; return true
        }
        assertFalse(aceptar(token, BOOT, AccionOffline.RETIRE))
        assertFalse(aceptar(token, ID, AccionOffline.MAINTENANCE))
        assertTrue(aceptar(token, BOOT, AccionOffline.MAINTENANCE))
        assertFalse(aceptar(token, BOOT, AccionOffline.MAINTENANCE))
        e.store.cerrar()
    }

    companion object {
        const val ID = "00000000-0000-4000-8000-000000000002"
        const val BOOT = "00000000-0000-4000-8000-000000000003"
        fun b64(bytes: ByteArray) = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        fun par() = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    }
}
