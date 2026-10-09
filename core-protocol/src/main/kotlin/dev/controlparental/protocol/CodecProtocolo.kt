package dev.controlparental.protocol

import kotlinx.serialization.json.*

object CodecProtocolo {
    const val MAX_FRAME = 16384
    const val MAX_PAYLOAD = 8192
    private val json = Json { encodeDefaults = true; classDiscriminator = "kind" }
    private val frames = setOf("SIGNED", "CHALLENGE", "AUTH_OK", "PING", "PONG", "PEER_STATUS", "PEER_UNAVAILABLE", "ERROR")
    private val propositos = mapOf(
        "AUTH_PROOF" to Proposito.AUTH, "SYNC_REQUEST" to Proposito.MESSAGE,
        "SET_POLICY" to Proposito.MESSAGE, "ACK" to Proposito.MESSAGE,
        "STATE" to Proposito.MESSAGE, "PAIR_ACCEPT" to Proposito.PAIR,
        "OFFLINE_CHALLENGE" to Proposito.OFFLINE, "OFFLINE_RESPONSE" to Proposito.OFFLINE,
    )

    fun leerFrame(bytes: ByteArray): Mensaje {
        val objeto = documento(bytes, MAX_FRAME)
        exigir(objeto.getValue("kind").jsonPrimitive.content in frames, "Payload suelto no permitido en WSS")
        return json.decodeFromJsonElement<Mensaje>(objeto)
    }

    fun leerOferta(bytes: ByteArray): OfertaHijo {
        val objeto = documento(bytes, MAX_FRAME)
        exigir(objeto.getValue("kind") == JsonPrimitive("CHILD_OFFER"), "QR no es una oferta del hijo")
        return json.decodeFromJsonElement<Mensaje>(objeto) as OfertaHijo
    }

    // Exclusivamente validación estructural. C02 verificará primero la firma sobre
    // los bytes originales. Este resultado NO acredita identidad ni autorización.
    fun leerPayloadSinVerificar(bytes: ByteArray, proposito: Proposito): Mensaje {
        val objeto = documento(bytes, MAX_PAYLOAD)
        exigir(canonico(objeto).toByteArray().contentEquals(bytes), "Payload no canónico")
        exigir(propositos[objeto.getValue("kind").jsonPrimitive.content] == proposito, "Propósito incompatible con payload")
        return json.decodeFromJsonElement<Mensaje>(objeto)
    }

    fun codificarPayload(mensaje: Mensaje): ByteArray {
        val objeto = json.encodeToJsonElement<Mensaje>(mensaje).jsonObject
        exigir(objeto.getValue("kind").jsonPrimitive.content in propositos, "Tipo no apto para firma")
        return codificar(objeto, MAX_PAYLOAD)
    }

    fun codificarFrame(mensaje: Mensaje): ByteArray {
        val objeto = json.encodeToJsonElement<Mensaje>(mensaje).jsonObject
        exigir(objeto.getValue("kind").jsonPrimitive.content in frames, "Tipo no apto para frame WSS")
        return codificar(objeto, MAX_FRAME)
    }

    fun bytesPayload(sobre: SobreFirmado): ByteArray = Formatos.base64(sobre.payloadB64, MAX_PAYLOAD)

    private fun codificar(objeto: JsonObject, limite: Int): ByteArray {
        val bytes = canonico(objeto).toByteArray(Charsets.UTF_8)
        documento(bytes, limite)
        return bytes
    }

    private fun documento(bytes: ByteArray, limite: Int): JsonObject {
        exigir(bytes.isNotEmpty() && bytes.size <= limite, "Tamaño de mensaje no permitido")
        val objeto = JsonEstricto(utf8Estricto(bytes)).leer() as? JsonObject
            ?: throw MensajeInvalido("Se esperaba un objeto de protocolo")
        EsquemaCP1.validar(objeto)
        validarFormatos(objeto)
        return objeto
    }

    private fun validarFormatos(objeto: JsonObject) {
        for ((clave, valor) in objeto) {
            if (valor == JsonNull) continue
            val contenido = (valor as? JsonPrimitive)?.content ?: continue
            when (clave) {
                "seq", "stateSeq", "lastAcceptedSeq" -> Formatos.secuencia(contenido, clave == "lastAcceptedSeq")
                "nonce", "syncNonce", "commandNonce", "enrollNonce",
                "commandPayloadSha256B64", "lastCommandPayloadSha256B64" -> Formatos.base64(contenido, 32, 32)
                "payloadB64" -> Formatos.base64(contenido, MAX_PAYLOAD)
                "sigB64" -> FormatosAvanzados.firmaDer(Formatos.base64(contenido, 75))
                "parentPublicKeyB64", "childPublicKeyB64" -> FormatosAvanzados.publicaP256(Formatos.base64(contenido, 150))
                "relayOrigin" -> FormatosAvanzados.origenHttps(contenido)
            }
        }
        objeto["recoveryHashes"]?.jsonObject?.values?.forEach { Formatos.base64(it.jsonPrimitive.content, 32, 32) }
    }
}
