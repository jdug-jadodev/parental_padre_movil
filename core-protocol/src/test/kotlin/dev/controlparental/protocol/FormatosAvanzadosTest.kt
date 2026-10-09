package dev.controlparental.protocol

import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import org.junit.Assert.assertThrows
import org.junit.Test

class FormatosAvanzadosTest {
    @Test
    fun `DER exige dos enteros positivos sin bytes sobrantes`() {
        val valida = byteArrayOf(0x30, 6, 2, 1, 1, 2, 1, 1)
        FormatosAvanzados.firmaDer(valida)
        for (alterada in listOf(
            valida + byteArrayOf(0), valida.copyOf().apply { this[4] = 0 },
            valida.copyOf().apply { this[4] = 0x80.toByte() }, ByteArray(64),
        )) assertThrows(MensajeInvalido::class.java) { FormatosAvanzados.firmaDer(alterada) }
    }

    @Test
    fun `solo admite publicas SPKI P256`() {
        for (curva in listOf("secp256r1", "secp384r1")) {
            val generador = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec(curva)) }
            val publica = generador.generateKeyPair().public.encoded
            if (curva == "secp256r1") FormatosAvanzados.publicaP256(publica)
            else assertThrows(MensajeInvalido::class.java) { FormatosAvanzados.publicaP256(publica) }
        }
        assertThrows(MensajeInvalido::class.java) { FormatosAvanzados.publicaP256(byteArrayOf(1, 2, 3)) }
    }

    @Test
    fun `origen no permite rutas credenciales ni puertos imposibles`() {
        FormatosAvanzados.origenHttps("https://relay.example:443")
        for (origen in listOf("http://relay.example", "https://u@relay.example", "https://relay.example/", "https://relay.example:65536", "https://relay.example?x=1", "https://a-.example", "https://a..example")) {
            assertThrows(MensajeInvalido::class.java) { FormatosAvanzados.origenHttps(origen) }
        }
    }
}
