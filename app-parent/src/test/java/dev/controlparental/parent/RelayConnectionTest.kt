package dev.controlparental.parent

import dev.controlparental.parent.network.*
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first

class RelayConnectionTest {
    private class Socket(val eventos: EventosRelay) : SocketRelay {
        val enviados = mutableListOf<Mensaje>()
        var cancelado = false
        var acepta = true
        override fun enviar(texto: String): Boolean {
            check(!cancelado)
            enviados.add(CodecProtocolo.leerFrame(texto.toByteArray()))
            return acepta
        }
        override fun cancelar() { cancelado = true }
        fun mensaje(mensaje: Mensaje) = eventos.texto(CodecProtocolo.codificarFrame(mensaje).toString(Charsets.UTF_8))
    }
    private class Entorno {
        var tiempo = 0L
        var autoridad = true
        var firmas = 0
        var duranteFirma: () -> Unit = {}
        val sockets = mutableListOf<Socket>()
        val par = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        val publica = Base64.getUrlEncoder().withoutPadding().encodeToString(par.public.encoded)
        val config = ConfiguracionRelay("00000000-0000-4000-8000-000000000002", "https://prueba.invalid", publica)
        val conexion = RelayConnection(TransporteRelay { endpoint, eventos ->
            assertEquals("wss://prueba.invalid/ws", endpoint)
            Socket(eventos).also(sockets::add)
        }, { tiempo }, { check(autoridad) }, { prueba ->
            firmas++; duranteFirma()
            CriptografiaProtocolo.firmarSobre(prueba, Proposito.AUTH, ClaveVerificacion.desdeSpki(Rol.PARENT, publica)) { bytes ->
                Signature.getInstance("SHA256withECDSA").run { initSign(par.private); update(bytes); sign() }
            }
        })
        val reto = RetoConexion("00000000-0000-4000-8000-000000000001", "A".repeat(43))
        fun abrir(): Socket {
            conexion.primerPlano(); conexion.conectar(config)
            return sockets.last().also { it.eventos.abierto("cp.v1") }
        }
        fun autenticar(): Socket = abrir().also { it.mensaje(reto); it.mensaje(AutenticacionAceptada(reto.connectionId)) }
    }

    @Test fun exigePrimerPlanoYAutoridadAntesDeAbrirYNoDuplicaSockets() {
        val e = Entorno()
        e.conexion.conectar(e.config); assertTrue(e.sockets.isEmpty())
        e.autoridad = false; e.conexion.primerPlano(); e.conexion.conectar(e.config)
        assertTrue(e.sockets.isEmpty()); assertEquals(FaseRelay.AUTENTICACION_REQUERIDA, e.conexion.estado.value.fase)
        e.autoridad = true; e.conexion.conectar(e.config); e.conexion.conectar(e.config)
        assertEquals(1, e.sockets.size)
    }

