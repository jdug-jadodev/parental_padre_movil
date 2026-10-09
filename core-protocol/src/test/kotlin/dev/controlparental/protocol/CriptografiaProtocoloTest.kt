package dev.controlparental.protocol

import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class CriptografiaProtocoloTest {
    private val fixture = javaClass.getResourceAsStream("/vectores_crypto.json")!!.use {
        Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject
    }
    private val vectores = fixture.getValue("vectors").jsonArray
    private fun clave(rol: Rol) = ClaveVerificacion.desdeSpki(rol, fixture.getValue("publicKeys").jsonObject.getValue(rol.name).jsonPrimitive.content)
    private fun sobre(vector: JsonElement) = CodecProtocolo.leerFrame(vector.jsonObject.getValue("envelope").toString().toByteArray()) as SobreFirmado
    private fun efimera() = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    private fun firmante(privada: java.security.PrivateKey) = FirmanteMensaje { bytes ->
        Signature.getInstance("SHA256withECDSA").run { initSign(privada); update(bytes); sign() }
    }

    @Test
    fun `verifica los diez vectores y conserva hash de payload no de firma`() {
        for (vector in vectores) {
            val rol = Rol.valueOf(vector.jsonObject.getValue("signerRole").jsonPrimitive.content)
            val sobre = sobre(vector)
            val frame = CodecProtocolo.codificarFrame(sobre)
            val verificado = CriptografiaProtocolo.verificarSobre(frame, clave(rol), sobre.purpose)
            assertEquals(vector.jsonObject.getValue("payloadSha256B64").jsonPrimitive.content, verificado.payloadSha256B64)
            assertArrayEquals(CodecProtocolo.bytesPayload(sobre), verificado.bytesPayload())
            assertArrayEquals(frame, verificado.bytesFrame())
        }
    }

    @Test
    fun `rechaza otra clave bits alterados y cambio de proposito`() {
        for (vector in vectores) {
            val rol = Rol.valueOf(vector.jsonObject.getValue("signerRole").jsonPrimitive.content)
            val sobre = sobre(vector)
            val otra = if (rol == Rol.PARENT) Rol.CHILD else Rol.PARENT
            assertThrows(FirmaInvalida::class.java) { CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(sobre), clave(otra), sobre.purpose) }
            val payload = CodecProtocolo.bytesPayload(sobre).apply { this[lastIndex] = (this[lastIndex].toInt() xor 1).toByte() }
            val cambiado = sobre.copy(payloadB64 = Formatos.codificar(payload))
            assertThrows(FirmaInvalida::class.java) { CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(cambiado), clave(rol), sobre.purpose) }
            val otroProposito = if (sobre.purpose == Proposito.AUTH) Proposito.MESSAGE else Proposito.AUTH
            val otroSobre = sobre.copy(purpose = otroProposito)
            assertThrows(FirmaInvalida::class.java) { CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(otroSobre), clave(rol), otroProposito) }
        }
    }

    @Test
    fun `firma DER alterada se rechaza aunque conserve su formato`() {
        val sobre = sobre(vectores[0])
        val firma = Formatos.base64(sobre.sigB64, 75).apply { this[lastIndex] = (this[lastIndex].toInt() xor 1).toByte() }
        val cambiado = sobre.copy(sigB64 = Formatos.codificar(firma))
        assertThrows(FirmaInvalida::class.java) { CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(cambiado), clave(Rol.PARENT), sobre.purpose) }
    }

    @Test
    fun `firmante efimero crea sobre interoperable sin exportar privadas a archivos`() {
        val par = efimera()
        val publica = ClaveVerificacion.desdeSpki(Rol.PARENT, Formatos.codificar(par.public.encoded))
        val original = sobre(vectores[2])
        val mensaje = CodecProtocolo.leerPayloadSinVerificar(CodecProtocolo.bytesPayload(original), Proposito.MESSAGE)
        val firmado = CriptografiaProtocolo.firmarSobre(mensaje, Proposito.MESSAGE, publica, firmante(par.private))
        val resultado = CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(firmado), publica, Proposito.MESSAGE)
        assertEquals(mensaje, resultado.mensaje)
        assertEquals(fixture.getValue("vectors").jsonArray[2].jsonObject.getValue("payloadSha256B64").jsonPrimitive.content, resultado.payloadSha256B64)
    }

    @Test
    fun `firma valida no habilita JSON ambiguo ni rol impostor`() {
        val par = efimera()
        val publica = ClaveVerificacion.desdeSpki(Rol.PARENT, Formatos.codificar(par.public.encoded))
        val original = CodecProtocolo.bytesPayload(sobre(vectores[2])).toString(Charsets.UTF_8)
        for (texto in listOf(" $original", original.replace("\"v\":1", "\"v\":1,\"v\":1"), original.replace("\"v\":1", "\"v\":1e0"), original.replace("PARENT", "CHILD"))) {
            val payload = texto.toByteArray()
            val firma = firmante(par.private).firmar("CPv1/MESSAGE\n".toByteArray() + payload)
            val frame = CodecProtocolo.codificarFrame(SobreFirmado(Proposito.MESSAGE, Formatos.codificar(payload), Formatos.codificar(firma)))
            assertThrows(MensajeInvalido::class.java) { CriptografiaProtocolo.verificarSobre(frame, publica, Proposito.MESSAGE) }
        }
    }

    @Test
    fun `fallo del firmante o rol errado no genera autoridad alternativa`() {
        val par = efimera()
        val publica = ClaveVerificacion.desdeSpki(Rol.PARENT, Formatos.codificar(par.public.encoded))
        val original = sobre(vectores[2])
        val mensaje = CodecProtocolo.leerPayloadSinVerificar(CodecProtocolo.bytesPayload(original), Proposito.MESSAGE)
        assertThrows(IllegalStateException::class.java) {
            CriptografiaProtocolo.firmarSobre(mensaje, Proposito.MESSAGE, publica) { throw IllegalStateException("Cancelación ficticia") }
        }
        val otra = efimera()
        assertThrows(FirmaInvalida::class.java) { CriptografiaProtocolo.firmarSobre(mensaje, Proposito.MESSAGE, publica, firmante(otra.private)) }
        val rolErrado = ClaveVerificacion.desdeSpki(Rol.CHILD, Formatos.codificar(par.public.encoded))
        assertThrows(MensajeInvalido::class.java) { CriptografiaProtocolo.firmarSobre(mensaje, Proposito.MESSAGE, rolErrado, firmante(par.private)) }
    }

    @Test
    fun `resultado preserva bytes originales ante cambios del llamador`() {
        val sobre = sobre(vectores[2])
        val bytes = CodecProtocolo.codificarFrame(sobre)
        val resultado = CriptografiaProtocolo.verificarSobre(bytes, clave(Rol.PARENT), sobre.purpose)
        bytes.fill(0)
        resultado.bytesFrame().fill(0)
        resultado.bytesPayload().fill(0)
        assertArrayEquals(CodecProtocolo.codificarFrame(sobre), resultado.bytesFrame())
        assertArrayEquals(CodecProtocolo.bytesPayload(sobre), resultado.bytesPayload())
        assertThrows(MensajeInvalido::class.java) { CriptografiaProtocolo.verificarSobre(resultado.bytesFrame(), clave(Rol.PARENT), Proposito.OFFLINE) }
    }

    @Test
    fun `AUTH firmado correctamente no puede reclamar otro rol`() {
        val par = efimera()
        val publica = ClaveVerificacion.desdeSpki(Rol.PARENT, Formatos.codificar(par.public.encoded))
        val payload = CodecProtocolo.bytesPayload(sobre(vectores[1]))
        val firma = firmante(par.private).firmar("CPv1/AUTH\n".toByteArray() + payload)
        val frame = CodecProtocolo.codificarFrame(SobreFirmado(Proposito.AUTH, Formatos.codificar(payload), Formatos.codificar(firma)))
        assertThrows(MensajeInvalido::class.java) { CriptografiaProtocolo.verificarSobre(frame, publica, Proposito.AUTH) }
    }

    @Test
    fun `contenedor no firmado o algoritmo elegible nunca crea prueba de autoridad`() {
        val frame = """{"kind":"PEER_STATUS","online":true,"peerRole":"CHILD","v":1}"""
        assertThrows(MensajeInvalido::class.java) { CriptografiaProtocolo.verificarSobre(frame.toByteArray(), clave(Rol.CHILD), Proposito.MESSAGE) }
        val sobre = vectores[0].jsonObject.getValue("envelope").jsonObject
        for ((campo, valor) in listOf("alg" to JsonPrimitive("none"), "publicKeyB64" to fixture.getValue("publicKeys").jsonObject.getValue("PARENT"))) {
            val cambiado = JsonObject(sobre + (campo to valor))
            assertThrows(MensajeInvalido::class.java) { CriptografiaProtocolo.verificarSobre(cambiado.toString().toByteArray(), clave(Rol.PARENT), Proposito.AUTH) }
        }
    }
}
