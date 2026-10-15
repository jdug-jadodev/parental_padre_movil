package dev.controlparental.parent

import dev.controlparental.protocol.*
import dev.controlparental.protocol.cp2.*
import dev.controlparental.parent.network.*
import dev.controlparental.parent.security.*
import java.security.*
import java.security.spec.ECGenParameterSpec
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class AuthCp2Test {
    private val keys = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    private val publica = VinculacionCp2.b64(keys.public.encoded)
    private val id = "00000000-0000-4000-8000-000000000001"
    private val otra = "00000000-0000-4000-8000-000000000002"
    private val nonce = "A".repeat(43)
    private val prueba = PruebaAuthCp2(id, Rol.PARENT, otra, nonce)
    private var ahora = 100L
    private var snapshot: SnapshotAuthCp2? = SnapshotAuthCp2(id, publica, "revision-prueba", true)
    private fun firma(b: ByteArray): ByteArray = Signature.getInstance("SHA256withECDSA").run { initSign(keys.private); update(b); sign() }
    private fun firmar(p: ByteArray, k: String) = AutenticacionCp2.firmar(p, k, Rol.PARENT, ::firma)
    private fun cliente() = AuthClienteCp2({ ahora }, ::firmar, { snapshot })
    private fun reto() = AutenticacionCp2.frame(RetoAuthCp2(otra, nonce))
    private fun ok(c: String = otra) = AutenticacionCp2.frame(AceptadaAuthCp2(c))
    private fun rechaza(f: () -> Unit) { assertThrows(Exception::class.java, f) }

    @Test fun firmaOriginalConLfRealHashYCopias() {
        val p = AutenticacionCp2.payload(prueba)
        val esperado = "CPv2/AUTH\n".toByteArray() + p
        val d = AutenticacionCp2.firmar(p, publica, Rol.PARENT) { bytes -> assertArrayEquals(esperado, bytes); firma(bytes) }
        assertArrayEquals(p, d.payload())
        assertEquals(VinculacionCp2.b64(MessageDigest.getInstance("SHA-256").digest(p)), d.payloadSha256B64)
        val frame = d.frame(); frame.fill(0); p.fill(0); d.payload().fill(0)
        assertEquals(prueba, AutenticacionCp2.verificar(d.frame(), publica, Rol.PARENT).prueba)
    }
    @Test fun dominiosCp1PairDobleEscapeYClaveRolDistintosRechazados() {
        val p = AutenticacionCp2.payload(prueba)
        for (dominio in listOf("CPv1/AUTH\n", "CPv2/PAIR\n", "CPv2/AUTH\\n")) {
            val f = AutenticacionCp2.frame(SobreAuthCp2(VinculacionCp2.b64(p), VinculacionCp2.b64(firma(dominio.toByteArray() + p))))
            rechaza { AutenticacionCp2.verificar(f, publica, Rol.PARENT) }
        }
        val d = firmar(p, publica)
        rechaza { AutenticacionCp2.verificar(d.frame(), publica, Rol.CHILD) }
        val otraClave = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        rechaza { AutenticacionCp2.verificar(d.frame(), VinculacionCp2.b64(otraClave.public.encoded), Rol.PARENT) }
        rechaza { CodecProtocolo.leerFrame(d.frame()) }
        rechaza { VinculacionCp2.verificar(d.frame(), publica) }
    }
    @Test fun framesCerradosCanonicalidadTiposNonceVersionLimites() {
        val r = reto().toString(Charsets.UTF_8)
        val casos = listOf(" $r", "$r\n", r.replace("\"v\":2", "\"v\":1"),
            r.replace("\"v\":2", "\"v\":2,\"v\":2"), r.replace("\"v\":2", "\"v\":\"2\""),
            r.replace("\"v\":2", "\"extra\":0,\"v\":2"), r.replace("\"timeoutSec\":10", "\"timeoutSec\":11"),
            r.replace(nonce, nonce + "="), r.replace(nonce, "A".repeat(42) + "B"), r.replace(otra, otra.replace("4000", "5000")))
        casos.forEach { rechaza { AutenticacionCp2.leerFrame(it.toByteArray()) } }
        rechaza { AutenticacionCp2.leerFrame(ByteArray(16385) { 32 }) }
        rechaza { AutenticacionCp2.leerPrueba(ByteArray(8193) { 32 }) }
        rechaza { AutenticacionCp2.leerFrame(byteArrayOf(0xc3.toByte(), 0x28)) }
        rechaza { AutenticacionCp2.leerFrame(("[".repeat(7) + "0" + "]".repeat(7)).toByteArray()) }
        rechaza { AutenticacionCp2.leerPrueba(AutenticacionCp2.payload(prueba).toString(Charsets.UTF_8).replace("PARENT", "RELÁY").toByteArray()) }
    }
    @Test fun schemaIntegroCamposExactosYObligatoriosDeCincoObjetos() {
        val schema = Json.parseToJsonElement(java.io.File(requireNotNull(System.getProperty("schemaAuthCp2"))).readText()).jsonObject
        val defs = schema.getValue("\$defs").jsonObject
        val d = firmar(AutenticacionCp2.payload(prueba), publica)
        val casos = listOf("reto" to reto(), "prueba" to d.payload(), "aceptada" to ok(),
            "latido" to AutenticacionCp2.frame(LatidoAuthCp2(false, nonce)), "sobreAuth" to d.frame())
        for ((nombre, b) in casos) {
            val o = Json.parseToJsonElement(b.toString(Charsets.UTF_8)).jsonObject
            val def = defs.getValue(nombre).jsonObject
            assertEquals(def.getValue("properties").jsonObject.keys, o.keys)
            assertEquals(def.getValue("required").jsonArray.map { it.jsonPrimitive.content }.toSet(), o.keys)
            assertEquals(JsonPrimitive(false), def.getValue("additionalProperties"))
            fun leer(bytes: ByteArray) { if (nombre == "prueba") AutenticacionCp2.leerPrueba(bytes) else AutenticacionCp2.leerFrame(bytes) }
            o.keys.forEach { k -> rechaza { leer(JsonObject(o - k).toString().toByteArray()) } }
            rechaza { leer(JsonObject(o + ("extra" to JsonPrimitive(0))).toString().toByteArray()) }
        }
        assertEquals(LatidoAuthCp2(true, nonce), AutenticacionCp2.leerFrame(AutenticacionCp2.frame(LatidoAuthCp2(true, nonce))))
    }
    @Test fun snapshotFaltanteIncompatibleOCp1NoDisponible() {
        val c = cliente()
        for (s in listOf(null, snapshot!!.copy(compatibleAutorizado = false), snapshot!!.copy(protocol = "cp.v1"), snapshot!!.copy(role = Rol.CHILD))) {
            snapshot = s; val g = c.abrir("cp.v2")
            assertEquals(FaseAuthCp2.NO_DISPONIBLE, c.fase); assertNull(c.recibirReto(g, reto()))
        }
        val sinProveedor = AuthClienteCp2({ ahora }, ::firmar)
        sinProveedor.abrir("cp.v2"); assertEquals(FaseAuthCp2.NO_DISPONIBLE, sinProveedor.fase)
    }
    @Test fun authOkSoloTrasEnvioExactoContextoYNoIndicaRemotoListo() {
        val c = cliente(); val g = c.abrir("cp.v2"); val t = c.recibirReto(g, reto())!!
        val p = AutenticacionCp2.verificar(t.bytes(), publica, Rol.PARENT).prueba
        assertEquals(prueba, p); t.bytes().fill(0)
        assertEquals(FaseAuthCp2.PRUEBA_LISTA, c.fase)
        assertTrue(c.confirmarEnvio(t, true)); assertTrue(c.recibirAceptada(g, ok()))
        assertEquals(FaseAuthCp2.AUTENTICADO, c.fase)
        // Clase sin estado de hijo, permisos, STATE, APPLIED o método enviar órdenes.
    }
    @Test fun authOkPrematuroIdDistintoOEnvioFallidoRechazado() {
        val c = cliente(); var g = c.abrir("cp.v2")
        assertFalse(c.recibirAceptada(g, ok())); assertEquals(FaseAuthCp2.ERROR, c.fase)
        g = c.abrir("cp.v2"); val t = c.recibirReto(g, reto())!!
        assertFalse(c.recibirAceptada(g, ok())) // Solo firmada, todavía no enviada.
        g = c.abrir("cp.v2"); assertFalse(c.confirmarEnvio(c.recibirReto(g, reto())!!, false))
        g = c.abrir("cp.v2"); assertTrue(c.confirmarEnvio(c.recibirReto(g, reto())!!, true))
        assertFalse(c.recibirAceptada(g, ok(id)))
        assertFalse(c.confirmarEnvio(t, true))
    }
    @Test fun callbacksViejosReplayReconnectCerrarYFondo() {
        val c = cliente(); val g1 = c.abrir("cp.v2"); val t1 = c.recibirReto(g1, reto())!!
        c.segundoPlano(); val g2 = c.abrir("cp.v2"); assertTrue(g2 > g1)
        assertFalse(c.confirmarEnvio(t1, true)); assertFalse(c.recibirAceptada(g1, ok())); assertNull(c.recibirReto(g1, reto()))
        assertEquals(FaseAuthCp2.ESPERANDO_RETO, c.fase)
        val t2 = c.recibirReto(g2, reto())!!; assertTrue(c.confirmarEnvio(t2, true)); assertTrue(c.recibirAceptada(g2, ok()))
        assertFalse(c.recibirAceptada(g2, ok())); assertEquals(FaseAuthCp2.ERROR, c.fase)
        c.cerrar(); assertFalse(c.recibirAceptada(g2, ok()))
    }
    @Test fun timeoutMonotonicoSnapshotCambioYSubprotocoloSinFallback() {
        val c = cliente(); c.abrir("cp.v1"); assertEquals(FaseAuthCp2.NO_DISPONIBLE, c.fase)
        for (mutar in listOf<() -> Unit>({ ahora += 10_000 }, { ahora-- }, { snapshot = snapshot!!.copy(revision = "otra") })) {
            ahora = 100; val g = c.abrir("cp.v2"); mutar(); assertNull(c.recibirReto(g, reto())); assertEquals(FaseAuthCp2.ERROR, c.fase)
        }
        ahora = 100; val g = c.abrir("cp.v2"); val t = c.recibirReto(g, reto())!!
        ahora += 10_000; assertFalse(c.confirmarEnvio(t, true))
        ahora = 100; val h = c.abrir("cp.v2"); c.confirmarEnvio(c.recibirReto(h, reto())!!, true)
        ahora += 10_000; assertFalse(c.recibirAceptada(h, ok()))
    }
    @Test fun cierreDuranteFirmaNoPublicaPruebaNiDaniaGeneracionNueva() {
        lateinit var c: AuthClienteCp2
        c = AuthClienteCp2({ ahora }, { p, k -> c.cerrar(); c.abrir("cp.v2"); firmar(p, k) }, { snapshot })
        val vieja = c.abrir("cp.v2"); assertNull(c.recibirReto(vieja, reto()))
        assertEquals(FaseAuthCp2.ESPERANDO_RETO, c.fase); assertTrue(c.generacion > vieja)
    }
    @Test fun adaptadorAlmacenExistenteNoCreaClavesYExigeSesion() {
        val sesion = SesionAutoridad { ahora }
        var creadas = 0; var firmadas = 0
        val almacen = object : AlmacenClavePadre {
            override fun crearSiFalta(): String { creadas++; error("No crear") }
            override fun publicaSpki() = publica
            override fun firmar(bytes: ByteArray): ByteArray { firmadas++; return firma(bytes) }
        }
        val f = FirmanteAuthCp2(almacen, sesion); val p = AutenticacionCp2.payload(prueba)
        rechaza { f.firmar(p, publica) }
        sesion.primerPlano(); assertTrue(sesion.completar(sesion.solicitar()))
        assertEquals(prueba, f.firmar(p, publica).prueba)
        sesion.bloquear(); rechaza { f.firmar(p, publica) }
        assertEquals(0, creadas); assertEquals(1, firmadas)
    }
    @Test fun pruebaAjenaNonceTiempoFirmaYDerInvalidoNoAvanzan() {
        val ajena = AuthClienteCp2({ ahora }, { _, k -> firmar(AutenticacionCp2.payload(prueba.copy(nonce = VinculacionCp2.b64(ByteArray(32) { 1 }))), k) }, { snapshot })
        val g = ajena.abrir("cp.v2"); assertNull(ajena.recibirReto(g, reto())); assertEquals(FaseAuthCp2.ERROR, ajena.fase)
        val lento = AuthClienteCp2({ ahora }, { p, k -> ahora += 10_000; firmar(p, k) }, { snapshot })
        val h = lento.abrir("cp.v2"); assertNull(lento.recibirReto(h, reto())); assertEquals(FaseAuthCp2.ERROR, lento.fase)
        val p = VinculacionCp2.b64(AutenticacionCp2.payload(prueba))
        rechaza { AutenticacionCp2.frame(SobreAuthCp2(p, VinculacionCp2.b64(byteArrayOf(0x30, 6, 2, 1, 0, 2, 1, 1)))) }
        rechaza { AutenticacionCp2.frame(SobreAuthCp2(VinculacionCp2.b64(ByteArray(8193)), VinculacionCp2.b64(firma(byteArrayOf(1))))) }
    }
}
