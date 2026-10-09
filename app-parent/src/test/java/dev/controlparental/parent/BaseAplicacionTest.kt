package dev.controlparental.parent

import dev.controlparental.protocol.Protocolo
import org.junit.Assert.assertEquals
import org.junit.Test

class BaseAplicacionTest {
    @Test
    fun `la aplicacion usa el nucleo CP1`() {
        assertEquals(1, Protocolo.VERSION)
    }
}
