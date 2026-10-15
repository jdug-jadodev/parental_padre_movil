package dev.controlparental.parent

import dev.controlparental.parent.data.*
import dev.controlparental.parent.pairing.*
import dev.controlparental.parent.security.*
import dev.controlparental.protocol.*
import dev.controlparental.protocol.cp2.*
import java.security.*
import java.security.spec.ECGenParameterSpec
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class PairIntegradoCp2Test {
    private val keys = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    private val publica = VinculacionCp2.b64(keys.public.encoded)
    private val id = "00000000-0000-4000-8000-000000000001"
    private val oferta = VinculacionCp2.oferta(OfertaCp2(publica, id, "A".repeat(43)))
    private var reloj = 1000L
    private var auth = true
    private var identidad = publica
    private var firmadas = 0
    private class Archivo : ArchivoMetadatos {
        var bytes: ByteArray? = null; var escrituras = 0; var fallo = false; var malReadback = false
        override fun existe() = bytes != null
        override fun leer(): ByteArray = if (malReadback) byteArrayOf(0) else bytes!!.copyOf()
        override fun escribir(bytes: ByteArray) { escrituras++; if (fallo) error("Fallo simulado"); this.bytes = bytes.copyOf() }
    }
    private val archivoLegado = Archivo()
    private val archivoCp2 = Archivo()
    private val json = Json { encodeDefaults = true }
    private fun firma(b: ByteArray): ByteArray {
        firmadas++
        return Signature.getInstance("SHA256withECDSA").run { initSign(keys.private); update(b); sign() }
    }
    private fun datos(v: VinculoPersistido = VinculoPersistido.SIN_VINCULAR): MetadatosPadre {
        if (v == VinculoPersistido.SIN_VINCULAR) return MetadatosPadre(parentPublicKeyB64 = publica)
        val a = AceptacionVinculo(id, id, "A".repeat(43), publica, publica, "https://relay.invalid",
            HashesRecuperacion("A".repeat(43), "A".repeat(43), "A".repeat(43)))
        val s = CriptografiaProtocolo.firmarSobre(a, Proposito.PAIR, ClaveVerificacion.desdeSpki(Rol.PARENT, publica), ::firma)
        return MetadatosPadre(parentPublicKeyB64 = publica, pairId = id, childPublicKeyB64 = publica,
            relayOrigin = "https://relay.invalid", vinculo = v, pairAcceptanceFrameB64 = VinculacionCp2.b64(CodecProtocolo.codificarFrame(s)))
    }
    private fun conFuente(d: MetadatosPadre = datos(), accion: suspend (VincularCp2, ParentStore, PairStoreCp2) -> Unit) = runBlocking {
        archivoLegado.bytes = json.encodeToString(d).toByteArray()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val cifrador = object : CifradorMetadatos {
            override fun prepararClave() { error("No crear") }
            override fun cifrar(bytes: ByteArray): ByteArray { error("No escribir legado") }
            override fun descifrar(bytes: ByteArray) = bytes.copyOf()
        }
        val legacy = ParentStore(archivoLegado, cifrador, scope)
        val area = PairStoreCp2(archivoCp2)
        val c = VincularCp2(legacy, area, { identidad }, { check(auth) }, { b, _ -> firma(b) }, { reloj })
        try { accion(c, legacy, area) } finally { legacy.cerrar(); scope.cancel() }
    }
    private suspend fun preparar(c: VincularCp2) = c.preparar(oferta, "https://relay.invalid", 1000, true)
    private suspend fun rechaza(f: suspend () -> Unit) { var error = false; try { f() } catch (_: Exception) { error = true }; assertTrue("Debe rechazar", error) }

    @Test fun preparadoLegadoSeConservaYAltaCp2PendienteSoloPublica() = conFuente(datos(VinculoPersistido.PREPARADO)) { c, _, _ ->
        val antes = archivoLegado.bytes!!.copyOf(); val t = preparar(c); val codigos = t.mostrarCodigos().values.toList()
        val original = t.payload(); val d = c.firmar(t, true, true, true)
        assertArrayEquals(original, d.payload()); assertArrayEquals(antes, archivoLegado.bytes)
        assertEquals(0, archivoLegado.escrituras); assertEquals(1, archivoCp2.escrituras)
        val r = c.inspeccionar().pendiente!!
        assertEquals("PENDIENTE_CONFIRMACION_HIJO", r.etapa)
        codigos.forEach { assertFalse(archivoCp2.bytes!!.toString(Charsets.UTF_8).contains(it)) }
        assertTrue(t.mostrarCodigos().isEmpty())
        assertTrue(c.exportarPublico().contains("PROTOCOL=cp.v2"))
        rechaza { preparar(c) }; assertEquals(1, archivoCp2.escrituras)
    }
    @Test fun reinicioLeeVerificaPendientePeroNoReiniciaCeremonia() = conFuente { c, _, _ ->
        val t = preparar(c); c.firmar(t, true, true, true)
        val estado = c.inspeccionar(); val reabierto = PairStoreCp2(archivoCp2)
        assertEquals(estado.pendiente, reabierto.leer(publica, estado.legadoHash))
        c.descartar(); rechaza { c.firmar(t, true, true, true) }
        rechaza { preparar(c) }
    }
    @Test fun activoRecuperacionYPreparadoIncompletoBloquean() {
        for (v in listOf(VinculoPersistido.VINCULADO, VinculoPersistido.REQUIERE_RECUPERACION)) conFuente(datos(v)) { c, _, _ -> rechaza { preparar(c) } }
        conFuente(datos(VinculoPersistido.PREPARADO).copy(pairAcceptanceFrameB64 = null)) { c, _, _ -> rechaza { preparar(c) } }
        assertEquals(0, archivoCp2.escrituras)
    }
    @Test fun contadorOEvidenciaLegadaBloquean() = conFuente(datos(VinculoPersistido.PREPARADO).copy(contadorLocal = "1")) { c, _, _ ->
        rechaza { preparar(c) }; assertEquals(0, archivoCp2.escrituras)
    }
    @Test fun archivoLegadoAusenteCorruptoNoEsVirgen() = conFuente { c, _, _ ->
        archivoLegado.bytes = null; rechaza { preparar(c) }; archivoLegado.bytes = byteArrayOf(1)
        rechaza { preparar(c) }; assertEquals(0, archivoCp2.escrituras)
    }
    @Test fun identidadDiferenteAuthYConsentimientoBloquean() = conFuente { c, _, _ ->
        rechaza { c.preparar(oferta, "https://relay.invalid", 1000, false) }
        auth = false; rechaza { preparar(c) }; auth = true
        identidad = "A".repeat(122); rechaza { preparar(c) }; assertEquals(0, archivoCp2.escrituras)
    }
    @Test fun cancelacionCaducidadOriginalYConfirmacionesInvalidan() = conFuente { c, _, _ ->
        var t = preparar(c); c.descartar(); rechaza { c.firmar(t, true, true, true) }
        reloj = 2000; t = preparar(c); reloj = 301000; rechaza { c.firmar(t, true, true, true) }
        reloj = 1000; t = preparar(c); rechaza { c.firmar(t, true, false, true) }
        rechaza { c.firmar(t, true, true, true) }; assertEquals(0, archivoCp2.escrituras)
    }
    @Test fun cambioLegadoDurantePreparacionNoFirmaNiSobrescribe() = conFuente { c, _, _ ->
        val t = preparar(c)
        archivoLegado.bytes = json.encodeToString(datos(VinculoPersistido.PREPARADO)).toByteArray()
        rechaza { c.firmar(t, true, true, true) }; assertEquals(0, archivoCp2.escrituras)
    }
    @Test fun escrituraFallidaOReadbackDistintoBloqueanInclusoReintento() {
        for (readback in listOf(false, true)) conFuente { c, _, _ ->
            archivoCp2.bytes = null; archivoCp2.fallo = !readback; archivoCp2.malReadback = readback
            val t = preparar(c); rechaza { c.firmar(t, true, true, true) }
            archivoCp2.fallo = false; archivoCp2.malReadback = false
            rechaza { preparar(c) }
        }
    }
    @Test fun corrupcionPerfilEtapaHashOFrameBloqueaSinReemplazo() = conFuente { c, _, _ ->
        val t = preparar(c); c.firmar(t, true, true, true); val s = archivoCp2.bytes!!.toString(Charsets.UTF_8)
        val h = c.inspeccionar().legadoHash
        for (alterado in listOf(s.replace("cp.v2", "cp.v1"), s.replace("PENDIENTE_CONFIRMACION_HIJO", "ACTIVO"),
            s.replace("\"schemaVersion\":1", "\"schemaVersion\":2"), s + "\n", "{}", "{\"schemaVersion\":1,\"schemaVersion\":1}")) {
            archivoCp2.bytes = alterado.toByteArray(); rechaza { PairStoreCp2(archivoCp2).leer(publica, h) }
        }
        assertEquals(1, archivoCp2.escrituras)
    }
    @Test fun lecturaLegadaV2NoMigraArchivo() = conFuente { c, _, _ ->
        val viejo = archivoLegado.bytes!!.toString(Charsets.UTF_8).replace("\"schemaVersion\":3", "\"schemaVersion\":2").replace(",\"intencionLocal\":null", "")
        archivoLegado.bytes = viejo.toByteArray(); c.inspeccionar()
        assertArrayEquals(viejo.toByteArray(), archivoLegado.bytes); assertEquals(0, archivoLegado.escrituras)
    }
    @Test fun firmanteRealInyectadoNoCreaIdentidadYRechazaOtroDominio() {
        val sesion = SesionAutoridad { reloj }; sesion.primerPlano(); sesion.completar(sesion.solicitar())
        var creadas = 0
        val a = AutoridadPadre(object : AlmacenClavePadre {
            override fun crearSiFalta(): String { creadas++; error("No crear") }
            override fun publicaSpki() = publica
            override fun firmar(bytes: ByteArray) = firma(bytes)
        }, sesion)
        conFuente { c, _, _ ->
            val t = preparar(c); val payload = t.payload()
            val d = VinculacionCp2.firmar(payload, publica) { a.firmarPairCp2(it, publica) }
            assertArrayEquals(payload, d.payload()); rechaza { a.firmarPairCp2("CPv2/AUTH\n".toByteArray() + payload, publica) }
        }
        assertEquals(0, creadas)
    }
}
