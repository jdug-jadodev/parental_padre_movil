package dev.controlparental.parent

import dev.controlparental.protocol.CodecProtocolo
import dev.controlparental.protocol.Latido
import dev.controlparental.protocol.MensajeInvalido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

// Comprueba recursos y APIs reales de Android; solo se ejecutará tras habilitar ADB.
class ProtocoloAndroidTest {
    @Test
    fun elEsquemaCongeladoEstaDisponibleDentroDelAPK() {
        val frame = """{"kind":"PING","nonce":"${"A".repeat(43)}","v":1}"""
        val mensaje = CodecProtocolo.leerFrame(frame.toByteArray()) as Latido
        assertEquals("A".repeat(43), mensaje.nonce)
        assertEquals(frame, CodecProtocolo.codificarFrame(mensaje).toString(Charsets.UTF_8))
    }

    @Test
    fun elParserAndroidNoAceptaClavesDuplicadas() {
        val frame = """{"kind":"PING","nonce":"${"A".repeat(43)}","v":1,"v":1}"""
        assertThrows(MensajeInvalido::class.java) { CodecProtocolo.leerFrame(frame.toByteArray()) }
    }
}
