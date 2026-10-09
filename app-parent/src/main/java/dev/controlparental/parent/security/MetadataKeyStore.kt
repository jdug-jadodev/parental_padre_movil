package dev.controlparental.parent.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

internal class MetadataKeyStore(private val alias: String = ParentKeyStore.ALIAS_METADATOS) {
    init { require(alias != ParentKeyStore.ALIAS_FIRMA) }
    private fun almacen() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    @Synchronized fun preparar() {
        if (!almacen().containsAlias(alias)) {
            val generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            generador.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
            generador.generateKey()
        }
        cargar()
    }

    // Leer/descifrar nunca genera una clave ausente tras reinstalación o pérdida.
    @Synchronized fun cargar(): SecretKey {
        val clave = almacen().getKey(alias, null) as? SecretKey
            ?: throw IllegalStateException("Clave de metadatos ausente; requiere recuperación")
        val info = SecretKeyFactory.getInstance(clave.algorithm, "AndroidKeyStore").getKeySpec(clave, KeyInfo::class.java) as KeyInfo
        check(clave.encoded == null && clave.algorithm == "AES" && info.keySize == 256 &&
            info.purposes == (KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT) &&
            info.blockModes.toSet() == setOf(KeyProperties.BLOCK_MODE_GCM) &&
            info.encryptionPaddings.toSet() == setOf(KeyProperties.ENCRYPTION_PADDING_NONE))
        return clave
    }
}
