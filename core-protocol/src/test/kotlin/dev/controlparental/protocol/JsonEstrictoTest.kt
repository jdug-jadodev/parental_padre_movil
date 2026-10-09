package dev.controlparental.protocol

import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class JsonEstrictoTest {
    @Test
    fun `ordena objetos pero conserva el orden de arrays`() {
        val objeto = JsonEstricto(""" { "z": [2,1], "a": null, "b": true } """).leer()
        assertEquals("""{"a":null,"b":true,"z":[2,1]}""", canonico(objeto))
    }

    @Test
    fun `rechaza duplicados incluso con escapes equivalentes`() {
        for (texto in listOf("""{"v":1,"v":2}""", """{"v":1,"\u0076":1}""")) {
            assertThrows(MensajeInvalido::class.java) { JsonEstricto(texto).leer() }
        }
    }

    @Test
    fun `rechaza numeros ambiguos sintaxis invalida y controles`() {
        for (texto in listOf(
            "01", "-0", "-1", "1.0", "1e0", "9007199254740992", "[1,]", "{\"a\":1,}",
            "true false", "\"ñ\"", "\"\\n\"", "\"\\u0000\"", "\"\\q\"", "\"sin cierre",
        )) {
            assertThrows(texto, MensajeInvalido::class.java) { JsonEstricto(texto).leer() }
        }
    }

    @Test
    fun `rechaza profundidad excesiva y bytes UTF8 rotos`() {
        assertThrows(MensajeInvalido::class.java) { JsonEstricto("[".repeat(34) + "0" + "]".repeat(34)).leer() }
        assertThrows(MensajeInvalido::class.java) { utf8Estricto(byteArrayOf(0xc3.toByte(), 0x28)) }
        assertEquals("ASCII", utf8Estricto("ASCII".toByteArray()))
    }

    @Test
    fun `escapes imprimibles se normalizan sin perder datos`() {
        assertEquals(JsonPrimitive("A"), JsonEstricto("\"\\u0041\"").leer())
        assertEquals("\"A\"", canonico(JsonEstricto("\"\\u0041\"").leer()))
    }
}
