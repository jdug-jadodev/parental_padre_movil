package dev.controlparental.protocol.cp2

import dev.controlparental.protocol.*
import kotlinx.serialization.json.*
import java.math.BigInteger
import java.security.*
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec

data class OfertaCp2(val childPublicKeyB64: String, val enrollId: String, val enrollNonce: String)
data class RecuperacionCp2(val MAINTENANCE: String, val RECOVER_PARENT: String, val RETIRE: String)
data class AceptacionCp2(val pairId: String, val enrollId: String, val enrollNonce: String,
    val parentPublicKeyB64: String, val childPublicKeyB64: String, val relayOrigin: String,
    val recoveryHashes: RecuperacionCp2)

// Verificación criptográfica, NO frescura, autorización humana o commit.
class PairVerificadoCp2 internal constructor(val aceptacion: AceptacionCp2, payload: ByteArray, frame: ByteArray) {
    private val p = payload.copyOf()
    private val f = frame.copyOf()
    val payloadSha256B64: String = VinculacionCp2.b64(MessageDigest.getInstance("SHA-256").digest(p))
    fun payload(): ByteArray = p.copyOf()
    fun frame(): ByteArray = f.copyOf()
}

object VinculacionCp2 {
    const val MAX_FRAME = 16384
    const val MAX_PAYLOAD = 8192
    private val uuid = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    private val orden = BigInteger("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16)
    fun b64(bytes: ByteArray): String = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    private fun base64(s: String, max: Int, exacto: Int? = null): ByteArray = Formatos.base64(s, max, exacto)
    internal fun campos(o: JsonObject, vararg nombres: String) { require(o.keys == nombres.toSet()) { "Campos CP/2 inesperados" } }
    internal fun texto(o: JsonObject, k: String): String {
        val p = o.getValue(k) as? JsonPrimitive ?: error("Cadena requerida")
        require(p.isString); return p.content
    }
    private fun cabecera(o: JsonObject, kind: String) {
        require(o["v"] == JsonPrimitive(2) && texto(o, "kind") == kind)
        if (kind != "SIGNED") require(texto(o, "protocol") == "cp.v2")
    }
    // Barrera previa al parser compartido (cuyo límite propio es mayor): no modificar CP/1.
    internal fun documento(bytes: ByteArray, max: Int): JsonObject {
        require(bytes.isNotEmpty() && bytes.size <= max)
        val s = utf8Estricto(bytes)
        var nivel = 0; var cadena = false; var escape = false
        for (c in s) {
            if (cadena) {
                if (escape) escape = false else if (c == '\\') escape = true else if (c == '"') cadena = false
            } else when (c) {
                '"' -> cadena = true
                '{', '[' -> { nivel++; require(nivel <= 6) { "Profundidad CP/2 excesiva" } }
                '}', ']' -> { nivel--; require(nivel >= 0) }
            }
        }
        val o = JsonEstricto(s).leer() as? JsonObject ?: error("Objeto requerido")
        require(canonico(o).toByteArray(Charsets.UTF_8).contentEquals(bytes)) { "CP/2 no canónico" }
        return o
    }
    fun publica(s: String) {
        require(s.length in 100..200)
        val bytes = base64(s, 150)
        FormatosAvanzados.publicaP256(bytes)
        val k = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(bytes)) as ECPublicKey
        val primo = (k.params.curve.field as java.security.spec.ECFieldFp).p
        val x = k.w.affineX; val y = k.w.affineY
        require(x.signum() >= 0 && x < primo && y.signum() >= 0 && y < primo)
        require(y.pow(2).mod(primo) == x.pow(3).add(k.params.curve.a.multiply(x)).add(k.params.curve.b).mod(primo))
    }
    fun origen(s: String) {
        require(s.length in 9..200 && Regex("^https://[a-z0-9][a-z0-9.-]*(?::[0-9]{1,5})?$").matches(s))
        FormatosAvanzados.origenHttps(s)
        require(java.net.URI(s).toASCIIString() == s)
    }
    fun leerOferta(bytes: ByteArray): OfertaCp2 {
        val o = documento(bytes, MAX_FRAME)
        campos(o, "v", "kind", "protocol", "childPublicKeyB64", "enrollId", "enrollNonce")
        cabecera(o, "CHILD_OFFER")
        return OfertaCp2(texto(o, "childPublicKeyB64"), texto(o, "enrollId"), texto(o, "enrollNonce")).also {
            publica(it.childPublicKeyB64); require(uuid.matches(it.enrollId)); base64(it.enrollNonce, 32, 32)
        }
    }
    fun oferta(o: OfertaCp2): ByteArray = codificar(buildJsonObject {
        put("v", 2); put("kind", "CHILD_OFFER"); put("protocol", "cp.v2")
        put("childPublicKeyB64", o.childPublicKeyB64); put("enrollId", o.enrollId); put("enrollNonce", o.enrollNonce)
    }).also { leerOferta(it) }
    fun leerAceptacion(bytes: ByteArray): AceptacionCp2 {
        val o = documento(bytes, MAX_PAYLOAD)
        campos(o, "v", "kind", "protocol", "pairId", "enrollId", "enrollNonce", "parentPublicKeyB64", "childPublicKeyB64", "relayOrigin", "recoveryHashes")
        cabecera(o, "PAIR_ACCEPT")
        val r = o.getValue("recoveryHashes").jsonObject
        campos(r, "MAINTENANCE", "RECOVER_PARENT", "RETIRE")
        val h = RecuperacionCp2(texto(r, "MAINTENANCE"), texto(r, "RECOVER_PARENT"), texto(r, "RETIRE"))
        listOf(h.MAINTENANCE, h.RECOVER_PARENT, h.RETIRE).forEach { base64(it, 32, 32) }
        return AceptacionCp2(texto(o, "pairId"), texto(o, "enrollId"), texto(o, "enrollNonce"),
            texto(o, "parentPublicKeyB64"), texto(o, "childPublicKeyB64"), texto(o, "relayOrigin"), h).also {
            require(uuid.matches(it.pairId) && uuid.matches(it.enrollId)); base64(it.enrollNonce, 32, 32)
            publica(it.parentPublicKeyB64); publica(it.childPublicKeyB64); origen(it.relayOrigin)
        }
    }
    fun payload(a: AceptacionCp2): ByteArray = codificar(buildJsonObject {
        put("v", 2); put("kind", "PAIR_ACCEPT"); put("protocol", "cp.v2"); put("pairId", a.pairId)
        put("enrollId", a.enrollId); put("enrollNonce", a.enrollNonce)
        put("parentPublicKeyB64", a.parentPublicKeyB64); put("childPublicKeyB64", a.childPublicKeyB64)
        put("relayOrigin", a.relayOrigin)
        put("recoveryHashes", buildJsonObject {
            put("MAINTENANCE", a.recoveryHashes.MAINTENANCE); put("RECOVER_PARENT", a.recoveryHashes.RECOVER_PARENT)
            put("RETIRE", a.recoveryHashes.RETIRE)
        })
    }).also { leerAceptacion(it) }
    private fun codificar(o: JsonObject) = canonico(o).toByteArray(Charsets.UTF_8)
    private fun bytesFirma(p: ByteArray) = "CPv2/PAIR\n".toByteArray(Charsets.UTF_8) + p
    fun validarDer(f: ByteArray) {
        FormatosAvanzados.firmaDer(f)
        var i = 2
        repeat(2) {
            i++; val n = f[i++].toInt(); val valor = BigInteger(1, f.copyOfRange(i, i + n))
            require(valor > BigInteger.ZERO && valor < orden); i += n
        }
    }
    fun firmar(payload: ByteArray, publicaPadreEsperada: String, firmante: (ByteArray) -> ByteArray): PairVerificadoCp2 {
        val p = payload.copyOf(); val a = leerAceptacion(p)
        require(a.parentPublicKeyB64 == publicaPadreEsperada)
        val f = firmante(bytesFirma(p)).copyOf(); validarDer(f)
        val frame = codificar(buildJsonObject {
            put("v", 2); put("kind", "SIGNED"); put("purpose", "PAIR"); put("payloadB64", b64(p)); put("sigB64", b64(f))
        })
        return verificar(frame, publicaPadreEsperada)
    }
    fun verificar(bytes: ByteArray, publicaPadreEsperada: String): PairVerificadoCp2 {
        val frame = bytes.copyOf(); val o = documento(frame, MAX_FRAME)
        campos(o, "v", "kind", "purpose", "payloadB64", "sigB64"); cabecera(o, "SIGNED")
        require(texto(o, "purpose") == "PAIR"); publica(publicaPadreEsperada)
        val p = base64(texto(o, "payloadB64"), MAX_PAYLOAD)
        val f = base64(texto(o, "sigB64"), 72); validarDer(f)
        val k = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(base64(publicaPadreEsperada, 150)))
        require(Signature.getInstance("SHA256withECDSA").run { initVerify(k); update(bytesFirma(p)); verify(f) })
        val a = leerAceptacion(p); require(a.parentPublicKeyB64 == publicaPadreEsperada)
        return PairVerificadoCp2(a, p, frame)
    }
}
