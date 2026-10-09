package dev.controlparental.protocol

import org.junit.Assert.assertEquals
import org.junit.Test

class ProtocoloTest {
    @Test
    fun `la base conserva la version del contrato`() {
        assertEquals(1, Protocolo.VERSION)
    }
}
