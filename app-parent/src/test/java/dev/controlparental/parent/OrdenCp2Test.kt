package dev.controlparental.parent

import dev.controlparental.parent.pairing.*
import dev.controlparental.parent.security.*
import dev.controlparental.protocol.*
import dev.controlparental.protocol.cp2.*
import java.security.*
import java.security.spec.ECGenParameterSpec
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

// Todos los snapshots/STATE/claves de esta clase son ficticios y exclusivos de JVM test.
class OrdenCp2Test {
    private val id = "00000000-0000-4000-8000-000000000001"
    private val otro = "00000000-0000-4000-8000-000000000002"
    private val nonce = "A".repeat(43)
    private val keys = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    private val publica = VinculacionCp2.b64(keys.public.encoded)
    private var ahora = 100L
    private val sesion = SesionAutoridad { ahora }
    private var firmas = 0
    private var alFirmar: () -> Unit = {}
    private var publicaActual = publica
    private val autoridad = AutoridadPadre(object : AlmacenClavePadre {
        override fun crearSiFalta(): String = error("No crear identidad")
        override fun publicaSpki() = publicaActual
        override fun firmar(bytes: ByteArray): ByteArray { firmas++; alFirmar(); return firma(bytes) }
    }, sesion)
    private val original = SnapshotMessageCp2(id, publica, "fake-solo-test", true)
    private var snapshot: SnapshotMessageCp2? = original
    private val estadoOriginal = ContextoStateMessageCp2(id, nonce, AccionCp2.ALLOW_FOR, otro, id, "0", nonce, "0")
    private var estado: ContextoStateMessageCp2? = estadoOriginal
    private val fuente = object : FuentePreparacionMessageCp2 {
        override fun snapshotConfirmado() = snapshot
        override fun estadoVerificado() = estado
    }
    private fun caso() = PrepararMensajeCp2(fuente, autoridad) { ahora }
    private fun autenticar() { sesion.primerPlano(); assertTrue(sesion.completar(sesion.solicitar())) }
    private fun firma(b: ByteArray) = Signature.getInstance("SHA256withECDSA").run { initSign(keys.private); update(b); sign() }
    private fun policy() = SetPolicyCp2(id, otro, id, otro, "1", "0", nonce, AccionCp2.ALLOW_FOR, 60)
    private fun sync() = SyncRequestCp2(id, nonce, null)
    private fun rechaza(f: () -> Unit) { assertThrows(Exception::class.java, f) }
    private fun obj(b: ByteArray) = Json.parseToJsonElement(b.toString(Charsets.UTF_8)).jsonObject
    private fun canonicoTest(o: JsonObject) = JsonObject(o.toSortedMap()).toString()
    private fun cambiar(b: ByteArray, k: String, v: JsonElement) = canonicoTest(JsonObject(obj(b) + (k to v))).toByteArray()

    @Test fun fuenteO01SuministradaCamposRequiredConstEnumsYDuracion() {
        val raiz = java.nio.file.Path.of(requireNotNull(System.getProperty("schemaCp2"))).parent
        val schema = obj(java.nio.file.Files.readAllBytes(raiz.resolve("ordenes.schema.json")))
        val defs = schema.getValue("\$defs").jsonObject
        val sobre = MensajesPadreCp2.firmar(MensajesPadreCp2.payload(policy()), publica, ::firma).frame()
        for ((nombre, bytes) in listOf("sync" to MensajesPadreCp2.payload(sync()),
            "orden" to MensajesPadreCp2.payload(policy()), "sobreMessage" to sobre)) {
            val d = defs.getValue(nombre).jsonObject; val propiedades = d.getValue("properties").jsonObject
            assertEquals(obj(bytes).keys, propiedades.keys)
            assertEquals(obj(bytes).keys, d.getValue("required").jsonArray.map { it.jsonPrimitive.content }.toSet())
            assertEquals(when (nombre) { "sync" -> 6; "orden" -> 12; else -> 5 }, propiedades.size)
            assertEquals(JsonPrimitive(false), d["additionalProperties"])
            propiedades.forEach { (k, v) -> v.jsonObject["const"]?.let { assertEquals(it, obj(bytes)[k]) } }
        }
        val acciones = AccionCp2.entries.map { JsonPrimitive(it.name) }
        assertEquals(JsonArray(listOf(JsonNull) + acciones), defs.getValue("sync").jsonObject.getValue("properties").jsonObject.getValue("requestedAction").jsonObject["enum"])
        assertEquals(JsonArray(acciones), defs.getValue("orden").jsonObject.getValue("properties").jsonObject.getValue("action").jsonObject["enum"])
        val condicion = defs.getValue("orden").jsonObject.getValue("allOf").jsonArray.single().jsonObject
        val duracion = condicion.getValue("then").jsonObject.getValue("properties").jsonObject.getValue("durationSec").jsonObject
        assertEquals(JsonPrimitive(60), duracion["minimum"]); assertEquals(JsonPrimitive(86400), duracion["maximum"])
        assertEquals(JsonPrimitive(60), duracion["multipleOf"])
        assertEquals(JsonPrimitive("null"), condicion.getValue("else").jsonObject.getValue("properties").jsonObject.getValue("durationSec").jsonObject["type"])
    }

