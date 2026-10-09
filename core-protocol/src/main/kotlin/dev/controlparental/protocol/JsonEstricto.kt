package dev.controlparental.protocol

import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class MensajeInvalido(detalle: String) : IllegalArgumentException(detalle)

// Se analiza antes de construir DTO: un parser convencional puede perder duplicados.
internal class JsonEstricto(private val texto: String) {
    private var posicion = 0

    fun leer(): JsonElement {
        val resultado = valor(0)
        espacios()
        exigir(posicion == texto.length, "Contenido después del JSON")
        return resultado
    }

    private fun valor(profundidad: Int): JsonElement {
        exigir(profundidad <= 32, "JSON demasiado profundo")
        espacios()
        return when (actual()) {
            '{' -> objeto(profundidad + 1)
            '[' -> arreglo(profundidad + 1)
            '"' -> JsonPrimitive(cadena())
            't' -> literal("true", JsonPrimitive(true))
            'f' -> literal("false", JsonPrimitive(false))
            'n' -> literal("null", JsonNull)
            in '0'..'9' -> numero()
            else -> throw MensajeInvalido("Valor JSON no permitido")
        }
    }

    private fun objeto(profundidad: Int): JsonObject {
        posicion++
        val campos = linkedMapOf<String, JsonElement>()
        espacios()
        if (consumir('}')) return JsonObject(campos)
        do {
            espacios()
            val clave = cadena()
            exigir(!campos.containsKey(clave), "Clave JSON duplicada")
            espacios()
            exigir(consumir(':'), "Falta separador de campo")
            campos[clave] = valor(profundidad)
            espacios()
            if (consumir('}')) return JsonObject(campos)
        } while (consumir(','))
        throw MensajeInvalido("Objeto JSON sin cierre")
    }

    private fun arreglo(profundidad: Int): JsonArray {
        posicion++
        val elementos = mutableListOf<JsonElement>()
        espacios()
        if (consumir(']')) return JsonArray(elementos)
        do {
            elementos += valor(profundidad)
            espacios()
            if (consumir(']')) return JsonArray(elementos)
        } while (consumir(','))
        throw MensajeInvalido("Array JSON sin cierre")
    }

    private fun cadena(): String {
        exigir(consumir('"'), "Se esperaba una cadena JSON")
        val inicio = posicion - 1
        while (posicion < texto.length) {
            val caracter = texto[posicion++]
            if (caracter == '"') {
                val decodificada = try {
                    Json.decodeFromString<String>(texto.substring(inicio, posicion))
                } catch (_: IllegalArgumentException) {
                    throw MensajeInvalido("Escape de cadena inválido")
                }
                exigir(decodificada.all { it in ' '..'~' }, "Solo se admite ASCII imprimible")
                return decodificada
            }
            exigir(caracter >= ' ', "Control no permitido en cadena")
            if (caracter == '\\') {
                exigir(posicion < texto.length, "Escape JSON incompleto")
                posicion++
            }
        }
        throw MensajeInvalido("Cadena JSON sin cierre")
    }

    private fun numero(): JsonPrimitive {
        val inicio = posicion
        while (actual() in '0'..'9') posicion++
        val literal = texto.substring(inicio, posicion)
        exigir(literal == "0" || literal.first() != '0', "Número con ceros iniciales")
        val numero = literal.toLongOrNull() ?: throw MensajeInvalido("Entero fuera de rango")
        exigir(numero <= 9007199254740991L, "Entero JSON no seguro")
        return JsonPrimitive(numero)
    }

    private fun literal(esperado: String, resultado: JsonElement): JsonElement {
        exigir(texto.startsWith(esperado, posicion), "Literal JSON inválido")
        posicion += esperado.length
        return resultado
    }

    private fun espacios() {
        while (actual() in listOf(' ', '\t', '\r', '\n')) posicion++
    }

    private fun actual(): Char = texto.getOrNull(posicion) ?: '\u0000'
    private fun consumir(caracter: Char): Boolean = (actual() == caracter).also { if (it) posicion++ }
}

internal fun exigir(condicion: Boolean, detalle: String) {
    if (!condicion) throw MensajeInvalido(detalle)
}

internal fun utf8Estricto(bytes: ByteArray): String = try {
    Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(bytes)).toString()
} catch (_: java.nio.charset.CharacterCodingException) {
    throw MensajeInvalido("UTF-8 inválido")
}

internal fun canonico(valor: JsonElement): String = when (valor) {
    is JsonObject -> valor.keys.sorted().joinToString(",", "{", "}") { clave ->
        JsonPrimitive(clave).toString() + ":" + canonico(valor.getValue(clave))
    }
    is JsonArray -> valor.joinToString(",", "[", "]", transform = ::canonico)
    else -> valor.toString()
}
