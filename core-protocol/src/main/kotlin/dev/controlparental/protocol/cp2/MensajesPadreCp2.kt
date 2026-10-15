package dev.controlparental.protocol.cp2

import dev.controlparental.protocol.*
import dev.controlparental.protocol.cp2.VinculacionCp2.campos
import dev.controlparental.protocol.cp2.VinculacionCp2.documento
import dev.controlparental.protocol.cp2.VinculacionCp2.texto
import java.security.*
import java.security.spec.X509EncodedKeySpec
import kotlinx.serialization.json.*

enum class AccionCp2 { LOCK, ALLOW, ALLOW_FOR }
sealed interface MensajePadreCp2 { val pairId: String }
data class SyncRequestCp2(override val pairId: String, val syncNonce: String,
    val requestedAction: AccionCp2?) : MensajePadreCp2
data class SetPolicyCp2(override val pairId: String, val bootId: String, val childSessionId: String,
    val commandId: String, val seq: String, val expectedRevision: String, val commandNonce: String,
    val action: AccionCp2, val durationSec: Int?) : MensajePadreCp2

// Solo criptografía; no autoriza envío, reserva secuencia ni acredita aplicación.
class MessageVerificadoCp2 internal constructor(val mensaje: MensajePadreCp2, p: ByteArray, f: ByteArray) {
    private val original = p.copyOf(); private val sobre = f.copyOf()
    val payloadSha256B64 = VinculacionCp2.b64(MessageDigest.getInstance("SHA-256").digest(original))
    fun payload() = original.copyOf()
    fun frame() = sobre.copyOf()
}

// Fuente parcial ORD01: lista cerrada del propietario, sin cambiar PAIR/AUTH ni CP/1.
object MensajesPadreCp2 {
    private val uuid = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    fun contador(s: String, positivo: Boolean = false): Long {
        require(Regex(if (positivo) "[1-9][0-9]*" else "0|[1-9][0-9]*").matches(s))
        return requireNotNull(s.toLongOrNull())
    }
    private fun accion(o: JsonObject, k: String): AccionCp2? =
        if (o.getValue(k) == JsonNull) null else AccionCp2.valueOf(texto(o, k))
    fun leerPayload(bytes: ByteArray): MensajePadreCp2 {
        val o = documento(bytes, VinculacionCp2.MAX_PAYLOAD)
        require(o["v"] == JsonPrimitive(2) && texto(o, "sender") == "PARENT")
        val m = when (texto(o, "kind")) {
            "SYNC_REQUEST" -> {
                campos(o, "v", "kind", "pairId", "sender", "syncNonce", "requestedAction")
                SyncRequestCp2(texto(o, "pairId"), texto(o, "syncNonce"), accion(o, "requestedAction")).also {
                    Formatos.base64(it.syncNonce, 32, 32)
                }
            }
            "SET_POLICY" -> {
                campos(o, "v", "kind", "sender", "pairId", "bootId", "childSessionId", "commandId", "seq",
                    "expectedRevision", "commandNonce", "action", "durationSec")
                val a = requireNotNull(accion(o, "action"))
                val d = o.getValue("durationSec")
                val duracion = if (d == JsonNull) null else {
                    val n = d as? JsonPrimitive ?: error("Duración entera requerida")
                    require(!n.isString && Regex("[1-9][0-9]*").matches(n.content))
                    requireNotNull(n.content.toIntOrNull())
                }
                SetPolicyCp2(texto(o, "pairId"), texto(o, "bootId"), texto(o, "childSessionId"), texto(o, "commandId"),
                    texto(o, "seq"), texto(o, "expectedRevision"), texto(o, "commandNonce"), a, duracion).also {
                    listOf(it.bootId, it.childSessionId, it.commandId).forEach { id -> require(uuid.matches(id)) }
                    contador(it.seq, true); contador(it.expectedRevision)
                    Formatos.base64(it.commandNonce, 32, 32)
                    if (a == AccionCp2.ALLOW_FOR) require(duracion != null && duracion in 60..86400 && duracion % 60 == 0)
                    else require(duracion == null)
                }
            }
            else -> error("Mensaje fuera de ORD01")
        }
        require(uuid.matches(m.pairId)); return m
    }
    fun payload(m: MensajePadreCp2): ByteArray = codificar(buildJsonObject {
        put("v", 2); put("sender", "PARENT"); put("pairId", m.pairId)
        when (m) {
            is SyncRequestCp2 -> {
                put("kind", "SYNC_REQUEST"); put("syncNonce", m.syncNonce)
                put("requestedAction", m.requestedAction?.let { JsonPrimitive(it.name) } ?: JsonNull)
            }
            is SetPolicyCp2 -> {
                put("kind", "SET_POLICY"); put("bootId", m.bootId); put("childSessionId", m.childSessionId)
                put("commandId", m.commandId); put("seq", m.seq); put("expectedRevision", m.expectedRevision)
                put("commandNonce", m.commandNonce); put("action", m.action.name)
                put("durationSec", m.durationSec?.let { JsonPrimitive(it) } ?: JsonNull)
            }
        }
    }).also { leerPayload(it) }
    private fun codificar(o: JsonObject) = canonico(o).toByteArray(Charsets.UTF_8)
    private fun bytesFirma(p: ByteArray) = "CPv2/MESSAGE\n".toByteArray(Charsets.UTF_8) + p
    fun firmar(payload: ByteArray, publicaEsperada: String, firmante: (ByteArray) -> ByteArray): MessageVerificadoCp2 {
        val p = payload.copyOf(); leerPayload(p); VinculacionCp2.publica(publicaEsperada)
        val f = firmante(bytesFirma(p)).copyOf(); VinculacionCp2.validarDer(f)
        val sobre = codificar(buildJsonObject {
            put("v", 2); put("kind", "SIGNED"); put("purpose", "MESSAGE")
            put("payloadB64", VinculacionCp2.b64(p)); put("sigB64", VinculacionCp2.b64(f))
        })
        return verificar(sobre, publicaEsperada)
    }
    fun verificar(bytes: ByteArray, publicaEsperada: String): MessageVerificadoCp2 {
        val f = bytes.copyOf(); val o = documento(f, VinculacionCp2.MAX_FRAME)
        campos(o, "v", "kind", "purpose", "payloadB64", "sigB64")
        require(o["v"] == JsonPrimitive(2) && texto(o, "kind") == "SIGNED" && texto(o, "purpose") == "MESSAGE")
        val p = Formatos.base64(texto(o, "payloadB64"), VinculacionCp2.MAX_PAYLOAD)
        val m = leerPayload(p)
        val firma = Formatos.base64(texto(o, "sigB64"), 72); VinculacionCp2.validarDer(firma)
        VinculacionCp2.publica(publicaEsperada)
        val k = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(Formatos.base64(publicaEsperada, 150)))
        require(Signature.getInstance("SHA256withECDSA").run { initVerify(k); update(bytesFirma(p)); verify(firma) })
        return MessageVerificadoCp2(m, p, f)
    }
}
