package dev.controlparental.parent

import androidx.test.platform.app.InstrumentationRegistry
import dev.controlparental.parent.data.*
import dev.controlparental.parent.pairing.PairChild
import dev.controlparental.parent.security.*
import dev.controlparental.protocol.*
import java.io.File
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

// Exclusivamente APK instrumentado. Firma JCA efímera NO acredita autenticación
// ni sustituye Keystore de autoridad en producción. AES/archivo sí son Android reales.
internal class EntornoVinculoAndroid : AutoCloseable {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val id = UUID.randomUUID().toString()
    private val directorio = File(contexto.noBackupFilesDir, "cp.test.pair.$id").apply { check(mkdir()) }
    val ruta = File(directorio, "estado.enc")
    private val alias = "cp.test.pair.metadata.$id"
    private val claves = MetadataKeyStore(alias)
    val cifrador = CifradoMetadatos(claves::preparar, claves::cargar)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val abiertos = mutableListOf<ParentStore>()
    private val par = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    val publica = Base64.getUrlEncoder().withoutPadding().encodeToString(par.public.encoded)
    private val publicaHijo = InstrumentationRegistry.getInstrumentation().context.assets.open("vectores_crypto.json").use {
        Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject.getValue("publicKeys")
            .jsonObject.getValue("CHILD").jsonPrimitive.content
    }
    val oferta = buildJsonObject {
        put("v", 1); put("kind", "CHILD_OFFER"); put("childPublicKeyB64", publicaHijo)
        put("enrollId", UUID.randomUUID().toString())
        put("enrollNonce", Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32) { 2 }))
    }.toString()
    fun abrir(): ParentStore = ParentStore(ArchivoAtomicoMetadatos(ruta), cifrador, scope).also { abiertos.add(it) }
    fun flujo(store: ParentStore, falloFirma: Boolean = false) = PairChild(store, { publica }, { aceptacion ->
        check(!falloFirma) { "Firma bloqueada ficticia" }
        CriptografiaProtocolo.firmarSobre(aceptacion, Proposito.PAIR,
            ClaveVerificacion.desdeSpki(Rol.PARENT, publica)) { bytes ->
            Signature.getInstance("SHA256withECDSA").run { initSign(par.private); update(bytes); sign() }
        }
    })
    override fun close() {
        try { runBlocking { abiertos.forEach { it.cerrar() } } }
        finally {
            scope.cancel()
            KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
            for (sufijo in listOf("", ".bak", ".new")) {
                val archivo = File(ruta.path + sufijo)
                if (archivo.exists()) check(archivo.delete())
            }
            check(directorio.delete())
        }
    }
}
