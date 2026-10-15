package dev.controlparental.parent

import dev.controlparental.protocol.*
import dev.controlparental.protocol.cp2.*
import dev.controlparental.parent.pairing.*
import java.security.*
import java.security.spec.ECGenParameterSpec
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class PairCp2Test {
    private val padre = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    private val hijo = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    private val publica = VinculacionCp2.b64(padre.public.encoded)
    private val id = "00000000-0000-4000-8000-000000000001"
    private val nonce = "A".repeat(43)
    private val oferta = VinculacionCp2.oferta(OfertaCp2(VinculacionCp2.b64(hijo.public.encoded), id, nonce))
    private var ahora = 1000L
    private var alta = AltaCp2(publica, "generacion-1", "boot-prueba-1", true, true)
    private var autenticada = true
    private var firmas = 0
    private fun firma(bytes: ByteArray): ByteArray {
        firmas++
        return Signature.getInstance("SHA256withECDSA").run { initSign(padre.private); update(bytes); sign() }
    }
    private fun flujo() = PrepararPairCp2({ ahora }, { alta }, { check(autenticada) }, ::firma)
    private fun rechaza(bloque: () -> Unit) { assertThrows(Exception::class.java, bloque) }
    private fun preparar(f: PrepararPairCp2) = f.preparar(oferta, "https://relay.invalid")

    @Test fun firmaBytesExactosInmutablesSinCommitYHashPayload() {
        val f = flujo(); val t = preparar(f); val original = t.payload()
        assertEquals(3, t.mostrarCodigos().values.toSet().size)
        assertTrue(t.mostrarCodigos().values.all { Regex("[A-Z2-7]{32}").matches(it) })
        val copia = t.payload(); copia.fill(0)
        val d = f.firmar(t, true, true, true)
        assertArrayEquals(original, d.payload())
        assertEquals(VinculacionCp2.b64(MessageDigest.getInstance("SHA-256").digest(original)), d.payloadSha256B64)
        d.frame().fill(0); d.payload().fill(0)
        assertEquals(d.aceptacion, VinculacionCp2.verificar(d.frame(), publica).aceptacion)
        assertTrue(t.mostrarCodigos().isEmpty()); rechaza { f.firmar(t, true, true, true) }
        // No existe método store/commit en este caso de uso.
    }
    @Test fun cp1NoSeConvierteNiAceptaCp2() {
        rechaza { CodecProtocolo.leerOferta(oferta) }
        rechaza { VinculacionCp2.leerOferta(oferta.toString(Charsets.UTF_8).replace("\"v\":2", "\"v\":1").toByteArray()) }
        val d = flujo().let { it.firmar(preparar(it), true, true, true) }
        rechaza { CodecProtocolo.leerFrame(d.frame()) }
        rechaza { VinculacionCp2.verificar(d.frame(), VinculacionCp2.b64(hijo.public.encoded)) }
        val o = Json.parseToJsonElement(d.frame().toString(Charsets.UTF_8)).jsonObject
        for (dominio in listOf("CPv1/PAIR\n", "CPv2/OFFLINE\n")) {
            val sig = firma(dominio.toByteArray() + d.payload())
            val malo = JsonObject(o + ("sigB64" to JsonPrimitive(VinculacionCp2.b64(sig))))
            rechaza { VinculacionCp2.verificar(malo.toString().toByteArray(), publica) }
        }
    }
    @Test fun camposCanonicosDuplicadosTiposYLimites() {
        val s = oferta.toString(Charsets.UTF_8)
        val casos = listOf(" $s", "$s\n", s.replace("\"v\":2", "\"v\":\"2\""),
            s.replace("\"v\":2", "\"v\":2,\"v\":2"), s.replace("\"v\":2", "\"extra\":0,\"v\":2"),
            s.replace("\"v\":2", "\"v\":2.0"), s.replace("cp.v2", "cp.v1"),
            s.replace(id, id.uppercase().replace("4000", "5000")), s.replace(nonce, nonce + "="),
            s.replace(nonce, "A".repeat(42) + "B"), s.replace("CHILD_OFFER", "CHILD_ÓFFER"))
        casos.forEach { rechaza { VinculacionCp2.leerOferta(it.toByteArray(Charsets.UTF_8)) } }
        rechaza { VinculacionCp2.leerOferta(ByteArray(16385) { 32 }) }
        rechaza { VinculacionCp2.leerAceptacion(ByteArray(8193) { 32 }) }
        rechaza { VinculacionCp2.leerOferta(byteArrayOf(0xc3.toByte(), 0x28)) }
        rechaza { VinculacionCp2.leerOferta(("[".repeat(7) + "0" + "]".repeat(7)).toByteArray()) }
    }
    @Test fun spkiOrigenYDerRangoEstrictos() {
        val k = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp384r1")) }.generateKeyPair()
        rechaza { VinculacionCp2.publica(VinculacionCp2.b64(k.public.encoded)) }
        rechaza { VinculacionCp2.publica(VinculacionCp2.b64(padre.public.encoded + 0)) }
        for (s in listOf("http://relay.invalid", "https://Relay.invalid", "https://relay.invalid/", "https://a:0", "https://a:65536", "https://u@a", "https://a?q=1")) rechaza { VinculacionCp2.origen(s) }
        VinculacionCp2.origen("https://relay.invalid:443")
        rechaza { VinculacionCp2.validarDer(byteArrayOf(0x30, 6, 2, 1, 0, 2, 1, 1)) }
        val n = java.math.BigInteger("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16).toByteArray()
        rechaza { VinculacionCp2.validarDer(byteArrayOf(0x30, (n.size + 5).toByte(), 2, n.size.toByte()) + n + byteArrayOf(2, 1, 1)) }
    }
    @Test fun legadoInciertoNoAutorizadoBloqueaAntesDeCodigosYFirma() {
        for (a in listOf(alta.copy(ausenciaCoherente = false), alta.copy(autorizada = false))) {
            alta = a; rechaza { preparar(flujo()) }
        }
        assertEquals(0, firmas)
    }
    @Test fun ticketsCaducanEnBootGeneracionFondoCambioOfertaOAutenticacion() {
        val inicial = alta
        for (mutar in listOf<() -> Unit>({ ahora += 300_000 }, { ahora-- }, { alta = alta.copy(boot = "otro") },
            { alta = alta.copy(generacion = "otra") }, { autenticada = false })) {
            alta = inicial; ahora = 1000; autenticada = true
            val f = flujo(); val t = preparar(f); mutar()
            rechaza { f.firmar(t, true, true, true) }; assertTrue(t.mostrarCodigos().isEmpty())
        }
        alta = inicial; ahora = 1000; autenticada = true
        val f = flujo(); val viejo = preparar(f); f.descartar() // salida/fondo/destrucción del propietario del flujo
        rechaza { f.firmar(viejo, true, true, true) }
        val anterior = preparar(f); preparar(f)
        rechaza { f.firmar(anterior, true, true, true) }
        assertEquals(0, firmas)
    }
    @Test fun confirmacionesHumanasObligatoriasYDominiosRecuperacionSeparados() {
        for (i in 0..2) {
            val f = flujo(); val t = preparar(f)
            rechaza { f.firmar(t, i != 0, i != 1, i != 2) }
        }
        val codigo = "A".repeat(32) // Ficticio, no se usa como credencial de app.
        assertNotEquals(RecoveryCodeGenerator.hash(id, PropositoRecuperacion.MAINTENANCE, codigo),
            PrepararPairCp2.hashRecuperacion(id, PropositoRecuperacion.MAINTENANCE, codigo))
        assertNotEquals(PrepararPairCp2.hashRecuperacion(id, PropositoRecuperacion.MAINTENANCE, codigo),
            PrepararPairCp2.hashRecuperacion(id, PropositoRecuperacion.RETIRE, codigo))
    }
    @Test fun qrOfertaYFirmaRealFicticiaPreservanBytes() {
        val d = flujo().let { it.firmar(preparar(it), true, true, true) }
        for (b in listOf(oferta, d.frame())) {
            val m = QrLocal.generar(b.toString(Charsets.UTF_8))
            val y = ByteArray(m.width * m.height) { if (m[it % m.width, it / m.width]) 0 else -1 }
            assertArrayEquals(b, QrLocal.leerLuminancia(m.width, m.height, y).toByteArray(Charsets.UTF_8))
        }
        VinculacionCp2.verificar(d.frame(), publica)
    }
    @Test fun ofertaPublicaFijaYEsquemaFuenteConCamposExactos() {
        val fixture = javaClass.getResourceAsStream("/qr01_respuesta_publica.json")!!.use {
            Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject
        }
        val fija = OfertaCp2(fixture.getValue("publica").jsonPrimitive.content, id, nonce)
        val bytes = VinculacionCp2.oferta(fija)
        val m = QrLocal.generar(bytes.toString(Charsets.UTF_8))
        val pixels = IntArray(m.width * m.height) { if (m[it % m.width, it / m.width]) 0xff000000.toInt() else -1 }
        val leido = QrLocal.leer(m.width, m.height, pixels).toByteArray(Charsets.UTF_8)
        assertArrayEquals(bytes, leido); assertEquals(fija, VinculacionCp2.leerOferta(leido))
        val schema = Json.parseToJsonElement(java.io.File(requireNotNull(System.getProperty("schemaCp2"))).readText()).jsonObject
        val defs = schema.getValue("\$defs").jsonObject
        val d = flujo().let { it.firmar(preparar(it), true, true, true) }
        for ((nombre, b) in listOf("oferta" to bytes, "aceptacion" to d.payload(), "sobrePair" to d.frame())) {
            val def = defs.getValue(nombre).jsonObject
            val o = Json.parseToJsonElement(b.toString(Charsets.UTF_8)).jsonObject
            assertEquals(def.getValue("properties").jsonObject.keys, o.keys)
            assertEquals(def.getValue("required").jsonArray.map { it.jsonPrimitive.content }.toSet(), o.keys)
            assertEquals(JsonPrimitive(false), def.getValue("additionalProperties"))
            for (k in o.keys) {
                val sinCampo = JsonObject(o - k).toString().toByteArray()
                rechaza { when (nombre) {
                    "oferta" -> VinculacionCp2.leerOferta(sinCampo)
                    "aceptacion" -> VinculacionCp2.leerAceptacion(sinCampo)
                    else -> VinculacionCp2.verificar(sinCampo, publica)
                } }
            }
        }
    }
}
