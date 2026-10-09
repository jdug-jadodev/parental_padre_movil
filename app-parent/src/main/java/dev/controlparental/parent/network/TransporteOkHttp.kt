package dev.controlparental.parent.network

import dev.controlparental.protocol.CodecProtocolo
import java.net.ProtocolException
import java.net.URI
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import okhttp3.*
import okio.ByteString

internal class TransporteOkHttp(private val cliente: OkHttpClient = clienteSeguro()) : TransporteRelay {
    override fun abrir(endpoint: String, eventos: EventosRelay): SocketRelay {
        val uri = URI(endpoint)
        require(uri.scheme == "wss" && uri.host != null && uri.rawPath == "/ws" &&
            uri.rawUserInfo == null && uri.rawQuery == null && uri.rawFragment == null)
        val solicitud = Request.Builder().url(endpoint).header("Sec-WebSocket-Protocol", "cp.v1").build()
        val socket = cliente.newWebSocket(solicitud, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                eventos.abierto(response.headers.values("Sec-WebSocket-Protocol").singleOrNull())
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text.length > CodecProtocolo.MAX_FRAME || text.toByteArray(Charsets.UTF_8).size > CodecProtocolo.MAX_FRAME) {
                    eventos.fallo(true); webSocket.cancel()
                } else eventos.texto(text)
            }
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                eventos.fallo(true); webSocket.cancel() // CP/1 usa frames JSON de texto.
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, ""); eventos.fallo(false)
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { eventos.fallo(false) }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                val seguridad = generateSequence(t) { it.cause }.any {
                    it is SSLException || it is CertificateException || it is ProtocolException
                } || response?.code in listOf(401, 403)
                eventos.fallo(seguridad)
            }
        })
        return object : SocketRelay {
            override fun enviar(texto: String): Boolean {
                require(texto.toByteArray(Charsets.UTF_8).size <= CodecProtocolo.MAX_FRAME)
                return socket.send(texto) // Solo encola: no demuestra entrega ni aplicación.
            }
            override fun cancelar() { socket.cancel() }
        }
    }
    fun liberar() { cliente.dispatcher.cancelAll(); cliente.connectionPool.evictAll(); cliente.dispatcher.executorService.shutdown() }
    companion object {
        fun clienteSeguro(): OkHttpClient = OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
            .followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
            .connectTimeout(10, TimeUnit.SECONDS).readTimeout(0, TimeUnit.SECONDS)
            .build() // Confianza/hostname del sistema, sin bypass ni certificados de pruebas.
    }
}
