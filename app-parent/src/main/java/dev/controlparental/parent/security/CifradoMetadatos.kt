package dev.controlparental.parent.security

import dev.controlparental.parent.data.CifradorMetadatos
import dev.controlparental.parent.data.ParentStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Clave inyectada desde Keystore. No acepta nonce externo para cifrar.
internal class CifradoMetadatos(
    private val preparar: () -> Unit,
    private val clave: () -> SecretKey,
) : CifradorMetadatos {
    override fun prepararClave() = preparar()
    override fun cifrar(bytes: ByteArray): ByteArray {
        require(bytes.size <= ParentStore.MAX_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, clave())
        cipher.updateAAD(CABECERA)
        check(cipher.iv.size == 12)
        return CABECERA + cipher.iv + cipher.doFinal(bytes)
    }

    override fun descifrar(bytes: ByteArray): ByteArray {
        require(bytes.size in (CABECERA.size + 12 + 16)..(ParentStore.MAX_BYTES + CABECERA.size + 12 + 16))
        require(bytes.copyOfRange(0, CABECERA.size).contentEquals(CABECERA))
        val nonce = bytes.copyOfRange(CABECERA.size, CABECERA.size + 12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, nonce))
        cipher.updateAAD(CABECERA)
        return cipher.doFinal(bytes, CABECERA.size + 12, bytes.size - CABECERA.size - 12)
    }

    companion object { private val CABECERA = "CPSTORE1".toByteArray(Charsets.US_ASCII) }
}
