package dev.controlparental.protocol.cp2

import dev.controlparental.protocol.*
import java.nio.file.Files
import java.nio.file.Path
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

internal object ComprobarVectorPublicoCp2 {
    val campos = setOf("fixture", "parentPublicKeyB64", "childPublicKeyB64", "childOfferUtf8", "pairFrameUtf8", "pairPayloadSha256B64", "authParentFrameUtf8", "authChildFrameUtf8")
    fun texto(o: JsonObject, k: String) = o.getValue(k).jsonPrimitive.content
    fun bytes(o: JsonObject, k: String) = texto(o, k).toByteArray(Charsets.UTF_8)
    fun payloadOriginal(frame: ByteArray): ByteArray = Base64.getUrlDecoder().decode(
        Json.parseToJsonElement(frame.toString(Charsets.UTF_8)).jsonObject.getValue("payloadB64").jsonPrimitive.content)
    fun comprobar(o: JsonObject) {
        assertEquals(campos, o.keys); assertTrue(o.values.all { it is JsonPrimitive && it.isString })
        assertEquals("CP2_PUBLICO_EFIMERO_SIN_AUTORIDAD", texto(o, "fixture"))
        val p = texto(o, "parentPublicKeyB64"); val h = texto(o, "childPublicKeyB64")
        assertNotEquals(p, h)
        val ofertaBytes = bytes(o, "childOfferUtf8"); val oferta = VinculacionCp2.leerOferta(ofertaBytes)
        assertArrayEquals(ofertaBytes, VinculacionCp2.oferta(oferta)); assertEquals(h, oferta.childPublicKeyB64)
        val frame = bytes(o, "pairFrameUtf8")
        // La pública esperada es la fijada por el fixture, nunca extraída del payload.
        val d = VinculacionCp2.verificar(frame, p)
        assertArrayEquals(frame, d.frame()); assertArrayEquals(payloadOriginal(frame), d.payload())
        assertEquals(texto(o, "pairPayloadSha256B64"), d.payloadSha256B64)
        assertEquals(VinculacionCp2.b64(MessageDigest.getInstance("SHA-256").digest(d.payload())), d.payloadSha256B64)
        assertEquals(p, d.aceptacion.parentPublicKeyB64); assertEquals(h, d.aceptacion.childPublicKeyB64)
        assertEquals(oferta.enrollId, d.aceptacion.enrollId); assertEquals(oferta.enrollNonce, d.aceptacion.enrollNonce)
        assertEquals("https://relay.example:443", d.aceptacion.relayOrigin)
        val camposPair = setOf("v", "kind", "protocol", "pairId", "enrollId", "enrollNonce", "parentPublicKeyB64", "childPublicKeyB64", "relayOrigin", "recoveryHashes")
        val obj = Json.parseToJsonElement(d.payload().toString(Charsets.UTF_8)).jsonObject
        assertEquals(10, obj.size); assertEquals(camposPair, obj.keys)
        assertEquals(6, Json.parseToJsonElement(ofertaBytes.toString(Charsets.UTF_8)).jsonObject.size)
        assertEquals(5, Json.parseToJsonElement(frame.toString(Charsets.UTF_8)).jsonObject.size)
        for ((campo, publica, rol) in listOf(Triple("authParentFrameUtf8", p, Rol.PARENT), Triple("authChildFrameUtf8", h, Rol.CHILD))) {
            val original = bytes(o, campo); val a = AutenticacionCp2.verificar(original, publica, rol)
            assertArrayEquals(original, a.frame()); assertArrayEquals(payloadOriginal(original), a.payload())
            assertEquals(d.aceptacion.pairId, a.prueba.pairId); assertEquals(rol, a.prueba.role)
        }
    }
}