    @Test fun retoFirmadoExactoAuthOkCorrelacionadoYPresenciaSoloIndicativa() {
        val e = Entorno()
        val socket = e.abrir()
        socket.mensaje(e.reto)
        val auth = socket.enviados.single() as SobreFirmado
        val prueba = CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(auth),
            ClaveVerificacion.desdeSpki(Rol.PARENT, e.publica), Proposito.AUTH).mensaje as PruebaAutenticacion
        assertEquals(e.reto.nonce, prueba.nonce); assertEquals(e.reto.connectionId, prueba.connectionId)
        assertEquals(e.config.pairId, prueba.pairId); assertEquals(Rol.PARENT, prueba.role)
        assertEquals(FaseRelay.ESPERANDO_AUTH_OK, e.conexion.estado.value.fase)
        socket.mensaje(AutenticacionAceptada(e.reto.connectionId))
        assertEquals(FaseRelay.AUTENTICADO, e.conexion.estado.value.fase)
        socket.mensaje(Presencia(Rol.CHILD, true))
        assertEquals(true, e.conexion.estado.value.hijoIndicativo)
        assertEquals(1, e.firmas)
    }

    @Test fun erroresDeSubprotocoloAuthPrematuroIdAjenoORetoDuplicadoCierran() {
        for (caso in 0..3) {
            val e = Entorno(); val socket = e.abrir()
            when (caso) {
                0 -> socket.eventos.abierto("otro")
                1 -> socket.mensaje(AutenticacionAceptada(e.reto.connectionId))
                2 -> { socket.mensaje(e.reto); socket.mensaje(AutenticacionAceptada(e.config.pairId)) }
                3 -> { socket.mensaje(e.reto); socket.mensaje(e.reto) }
            }
            assertTrue(socket.cancelado); assertEquals(FaseRelay.ERROR_SEGURO, e.conexion.estado.value.fase)
            e.tiempo += 100_000; e.conexion.avanzarReloj(); assertEquals(1, e.sockets.size)
        }
    }

    @Test fun caducidadDuranteFirmaONuevaAutenticacionNoEnviaAuth() {
        for (caducidad in listOf(true, false)) {
            val e = Entorno(); val socket = e.abrir()
            e.duranteFirma = { if (caducidad) e.tiempo = 10_000 else e.autoridad = false }
            socket.mensaje(e.reto)
            assertTrue(socket.enviados.isEmpty()); assertTrue(socket.cancelado)
        }
    }

    @Test fun pongDebeCorresponderAlDesafioVigenteYNoSeAceptaDuplicado() {
        val e = Entorno(); val socket = e.autenticar()
        val ping = socket.enviados.last() as Latido
        assertFalse(e.conexion.estado.value.canalSano)
        socket.mensaje(RespuestaLatido(ping.nonce))
        assertTrue(e.conexion.estado.value.canalSano)
        e.tiempo = 9999; e.conexion.avanzarReloj(); assertEquals(2, socket.enviados.size)
        socket.mensaje(RespuestaLatido(ping.nonce))
        assertEquals(FaseRelay.ERROR_SEGURO, e.conexion.estado.value.fase)
    }

    @Test fun perdidaPongReconectaConBackoffSinAceptarCallbacksViejos() {
        val e = Entorno(); val antiguo = e.autenticar()
        e.tiempo = 15_000; e.conexion.avanzarReloj()
        assertTrue(antiguo.cancelado); assertEquals(FaseRelay.REINTENTO, e.conexion.estado.value.fase)
        assertFalse(e.conexion.estado.value.canalSano)
        e.tiempo += 999; e.conexion.avanzarReloj(); assertEquals(1, e.sockets.size)
        e.tiempo += 300; e.conexion.avanzarReloj(); assertEquals(2, e.sockets.size)
        antiguo.mensaje(AutenticacionAceptada(e.reto.connectionId))
        antiguo.eventos.fallo(true)
        assertEquals(FaseRelay.CONECTANDO, e.conexion.estado.value.fase)
    }

    @Test fun fondoCierraYNoEnviaNiReconectaHastaAutoridadVigente() {
        val e = Entorno(); val socket = e.autenticar()
        e.conexion.segundoPlano(); assertTrue(socket.cancelado)
        e.tiempo = 120_000; e.conexion.avanzarReloj(); assertEquals(1, e.sockets.size)
        socket.eventos.texto("{}"); assertEquals(FaseRelay.DESCONECTADO, e.conexion.estado.value.fase)
        e.autoridad = false; e.conexion.primerPlano()
        assertEquals(FaseRelay.AUTENTICACION_REQUERIDA, e.conexion.estado.value.fase)
        assertEquals(1, e.sockets.size)
    }

    @Test fun certificadoInvalidoNoReintentaYHttpRutaOCredencialesSeRechazan() {
        val e = Entorno(); val socket = e.abrir()
        socket.eventos.fallo(true)
        e.conexion.segundoPlano(); e.conexion.primerPlano(); e.tiempo += 60_000; e.conexion.avanzarReloj()
        assertEquals(1, e.sockets.size)
        for (origen in listOf("http://prueba.invalid", "https://prueba.invalid/", "https://adulto@prueba.invalid", "https://prueba.invalid?a=1")) {
            assertThrows(IllegalArgumentException::class.java) { ConfiguracionRelay(e.config.pairId, origen, e.publica) }
        }
    }

    @Test fun falloSincronoAlAbrirCancelaSocketDevueltoSinDejarHuerfanos() {
        val e = Entorno()
        var cancelado = false
        val conexion = RelayConnection(TransporteRelay { _, eventos ->
            eventos.fallo(true)
            object : SocketRelay {
                override fun enviar(texto: String) = error("No enviar tras fallo")
                override fun cancelar() { cancelado = true }
            }
        }, { 0L }, {}, { error("No firmar sin reto") })
        conexion.primerPlano(); conexion.conectar(e.config)
        assertTrue(cancelado)
        assertEquals(FaseRelay.ERROR_SEGURO, conexion.estado.value.fase)
    }

    @Test fun fondoDuranteFirmaNuncaEnviaYDescartaRetoSinReconexionOculta() {
        val e = Entorno(); val socket = e.abrir()
        e.duranteFirma = { e.conexion.segundoPlano() }
        socket.mensaje(e.reto)
        assertTrue(socket.cancelado); assertTrue(socket.enviados.isEmpty())
        assertEquals(FaseRelay.DESCONECTADO, e.conexion.estado.value.fase)
        e.tiempo = 120_000; e.conexion.avanzarReloj()
        assertEquals(1, e.sockets.size)
    }

    private fun sobreMensaje(e: Entorno): SobreFirmado = CriptografiaProtocolo.firmarSobre(
        SolicitudSincronizacion(e.config.pairId, "A".repeat(43)), Proposito.MESSAGE,
        ClaveVerificacion.desdeSpki(Rol.PARENT, e.publica)) { bytes ->
        Signature.getInstance("SHA256withECDSA").run { initSign(e.par.private); update(bytes); sign() }
    }

    @Test fun rutaP05ExigePongYTicketActualYEntregaSobreSinAcreditarPayload() = runTest {
        val e = Entorno(); val socket = e.autenticar()
        assertNull(e.conexion.sesionPoliticas())
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        val ticket = e.conexion.sesionPoliticas()!!
        val sobre = sobreMensaje(e)
        val frame = CodecProtocolo.codificarFrame(sobre)
        assertTrue(e.conexion.enviarPolitica(ticket, frame))
        // No se acredita rol/firma del hijo aquí: la verificadora P05 lo rechazará.
        socket.mensaje(sobre)
        val trama = e.conexion.mensajesHijo.first()
        assertEquals(ticket, trama.ticket); assertArrayEquals(frame, trama.bytes())
        e.conexion.segundoPlano()
        assertNull(e.conexion.sesionPoliticas()); assertFalse(e.conexion.enviarPolitica(ticket, frame))
    }

    @Test fun rutaP05NoEnviaSiPongCaducaNiErrorRelayConfirmaOrden() {
        val e = Entorno(); val socket = e.autenticar()
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        val ticket = e.conexion.sesionPoliticas()!!
        socket.mensaje(ErrorProtocolo(Motivo.PEER_OFFLINE))
        assertEquals(FaseRelay.AUTENTICADO, e.conexion.estado.value.fase)
        e.tiempo = 15_000; e.conexion.avanzarReloj()
        e.tiempo = 30_000 // Caduca incluso antes del siguiente tick de Android.
        assertNull(e.conexion.sesionPoliticas())
        assertFalse(e.conexion.enviarPolitica(ticket, CodecProtocolo.codificarFrame(sobreMensaje(e))))
    }

    @Test fun latidosCadaQuinceSegundosDesdeEnvioAunquePongSeDemore() {
        val e = Entorno(); val socket = e.autenticar()
        e.tiempo = 2_000
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        for (instante in listOf(10_000L, 14_999L)) {
            e.tiempo = instante; e.conexion.avanzarReloj()
            assertEquals(1, socket.enviados.filterIsInstance<Latido>().size)
        }
        e.tiempo = 15_000; e.conexion.avanzarReloj()
        assertEquals(2, socket.enviados.filterIsInstance<Latido>().size)
        e.tiempo = 19_000
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        e.tiempo = 29_999; e.conexion.avanzarReloj()
        assertEquals(2, socket.enviados.filterIsInstance<Latido>().size)
        e.tiempo = 30_000; e.conexion.avanzarReloj()
        assertEquals(3, socket.enviados.filterIsInstance<Latido>().size)
    }

    @Test fun canalCaducaALosCuarentaYCincoSegundosSinTickNiPingPendiente() {
        val e = Entorno(); val socket = e.autenticar()
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        val ticket = e.conexion.sesionPoliticas()!!
        e.tiempo = 44_999; assertEquals(ticket, e.conexion.sesionPoliticas())
        e.tiempo = 45_000
        assertNull(e.conexion.sesionPoliticas())
        assertFalse(e.conexion.enviarPolitica(ticket, CodecProtocolo.codificarFrame(sobreMensaje(e))))
        assertEquals(2, socket.enviados.size)
        e.conexion.avanzarReloj()
        assertTrue(socket.cancelado); assertFalse(e.conexion.estado.value.canalSano)
        assertEquals(FaseRelay.REINTENTO, e.conexion.estado.value.fase)
    }

    @Test fun pausaBreveEnviaUnSoloLatidoSinRafagaYNoDuplicaDesafioPendiente() {
        val e = Entorno(); val socket = e.autenticar()
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        e.tiempo = 40_000; repeat(5) { e.conexion.avanzarReloj() }
        assertEquals(2, socket.enviados.filterIsInstance<Latido>().size)
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        e.tiempo = 54_999; e.conexion.avanzarReloj()
        assertEquals(2, socket.enviados.filterIsInstance<Latido>().size)
        e.tiempo = 55_000; e.conexion.avanzarReloj()
        assertEquals(3, socket.enviados.filterIsInstance<Latido>().size)
    }

    @Test fun pongNuevoRenuevaCaducidadPeroPresenciaNoLaRenueva() {
        val e = Entorno(); val socket = e.autenticar()
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        e.tiempo = 15_000; e.conexion.avanzarReloj()
        e.tiempo = 20_000
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        e.tiempo = 45_000; socket.mensaje(Presencia(Rol.CHILD, true))
        assertNotNull(e.conexion.sesionPoliticas())
        e.tiempo = 64_999; assertNotNull(e.conexion.sesionPoliticas())
        e.tiempo = 65_000; socket.mensaje(Presencia(Rol.CHILD, true))
        assertNull(e.conexion.sesionPoliticas())
    }

    @Test fun nuevaConexionNoHeredaPongNiCadenciaDeLaAnterior() {
        val e = Entorno(); val antiguo = e.autenticar()
        antiguo.mensaje(RespuestaLatido((antiguo.enviados.last() as Latido).nonce))
        e.conexion.desconectar(); e.tiempo = 100_000
        val nuevo = e.autenticar()
        assertNull(e.conexion.sesionPoliticas()); assertFalse(e.conexion.estado.value.canalSano)
        nuevo.mensaje(RespuestaLatido((nuevo.enviados.last() as Latido).nonce))
        assertNotNull(e.conexion.sesionPoliticas())
        e.tiempo = 114_999; e.conexion.avanzarReloj()
        assertEquals(1, nuevo.enviados.filterIsInstance<Latido>().size)
        e.tiempo = 115_000; e.conexion.avanzarReloj()
        assertEquals(2, nuevo.enviados.filterIsInstance<Latido>().size)
    }

    @Test fun pongTardioNoResucitaCanalCaducadoAunqueNoHayaTick() {
        val e = Entorno(); val socket = e.autenticar()
        socket.mensaje(RespuestaLatido((socket.enviados.last() as Latido).nonce))
        e.tiempo = 40_000; e.conexion.avanzarReloj()
        val pendiente = socket.enviados.last() as Latido
        e.tiempo = 45_000; socket.mensaje(RespuestaLatido(pendiente.nonce))
        assertTrue(socket.cancelado); assertNull(e.conexion.sesionPoliticas())
        assertFalse(e.conexion.estado.value.canalSano)
    }
}
