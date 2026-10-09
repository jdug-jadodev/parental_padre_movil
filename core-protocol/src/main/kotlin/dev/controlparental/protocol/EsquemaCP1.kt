package dev.controlparental.protocol

import kotlinx.serialization.json.*

// Intérprete del subconjunto usado por el esquema congelado, no de todo JSON Schema.
// El recurso se incorpora directamente desde compartido/, sin una segunda copia editable.
internal object EsquemaCP1 {
    private val raiz: JsonObject = EsquemaCP1::class.java
        .getResourceAsStream("/protocolo.schema.json")!!.use {
            Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject
        }

    fun validar(valor: JsonObject) {
        exigir(coincide(raiz, valor), "El mensaje no cumple el esquema CP/1")
    }

    private fun coincide(esquema: JsonObject, valor: JsonElement): Boolean {
        esquema["\$ref"]?.let {
            val referencia = it.jsonPrimitive.content
            exigir(referencia.startsWith("#/\$defs/"), "Referencia de esquema no soportada")
            return coincide(raiz.getValue("\$defs").jsonObject.getValue(referencia.substringAfterLast('/')).jsonObject, valor)
        }
        esquema["const"]?.let { if (valor != it) return false }
        esquema["enum"]?.let { if (valor !in it.jsonArray) return false }
        esquema["type"]?.let { if (!tipo(it.jsonPrimitive.content, valor)) return false }
        esquema["anyOf"]?.let { opciones ->
            if (opciones.jsonArray.none { coincide(it.jsonObject, valor) }) return false
        }
        esquema["oneOf"]?.let { opciones ->
            if (opciones.jsonArray.count { coincide(it.jsonObject, valor) } != 1) return false
        }
        esquema["allOf"]?.let { opciones ->
            if (opciones.jsonArray.any { !coincide(it.jsonObject, valor) }) return false
        }
        esquema["if"]?.let {
            val rama = if (coincide(it.jsonObject, valor)) esquema["then"] else esquema["else"]
            if (rama != null && !coincide(rama.jsonObject, valor)) return false
        }
        if (valor is JsonObject) {
            val propiedades = esquema["properties"]?.jsonObject ?: emptyMap()
            esquema["required"]?.let { requeridos ->
                if (requeridos.jsonArray.any { it.jsonPrimitive.content !in valor }) return false
            }
            if (esquema["additionalProperties"] == JsonPrimitive(false) && valor.keys.any { it !in propiedades }) return false
            for ((clave, definicion) in propiedades) {
                if (clave in valor && !coincide(definicion.jsonObject, valor.getValue(clave))) return false
            }
        }
        if (valor is JsonPrimitive && valor.isString) {
            val cadena = valor.content
            esquema["pattern"]?.let { if (!Regex(it.jsonPrimitive.content).containsMatchIn(cadena)) return false }
            esquema["minLength"]?.let { if (cadena.length < it.jsonPrimitive.int) return false }
            esquema["maxLength"]?.let { if (cadena.length > it.jsonPrimitive.int) return false }
        }
        if (valor is JsonPrimitive && !valor.isString && valor.longOrNull != null) {
            val numero = valor.long
            esquema["minimum"]?.let { if (numero < it.jsonPrimitive.long) return false }
            esquema["maximum"]?.let { if (numero > it.jsonPrimitive.long) return false }
        }
        return true
    }

    private fun tipo(tipo: String, valor: JsonElement): Boolean = when (tipo) {
        "object" -> valor is JsonObject
        "null" -> valor == JsonNull
        "string" -> valor is JsonPrimitive && valor.isString
        "boolean" -> valor is JsonPrimitive && !valor.isString && valor.booleanOrNull != null
        "integer" -> valor is JsonPrimitive && !valor.isString && valor.longOrNull != null
        else -> throw MensajeInvalido("Tipo de esquema no soportado")
    }
}
