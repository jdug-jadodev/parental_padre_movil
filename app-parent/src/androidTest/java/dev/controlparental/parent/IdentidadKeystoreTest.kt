package dev.controlparental.parent

import android.app.KeyguardManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import androidx.test.platform.app.InstrumentationRegistry
import dev.controlparental.parent.security.*
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.spec.ECGenParameterSpec
import java.util.UUID
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

// Nunca cambia credencial/biometría ni toca el alias de autoridad real.
class IdentidadKeystoreTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private fun almacen() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    @Test fun identidadProtegidaConservaPublicaAlReabrirAlmacen() {
        assumeTrue("Requiere credencial segura configurada por el propietario",
            contexto.getSystemService(KeyguardManager::class.java).isDeviceSecure)
        val alias = "cp.test.identity.${UUID.randomUUID()}"
        try {
            val original = ParentKeyStore(contexto, alias).crearSiFalta()
            val reabierto = ParentKeyStore(contexto, alias)
            assertEquals(original, reabierto.crearSiFalta())
            assertEquals(original, reabierto.publicaSpki())
            val privada = almacen().getKey(alias, null) as PrivateKey
            assertNull(privada.encoded)
            val info = KeyFactory.getInstance("EC", "AndroidKeyStore").getKeySpec(privada, KeyInfo::class.java)
            assertTrue(info.isUserAuthenticationRequired)
            assertEquals(60, info.userAuthenticationValidityDurationSeconds)
            assertEquals(ParentKeyStore.TIPOS_AUTENTICACION, info.userAuthenticationType)
            assertEquals(256, info.keySize)
            assertNotEquals(ParentKeyStore.ALIAS_FIRMA, ParentKeyStore.ALIAS_METADATOS)
        } finally { almacen().deleteEntry(alias) }
    }

    @Test fun aliasIncompatibleNoEsSustituidoPorClaveNueva() {
        assumeTrue(contexto.getSystemService(KeyguardManager::class.java).isDeviceSecure)
        val alias = "cp.test.incompatible.${UUID.randomUUID()}"
        try {
            val generador = KeyPairGenerator.getInstance("EC", "AndroidKeyStore")
            generador.initialize(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256).build())
            val original = generador.generateKeyPair().public.encoded
            assertThrows(IllegalStateException::class.java) { ParentKeyStore(contexto, alias).crearSiFalta() }
            assertArrayEquals(original, almacen().getCertificate(alias).publicKey.encoded)
        } finally { almacen().deleteEntry(alias) }
    }
}
