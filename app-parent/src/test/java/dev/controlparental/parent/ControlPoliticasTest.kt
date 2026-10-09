package dev.controlparental.parent

import dev.controlparental.parent.data.*
import dev.controlparental.parent.policy.*
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

class ControlPoliticasTest {
    private class Archivo : ArchivoMetadatos {
        var bytes: ByteArray? = null
        var despues: () -> Unit = {}
        var fallar = false
        override fun existe() = bytes != null
        override fun leer() = bytes!!.copyOf()
        override fun escribir(bytes: ByteArray) { check(!fallar); this.bytes = bytes.copyOf(); despues() }
    }
    private class Entorno(scope: CoroutineScope) : CanalPoliticas {
        var tiempo = 0L
        var ticket: Long? = 1L
        var autoridad = true
        var despuesDeFirma: () -> Unit = {}
        var firmas = 0
        val archivo = Archivo()
        val padre = par()
        val hijo = par()
        val datos = MetadatosPadre(parentPublicKeyB64 = b64(padre.public.encoded),
            pairId = ID, childPublicKeyB64 = b64(hijo.public.encoded), relayOrigin = "https://prueba.invalid",
            vinculo = VinculoPersistido.VINCULADO)
        val aes = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val cifrador = CifradoMetadatos({}, { aes })
        val store = ParentStore(archivo, cifrador, scope)
        val enviados = mutableListOf<ByteArray>()
        val control = ControlPoliticas(store, this, { tiempo }, { check(autoridad) }, { mensaje ->
            firmas++; firmar(mensaje, Rol.PARENT).also { despuesDeFirma() }
        })
        override fun actual() = ticket
        override fun enviar(ticket: Long, frame: ByteArray): Boolean {
            if (ticket != this.ticket) return false
            enviados.add(frame.copyOf()); return true
        }
        fun firmar(mensaje: Mensaje, rol: Rol): SobreFirmado {
            val par = if (rol == Rol.PARENT) padre else hijo
            return CriptografiaProtocolo.firmarSobre(mensaje, Proposito.MESSAGE,
                ClaveVerificacion.desdeSpki(rol, b64(par.public.encoded))) { bytes ->
                Signature.getInstance("SHA256withECDSA").run { initSign(par.private); update(bytes); sign() }
            }
        }
        fun mensaje(frame: ByteArray) = CodecProtocolo.leerPayloadSinVerificar(
            CodecProtocolo.bytesPayload(CodecProtocolo.leerFrame(frame) as SobreFirmado), Proposito.MESSAGE)
        fun estado(seq: String = "1"): EstadoDispositivo {
            val sync = enviados.map(::mensaje).filterIsInstance<SolicitudSincronizacion>().last()
            return EstadoDispositivo(ID, sync.syncNonce, BOOT, SESION, seq, "0", null, null, ResultadoEstado.NONE,
                Modo.LOCKED, Motivo.PARENT_LOCK, null, true, true, true, true, "A".repeat(43))
        }
        suspend fun recibir(mensaje: Mensaje) = control.recibir(ticket!!, CodecProtocolo.codificarFrame(firmar(mensaje, Rol.CHILD)))
        suspend fun preparar() {
            store.inicializar(datos); control.sincronizar(); recibir(estado())
        }
        suspend fun enviar(): OrdenPolitica {
            val confirmacion = control.prepararIntencion(Accion.LOCK_FOR, 60)
            control.enviarConfirmada(confirmacion)
            return enviados.map(::mensaje).filterIsInstance<OrdenPolitica>().last()
        }
        fun hashOrden(): String = CriptografiaProtocolo.verificarSobre(enviados.last(),
            ClaveVerificacion.desdeSpki(Rol.PARENT, datos.parentPublicKeyB64), Proposito.MESSAGE).payloadSha256B64
        fun ack(orden: OrdenPolitica, resultado: ResultadoAck = ResultadoAck.APPLIED) = Confirmacion(ID, BOOT, SESION,
            orden.commandId, orden.seq, hashOrden(), resultado, Modo.LOCKED, Motivo.PARENT_LOCK, 60)
    }

