package dev.controlparental.protocol.cp2

import dev.controlparental.protocol.Rol
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.UUID
import kotlinx.serialization.json.*

// No se compila en main ni crea archivos salvo al invocar explícitamente main.
object GenerarVectorPublicoCp2 {
    private val jsonSalida = Json { prettyPrint = true }
    @JvmStatic fun main(args: Array<String>) {
        require(args.size == 1)
        val destino = Path.of(args.single())
        check(!Files.exists(destino)) { "Vector ya existente: no regenerar" }
        val vector = generar()
        ComprobarVectorPublicoCp2.comprobar(vector)
        val bytes = (jsonSalida.encodeToString(JsonObject.serializer(), vector) + "\n").toByteArray(Charsets.UTF_8)
        Files.write(destino, bytes, CREATE_NEW)
        println("VECTOR_JVM_PUBLICADO SHA256=" + MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) })
    }
    private fun generar(): JsonObject {
        val g = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }
        val padre = g.generateKeyPair(); val hijo = g.generateKeyPair()
        val p = VinculacionCp2.b64(padre.public.encoded); val h = VinculacionCp2.b64(hijo.public.encoded)
        val random = SecureRandom()
        fun hashPublicoFicticio() = VinculacionCp2.b64(ByteArray(32).also(random::nextBytes))
        fun id() = UUID.randomUUID().toString()
        fun firmar(k: KeyPair, bytes: ByteArray) = Signature.getInstance("SHA256withECDSA").run { initSign(k.private); update(bytes); sign() }
        val oferta = OfertaCp2(h, id(), hashPublicoFicticio())
        val pairId = id()
        val a = AceptacionCp2(pairId, oferta.enrollId, oferta.enrollNonce, p, h, "https://relay.example:443",
            RecuperacionCp2(hashPublicoFicticio(), hashPublicoFicticio(), hashPublicoFicticio()))
        val pair = VinculacionCp2.firmar(VinculacionCp2.payload(a), p) { firmar(padre, it) }
        fun auth(rol: Rol, k: KeyPair): AuthVerificadaCp2 {
            val prueba = PruebaAuthCp2(pairId, rol, id(), hashPublicoFicticio())
            return AutenticacionCp2.firmar(AutenticacionCp2.payload(prueba), VinculacionCp2.b64(k.public.encoded), rol) { firmar(k, it) }
        }
        return buildJsonObject {
            put("fixture", "CP2_PUBLICO_EFIMERO_SIN_AUTORIDAD")
            put("parentPublicKeyB64", p); put("childPublicKeyB64", h)
            put("childOfferUtf8", VinculacionCp2.oferta(oferta).toString(Charsets.UTF_8))
            put("pairFrameUtf8", pair.frame().toString(Charsets.UTF_8)); put("pairPayloadSha256B64", pair.payloadSha256B64)
            put("authParentFrameUtf8", auth(Rol.PARENT, padre).frame().toString(Charsets.UTF_8))
            put("authChildFrameUtf8", auth(Rol.CHILD, hijo).frame().toString(Charsets.UTF_8))
        }
    }
}
