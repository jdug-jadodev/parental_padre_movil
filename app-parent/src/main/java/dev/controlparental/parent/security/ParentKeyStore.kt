package dev.controlparental.parent.security

import android.app.KeyguardManager
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import dev.controlparental.protocol.ClaveVerificacion
import dev.controlparental.protocol.Rol
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64

internal class ParentKeyStore(context: Context, private val alias: String = ALIAS_FIRMA) : AlmacenClavePadre {
    private val keyguard = context.getSystemService(KeyguardManager::class.java)
    private fun almacen() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private fun exigirBloqueoSeguro() {
        check(keyguard.isDeviceSecure) { "Configura PIN, patrón o contraseña del sistema antes de crear autoridad" }
    }

    @Synchronized override fun crearSiFalta(): String {
        exigirBloqueoSeguro()
        if (!almacen().containsAlias(alias)) {
            val generador = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
            generador.initialize(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(60, TIPOS_AUTENTICACION)
                .build())
            generador.generateKeyPair()
        }
        // Si el alias existe pero está dañado/incompatible, no se reemplaza.
        return publicaSpki()
    }

    private fun privadaValidada(): PrivateKey {
        exigirBloqueoSeguro()
        val privada = almacen().getKey(alias, null) as? PrivateKey
            ?: throw IllegalStateException("Identidad ausente o inaccesible; requiere configuración/recuperación presencial")
        val info = KeyFactory.getInstance(privada.algorithm, "AndroidKeyStore")
            .getKeySpec(privada, KeyInfo::class.java)
        check(privada.encoded == null && privada.algorithm == "EC" && info.keySize == 256 &&
            info.isUserAuthenticationRequired && info.userAuthenticationValidityDurationSeconds == 60 &&
            info.userAuthenticationType == TIPOS_AUTENTICACION &&
            info.purposes == KeyProperties.PURPOSE_SIGN &&
            info.digests.toSet() == setOf(KeyProperties.DIGEST_SHA256)) {
            "Identidad incompatible; no se sustituirá automáticamente"
        }
        return privada
    }

    @Synchronized override fun publicaSpki(): String {
        privadaValidada()
        val publica = almacen().getCertificate(alias)?.publicKey
            ?: throw IllegalStateException("No se pudo leer la pública de la identidad")
        val spki = Base64.getUrlEncoder().withoutPadding().encodeToString(publica.encoded)
        ClaveVerificacion.desdeSpki(Rol.PARENT, spki)
        return spki
    }

    @Synchronized override fun firmar(bytes: ByteArray): ByteArray {
        val privada = privadaValidada()
        return Signature.getInstance("SHA256withECDSA").run {
            initSign(privada)
            update(bytes)
            sign()
        }
    }

    companion object {
        const val ALIAS_FIRMA = "cp.parent.sign.v1"
        // Reservado para almacenamiento posterior; nunca reutilizar la clave EC para cifrado.
        const val ALIAS_METADATOS = "cp.parent.metadata.v1"
        const val TIPOS_AUTENTICACION = KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
    }
}
