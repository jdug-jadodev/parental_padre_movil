package dev.controlparental.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FormatosTest {
    @Test
    fun `base64 exige bits canonicos y longitud real`() {
        assertEquals("a", String(Formatos.base64("YQ", 1)))
        for (valor in listOf("YQ==", "YR", "YQ+", "", "A", "YWE")) {
            assertThrows(MensajeInvalido::class.java) { Formatos.base64(valor, 1) }
        }
        assertThrows(MensajeInvalido::class.java) { Formatos.base64("YQ", 32, 32) }
    }

    @Test
    fun `secuencias mantienen precision hasta LongMAX sin permitir desbordamiento`() {
        assertEquals(Long.MAX_VALUE, Formatos.secuencia("9223372036854775807"))
        assertEquals(9007199254740993L, Formatos.secuencia("9007199254740993"))
        assertEquals(0L, Formatos.secuencia("0", permiteCero = true))
        for (valor in listOf("0", "01", "-1", "+1", "1.0", " 1", "9223372036854775808")) {
            assertThrows(MensajeInvalido::class.java) { Formatos.secuencia(valor) }
        }
    }
}
