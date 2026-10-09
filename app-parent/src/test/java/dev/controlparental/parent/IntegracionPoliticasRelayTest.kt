package dev.controlparental.parent

import dev.controlparental.parent.data.*
import dev.controlparental.parent.network.*
import dev.controlparental.parent.policy.*
import dev.controlparental.parent.security.CifradoMetadatos
import dev.controlparental.protocol.*
import java.io.File
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.crypto.KeyGenerator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.Assert.*
import org.junit.Test

// Opt-in -PrutaRelayIntegracion. Solo JVM/loopback y claves efímeras; nunca APK operativo.
class IntegracionPoliticasRelayTest {
    @Test fun politicasDuraderasGuardasReintentosYReconciliacionContraRelayReal() = runBlocking {
        val raiz = File(requireNotNull(System.getProperty("raizPadre")))
        val proceso = ProcessBuilder("node", File(raiz, "pruebas/relay-politicas.mjs").path,
            requireNotNull(System.getProperty("rutaRelayIntegracion")), raiz.path).start()
        val lineas = LinkedBlockingQueue<String>()
        val lector = launch(Dispatchers.IO) { proceso.inputStream.bufferedReader().useLines { it.forEach(lineas::offer) } }
        // El harness no imprime payloads, claves ni bootstrap en errores.
        val errores = launch(Dispatchers.IO) { proceso.errorStream.bufferedReader().useLines { it.forEach { linea -> System.err.println(linea) } } }
        val salida = proceso.outputStream.bufferedWriter()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val stores = mutableListOf<ParentStore>()
        val padre = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        val publica = Base64.getUrlEncoder().withoutPadding().encodeToString(padre.public.encoded)
        val cert = HeldCertificate.Builder().commonName("127.0.0.1").addSubjectAlternativeName("127.0.0.1").build()
        val confianza = HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
        val transporte = TransporteOkHttp(TransporteOkHttp.clienteSeguro().newBuilder()
            .sslSocketFactory(confianza.sslSocketFactory(), confianza.trustManager).build())
        fun firmar(mensaje: Mensaje, purpose: Proposito) = CriptografiaProtocolo.firmarSobre(mensaje, purpose,
            ClaveVerificacion.desdeSpki(Rol.PARENT, publica)) { bytes ->
            Signature.getInstance("SHA256withECDSA").run { initSign(padre.private); update(bytes); sign() }
        }
        val conexion = RelayConnection(transporte, { System.nanoTime() / 1_000_000 }, {}, { firmar(it, Proposito.AUTH) })
        try {
            salida.write(buildJsonObject {
                put("publicaPadre", publica); put("certificado", cert.certificatePem()); put("claveTls", cert.privateKeyPkcs8Pem())
            }.toString()); salida.newLine(); salida.flush()
            val bootstrap = lineas.poll(15, TimeUnit.SECONDS) ?: error("Relay de prueba no arrancó")
            val datos = Json.parseToJsonElement(bootstrap).jsonObject
            fun campo(nombre: String) = datos.getValue(nombre).jsonPrimitive.content
            val config = ConfiguracionRelay(campo("pairId"), campo("origen"), publica)
            suspend fun conectar() {
                conexion.primerPlano(); conexion.conectar(config)
                withTimeout(10_000) { conexion.estado.first { it.canalSano } }
                assertNotNull(conexion.sesionPoliticas())
            }
            conectar()
            var tiempo = 0L
            var redValidada = true // Señal ficticia solo JVM, no NetworkCallback Android.
            var autoridad = true // Sesión ficticia, no prompt/Keystore Android.
            val canalReal = CanalPoliticasRelay(conexion) { redValidada }
            val aes = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
            val cifrador = CifradoMetadatos({}, { aes })
            class Archivo : ArchivoMetadatos {
                var bytes: ByteArray? = null
                override fun existe() = bytes != null
                override fun leer() = bytes!!.copyOf()
                override fun escribir(bytes: ByteArray) { this.bytes = bytes.copyOf() }
            }
            suspend fun almacen(archivo: Archivo, vinculo: VinculoPersistido): ParentStore {
                val store = ParentStore(archivo, cifrador, scope).also(stores::add)
                if (!store.existe()) store.inicializar(MetadatosPadre(parentPublicKeyB64 = publica,
                    pairId = config.pairId, childPublicKeyB64 = campo("publicaHijo"), relayOrigin = config.relayOrigin, vinculo = vinculo))
                return store
            }
            var comprobacionesPersistencia = 0
            fun control(store: ParentStore): ControlPoliticas {
                val canal = object : CanalPoliticas {
                    override fun actual() = canalReal.actual()
                    override fun enviar(ticket: Long, frame: ByteArray): Boolean {
                        val mensaje = CodecProtocolo.leerPayloadSinVerificar(
                            CodecProtocolo.bytesPayload(CodecProtocolo.leerFrame(frame) as SobreFirmado), Proposito.MESSAGE)
                        if (mensaje is OrdenPolitica) {
                            // Callback síncrono: lee el almacén real antes de tocar el socket.
                            val pendiente = runBlocking { store.leer() }.pendiente!!
                            assertEquals(EtapaIntencion.ENVIADA_SIN_CONFIRMAR, pendiente.etapa)
                            assertArrayEquals(frame, Base64.getUrlDecoder().decode(pendiente.frameB64))
                            comprobacionesPersistencia++
                        }
                        return canalReal.enviar(ticket, frame)
                    }
                }
                return ControlPoliticas(store, canal, { tiempo }, { check(autoridad) }, { firmar(it, Proposito.MESSAGE) })
            }
            suspend fun recibir(c: ControlPoliticas) {
                val trama = withTimeout(10_000) { conexion.mensajesHijo.first() }
                c.recibir(trama.ticket, trama.bytes())
            }
            suspend fun sync(c: ControlPoliticas) { c.sincronizar(); recibir(c) }
            val archivo = Archivo()
            val store = almacen(archivo, VinculoPersistido.VINCULADO)
            val c = control(store)
            redValidada = false
            assertTrue(runCatching { c.sincronizar() }.isFailure)
            redValidada = true
            autoridad = false
            assertTrue(runCatching { c.sincronizar() }.isFailure)
            autoridad = true
            sync(c) // Primer STATE: hijo no apto.
            assertTrue(runCatching { c.prepararIntencion(Accion.ALLOW, null) }.isFailure)
            assertEquals("0", store.leer().contadorLocal)
            val preparadaStore = almacen(Archivo(), VinculoPersistido.PREPARADO)
            val preparada = control(preparadaStore)
            sync(preparada) // Segundo STATE: apto, pero vínculo solo preparado.
            assertTrue(runCatching { preparada.prepararIntencion(Accion.ALLOW, null) }.isFailure)
            assertEquals(VinculoPersistido.PREPARADO, preparadaStore.leer().vinculo)
            assertNull(preparadaStore.leer().pendiente)
            sync(c) // Tercer STATE: apto para el almacén ficticio VINCULADO.
            val confirmacion = c.prepararIntencion(Accion.LOCK_FOR, 60)
            assertNull(store.leer().pendiente)
            autoridad = false
            assertTrue(runCatching { c.enviarConfirmada(confirmacion) }.isFailure)
            assertNull(store.leer().pendiente)
            autoridad = true
            c.enviarConfirmada(confirmacion)
            assertEquals("1", store.leer().contadorLocal)
            val pendienteOriginal = store.leer().pendiente!!
            tiempo = 5_000; c.avanzarReloj()
            tiempo = 10_000; c.avanzarReloj()
            assertTrue(runCatching { recibir(c) }.isFailure) // ACK firmado pero hash ajeno.
            assertEquals(pendienteOriginal, store.leer().pendiente)
            recibir(c) // ACK pendiente correlacionado.
            assertEquals(FasePolitica.PENDIENTE_APLICACION, c.resultado.value.fase)
            assertNotNull(store.leer().pendiente)
            tiempo = 15_000; c.avanzarReloj()
            assertEquals(3, comprobacionesPersistencia)
            assertTrue(runCatching { c.enviarConfirmada(confirmacion) }.isFailure)
            conexion.segundoPlano(); assertNull(canalReal.actual())
            store.cerrar(); stores.remove(store)
            val reabierto = almacen(archivo, VinculoPersistido.VINCULADO)
            assertNotNull(reabierto.leer().pendiente)
            val nuevo = control(reabierto)
            conectar()
            nuevo.avanzarReloj() // Nunca reproduce el comando duradero anterior.
            sync(nuevo) // Nueva sesión del hijo, mismo registro id/seq/hash, fallo simulado.
            assertEquals(FasePolitica.FALLO_APLICACION, nuevo.resultado.value.fase)
            assertNull(reabierto.leer().pendiente); assertEquals("1", reabierto.leer().contadorLocal)
            assertEquals(3, comprobacionesPersistencia)
            conexion.desconectar()
            salida.write("FIN\n"); salida.flush()
            assertEquals("OK_POLITICAS", lineas.poll(10, TimeUnit.SECONDS))
            assertTrue(proceso.waitFor(10, TimeUnit.SECONDS)); assertEquals(0, proceso.exitValue())
        } finally {
            conexion.desconectar(); transporte.liberar()
            stores.forEach { it.cerrar() }; scope.cancel()
            salida.close(); proceso.destroyForcibly(); proceso.waitFor(5, TimeUnit.SECONDS)
            lector.cancelAndJoin(); errores.cancelAndJoin()
        }
    }
}
