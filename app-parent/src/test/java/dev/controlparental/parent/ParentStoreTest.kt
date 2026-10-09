package dev.controlparental.parent

import dev.controlparental.parent.data.*
import dev.controlparental.parent.security.CifradoMetadatos
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import javax.crypto.KeyGenerator
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ParentStoreTest {
    // Dobles exclusivamente JVM: no sustituyen AtomicFile ni Keystore físicos.
    private class ArchivoFalso : ArchivoMetadatos {
        var bytes: ByteArray? = null
        var fallo = 0
        var despuesDeGuardar: () -> Unit = {}
        override fun existe() = bytes != null
        override fun leer() = bytes!!.copyOf()
        override fun escribir(bytes: ByteArray) {
            if (fallo == 1) error("Fallo antes de commit ficticio")
            this.bytes = bytes.copyOf()
            despuesDeGuardar()
            if (fallo == 2) error("Fallo después de commit ficticio")
        }
    }

    private class Entorno {
        val archivo = ArchivoFalso()
        val par = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        val publica = Base64.getUrlEncoder().withoutPadding().encodeToString(par.public.encoded)
        val parHijo = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        val publicaHijo = Base64.getUrlEncoder().withoutPadding().encodeToString(parHijo.public.encoded)
        val clave = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        var preparaciones = 0
        val cifrador = CifradoMetadatos({ preparaciones++ }, { clave })
        fun datos() = MetadatosPadre(parentPublicKeyB64 = publica,
            pairId = "00000000-0000-4000-8000-000000000002", childPublicKeyB64 = publicaHijo,
            relayOrigin = "https://prueba.invalid", vinculo = VinculoPersistido.VINCULADO)

        fun firmar(mensaje: Mensaje, rol: Rol): ByteArray {
            val parFirma = if (rol == Rol.PARENT) par else parHijo
            val clavePublica = if (rol == Rol.PARENT) publica else publicaHijo
            val sobre = CriptografiaProtocolo.firmarSobre(mensaje, Proposito.MESSAGE,
                ClaveVerificacion.desdeSpki(rol, clavePublica)) { bytes ->
                Signature.getInstance("SHA256withECDSA").run { initSign(parFirma.private); update(bytes); sign() }
            }
            return CodecProtocolo.codificarFrame(sobre)
        }

        fun orden(reserva: PendingIntentRecord) = OrdenPolitica(datos().pairId!!,
            "00000000-0000-4000-8000-000000000003", "00000000-0000-4000-8000-000000000004",
            reserva.commandId, "A".repeat(43), reserva.seq, reserva.action, reserva.durationSec)
    }

    @Test fun reservaConsumeContadorYCancelaSinReutilizarSecuencia() = runTest {
        val entorno = Entorno()
        val store = ParentStore(entorno.archivo, entorno.cifrador, backgroundScope)
        store.inicializar(entorno.datos())
        val primera = store.reservarIntencion(Accion.LOCK, null, "9")
        assertEquals("10", primera.seq)
        store.cancelarIntencion(primera.commandId)
        assertEquals("10", store.leer().contadorLocal)
        assertNull(store.leer().pendiente)
        val segunda = store.reservarIntencion(Accion.ALLOW_FOR, 60, "1")
        assertEquals("11", segunda.seq)
        assertNotEquals(primera.commandId, segunda.commandId)
        assertEquals(1, entorno.preparaciones)
        store.cerrar()
    }

    @Test fun cierreAntesDeEnvioMantieneReservaYNoReutilizaSecuencia() = runTest {
        val e = Entorno()
        val original = ParentStore(e.archivo, e.cifrador, backgroundScope)
        original.inicializar(e.datos())
        val reserva = original.reservarIntencion(Accion.LOCK, null, "0")
        original.cerrar()
        val reabierto = ParentStore(e.archivo, e.cifrador, backgroundScope)
        assertEquals(reserva, reabierto.leer().pendiente)
        assertTrue(runCatching { reabierto.reservarIntencion(Accion.ALLOW, null, "0") }.isFailure)
        reabierto.cancelarIntencion(reserva.commandId)
        assertEquals("2", reabierto.reservarIntencion(Accion.LOCK, null, "0").seq)
        reabierto.cerrar()
    }

    @Test fun falloAntesDeCommitBloqueaInstanciaYRecuperaEstadoAnterior() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        e.archivo.fallo = 1
        assertTrue(runCatching { store.reservarIntencion(Accion.LOCK, null, "0") }.exceptionOrNull() is AlmacenNoDisponible)
        assertTrue(runCatching { store.leer() }.exceptionOrNull() is AlmacenNoDisponible)
        store.cerrar()
        e.archivo.fallo = 0
        val recuperado = ParentStore(e.archivo, e.cifrador, backgroundScope)
        assertEquals("0", recuperado.leer().contadorLocal)
        assertNull(recuperado.leer().pendiente)
        recuperado.cerrar()
    }

    @Test fun errorPosteriorACommitNoPermiteReservarOtraOrdenSobreLaPersistida() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        e.archivo.fallo = 2
        assertTrue(runCatching { store.reservarIntencion(Accion.LOCK, null, "0") }.isFailure)
        store.cerrar()
        e.archivo.fallo = 0
        val recuperado = ParentStore(e.archivo, e.cifrador, backgroundScope)
        assertEquals("1", recuperado.leer().contadorLocal)
        assertEquals(Accion.LOCK, recuperado.leer().pendiente!!.action)
        assertTrue(runCatching { recuperado.reservarIntencion(Accion.ALLOW, null, "0") }.isFailure)
        recuperado.cerrar()
    }

    @Test fun solicitudesConcurrentesConservanUnaSolaIntencion() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        val solicitudes = (1..12).map { async { runCatching { store.reservarIntencion(Accion.LOCK, null, "0") } } }
        val resultados = solicitudes.map { it.await() }
        assertEquals(1, resultados.count { it.isSuccess })
        assertEquals("1", store.leer().contadorLocal)
        store.cerrar()
    }

    @Test fun cancelacionDelSolicitanteDespuesDeCommitNoDeshaceLaReserva() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        var solicitante: kotlinx.coroutines.Job? = null
        e.archivo.despuesDeGuardar = { solicitante!!.cancel() }
        solicitante = launch { store.reservarIntencion(Accion.LOCK, null, "0") }
        solicitante.join()
        e.archivo.despuesDeGuardar = {}
        assertEquals("1", store.leer().contadorLocal)
        assertNotNull(store.leer().pendiente)
        store.cerrar()
    }

    @Test fun agotamientoYDuracionesInvalidasNoEscriben() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        for (remota in listOf(Long.MAX_VALUE.toString(), "-1", "01", "9223372036854775808")) {
            assertTrue(runCatching { store.reservarIntencion(Accion.LOCK, null, remota) }.isFailure)
        }
        assertTrue(runCatching { store.reservarIntencion(Accion.ALLOW_FOR, 59, "0") }.isFailure)
        assertEquals("0", store.leer().contadorLocal)
        assertNull(store.leer().pendiente)
        store.cerrar()
    }

    @Test fun cifradoAlteradoYReinstalacionNoReinicializanDatos() = runTest {
        for (perderClave in listOf(false, true)) {
            val e = Entorno()
            val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
            store.inicializar(e.datos())
            store.cerrar()
            if (!perderClave) e.archivo.bytes!![30] = (e.archivo.bytes!![30].toInt() xor 1).toByte()
            val claveNueva = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
            val cifrador = if (perderClave) CifradoMetadatos({ error("No debe preparar una alternativa") }, { claveNueva }) else e.cifrador
            val reabierto = ParentStore(e.archivo, cifrador, backgroundScope)
            assertTrue(runCatching { reabierto.leer() }.exceptionOrNull() is AlmacenNoDisponible)
            assertTrue(runCatching { reabierto.inicializar(e.datos()) }.isFailure)
            assertEquals(1, e.preparaciones)
            reabierto.cerrar()
        }
    }

    @Test fun comandoFirmadoSeConservaExactoYNoSeEnviaNiReenviaAlReabrir() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        val reserva = store.reservarIntencion(Accion.LOCK, null, "0")
        val frame = e.firmar(e.orden(reserva), Rol.PARENT)
        assertTrue(runCatching { store.marcarEnviada(reserva.commandId) }.isFailure)
        val cambiado = e.firmar(e.orden(reserva).copy(action = Accion.ALLOW), Rol.PARENT)
        assertTrue(runCatching { store.guardarComandoFirmado(reserva.commandId, cambiado) }.isFailure)
        store.guardarComandoFirmado(reserva.commandId, frame)
        store.marcarEnviada(reserva.commandId)
        store.cerrar()
        val reabierto = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val recuperada = reabierto.leer().pendiente!!
        assertEquals(EtapaIntencion.ENVIADA_SIN_CONFIRMAR, recuperada.etapa)
        assertArrayEquals(frame, Base64.getUrlDecoder().decode(recuperada.frameB64))
        reabierto.cerrar()
    }

    @Test fun cancelarNoBorraEvidenciaRemotaNiLaConfundeConAplicacionNueva() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        val reserva = store.reservarIntencion(Accion.LOCK, null, "0")
        val ack = Confirmacion(e.datos().pairId!!,
            "00000000-0000-4000-8000-000000000003", "00000000-0000-4000-8000-000000000004",
            reserva.commandId, reserva.seq, "A".repeat(43), ResultadoAck.APPLIED, Modo.LOCKED, Motivo.PARENT_LOCK, null)
        val frame = e.firmar(ack, Rol.CHILD)
        val documento = CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.CHILD, e.publicaHijo), Proposito.MESSAGE)
        store.retenerEvidencia(documento)
        store.cancelarIntencion(reserva.commandId)
        assertNull(store.leer().pendiente)
        assertEquals("1", store.leer().contadorLocal)
        assertArrayEquals(frame, Base64.getUrlDecoder().decode(store.leer().ultimaEvidenciaFrameB64))
        store.cerrar()
    }

    @Test fun versionDesconocidaYJSONDuplicadoNoAbrenEnrolamiento() = runTest {
        for (duplicar in listOf(false, true)) {
            val e = Entorno()
            val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
            store.inicializar(e.datos())
            store.cerrar()
            val texto = e.cifrador.descifrar(e.archivo.bytes!!).toString(Charsets.UTF_8)
            val invalido = if (duplicar) texto.replace("\"schemaVersion\":3", "\"schemaVersion\":3,\"schemaVersion\":3")
                else texto.replace("\"schemaVersion\":3", "\"schemaVersion\":999")
            e.archivo.bytes = e.cifrador.cifrar(invalido.toByteArray())
            val recuperado = ParentStore(e.archivo, e.cifrador, backgroundScope)
            assertTrue(runCatching { recuperado.leer() }.exceptionOrNull() is AlmacenNoDisponible)
            assertTrue(runCatching { recuperado.inicializar(e.datos()) }.isFailure)
            recuperado.cerrar()
        }
    }

    @Test fun intencionOfflinePersisteSinReservarFirmarNiReemplazarSinConfirmacion() = runTest {
        val e = Entorno(); val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        val local = store.guardarIntencionLocal(Accion.ALLOW_FOR, 60)
        assertEquals("0", store.leer().contadorLocal); assertNull(store.leer().pendiente)
        assertTrue(runCatching { store.guardarIntencionLocal(Accion.LOCK, null) }.isFailure)
        store.cerrar()
        val reabierto = ParentStore(e.archivo, e.cifrador, backgroundScope)
        assertEquals(local, reabierto.leer().intencionLocal)
        val reemplazo = reabierto.guardarIntencionLocal(Accion.LOCK_FOR, 604800, local.id)
        assertNotEquals(local.id, reemplazo.id)
        assertTrue(runCatching { reabierto.reservarIntencion(Accion.LOCK_FOR, 604800, "9", local.id) }.isFailure)
        val reserva = reabierto.reservarIntencion(Accion.LOCK_FOR, 604800, "9", reemplazo.id)
        assertEquals("10", reserva.seq); assertNull(reabierto.leer().intencionLocal)
        assertTrue(runCatching { reabierto.guardarIntencionLocal(Accion.ALLOW, null) }.isFailure)
        reabierto.cerrar()
    }

    @Test fun descartarSeleccionLocalNoConsumeContadorYPreparadoNoLaHabilita() = runTest {
        val e = Entorno(); val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        val local = store.guardarIntencionLocal(Accion.LOCK, null)
        assertTrue(runCatching { store.descartarIntencionLocal("otro") }.isFailure)
        store.descartarIntencionLocal(local.id)
        assertNull(store.leer().intencionLocal); assertEquals("0", store.leer().contadorLocal)
        store.cerrar()
        val preparado = Entorno(); val otro = ParentStore(preparado.archivo, preparado.cifrador, backgroundScope)
        otro.inicializar(preparado.datos().copy(vinculo = VinculoPersistido.PREPARADO))
        assertTrue(runCatching { otro.guardarIntencionLocal(Accion.ALLOW, null) }.isFailure)
        otro.cerrar()
    }

    @Test fun migraV2SinPerderComandoOEvidenciaNiInventarIntencionOffline() = runTest {
        val e = Entorno(); val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        store.inicializar(e.datos())
        val reserva = store.reservarIntencion(Accion.LOCK, null, "8")
        val frame = e.firmar(e.orden(reserva), Rol.PARENT)
        store.guardarComandoFirmado(reserva.commandId, frame); store.marcarEnviada(reserva.commandId)
        val ordenDoc = CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.PARENT, e.publica), Proposito.MESSAGE)
        val ack = Confirmacion(e.datos().pairId!!, "00000000-0000-4000-8000-000000000003",
            "00000000-0000-4000-8000-000000000004", reserva.commandId, reserva.seq,
            ordenDoc.payloadSha256B64, ResultadoAck.ACCEPTED_PENDING_ENFORCEMENT, Modo.LOCKED, Motivo.PARENT_LOCK, null)
        store.retenerEvidencia(CriptografiaProtocolo.verificarSobre(e.firmar(ack, Rol.CHILD),
            ClaveVerificacion.desdeSpki(Rol.CHILD, e.publicaHijo), Proposito.MESSAGE))
        val antes = store.leer(); store.cerrar()
        val v2 = e.cifrador.descifrar(e.archivo.bytes!!).toString(Charsets.UTF_8)
            .replace("\"schemaVersion\":3", "\"schemaVersion\":2").replace(",\"intencionLocal\":null", "")
        e.archivo.bytes = e.cifrador.cifrar(v2.toByteArray())
        val reabierto = ParentStore(e.archivo, e.cifrador, backgroundScope)
        assertEquals(antes, reabierto.leer()); assertNull(reabierto.leer().intencionLocal)
        assertEquals(3, reabierto.leer().schemaVersion)
        reabierto.cerrar()
    }
}
