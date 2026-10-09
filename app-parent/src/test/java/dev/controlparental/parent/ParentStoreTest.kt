package dev.controlparental.parent

import dev.controlparental.parent.data.*
import dev.controlparental.parent.security.CifradoMetadatos
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import javax.crypto.KeyGenerator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ParentStoreTest {
    // Dobles exclusivamente JVM: no sustituyen AtomicFile ni Keystore físicos.
    private class ArchivoFalso : ArchivoMetadatos {
        var bytes: ByteArray? = null
        var fallo = 0
        override fun existe() = bytes != null
        override fun leer() = bytes!!.copyOf()
        override fun escribir(bytes: ByteArray) {
            if (fallo == 1) error("Fallo antes de commit ficticio")
            this.bytes = bytes.copyOf()
            if (fallo == 2) error("Fallo después de commit ficticio")
        }
    }

    private class Entorno {
        val archivo = ArchivoFalso()
        val par = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        val publica = Base64.getUrlEncoder().withoutPadding().encodeToString(par.public.encoded)
        val clave = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        var preparaciones = 0
        val cifrador = CifradoMetadatos({ preparaciones++ }, { clave })
        fun datos() = MetadatosPadre(parentPublicKeyB64 = publica,
            pairId = "00000000-0000-4000-8000-000000000002", childPublicKeyB64 = publica,
            relayOrigin = "https://prueba.invalid", vinculo = VinculoPersistido.VINCULADO)
    }

    @Test fun reservaConsumeContadorYCancelaSinReutilizarSecuencia() = runTest {
        val entorno = Entorno()
        val store = ParentStore(entorno.archivo, entorno.cifrador, backgroundScope)
        store.inicializar(entorno.datos())
        val primera = store.reservarIntencion(Accion.LOCK, null, "9")
        assertEquals("10", primera.seq)
        store.cancelarIntencion(primera.commandId)
        assertEquals("10", store.leer().contadorLocal)
        assertNull(store.leer().pendiente)
        val segunda = store.reservarIntencion(Accion.ALLOW_FOR, 60, "1")
        assertEquals("11", segunda.seq)
        assertNotEquals(primera.commandId, segunda.commandId)
        assertEquals(1, entorno.preparaciones)
        store.cerrar()
    }
}
