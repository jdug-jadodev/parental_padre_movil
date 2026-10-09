package dev.controlparental.protocol

import java.io.File
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

// Separado del test JVM puro: requiere npm ci en relay y Node 24 disponibles.
class InteroperabilidadTest {
    private val raiz = File(System.getProperty("raiz.proyecto"))

    @Test(timeout = 90000)
    fun `firmas nuevas cruzan JVM y Node en ambos sentidos para los diez vectores`() {
        val fixture = javaClass.getResourceAsStream("/vectores_crypto.json")!!.use {
            Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject
        }
        for (vector in fixture.getValue("vectors").jsonArray) {
            val datos = vector.jsonObject
            val rol = Rol.valueOf(datos.getValue("signerRole").jsonPrimitive.content)
            val original = CodecProtocolo.leerFrame(datos.getValue("envelope").toString().toByteArray()) as SobreFirmado
            val payload = CodecProtocolo.bytesPayload(original)
            val mensaje = CodecProtocolo.leerPayloadSinVerificar(payload, original.purpose)
            val par = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
            val publicaB64 = Formatos.codificar(par.public.encoded)
            val publica = ClaveVerificacion.desdeSpki(rol, publicaB64)
            val firmado = CriptografiaProtocolo.firmarSobre(mensaje, original.purpose, publica) { bytes ->
                Signature.getInstance("SHA256withECDSA").run { initSign(par.private); update(bytes); sign() }
            }
            val respuestaNode = invocarNode(buildJsonObject {
                put("accion", "verificar"); put("rol", rol.name); put("proposito", original.purpose.name)
                put("publicaB64", publicaB64)
                put("sobre", Json.parseToJsonElement(CodecProtocolo.codificarFrame(firmado).toString(Charsets.UTF_8)))
            })
            assertTrue(respuestaNode.getValue("ok").jsonPrimitive.boolean)
            assertEquals(datos.getValue("payloadSha256B64"), respuestaNode.getValue("hash"))

            val firmaNode = invocarNode(buildJsonObject {
                put("accion", "firmar"); put("rol", rol.name); put("proposito", original.purpose.name)
                put("payloadB64", original.payloadB64)
            })
            val claveNode = ClaveVerificacion.desdeSpki(rol, firmaNode.getValue("publicaB64").jsonPrimitive.content)
            val resultadoJVM = CriptografiaProtocolo.verificarSobre(firmaNode.getValue("sobre").toString().toByteArray(), claveNode, original.purpose)
            assertArrayEquals(payload, resultadoJVM.bytesPayload())
            assertEquals(datos.getValue("payloadSha256B64").jsonPrimitive.content, resultadoJVM.payloadSha256B64)
        }
    }

    private fun invocarNode(peticion: JsonObject): JsonObject {
        val proceso = ProcessBuilder("node", "--import", "tsx", "test/soporte/interoperabilidad.mjs")
            .directory(File(raiz, "relay")).redirectErrorStream(true).start()
        try {
            proceso.outputStream.use { it.write(peticion.toString().toByteArray(Charsets.UTF_8)) }
            assertTrue("Node no respondió dentro del límite", proceso.waitFor(15, TimeUnit.SECONDS))
            val salida = proceso.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            assertEquals("La herramienta Node falló: $salida", 0, proceso.exitValue())
            return Json.parseToJsonElement(salida).jsonObject
        } finally {
            if (proceso.isAlive) proceso.destroyForcibly()
        }
    }
}