class InteropCp2Test {
    private fun nodeBytes() = javaClass.getResourceAsStream("/cp2_node_publico.json")!!.use { it.readBytes() }
    private fun node() = Json.parseToJsonElement(nodeBytes().toString(Charsets.UTF_8)).jsonObject
    private fun rechaza(f: () -> Unit) { assertThrows(Exception::class.java, f) }
    @Test fun nodeOriginalHashArchivoYFirmaBytesIdentidadContexto() {
        val bytes = nodeBytes()
        assertEquals(10.toByte(), bytes.last())
        assertEquals("967ae05aecd28c6b7f90b8ae90e8421b408dd70acbf22fd30911085c69a964ec",
            MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) })
        val o = node(); ComprobarVectorPublicoCp2.comprobar(o)
        val d = VinculacionCp2.verificar(ComprobarVectorPublicoCp2.bytes(o, "pairFrameUtf8"), ComprobarVectorPublicoCp2.texto(o, "parentPublicKeyB64"))
        assertEquals("0eb84428-63f1-4d1e-9787-eac194d06359", d.aceptacion.pairId)
        assertEquals("6442f9e0-52d5-4020-abe0-ddccfecdb3d4", d.aceptacion.enrollId)
        assertEquals("oTQYu2mFOswjxW871k1p4Zno9Az03rmDy4E9vHqxE0M", d.aceptacion.enrollNonce)
        val schema = Path.of(requireNotNull(System.getProperty("raiz.cp2")), "contrato-cp2/vinculacion.schema.json")
        val def = Json.parseToJsonElement(Files.readString(schema)).jsonObject.getValue("\$defs").jsonObject.getValue("aceptacion").jsonObject
        val campos = Json.parseToJsonElement(d.payload().toString(Charsets.UTF_8)).jsonObject.keys
        assertEquals(10, def.getValue("required").jsonArray.size)
        assertEquals(campos, def.getValue("required").jsonArray.map { it.jsonPrimitive.content }.toSet())
        assertEquals(campos, def.getValue("properties").jsonObject.keys)
    }
    @Test fun vueltaJvmPublicadaSeVerificaSinRegenerar() {
        val ruta = Path.of(requireNotNull(System.getProperty("raiz.cp2")), "pruebas/interop_cp2_jvm_publica.json")
        val bytes = Files.readAllBytes(ruta); assertEquals(10.toByte(), bytes.last())
        assertEquals("fb830c79349479eca0e7ee5392c05d5a32ca62157af7e00fd1d890dcf2c65d4b",
            MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) })
        ComprobarVectorPublicoCp2.comprobar(Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject)
    }
    @Test fun nodeClavesRolesVersionYMutacionesDeBytesSeRechazan() {
        val o = node(); val p = ComprobarVectorPublicoCp2.texto(o, "parentPublicKeyB64"); val h = ComprobarVectorPublicoCp2.texto(o, "childPublicKeyB64")
        val pair = ComprobarVectorPublicoCp2.bytes(o, "pairFrameUtf8")
        rechaza { VinculacionCp2.verificar(pair, h) }
        rechaza { CriptografiaProtocolo.verificarSobre(pair, ClaveVerificacion.desdeSpki(Rol.PARENT, p), Proposito.PAIR) }
        rechaza { VinculacionCp2.leerOferta(ComprobarVectorPublicoCp2.bytes(o, "childOfferUtf8").toString(Charsets.UTF_8).replace("\"v\":2", "\"v\":1").toByteArray()) }
        fun alterar(frame: ByteArray, antes: String, despues: String): ByteArray {
            val obj = Json.parseToJsonElement(frame.toString(Charsets.UTF_8)).jsonObject
            val payload = ComprobarVectorPublicoCp2.payloadOriginal(frame).toString(Charsets.UTF_8)
            check(antes in payload)
            return JsonObject(obj + ("payloadB64" to JsonPrimitive(VinculacionCp2.b64(payload.replace(antes, despues).toByteArray())))).toString().toByteArray()
        }
        rechaza { VinculacionCp2.verificar(alterar(pair, "relay.example:443", "relay.example:444"), p) }
        for ((campo, publica, rol, otraClave, otroRol) in listOf(
            listOf("authParentFrameUtf8", p, "PARENT", h, "CHILD"), listOf("authChildFrameUtf8", h, "CHILD", p, "PARENT"))) {
            val f = ComprobarVectorPublicoCp2.bytes(o, campo)
            rechaza { AutenticacionCp2.verificar(f, otraClave, Rol.valueOf(rol)) }
            rechaza { AutenticacionCp2.verificar(f, publica, Rol.valueOf(otroRol)) }
            rechaza { AutenticacionCp2.verificar(alterar(f, rol, otroRol), publica, Rol.valueOf(otroRol)) }
            rechaza { CodecProtocolo.leerFrame(f) }
        }
    }
    @Test fun dominiosIncorrectosEnFirmanteEfimeroRechazadosPorCodecs() {
        val k = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        val publica = VinculacionCp2.b64(k.public.encoded)
        fun firmar(b: ByteArray) = Signature.getInstance("SHA256withECDSA").run { initSign(k.private); update(b); sign() }
        val o = node()
        val a = VinculacionCp2.leerAceptacion(ComprobarVectorPublicoCp2.payloadOriginal(ComprobarVectorPublicoCp2.bytes(o, "pairFrameUtf8"))).copy(parentPublicKeyB64 = publica)
        val p = VinculacionCp2.payload(a)
        for (prefix in listOf("CPv1/PAIR\n", "CPv2/AUTH\n", "CPv2/PAIR\\n")) {
            rechaza { VinculacionCp2.firmar(p, publica) { firmar(prefix.toByteArray() + p) } }
        }
        val auth = ComprobarVectorPublicoCp2.payloadOriginal(ComprobarVectorPublicoCp2.bytes(o, "authParentFrameUtf8"))
        for (prefix in listOf("CPv1/AUTH\n", "CPv2/PAIR\n", "CPv2/AUTH\\n")) {
            rechaza { AutenticacionCp2.firmar(auth, publica, Rol.PARENT) { firmar(prefix.toByteArray() + auth) } }
        }
    }
}
