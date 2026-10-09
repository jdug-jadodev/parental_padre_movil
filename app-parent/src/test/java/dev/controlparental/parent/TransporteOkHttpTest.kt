package dev.controlparental.parent

import dev.controlparental.parent.network.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import okhttp3.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.Assert.*
import org.junit.Test
import okio.ByteString.Companion.encodeUtf8

class TransporteOkHttpTest {
    private class Eventos : EventosRelay {
        val abierto = CountDownLatch(1)
        val recibido = CountDownLatch(1)
        val fallido = CountDownLatch(1)
        val protocolo = AtomicReference<String?>()
        val mensaje = AtomicReference<String?>()
        val permanente = AtomicReference<Boolean?>()
        override fun abierto(subprotocolo: String?) { protocolo.set(subprotocolo); abierto.countDown() }
        override fun texto(texto: String) { mensaje.set(texto); recibido.countDown() }
        override fun fallo(permanente: Boolean) { this.permanente.compareAndSet(null, permanente); fallido.countDown() }
    }
    // Certificados efímeros únicamente JVM/loopback; jamás configuración de producción.
    private fun servidor(hostCertificado: String, prueba: (MockWebServer, HandshakeCertificates) -> Unit) {
        val cert = HeldCertificate.Builder().commonName(hostCertificado).addSubjectAlternativeName(hostCertificado).build()
        val tls = HandshakeCertificates.Builder().heldCertificate(cert).build()
        val confianzaTest = HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
        MockWebServer().use { server ->
            server.useHttps(tls.sslSocketFactory(), false)
            server.start()
            prueba(server, confianzaTest)
        }
    }
    private fun clienteTest(confianza: HandshakeCertificates) = TransporteOkHttp.clienteSeguro().newBuilder()
        .sslSocketFactory(confianza.sslSocketFactory(), confianza.trustManager).build()

    @Test fun wssRealLocalConCertificadoDeTestValidoNegociaSubprotocoloYTexto() = servidor("localhost") { server, confianza ->
        server.enqueue(MockResponse().setHeader("Sec-WebSocket-Protocol", "cp.v1").withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) { webSocket.send("Texto público de prueba") }
        }))
        val transporte = TransporteOkHttp(clienteTest(confianza))
        val eventos = Eventos()
        try {
            val socket = transporte.abrir("wss://localhost:${server.port}/ws", eventos)
            assertTrue(eventos.abierto.await(10, TimeUnit.SECONDS))
            assertTrue(eventos.recibido.await(10, TimeUnit.SECONDS))
            assertEquals("cp.v1", eventos.protocolo.get())
            assertEquals("Texto público de prueba", eventos.mensaje.get())
            val request = server.takeRequest(10, TimeUnit.SECONDS)!!
            assertEquals("/ws", request.path); assertEquals("cp.v1", request.getHeader("Sec-WebSocket-Protocol"))
            socket.cancelar()
        } finally { transporte.liberar() }
    }

    @Test fun certificadoNoConfiableSeRechazaSinAbrirWebSocket() = servidor("localhost") { server, _ ->
        val transporte = TransporteOkHttp()
        val eventos = Eventos()
        try {
            transporte.abrir("wss://localhost:${server.port}/ws", eventos)
            assertTrue(eventos.fallido.await(10, TimeUnit.SECONDS)); assertEquals(true, eventos.permanente.get())
            assertEquals(1L, eventos.abierto.count)
        } finally { transporte.liberar() }
    }

    @Test fun certificadoConfiableDeOtroHostNoDeshabilitaVerificacionHostname() = servidor("otro.invalid") { server, confianza ->
        val transporte = TransporteOkHttp(clienteTest(confianza))
        val eventos = Eventos()
        try {
            transporte.abrir("wss://localhost:${server.port}/ws", eventos)
            assertTrue(eventos.fallido.await(10, TimeUnit.SECONDS)); assertEquals(true, eventos.permanente.get())
            assertEquals(1L, eventos.abierto.count)
        } finally { transporte.liberar() }
    }

    @Test fun noAdmiteWsHttpRutasOCredencialesNiSigueRedirecciones() {
        val transporte = TransporteOkHttp()
        try {
            for (url in listOf("ws://localhost/ws", "http://localhost/ws", "wss://localhost/otra", "wss://adulto@localhost/ws")) {
                assertThrows(IllegalArgumentException::class.java) { transporte.abrir(url, Eventos()) }
            }
            val cliente = TransporteOkHttp.clienteSeguro()
            assertFalse(cliente.followRedirects); assertFalse(cliente.followSslRedirects)
            assertFalse(cliente.retryOnConnectionFailure)
            assertFalse(cliente.connectionSpecs.any { !it.isTls })
            cliente.dispatcher.executorService.shutdown()
        } finally { transporte.liberar() }
    }

    @Test fun frameBinarioSeRechazaSinPublicarloComoTexto() = servidor("localhost") { server, confianza ->
        server.enqueue(MockResponse().setHeader("Sec-WebSocket-Protocol", "cp.v1").withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) { webSocket.send("{}".encodeUtf8()) }
        }))
        val transporte = TransporteOkHttp(clienteTest(confianza))
        val eventos = Eventos()
        try {
            transporte.abrir("wss://localhost:${server.port}/ws", eventos)
            assertTrue(eventos.fallido.await(10, TimeUnit.SECONDS))
            assertEquals(true, eventos.permanente.get())
            assertNull(eventos.mensaje.get())
        } finally { transporte.liberar() }
    }

    @Test fun redireccionRealNoEnviaSolicitudAOtraRuta() = servidor("localhost") { server, confianza ->
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", "https://localhost:${server.port}/otra"))
        val transporte = TransporteOkHttp(clienteTest(confianza))
        val eventos = Eventos()
        try {
            transporte.abrir("wss://localhost:${server.port}/ws", eventos)
            assertTrue(eventos.fallido.await(10, TimeUnit.SECONDS))
            assertEquals(true, eventos.permanente.get())
            assertEquals(1L, eventos.abierto.count)
            assertEquals(1, server.requestCount)
        } finally { transporte.liberar() }
    }
}
