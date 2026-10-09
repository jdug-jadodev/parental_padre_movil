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
import dev.controlparental.parent.network.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async

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
        fun firmar(mensaje: Mensaje, rol: Rol, proposito: Proposito = Proposito.MESSAGE): SobreFirmado {
            val par = if (rol == Rol.PARENT) padre else hijo
            return CriptografiaProtocolo.firmarSobre(mensaje, proposito,
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
        fun hashOrden(): String = CriptografiaProtocolo.verificarSobre(enviados.last { mensaje(it) is OrdenPolitica },
            ClaveVerificacion.desdeSpki(Rol.PARENT, datos.parentPublicKeyB64), Proposito.MESSAGE).payloadSha256B64
        fun ack(orden: OrdenPolitica, resultado: ResultadoAck = ResultadoAck.APPLIED) = Confirmacion(ID, BOOT, SESION,
            orden.commandId, orden.seq, hashOrden(), resultado, Modo.LOCKED, Motivo.PARENT_LOCK, 60)
    }

    @Test fun syncFirmadaNoConsumeSecuenciaYRechazaNonceParejaOFirmaAjenos() = runTest {
        val e = Entorno(backgroundScope); e.store.inicializar(e.datos); e.control.sincronizar()
        assertEquals("0", e.store.leer().contadorLocal)
        assertTrue(runCatching { e.recibir(e.estado().copy(syncNonce = "E".repeat(43))) }.isFailure)
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
            ack.copy(commandPayloadSha256B64 = "E".repeat(43)), ack.copy(bootId = ID), ack.copy(childSessionId = ID))) {
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

    @Test fun ackPendienteNoEsAplicadoYDetieneRetransmisionHastaResultadoReal() = runTest {
        val e = Entorno(backgroundScope); e.preparar(); val orden = e.enviar(); val ack = e.ack(orden)
        e.recibir(ack.copy(result = ResultadoAck.ACCEPTED_PENDING_ENFORCEMENT))
        assertEquals(FasePolitica.PENDIENTE_APLICACION, e.control.resultado.value.fase)
        assertNotNull(e.store.leer().pendiente)
        e.tiempo = 10_000; e.control.avanzarReloj(); assertEquals(2, e.enviados.size)
        e.recibir(ack.copy(result = ResultadoAck.ENFORCEMENT_FAILED))
        assertEquals(FasePolitica.FALLO_APLICACION, e.control.resultado.value.fase)
        assertNull(e.store.leer().pendiente)
        e.store.cerrar()
    }

    @Test fun rechazoFirmadoEsRechazoNoAplicacion() = runTest {
        val e = Entorno(backgroundScope); e.preparar(); val orden = e.enviar()
        e.recibir(e.ack(orden, ResultadoAck.REJECTED).copy(reason = Motivo.STALE_NONCE))
        assertEquals(FasePolitica.RECHAZADA, e.control.resultado.value.fase)
        assertNull(e.store.leer().pendiente); assertEquals("1", e.store.leer().contadorLocal)
        e.store.cerrar()
    }

    @Test fun estadosFueraDeOrdenOSesionNuevaNoRenuevanConfirmacion() = runTest {
        val e = Entorno(backgroundScope); e.preparar()
        val confirmacion = e.control.prepararIntencion(Accion.LOCK, null)
        assertTrue(runCatching { e.recibir(e.estado("1")) }.isFailure)
        assertTrue(runCatching { e.recibir(e.estado("2").copy(childSessionId = ID)) }.isFailure)
        e.recibir(e.estado("2"))
        assertTrue(runCatching { e.control.enviarConfirmada(confirmacion) }.isFailure)
        e.control.sincronizar()
        assertTrue(runCatching { e.recibir(e.estado("2")) }.isFailure)
        e.recibir(e.estado("3"))
        e.store.cerrar()
    }

    @Test fun hijoNoAptoOPreparadoNoPermiteOrdenNiPromocionaVinculo() = runTest {
        for (caso in 0..4) {
            val e = Entorno(backgroundScope)
            e.store.inicializar(if (caso == 4) e.datos.copy(vinculo = VinculoPersistido.PREPARADO) else e.datos)
            e.control.sincronizar()
            val estado = when (caso) {
                0 -> e.estado().copy(isDeviceOwner = false)
                1 -> e.estado().copy(networkValidated = false)
                2 -> e.estado().copy(mode = Modo.SETUP)
                3 -> e.estado().copy(channelHealthy = false)
                else -> e.estado()
            }
            e.recibir(estado)
            assertTrue(runCatching { e.control.prepararIntencion(Accion.ALLOW, null) }.isFailure)
            assertNull(e.store.leer().pendiente); assertEquals("0", e.store.leer().contadorLocal)
            assertEquals(if (caso == 4) VinculoPersistido.PREPARADO else VinculoPersistido.VINCULADO, e.store.leer().vinculo)
            e.store.cerrar()
        }
    }

    @Test fun duracionInvalidaOContadorAgotadoNoEnviaOrden() = runTest {
        val e = Entorno(backgroundScope); e.preparar()
        for (duracion in listOf(59, 604801)) {
            assertTrue(runCatching { e.control.prepararIntencion(Accion.ALLOW_FOR, duracion) }.isFailure)
        }
        e.recibir(e.estado("2").copy(lastAcceptedSeq = Long.MAX_VALUE.toString(), lastCommandId = BOOT,
            lastCommandPayloadSha256B64 = "A".repeat(43), lastResult = ResultadoEstado.APPLIED))
        val confirmacion = e.control.prepararIntencion(Accion.LOCK, null)
        assertTrue(runCatching { e.control.enviarConfirmada(confirmacion) }.isFailure)
        assertEquals(1, e.enviados.size); assertEquals("0", e.store.leer().contadorLocal)
        e.store.cerrar()
    }

    @Test fun estadoCaducaDesdeSolicitudInclusoSiLlegaTardeYNoDesdeRecepcion() = runTest {
        val e = Entorno(backgroundScope); e.store.inicializar(e.datos); e.control.sincronizar()
        e.tiempo = 19_000; e.recibir(e.estado())
        val confirmacion = e.control.prepararIntencion(Accion.LOCK, null)
        e.tiempo = 20_000
        assertTrue(runCatching { e.control.enviarConfirmada(confirmacion) }.isFailure)
        assertNull(e.store.leer().pendiente)
        e.control.avanzarReloj(); assertEquals(FasePolitica.SIN_ESTADO, e.control.resultado.value.fase)
        e.store.cerrar()
    }

    @Test fun fondoDuranteReservaFirmaOPersistenciaNoEnviaYConservaPendiente() = runTest {
        for (etapa in 0..2) {
            val e = Entorno(backgroundScope); e.preparar()
            val confirmacion = e.control.prepararIntencion(Accion.LOCK, null)
            if (etapa == 1) e.despuesDeFirma = { e.ticket = null }
            else {
                var escrituras = 0
                e.archivo.despues = { if (++escrituras == (if (etapa == 0) 1 else 2)) e.ticket = null }
            }
            assertTrue(runCatching { e.control.enviarConfirmada(confirmacion) }.isFailure)
            assertEquals(1, e.enviados.size)
            val pendiente = e.store.leer().pendiente!!
            assertEquals("1", pendiente.seq)
            assertEquals(if (etapa == 2) EtapaIntencion.FIRMADA else EtapaIntencion.RESERVADA, pendiente.etapa)
            e.store.cerrar()
        }
    }

    @Test fun autoridadCaducadaDuranteFirmaNoEnviaYFalloDeArchivoNoEnvia() = runTest {
        for (archivo in listOf(false, true)) {
            val e = Entorno(backgroundScope); e.preparar()
            val confirmacion = e.control.prepararIntencion(Accion.ALLOW, null)
            if (archivo) e.archivo.fallar = true else e.despuesDeFirma = { e.autoridad = false }
            assertTrue(runCatching { e.control.enviarConfirmada(confirmacion) }.isFailure)
            assertEquals(1, e.enviados.size)
            e.store.cerrar()
        }
    }

    @Test fun reconexionReconciliaPorIdSeqHashSinReenviarNiCrearDuracionNueva() = runTest {
        val e = Entorno(backgroundScope); e.preparar(); val orden = e.enviar(); val hash = e.hashOrden()
        e.ticket = 2; e.control.sincronizar()
        val nuevo = e.estado("2").copy(childSessionId = ID, lastAcceptedSeq = orden.seq,
            lastCommandId = orden.commandId, lastCommandPayloadSha256B64 = hash, lastResult = ResultadoEstado.APPLIED)
        e.recibir(nuevo)
        assertEquals(FasePolitica.APLICADA, e.control.resultado.value.fase)
        assertNull(e.store.leer().pendiente)
        assertEquals(1, e.enviados.map(e::mensaje).filterIsInstance<OrdenPolitica>().size)
        assertTrue(runCatching { e.recibir(e.ack(orden)) }.isFailure)
        e.store.cerrar()
    }

    @Test fun reinicioProcesoNoReproduceComandoYResultadoAjenoConservaPendiente() = runTest {
        val e = Entorno(backgroundScope); e.preparar(); val orden = e.enviar(); e.store.cerrar()
        val reabierto = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val nuevo = ControlPoliticas(reabierto, e, { e.tiempo }, {}, { e.firmar(it, Rol.PARENT) })
        e.ticket = 2; nuevo.sincronizar()
        val estado = e.estado("2").copy(lastAcceptedSeq = orden.seq, lastCommandId = orden.commandId,
            lastCommandPayloadSha256B64 = "E".repeat(43), lastResult = ResultadoEstado.APPLIED)
        nuevo.recibir(2, CodecProtocolo.codificarFrame(e.firmar(estado, Rol.CHILD)))
        assertEquals(FasePolitica.REQUIERE_CONFIRMACION, nuevo.resultado.value.fase)
        assertNotNull(reabierto.leer().pendiente)
        assertTrue(runCatching { nuevo.prepararIntencion(Accion.ALLOW, null) }.isFailure)
        nuevo.descartarPendiente(orden.commandId)
        nuevo.sincronizar()
        val renovado = estado.copy(stateSeq = "3", syncNonce = e.estado().syncNonce)
        nuevo.recibir(2, CodecProtocolo.codificarFrame(e.firmar(renovado, Rol.CHILD)))
        val confirmacion = nuevo.prepararIntencion(Accion.ALLOW_FOR, 60)
        assertEquals(1, e.enviados.map(e::mensaje).filterIsInstance<OrdenPolitica>().size)
        nuevo.enviarConfirmada(confirmacion)
        val segunda = e.enviados.map(e::mensaje).filterIsInstance<OrdenPolitica>().last()
        assertEquals("2", segunda.seq); assertNotEquals(orden.commandId, segunda.commandId)
        reabierto.cerrar()
    }

    @Test fun falloPersistiendoAckNoPublicaAplicacionYReaperturaConservaCommitAtomico() = runTest {
        val e = Entorno(backgroundScope); e.preparar(); val orden = e.enviar()
        e.archivo.despues = { error("Interrupción después de commit ficticio") }
        assertTrue(runCatching { e.recibir(e.ack(orden)) }.exceptionOrNull() is AlmacenNoDisponible)
        assertEquals(FasePolitica.ENVIADA_SIN_CONFIRMAR, e.control.resultado.value.fase)
        e.store.cerrar(); e.archivo.despues = {}
        val reabierto = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val datos = reabierto.leer()
        assertNull(datos.pendiente); assertEquals("1", datos.contadorLocal)
        val evidencia = CriptografiaProtocolo.verificarSobre(Base64.getUrlDecoder().decode(datos.ultimaEvidenciaFrameB64),
            ClaveVerificacion.desdeSpki(Rol.CHILD, datos.childPublicKeyB64!!), Proposito.MESSAGE)
        assertEquals(ResultadoAck.APPLIED, (evidencia.mensaje as Confirmacion).result)
        reabierto.cerrar()
    }

    @Test fun falloAntesDeGuardarAckMantienePendienteYEvidenciaAnterior() = runTest {
        val e = Entorno(backgroundScope); e.preparar(); val evidencia = e.store.leer().ultimaEvidenciaFrameB64
        val orden = e.enviar(); e.archivo.fallar = true
        assertTrue(runCatching { e.recibir(e.ack(orden)) }.isFailure)
        assertEquals(FasePolitica.ENVIADA_SIN_CONFIRMAR, e.control.resultado.value.fase)
        e.store.cerrar(); e.archivo.fallar = false
        val reabierto = ParentStore(e.archivo, e.cifrador, backgroundScope)
        assertNotNull(reabierto.leer().pendiente)
        assertEquals(evidencia, reabierto.leer().ultimaEvidenciaFrameB64)
        reabierto.cerrar()
    }

    @Test fun perdidaAckNoReiniciaDuracionEnSimuladorLocalDeHijo() = runTest {
        val e = Entorno(backgroundScope); e.preparar(); val orden = e.enviar()
        val original = e.enviados.last().copyOf(); var inicioHijo: Long? = null; var aplicaciones = 0
        fun aplicar(frame: ByteArray): Confirmacion {
            assertArrayEquals(original, frame)
            if (inicioHijo == null) { inicioHijo = e.tiempo; aplicaciones++ }
            return e.ack(orden).copy(remainingSec = 60 - ((e.tiempo - inicioHijo!!) / 1000).toInt())
        }
        aplicar(original) // Primer ACK perdido.
        e.tiempo = 5_000; e.control.avanzarReloj(); aplicar(e.enviados.last()) // Segundo ACK perdido.
        e.tiempo = 10_000; e.control.avanzarReloj(); val ack = aplicar(e.enviados.last())
        e.recibir(ack)
        assertEquals(1, aplicaciones); assertEquals(50, ack.remainingSec)
        assertEquals(FasePolitica.APLICADA, e.control.resultado.value.fase)
        e.store.cerrar()
    }

    @Test fun ackCorrelacionadoDespuesDeCaducidadLocalNoPermiteNuevaOrden() = runTest {
        val e = Entorno(backgroundScope); e.preparar()
        e.tiempo = 19_000; val orden = e.enviar()
        e.tiempo = 24_000; e.control.avanzarReloj()
        assertEquals(2, e.enviados.map(e::mensaje).filterIsInstance<OrdenPolitica>().size)
        e.recibir(e.ack(orden))
        assertEquals(FasePolitica.APLICADA, e.control.resultado.value.fase)
        assertTrue(runCatching { e.control.prepararIntencion(Accion.ALLOW, null) }.isFailure)
        e.store.cerrar()
    }

    @Test fun recorridoLocalRelaySyncEstadoOrdenYAckConRedYLatidoSeparados() = runTest {
        val e = Entorno(backgroundScope); e.store.inicializar(e.datos)
        val frames = mutableListOf<ByteArray>()
        var eventos: EventosRelay? = null
        var cancelado = false
        var red = true
        val conexion = RelayConnection(TransporteRelay { _, receptor ->
            eventos = receptor
            object : SocketRelay {
                override fun enviar(texto: String): Boolean { frames.add(texto.toByteArray()); return !cancelado }
                override fun cancelar() { cancelado = true }
            }
        }, { e.tiempo }, { check(e.autoridad) }, { e.firmar(it, Rol.PARENT, Proposito.AUTH) })
        conexion.primerPlano(); conexion.conectar(ConfiguracionRelay(ID, e.datos.relayOrigin!!, e.datos.parentPublicKeyB64))
        eventos!!.abierto("cp.v1")
        fun frame(mensaje: Mensaje) = eventos!!.texto(CodecProtocolo.codificarFrame(mensaje).toString(Charsets.UTF_8))
        frame(RetoConexion(BOOT, "A".repeat(43))); frame(AutenticacionAceptada(BOOT))
        frame(RespuestaLatido((CodecProtocolo.leerFrame(frames.last()) as Latido).nonce))
        val adapter = CanalPoliticasRelay(conexion) { red }
        val control = ControlPoliticas(e.store, adapter, { e.tiempo }, { check(e.autoridad) }, { e.firmar(it, Rol.PARENT) })
        control.sincronizar()
        val sync = e.mensaje(frames.last()) as SolicitudSincronizacion
        val estado = EstadoDispositivo(ID, sync.syncNonce, BOOT, SESION, "1", "0", null, null, ResultadoEstado.NONE,
            Modo.LOCKED, Motivo.PARENT_LOCK, null, true, true, true, true, "A".repeat(43))
        frame(e.firmar(estado, Rol.CHILD))
        val recibida = conexion.mensajesHijo.first(); control.recibir(recibida.ticket, recibida.bytes())
        val confirmacion = control.prepararIntencion(Accion.LOCK_FOR, 60)
        control.enviarConfirmada(confirmacion)
        val ordenDoc = CriptografiaProtocolo.verificarSobre(frames.last(),
            ClaveVerificacion.desdeSpki(Rol.PARENT, e.datos.parentPublicKeyB64), Proposito.MESSAGE)
        val orden = ordenDoc.mensaje as OrdenPolitica
        frame(e.firmar(Confirmacion(ID, BOOT, SESION, orden.commandId, orden.seq, ordenDoc.payloadSha256B64,
            ResultadoAck.APPLIED, Modo.LOCKED, Motivo.PARENT_LOCK, 60), Rol.CHILD))
        val respuesta = conexion.mensajesHijo.first(); control.recibir(respuesta.ticket, respuesta.bytes())
        assertEquals(FasePolitica.APLICADA, control.resultado.value.fase)
        assertNull(e.store.leer().pendiente)
        red = false; assertNull(adapter.actual())
        assertFalse(adapter.enviar(respuesta.ticket, frames.last()))
        conexion.segundoPlano(); assertTrue(cancelado)
        e.store.cerrar()
    }

    @Test fun dobleConfirmacionConcurrenteSoloReservaYEnviaUnaOrden() = runTest {
        val e = Entorno(backgroundScope); e.preparar()
        val confirmacion = e.control.prepararIntencion(Accion.ALLOW_FOR, 604800)
        val operaciones = (1..2).map { async { runCatching { e.control.enviarConfirmada(confirmacion) } } }
        assertEquals(1, operaciones.map { it.await() }.count { it.isSuccess })
        assertEquals("1", e.store.leer().contadorLocal)
        assertEquals(1, e.enviados.map(e::mensaje).filterIsInstance<OrdenPolitica>().size)
        e.store.cerrar()
    }

    @Test fun vistaPierdeFrescuraAlDesconectarYConfirmacionLocalReemplazadaNoEnvia() = runTest {
        val e = Entorno(backgroundScope); e.preparar()
        val local = e.store.guardarIntencionLocal(Accion.ALLOW, null)
        val confirmacion = e.control.prepararIntencion(local.action, local.durationSec)
        e.store.guardarIntencionLocal(Accion.ALLOW, null, local.id)
        assertTrue(runCatching { e.control.enviarConfirmada(confirmacion) }.isFailure)
        assertEquals("0", e.store.leer().contadorLocal); assertEquals(1, e.enviados.size)
        assertTrue(e.control.vista().fresca)
        e.ticket = null
        assertFalse(e.control.vista().fresca); assertFalse(e.control.vista().puedeEnviar)
        e.store.cerrar()
    }

    @Test fun cerrarConfirmacionNoBorraIntencionOfflineNiLaEnvia() = runTest {
        val e = Entorno(backgroundScope); e.preparar()
        val local = e.store.guardarIntencionLocal(Accion.LOCK_FOR, 60)
        val confirmacion = e.control.prepararIntencion(local.action, local.durationSec)
        e.control.descartarConfirmacion()
        assertTrue(runCatching { e.control.enviarConfirmada(confirmacion) }.isFailure)
        assertEquals(local, e.store.leer().intencionLocal); assertEquals(1, e.enviados.size)
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