    @Test fun camposExactosDoceSeisYCincoNullObligatorioYCopias() {
        val p = MensajesPadreCp2.payload(policy()); val s = MensajesPadreCp2.payload(sync())
        assertEquals(setOf("v", "kind", "sender", "pairId", "bootId", "childSessionId", "commandId", "seq", "expectedRevision", "commandNonce", "action", "durationSec"), obj(p).keys)
        assertEquals(12, obj(p).size)
        assertEquals(setOf("v", "kind", "pairId", "sender", "syncNonce", "requestedAction"), obj(s).keys)
        assertEquals(6, obj(s).size); assertEquals(JsonNull, obj(s)["requestedAction"])
        for (b in listOf(p, s)) {
            assertArrayEquals(b, MensajesPadreCp2.payload(MensajesPadreCp2.leerPayload(b)))
            obj(b).keys.forEach { k -> rechaza { MensajesPadreCp2.leerPayload(canonicoTest(JsonObject(obj(b) - k)).toByteArray()) } }
            rechaza { MensajesPadreCp2.leerPayload(cambiar(b, "extra", JsonNull)) }
        }
        val d = MensajesPadreCp2.firmar(p, publica) { assertArrayEquals("CPv2/MESSAGE\n".toByteArray() + p, it); firma(it) }
        assertEquals(5, obj(d.frame()).size)
        assertEquals(setOf("v", "kind", "purpose", "payloadB64", "sigB64"), obj(d.frame()).keys)
        val f = d.frame(); f.fill(0); d.payload().fill(0); p.fill(0)
        assertEquals(policy(), MensajesPadreCp2.verificar(d.frame(), publica).mensaje)
        assertEquals(VinculacionCp2.b64(MessageDigest.getInstance("SHA-256").digest(d.payload())), d.payloadSha256B64)
    }
    @Test fun contadoresExtremosOverflowTiposYNoCerosIniciales() {
        val p = MensajesPadreCp2.payload(policy())
        for (k in listOf("seq", "expectedRevision")) {
            for (v in listOf("-1", "+1", "01", "00", "1.0", "1e2", "9223372036854775808", "9".repeat(200), ""))
                rechaza { MensajesPadreCp2.leerPayload(cambiar(p, k, JsonPrimitive(v))) }
            for (v in listOf(JsonPrimitive(1), JsonNull, JsonPrimitive(true))) rechaza { MensajesPadreCp2.leerPayload(cambiar(p, k, v)) }
            assertNotNull(MensajesPadreCp2.leerPayload(cambiar(p, k, JsonPrimitive(Long.MAX_VALUE.toString()))))
        }
        rechaza { MensajesPadreCp2.payload(policy().copy(seq = "0")) }
        assertNotNull(MensajesPadreCp2.payload(policy().copy(expectedRevision = "0")))
    }
    @Test fun duracionMinutosEnterosYAccionesCerradas() {
        for (a in listOf(AccionCp2.LOCK, AccionCp2.ALLOW)) {
            assertNotNull(MensajesPadreCp2.payload(policy().copy(action = a, durationSec = null)))
            rechaza { MensajesPadreCp2.payload(policy().copy(action = a)) }
        }
        for (d in listOf(null, 0, 1, 59, 61, 86401, Int.MAX_VALUE)) rechaza { MensajesPadreCp2.payload(policy().copy(durationSec = d)) }
        assertNotNull(MensajesPadreCp2.payload(policy().copy(durationSec = 86400)))
        val p = MensajesPadreCp2.payload(policy())
        for (v in listOf(JsonPrimitive("60"), JsonPrimitive(60.0), JsonPrimitive(true), JsonPrimitive(Long.MAX_VALUE)))
            rechaza { MensajesPadreCp2.leerPayload(cambiar(p, "durationSec", v)) }
        for (a in listOf("LOCK_FOR", "WAITING_ALLOW", "OFFLINE")) rechaza { MensajesPadreCp2.leerPayload(cambiar(p, "action", JsonPrimitive(a))) }
        rechaza { MensajesPadreCp2.leerPayload(cambiar(p, "action", JsonNull)) }
        rechaza { MensajesPadreCp2.leerPayload(cambiar(p, "deferredAllowSec", JsonNull)) }
        for (minutos in listOf(null, 0, -1, 1441, Int.MAX_VALUE)) {
            val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
            rechaza { c.prepararPolicy(t, otro, "1", minutos) }
        }
    }
    @Test fun canonicalidadUuidNonceUtf8DuplicadosLimitesYProfundidad() {
        val p = MensajesPadreCp2.payload(policy()); val s = p.toString(Charsets.UTF_8)
        for (texto in listOf(" $s", "$s\n", s.replace("\"v\":2", "\"v\":2,\"v\":2"),
            s.replace("PARENT", "CHILD"), s.replace(nonce, nonce + "="), s.replace(nonce, "A".repeat(42) + "B"),
            s.replace(otro, otro.replace("4000", "5000")), s.replace("\"v\":2", "\"v\":1"), s.replace("PARENT", "PÁRENT")))
            rechaza { MensajesPadreCp2.leerPayload(texto.toByteArray()) }
        rechaza { MensajesPadreCp2.leerPayload(byteArrayOf(0xc3.toByte(), 0x28)) }
        rechaza { MensajesPadreCp2.leerPayload(ByteArray(8193)) }
        rechaza { MensajesPadreCp2.verificar(ByteArray(16385), publica) }
        rechaza { MensajesPadreCp2.leerPayload(("[".repeat(7) + "0" + "]".repeat(7)).toByteArray()) }
        var invocado = false
        rechaza { MensajesPadreCp2.firmar(" $s".toByteArray(), publica) { invocado = true; firma(it) } }
        assertFalse(invocado)
    }
    @Test fun firmasDominioCruzadoClavesDerYMutacionRechazados() {
        val p = MensajesPadreCp2.payload(policy()); val d = MensajesPadreCp2.firmar(p, publica, ::firma)
        for (prefijo in listOf("CPv1/MESSAGE\n", "CPv2/AUTH\n", "CPv2/PAIR\n", "CPv2/MESSAGE\\n"))
            rechaza { MensajesPadreCp2.firmar(p, publica) { firma(prefijo.toByteArray() + p) } }
        val k = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        rechaza { MensajesPadreCp2.verificar(d.frame(), VinculacionCp2.b64(k.public.encoded)) }
        rechaza { MensajesPadreCp2.verificar(d.frame(), publica + "=") }
        rechaza { AutenticacionCp2.verificar(d.frame(), publica, Rol.PARENT) }
        rechaza { VinculacionCp2.verificar(d.frame(), publica) }
        rechaza { CodecProtocolo.leerFrame(d.frame()) }
        val alterado = VinculacionCp2.b64(MensajesPadreCp2.payload(policy().copy(seq = "2")))
        rechaza { MensajesPadreCp2.verificar(cambiar(d.frame(), "payloadB64", JsonPrimitive(alterado)), publica) }
        for (der in listOf(byteArrayOf(), byteArrayOf(0x30, 6, 2, 1, 0, 2, 1, 1), ByteArray(73)))
            rechaza { MensajesPadreCp2.firmar(p, publica) { der } }
    }
    @Test fun autoridadSesionIdentidadFondoYPostFirmaNoCreanClaves() {
        val p = MensajesPadreCp2.payload(sync())
        rechaza { autoridad.firmarMessageCp2(p, publica) }; assertEquals(0, firmas)
        autenticar(); assertEquals(sync(), autoridad.firmarMessageCp2(p, publica).mensaje)
        sesion.bloquear(); rechaza { autoridad.firmarMessageCp2(p, publica) }
        autenticar(); publicaActual = "distinta"; rechaza { autoridad.firmarMessageCp2(p, publica) }
        publicaActual = publica; autenticar(); alFirmar = { sesion.invalidar() }
        rechaza { autoridad.firmarMessageCp2(p, publica) }
    }
    @Test fun fuenteAusentePendienteCp1OCambiadaNoPermiteFirma() {
        for (s in listOf(null, original.copy(confirmadoCompatible = false), original.copy(protocol = "cp.v1"),
            original.copy(publicaPadre = "otra"), original.copy(revisionFuente = ""))) {
            snapshot = s; rechaza { caso().iniciarSync(nonce, AccionCp2.ALLOW_FOR) }
        }
        snapshot = original; val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
        snapshot = original.copy(revisionFuente = "cambio"); autenticar()
        rechaza { c.firmarSync(t) }; assertEquals(0, firmas)
    }
    @Test fun preparacionExactaConfirmacionExplicitaUnUsoSinPersistencia() {
        val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR); autenticar()
        assertEquals(t.sync, c.firmarSync(t).mensaje)
        val p = c.prepararPolicy(t, otro, "1", 1)
        assertEquals(policy(), p.policy)
        assertEquals(policy(), c.confirmarYFirmar(p, policy()).mensaje)
        rechaza { c.confirmarYFirmar(p, policy()) }
    }
    @Test fun estadoAusenteContextosYConfirmacionDistintosInvalidanSyncCompleto() {
        for (e in listOf(null, estadoOriginal.copy(pairId = otro), estadoOriginal.copy(syncNonce = VinculacionCp2.b64(ByteArray(32) { 1 })),
            estadoOriginal.copy(action = AccionCp2.LOCK))) {
            estado = e; val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
            rechaza { c.prepararPolicy(t, otro, "1", 1) }; estado = estadoOriginal
            rechaza { c.prepararPolicy(t, otro, "1", 1) }
        }
        val cambios = listOf(estadoOriginal.copy(bootId = id), estadoOriginal.copy(childSessionId = otro),
            estadoOriginal.copy(revision = "1"), estadoOriginal.copy(commandNonce = VinculacionCp2.b64(ByteArray(32) { 1 })))
        for (e in cambios) {
            estado = estadoOriginal; val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
            val p = c.prepararPolicy(t, otro, "1", 1); estado = e; autenticar()
            rechaza { c.confirmarYFirmar(p, p.policy) }
        }
        estado = estadoOriginal; val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
        val p = c.prepararPolicy(t, otro, "1", 1)
        rechaza { c.confirmarYFirmar(p, p.policy.copy(durationSec = 120)) }
        assertEquals(0, firmas)
    }
    @Test fun ticketCaducaDesdeInicioSinRejuvenecerYCancelaAlError() {
        for (delta in listOf(-1L, 20_000L, Long.MAX_VALUE - 100)) {
            ahora = 100; val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
            val p = c.prepararPolicy(t, otro, "1", 1); autenticar(); ahora += delta
            rechaza { c.confirmarYFirmar(p, p.policy) }
        }
        ahora = 100; val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
        ahora += 19_999; val p = c.prepararPolicy(t, otro, Long.MAX_VALUE.toString(), 1440); autenticar()
        assertNotNull(c.confirmarYFirmar(p, p.policy))
        val t2 = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR); c.cancelar()
        rechaza { c.prepararPolicy(t2, otro, "1", 1) }
    }
    @Test fun secuenciaAgotadaTicketsOtrosCasosYNuevoSyncNoReseteanNada() {
        estado = estadoOriginal.copy(lastAcceptedSeq = Long.MAX_VALUE.toString())
        val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
        rechaza { c.prepararPolicy(t, otro, Long.MAX_VALUE.toString(), 1) }
        estado = estadoOriginal; val t2 = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
        rechaza { caso().prepararPolicy(t2, otro, "1", 1) }
        c.iniciarSync(nonce, AccionCp2.ALLOW_FOR); rechaza { c.prepararPolicy(t2, otro, "1", 1) }
    }
    @Test fun sourceGateSinFuenteNiCasoConectadosAUiTransporteOAlmacen() {
        val raiz = java.nio.file.Path.of(requireNotNull(System.getProperty("schemaCp2"))).parent.parent.toFile()
        val main = java.io.File(raiz, "app-parent/src/main/java")
        val referencias = main.walkTopDown().filter { it.extension == "kt" && it.name != "PrepararMensajeCp2.kt" }
            .filter { Regex("PrepararMensajeCp2|FuentePreparacionMessageCp2|SnapshotMessageCp2|ContextoStateMessageCp2|TicketPolicyMessageCp2").containsMatchIn(it.readText()) }.toList()
        assertTrue("No conectar preparación a MAIN sin STATE/persistencia", referencias.isEmpty())
        val entradas = main.walkTopDown().filter { it.extension == "kt" && ("/ui/" in it.invariantSeparatorsPath ||
            "/network/" in it.invariantSeparatorsPath || it.name == "MainActivity.kt") }.toList()
        assertTrue(entradas.isNotEmpty())
        entradas.forEach { assertFalse(Regex("firmarMessageCp2|MensajesPadreCp2|SetPolicyCp2").containsMatchIn(it.readText())) }
    }
    @Test fun syncObservacionNoPreparaOrdenYLockAllowNoAceptanMinutos() {
        val c = caso(); val t = c.iniciarSync(nonce, null)
        rechaza { c.prepararPolicy(t, otro, "1", 1) }
        for (a in listOf(AccionCp2.LOCK, AccionCp2.ALLOW)) {
            estado = estadoOriginal.copy(action = a)
            val s = c.iniciarSync(nonce, a); val p = c.prepararPolicy(s, otro, "1", null)
            assertNull(p.policy.durationSec)
            val nuevo = c.iniciarSync(nonce, a); rechaza { c.prepararPolicy(nuevo, otro, "1", 1) }
        }
    }
    @Test fun cambioDuranteFirmaOCaducidadPosteriorNoDevuelvenOrden() {
        for (cambio in listOf<() -> Unit>({ ahora += 20_000 }, { snapshot = original.copy(revisionFuente = "nueva") },
            { estado = estadoOriginal.copy(revision = "2") }, { publicaActual = "otra" })) {
            ahora = 100; snapshot = original; estado = estadoOriginal; publicaActual = publica
            val c = caso(); val t = c.iniciarSync(nonce, AccionCp2.ALLOW_FOR)
            val p = c.prepararPolicy(t, otro, "1", 1); autenticar(); alFirmar = cambio
            rechaza { c.confirmarYFirmar(p, p.policy) }
            alFirmar = {}; rechaza { c.confirmarYFirmar(p, p.policy) }
        }
    }
    @Test fun sobreCerradoNullsCanonicalidadYDerFueraDeRango() {
        val d = MensajesPadreCp2.firmar(MensajesPadreCp2.payload(sync()), publica, ::firma)
        for (k in obj(d.frame()).keys) {
            rechaza { MensajesPadreCp2.verificar(canonicoTest(JsonObject(obj(d.frame()) - k)).toByteArray(), publica) }
            rechaza { MensajesPadreCp2.verificar(cambiar(d.frame(), k, JsonNull), publica) }
        }
        rechaza { MensajesPadreCp2.verificar(cambiar(d.frame(), "extra", JsonNull), publica) }
        rechaza { MensajesPadreCp2.verificar((d.frame().toString(Charsets.UTF_8) + "\n").toByteArray(), publica) }
        rechaza { MensajesPadreCp2.verificar(cambiar(d.frame(), "payloadB64", JsonPrimitive(VinculacionCp2.b64(d.payload()) + "=")), publica) }
        val derRango = byteArrayOf(0x30, 0x26, 2, 0x21, 0) + ByteArray(32) { 0xff.toByte() } + byteArrayOf(2, 1, 1)
        rechaza { MensajesPadreCp2.firmar(d.payload(), publica) { derRango } }
        for (campo in listOf("pairId", "bootId", "childSessionId", "commandId")) {
            val p = MensajesPadreCp2.payload(policy())
            rechaza { MensajesPadreCp2.leerPayload(cambiar(p, campo, JsonPrimitive("abcdefab-abcd-4abc-8abc-abcdefabcdef".uppercase()))) }
        }
    }
}
