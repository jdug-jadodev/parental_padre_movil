package dev.controlparental.protocol

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class CodecProtocoloTest {
    private fun recurso(nombre: String): ByteArray = javaClass.getResourceAsStream("/$nombre")!!.use { it.readBytes() }
    private fun payload(nombre: String): JsonObject = Json.parseToJsonElement(recurso(nombre).toString(Charsets.UTF_8)).jsonObject
    private fun rechazar(objeto: JsonObject, proposito: Proposito = Proposito.MESSAGE) {
        assertThrows(MensajeInvalido::class.java) {
            CodecProtocolo.leerPayloadSinVerificar(canonico(objeto).toByteArray(), proposito)
        }
    }

    @Test
    fun `todos los vectores preservan bytes exactos y producen DTO`() {
        val vectores = payload("vectores_crypto.json").getValue("vectors").jsonArray
        for (vector in vectores) {
            val sobreJson = vector.jsonObject.getValue("envelope")
            val sobre = CodecProtocolo.leerFrame(sobreJson.toString().toByteArray()) as SobreFirmado
            val bytes = CodecProtocolo.bytesPayload(sobre)
            val mensaje = CodecProtocolo.leerPayloadSinVerificar(bytes, sobre.purpose)
            assertArrayEquals(bytes, CodecProtocolo.codificarPayload(mensaje))
        }
    }

    @Test
    fun `frames de transporte y oferta local respetan sus canales`() {
        for (nombre in listOf("12_challenge", "13_auth_ok", "14_ping", "15_pong", "16_peer_status", "17_peer_unavailable", "18_error")) {
            val frame = CodecProtocolo.leerFrame(recurso("$nombre.frame.json"))
            assertEquals(frame, CodecProtocolo.leerFrame(CodecProtocolo.codificarFrame(frame)))
        }
        assertEquals(1, CodecProtocolo.leerOferta(recurso("11_child_offer.frame.json")).v)
        assertThrows(MensajeInvalido::class.java) { CodecProtocolo.leerFrame(recurso("11_child_offer.frame.json")) }
        assertThrows(MensajeInvalido::class.java) { CodecProtocolo.leerFrame(recurso("04_lock_for.payload.json")) }
    }

    @Test
    fun `rechaza campos desconocidos ausentes y tipos falsos`() {
        val orden = payload("04_lock_for.payload.json")
        rechazar(JsonObject(orden + ("extra" to JsonPrimitive(true))))
        rechazar(JsonObject(orden - "durationSec"))
        for ((clave, valor) in listOf(
            "v" to JsonPrimitive(2), "v" to JsonPrimitive("1"), "seq" to JsonPrimitive(7),
            "sender" to JsonPrimitive("CHILD"), "bootId" to JsonPrimitive("no-uuid"),
            "commandNonce" to JsonPrimitive("A".repeat(42) + "B"), "durationSec" to JsonPrimitive(true),
        )) rechazar(JsonObject(orden + (clave to valor)))
    }

    @Test
    fun `duraciones y secuencias validan limites reales`() {
        val orden = payload("04_lock_for.payload.json")
        for (duracion in listOf(60, 604800)) {
            val objeto = JsonObject(orden + ("durationSec" to JsonPrimitive(duracion)))
            CodecProtocolo.leerPayloadSinVerificar(canonico(objeto).toByteArray(), Proposito.MESSAGE)
        }
        for (duracion in listOf(JsonNull, JsonPrimitive(59), JsonPrimitive(604801))) {
            rechazar(JsonObject(orden + ("durationSec" to duracion)))
        }
        rechazar(JsonObject(orden + ("action" to JsonPrimitive("LOCK"))))
        val bloqueo = JsonObject(orden + mapOf("action" to JsonPrimitive("LOCK"), "durationSec" to JsonNull))
        assertTrue(CodecProtocolo.leerPayloadSinVerificar(canonico(bloqueo).toByteArray(), Proposito.MESSAGE) is OrdenPolitica)
        for (secuencia in listOf("0", "07", "9223372036854775808")) rechazar(JsonObject(orden + ("seq" to JsonPrimitive(secuencia))))
        val maximo = JsonObject(orden + ("seq" to JsonPrimitive("9223372036854775807")))
        assertEquals("9223372036854775807", (CodecProtocolo.leerPayloadSinVerificar(canonico(maximo).toByteArray(), Proposito.MESSAGE) as OrdenPolitica).seq)
    }

    @Test
    fun `estado inicial no admite inventar una orden aplicada`() {
        val inicial = payload("10_state_inicial.payload.json")
        rechazar(JsonObject(inicial + ("lastResult" to JsonPrimitive("APPLIED"))))
        rechazar(JsonObject(inicial + ("lastAcceptedSeq" to JsonPrimitive("7"))))
        rechazar(JsonObject(inicial + ("lastCommandId" to inicial.getValue("bootId"))))
        rechazar(JsonObject(inicial + ("stateSeq" to JsonPrimitive("0"))))
    }

    @Test
    fun `payload firmado debe ser canonico y tener proposito correcto`() {
        val orden = canonico(payload("04_lock_for.payload.json"))
        for (texto in listOf(" $orden", "$orden\n", orden.replace("\"v\":1", "\"v\":1,\"v\":1"), orden.replace("\"v\":1", "\"v\":1e0"))) {
            assertThrows(MensajeInvalido::class.java) { CodecProtocolo.leerPayloadSinVerificar(texto.toByteArray(), Proposito.MESSAGE) }
        }
        rechazar(payload("04_lock_for.payload.json"), Proposito.OFFLINE)
    }

    @Test
    fun `limites en bytes y firma DER malformada se rechazan`() {
        assertThrows(MensajeInvalido::class.java) { CodecProtocolo.leerFrame(ByteArray(16385) { 32 }) }
        assertThrows(MensajeInvalido::class.java) { CodecProtocolo.leerPayloadSinVerificar(ByteArray(8193) { 32 }, Proposito.MESSAGE) }
        assertThrows(MensajeInvalido::class.java) { CodecProtocolo.leerFrame(byteArrayOf(0xc3.toByte(), 0x28)) }
        val sobre = payload("04_lock_for.signed.json")
        val alterado = JsonObject(sobre + ("sigB64" to JsonPrimitive(Formatos.codificar(ByteArray(64)))))
        assertThrows(MensajeInvalido::class.java) { CodecProtocolo.leerFrame(alterado.toString().toByteArray()) }
    }

    @Test
    fun `contendor no canonico puede tener espacios pero no duplicados`() {
        val frame = """{ "nonce":"${"A".repeat(43)}", "kind":"PING", "v":1 }"""
        assertTrue(CodecProtocolo.leerFrame(frame.toByteArray()) is Latido)
        assertThrows(MensajeInvalido::class.java) { CodecProtocolo.leerFrame(frame.replace("\"v\":1", "\"v\":1,\"v\":1").toByteArray()) }
    }
}
