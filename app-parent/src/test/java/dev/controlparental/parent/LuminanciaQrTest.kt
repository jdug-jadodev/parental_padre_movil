package dev.controlparental.parent

import dev.controlparental.parent.pairing.*
import dev.controlparental.protocol.CodecProtocolo
import java.nio.ByteBuffer
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class LuminanciaQrTest {
    @Test fun copiaPosicionStridesYPaddingSinCambiarBuffer() {
        val buffer = ByteBuffer.allocate(26)
        for (y in 0..1) for (x in 0..2) buffer.put(8 + y * 10 + x * 2, (y * 3 + x).toByte())
        buffer.position(8); buffer.limit(23)
        val gris = LuminanciaQr.copiar(buffer, 3, 2, 10, 2)
        assertArrayEquals(byteArrayOf(0, 1, 2, 3, 4, 5), gris.bytes)
        assertEquals(8, buffer.position()); assertEquals(23, buffer.limit())
    }

    @Test fun limitesYPlanoTruncadoNoPermitenAccesoFueraDeBuffer() {
        val buffer = ByteBuffer.allocate(20)
        for (argumentos in listOf(intArrayOf(0, 1, 1, 1), intArrayOf(4097, 1, 4097, 1),
            intArrayOf(3, 2, 2, 1), intArrayOf(3, 2, 12, 4), intArrayOf(3, 2, 3, 0))) {
            assertThrows(IllegalArgumentException::class.java) {
                LuminanciaQr.copiar(buffer, argumentos[0], argumentos[1], argumentos[2], argumentos[3])
            }
        }
        buffer.limit(5)
        assertThrows(IllegalArgumentException::class.java) { LuminanciaQr.copiar(buffer, 3, 2, 3, 1) }
    }

    @Test fun submuestreoAcotaPlanosGrandes() {
        val buffer = ByteBuffer.allocate(4096 * 3)
        val gris = LuminanciaQr.copiar(buffer, 4096, 3, 4096, 1)
        assertEquals(2048, gris.ancho); assertEquals(2, gris.alto)
        assertEquals(4096, gris.bytes.size)
    }

    @Test fun ofertaQrSeLeeDesdePlanoYConPaddingEnLasCuatroRotaciones() {
        val par = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        val publica = Base64.getUrlEncoder().withoutPadding().encodeToString(par.public.encoded)
        val texto = buildJsonObject {
            put("v", 1); put("kind", "CHILD_OFFER"); put("childPublicKeyB64", publica)
            put("enrollId", "00000000-0000-4000-8000-000000000001"); put("enrollNonce", "A".repeat(43))
        }.toString()
        val matriz = QrLocal.generar(texto)
        val lado = matriz.width
        var bytes = ByteArray(lado * lado) { indice -> if (matriz[indice % lado, indice / lado]) 0 else -1 }
        repeat(4) {
            val stride = lado + 16
            val buffer = ByteBuffer.allocate(stride * lado + 7).apply { position(7) }
            for (y in 0 until lado) for (x in 0 until lado) buffer.put(7 + y * stride + x, bytes[y * lado + x])
            val gris = LuminanciaQr.copiar(buffer.asReadOnlyBuffer(), lado, lado, stride, 1)
            val leido = QrLocal.leerLuminancia(gris.ancho, gris.alto, gris.bytes)
            assertEquals(CodecProtocolo.leerOferta(texto.toByteArray()), CodecProtocolo.leerOferta(leido.toByteArray()))
            val anterior = bytes
            bytes = ByteArray(lado * lado) { indice ->
                val x = indice % lado; val y = indice / lado
                anterior[(lado - 1 - x) * lado + y]
            }
        }
    }

    @Test fun qrNoOfertaNoSuperaValidacionDelContrato() {
        val matriz = QrLocal.generar("Esto no es CHILD_OFFER")
        val bytes = ByteArray(matriz.width * matriz.height) { indice ->
            if (matriz[indice % matriz.width, indice / matriz.width]) 0 else -1
        }
        val texto = QrLocal.leerLuminancia(matriz.width, matriz.height, bytes)
        assertTrue(runCatching { CodecProtocolo.leerOferta(texto.toByteArray()) }.isFailure)
    }
}