    @Test fun syncFirmadaNoConsumeSecuenciaYRechazaNonceParejaOFirmaAjenos() = runTest {
        val e = Entorno(backgroundScope); e.store.inicializar(e.datos); e.control.sincronizar()
        assertEquals("0", e.store.leer().contadorLocal)
        assertTrue(runCatching { e.recibir(e.estado().copy(syncNonce = "B".repeat(43))) }.isFailure)
        assertTrue(runCatching { e.recibir(e.estado().copy(pairId = BOOT)) }.isFailure)
        val intruso = Entorno(backgroundScope)
        val falsa = CodecProtocolo.codificarFrame(intruso.firmar(e.estado(), Rol.CHILD))
        assertTrue(runCatching { e.control.recibir(1, falsa) }.exceptionOrNull() is FirmaInvalida)
        e.recibir(e.estado())
        assertEquals(FasePolitica.ESTADO_VERIFICADO, e.control.resultado.value.fase)
        e.store.cerrar(); intruso.store.cerrar()
    }

    @Test fun requiereConfirmacionReservaMaxRemotoYPersisteAntesDeEnviar() = runTest {
        val e = Entorno(backgroundScope); e.preparar()
        e.recibir(e.estado("2").copy(lastAcceptedSeq = "9", lastCommandId = BOOT,
            lastCommandPayloadSha256B64 = "A".repeat(43), lastResult = ResultadoEstado.APPLIED))
        val confirmacion = e.control.prepararIntencion(Accion.LOCK_FOR, 60)
        assertNull(e.store.leer().pendiente)
        assertTrue(runCatching { e.control.enviarConfirmada(ConfirmacionIntencion(Accion.LOCK_FOR, 60)) }.isFailure)
        e.control.enviarConfirmada(confirmacion)
        val pendiente = e.store.leer().pendiente!!
        assertEquals("10", pendiente.seq); assertEquals(EtapaIntencion.ENVIADA_SIN_CONFIRMAR, pendiente.etapa)
        assertArrayEquals(e.enviados.last(), Base64.getUrlDecoder().decode(pendiente.frameB64))
        assertTrue(runCatching { e.control.enviarConfirmada(confirmacion) }.isFailure)
        e.store.cerrar()
    }

    @Test fun ackDebeCoincidirConIdSecuenciaHashArranqueYSesion() = runTest {
        val e = Entorno(backgroundScope); e.preparar(); val orden = e.enviar(); val ack = e.ack(orden)
        for (ajeno in listOf(ack.copy(commandId = BOOT), ack.copy(seq = "8"),
            ack.copy(commandPayloadSha256B64 = "B".repeat(43)), ack.copy(bootId = ID), ack.copy(childSessionId = ID))) {
            assertTrue(runCatching { e.recibir(ajeno) }.isFailure)
            assertNotNull(e.store.leer().pendiente)
        }
        e.recibir(ack)
        assertEquals(FasePolitica.APLICADA, e.control.resultado.value.fase)
        assertNull(e.store.leer().pendiente); assertEquals("1", e.store.leer().contadorLocal)
        assertTrue(runCatching { e.recibir(ack.copy(result = ResultadoAck.ACCEPTED_PENDING_ENFORCEMENT)) }.isFailure)
        assertEquals(FasePolitica.APLICADA, e.control.resultado.value.fase)
        e.store.cerrar()
    }

    @Test fun sinAckRetransmiteSoloDosVecesLosMismosBytesYLuegoSincroniza() = runTest {
        val e = Entorno(backgroundScope); e.preparar(); e.enviar()
        val original = e.enviados.last().copyOf()
        e.tiempo = 5_000; e.control.avanzarReloj(); assertArrayEquals(original, e.enviados.last())
        e.tiempo = 10_000; e.control.avanzarReloj(); assertArrayEquals(original, e.enviados.last())
        assertEquals(2, e.firmas) // SYNC y orden; los reintentos no firman.
        e.tiempo = 15_000; e.control.avanzarReloj()
        assertTrue(e.mensaje(e.enviados.last()) is SolicitudSincronizacion)
        assertEquals(3, e.enviados.map(e::mensaje).filterIsInstance<OrdenPolitica>().size)
        assertNotNull(e.store.leer().pendiente); assertEquals("1", e.store.leer().contadorLocal)
        e.store.cerrar()
    }

    companion object {
        const val ID = "00000000-0000-4000-8000-000000000002"
        const val BOOT = "00000000-0000-4000-8000-000000000003"
        const val SESION = "00000000-0000-4000-8000-000000000004"
        fun b64(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        fun par() = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    }
}
