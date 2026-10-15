package dev.controlparental.protocol.cp2

import dev.controlparental.protocol.*
import dev.controlparental.protocol.cp2.VinculacionCp2.campos
import dev.controlparental.protocol.cp2.VinculacionCp2.documento
import dev.controlparental.protocol.cp2.VinculacionCp2.texto
import kotlinx.serialization.json.*
import java.security.*
import java.security.spec.X509EncodedKeySpec

sealed interface FrameAuthCp2
data class RetoAuthCp2(val connectionId: String, val nonce: String) : FrameAuthCp2
data class AceptadaAuthCp2(val connectionId: String) : FrameAuthCp2
data class LatidoAuthCp2(val pong: Boolean, val nonce: String) : FrameAuthCp2
data class SobreAuthCp2(val payloadB64: String, val sigB64: String) : FrameAuthCp2
data class PruebaAuthCp2(val pairId: String, val role: Rol, val connectionId: String, val nonce: String)
class AuthVerificadaCp2 internal constructor(val prueba: PruebaAuthCp2, payload: ByteArray, frame: ByteArray) {
    private val p = payload.copyOf(); private val f = frame.copyOf()
    val payloadSha256B64 = VinculacionCp2.b64(MessageDigest.getInstance("SHA-256").digest(p))
    fun payload(): ByteArray = p.copyOf()
    fun frame(): ByteArray = f.copyOf()
}

// AUTH criptográfico, sin autoridad de socket, frescura ni estado de hijo.
object AutenticacionCp2 {
    private val uuid = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    private fun id(s: String) { require(uuid.matches(s)) }
    private fun nonce(s: String) { Formatos.base64(s, 32, 32) }
    private fun version(o: JsonObject) { require(o["v"] == JsonPrimitive(2)) }
    private fun protocolo(o: JsonObject) { require(texto(o, "protocol") == "cp.v2") }
    fun leerFrame(bytes: ByteArray): FrameAuthCp2 {
        val o = documento(bytes, VinculacionCp2.MAX_FRAME); version(o)
        return when (texto(o, "kind")) {
            "CHALLENGE" -> {
                campos(o, "v", "kind", "connectionId", "nonce", "timeoutSec")
                require(o["timeoutSec"] == JsonPrimitive(10))
                RetoAuthCp2(texto(o, "connectionId"), texto(o, "nonce")).also { id(it.connectionId); nonce(it.nonce) }
            }
            "AUTH_OK" -> {
                campos(o, "v", "kind", "connectionId", "protocol"); protocolo(o)
                AceptadaAuthCp2(texto(o, "connectionId")).also { id(it.connectionId) }
            }
            "PING", "PONG" -> {
                campos(o, "v", "kind", "nonce")
                LatidoAuthCp2(texto(o, "kind") == "PONG", texto(o, "nonce")).also { nonce(it.nonce) }
            }
            "SIGNED" -> {
                campos(o, "v", "kind", "purpose", "payloadB64", "sigB64"); require(texto(o, "purpose") == "AUTH")
                SobreAuthCp2(texto(o, "payloadB64"), texto(o, "sigB64")).also {
                    Formatos.base64(it.payloadB64, VinculacionCp2.MAX_PAYLOAD)
                    VinculacionCp2.validarDer(Formatos.base64(it.sigB64, 72))
                }
            }
            else -> error("Frame no pertenece a AUTH CP/2")
        }
    }
    fun frame(f: FrameAuthCp2): ByteArray = canonico(buildJsonObject {
        put("v", 2)
        when (f) {
            is RetoAuthCp2 -> { put("kind", "CHALLENGE"); put("connectionId", f.connectionId); put("nonce", f.nonce); put("timeoutSec", 10) }
            is AceptadaAuthCp2 -> { put("kind", "AUTH_OK"); put("connectionId", f.connectionId); put("protocol", "cp.v2") }
            is LatidoAuthCp2 -> { put("kind", if (f.pong) "PONG" else "PING"); put("nonce", f.nonce) }
            is SobreAuthCp2 -> { put("kind", "SIGNED"); put("purpose", "AUTH"); put("payloadB64", f.payloadB64); put("sigB64", f.sigB64) }
        }
    }).toByteArray(Charsets.UTF_8).also { leerFrame(it) }
    fun leerPrueba(bytes: ByteArray): PruebaAuthCp2 {
        val o = documento(bytes, VinculacionCp2.MAX_PAYLOAD)
        campos(o, "v", "kind", "pairId", "role", "connectionId", "nonce", "protocol")
        version(o); protocolo(o); require(texto(o, "kind") == "AUTH_PROOF")
        return PruebaAuthCp2(texto(o, "pairId"), Rol.valueOf(texto(o, "role")), texto(o, "connectionId"), texto(o, "nonce")).also {
            require(it.role == Rol.PARENT || it.role == Rol.CHILD); id(it.pairId); id(it.connectionId); nonce(it.nonce)
        }
    }
    fun payload(p: PruebaAuthCp2): ByteArray = canonico(buildJsonObject {
        put("v", 2); put("kind", "AUTH_PROOF"); put("protocol", "cp.v2"); put("pairId", p.pairId)
        put("role", p.role.name); put("connectionId", p.connectionId); put("nonce", p.nonce)
    }).toByteArray(Charsets.UTF_8).also { leerPrueba(it) }
    private fun bytesFirma(p: ByteArray) = "CPv2/AUTH\n".toByteArray(Charsets.UTF_8) + p
    fun firmar(payload: ByteArray, publicaEsperada: String, rolEsperado: Rol, firmante: (ByteArray) -> ByteArray): AuthVerificadaCp2 {
        val p = payload.copyOf(); require(leerPrueba(p).role == rolEsperado)
        VinculacionCp2.publica(publicaEsperada)
        val f = firmante(bytesFirma(p)).copyOf(); VinculacionCp2.validarDer(f)
        return verificar(frame(SobreAuthCp2(VinculacionCp2.b64(p), VinculacionCp2.b64(f))), publicaEsperada, rolEsperado)
    }
    fun verificar(bytes: ByteArray, publicaEsperada: String, rolEsperado: Rol): AuthVerificadaCp2 {
        val frame = bytes.copyOf(); val o = leerFrame(frame) as? SobreAuthCp2 ?: error("Falta sobre AUTH")
        VinculacionCp2.publica(publicaEsperada)
        val p = Formatos.base64(o.payloadB64, VinculacionCp2.MAX_PAYLOAD)
        val k = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(Formatos.base64(publicaEsperada, 150)))
        require(Signature.getInstance("SHA256withECDSA").run { initVerify(k); update(bytesFirma(p)); verify(Formatos.base64(o.sigB64, 72)) })
        val prueba = leerPrueba(p); require(prueba.role == rolEsperado)
        return AuthVerificadaCp2(prueba, p, frame)
    }
}
