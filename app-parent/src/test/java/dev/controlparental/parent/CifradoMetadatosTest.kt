package dev.controlparental.parent

import dev.controlparental.parent.security.CifradoMetadatos
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import org.junit.Assert.*
import org.junit.Test

class CifradoMetadatosTest {
    private val clave = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val cifrador = CifradoMetadatos({}, { clave })

    @Test fun nonceNuevoYTextoNoVisibleEnCadaEscritura() {
        val texto = "Metadatos ficticios, sin claves privadas".toByteArray()
        val primero = cifrador.cifrar(texto)
        val segundo = cifrador.cifrar(texto)
        assertFalse(primero.contentEquals(segundo))
        assertFalse(primero.copyOfRange(8, 20).contentEquals(segundo.copyOfRange(8, 20)))
        assertArrayEquals(texto, cifrador.descifrar(primero))
        assertArrayEquals(texto, cifrador.descifrar(segundo))
        assertFalse(primero.toString(Charsets.UTF_8).contains("Metadatos"))
    }

    @Test fun alteracionesDeNonceOCiphertextNoAutorizanLectura() {
        val original = cifrador.cifrar("Solo pruebas".toByteArray())
        for (posicion in listOf(8, 20, original.lastIndex)) {
            val alterado = original.copyOf()
            alterado[posicion] = (alterado[posicion].toInt() xor 1).toByte()
            assertThrows(AEADBadTagException::class.java) { cifrador.descifrar(alterado) }
        }
    }

    @Test fun claveDistintaCabeceraVersionYTruncadoSeRechazan() {
        val original = cifrador.cifrar("Solo pruebas".toByteArray())
        val otraClave = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        assertThrows(AEADBadTagException::class.java) { CifradoMetadatos({}, { otraClave }).descifrar(original) }
        assertThrows(IllegalArgumentException::class.java) { cifrador.descifrar(original.copyOf(12)) }
        val otraVersion = original.copyOf().apply { this[7] = '2'.code.toByte() }
        assertThrows(IllegalArgumentException::class.java) { cifrador.descifrar(otraVersion) }
    }

    @Test fun perdidaDeClaveNoEjecutaPreparacionAlLeer() {
        var preparaciones = 0
        val perdido = CifradoMetadatos({ preparaciones++ }, { error("Clave ausente ficticia") })
        assertThrows(IllegalStateException::class.java) { perdido.descifrar(cifrador.cifrar(byteArrayOf(1))) }
        assertEquals(0, preparaciones)
    }
}
