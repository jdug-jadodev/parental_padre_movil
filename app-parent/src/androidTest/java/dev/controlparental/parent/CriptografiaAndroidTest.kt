package dev.controlparental.parent

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.test.platform.app.InstrumentationRegistry
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import java.util.UUID
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

// Claves/identidades ficticias; no acredita autenticación del adulto ni hardware específico.
class CriptografiaAndroidTest {
    private fun fixture(): JsonObject = InstrumentationRegistry.getInstrumentation().context.assets
        .open("vectores_crypto.json").use { Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject }
    private fun base64(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    @Test
    fun androidVerificaVectoresYRechazaFirmasAlteradas() {
        val fixture = fixture()
        for (vector in fixture.getValue("vectors").jsonArray) {
            val datos = vector.jsonObject
            val rol = Rol.valueOf(datos.getValue("signerRole").jsonPrimitive.content)
            val clave = ClaveVerificacion.desdeSpki(rol, fixture.getValue("publicKeys").jsonObject.getValue(rol.name).jsonPrimitive.content)
            val sobre = CodecProtocolo.leerFrame(datos.getValue("envelope").toString().toByteArray()) as SobreFirmado
            val resultado = CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(sobre), clave, sobre.purpose)
            assertEquals(datos.getValue("payloadSha256B64").jsonPrimitive.content, resultado.payloadSha256B64)
            val firma = Base64.getUrlDecoder().decode(sobre.sigB64).apply { this[lastIndex] = (this[lastIndex].toInt() xor 1).toByte() }
            assertThrows(FirmaInvalida::class.java) {
                CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(sobre.copy(sigB64 = base64(firma))), clave, sobre.purpose)
            }
        }
    }

    @Test
    fun keystoreFirmaConPrivadaTemporalNoExportable() {
        val alias = "cp.test.crypto.${UUID.randomUUID()}"
        val almacen = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        try {
            // Únicamente prueba criptográfica: la autenticación de autoridad se implementa en P01.
            val generador = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
            generador.initialize(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256).build())
            val par = generador.generateKeyPair()
            assertNull(par.private.encoded)
            val publica = ClaveVerificacion.desdeSpki(Rol.PARENT, base64(par.public.encoded))
            val mensaje = PruebaAutenticacion(
                "00000000-0000-4000-8000-000000000002", Rol.PARENT,
                "00000000-0000-4000-8000-000000000010", "A".repeat(43),
            )
            val sobre = CriptografiaProtocolo.firmarSobre(mensaje, Proposito.AUTH, publica) { bytes ->
                Signature.getInstance("SHA256withECDSA").run { initSign(par.private); update(bytes); sign() }
            }
            val resultado = CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(sobre), publica, Proposito.AUTH)
            assertEquals(mensaje, resultado.mensaje)
        } finally {
            // Nunca se elimina otro alias ni la futura identidad de producción.
            almacen.deleteEntry(alias)
        }
    }
}
