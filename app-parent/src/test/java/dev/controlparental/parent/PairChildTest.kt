package dev.controlparental.parent

import dev.controlparental.parent.data.*
import dev.controlparental.parent.pairing.*
import dev.controlparental.parent.security.CifradoMetadatos
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import javax.crypto.KeyGenerator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PairChildTest {
    private class Archivo : ArchivoMetadatos {
        var bytes: ByteArray? = null
        override fun existe() = bytes != null
        override fun leer() = bytes!!.copyOf()
        override fun escribir(bytes: ByteArray) { this.bytes = bytes.copyOf() }
    }
    private class Entorno {
        val padre = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        val hijo = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
        fun b64(bytes: ByteArray) = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val publica = b64(padre.public.encoded)
        val oferta = """{"v":1,"kind":"CHILD_OFFER","childPublicKeyB64":"${b64(hijo.public.encoded)}","enrollId":"00000000-0000-4000-8000-000000000001","enrollNonce":"${b64(ByteArray(32))}"}""".toByteArray()
        val archivo = Archivo()
        val clave = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val cifrador = CifradoMetadatos({}, { clave })
        var firmas = 0
        fun firma(aceptacion: AceptacionVinculo): SobreFirmado {
            firmas++
            return CriptografiaProtocolo.firmarSobre(aceptacion, Proposito.PAIR,
                ClaveVerificacion.desdeSpki(Rol.PARENT, publica)) { bytes ->
                Signature.getInstance("SHA256withECDSA").run { initSign(padre.private); update(bytes); sign() }
            }
        }
    }

    @Test fun codigos160BitsIndependientesNormalizacionYHashPorProposito() {
        assertEquals("A".repeat(32), RecoveryCodeGenerator.base32(ByteArray(20)))
        assertEquals("7".repeat(32), RecoveryCodeGenerator.base32(ByteArray(20) { -1 }))
        val codigos = RecoveryCodeGenerator().generar()
        assertEquals(3, codigos.mostrar().values.toSet().size)
        codigos.mostrar().values.forEach { assertEquals(32, RecoveryCodeGenerator.normalizar(it.lowercase()).length) }
        val codigo = "ABCD".repeat(8)
        val hashes = PropositoRecuperacion.entries.map { RecoveryCodeGenerator.hash("pareja-ficticia", it, codigo) }
        assertEquals(3, hashes.toSet().size)
        val esperado = Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256")
            .digest("CPv1/RECOVERY\npareja-ficticia\nMAINTENANCE\n$codigo".toByteArray()))
        assertEquals(esperado, hashes.first())
        for (invalido in listOf("1".repeat(32), "a".repeat(31), "a".repeat(32) + "\n", "é".repeat(32))) {
            assertThrows(IllegalArgumentException::class.java) { RecoveryCodeGenerator.normalizar(invalido) }
        }
        codigos.descartar()
        assertTrue(codigos.mostrar().isEmpty())
        assertFalse(codigos.toString().contains(codigo))
    }

    @Test fun dosQrOfflineFirmaComparacionExportacionPublicaYNoRevinculacion() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val flujo = PairChild(store, { e.publica }, e::firma)
        val ofertaQr = QrLocal.generar(e.oferta.toString(Charsets.UTF_8))
        fun leer(matriz: com.google.zxing.common.BitMatrix): String {
            val pixels = IntArray(matriz.width * matriz.height) { indice ->
                if (matriz[indice % matriz.width, indice / matriz.width]) 0xff000000.toInt() else 0xffffffff.toInt()
            }
            return QrLocal.leer(matriz.width, matriz.height, pixels)
        }
        val preparacion = flujo.preparar(leer(ofertaQr).toByteArray(), "https://prueba.invalid")
        val codigos = preparacion.codigos.mostrar().values.map { RecoveryCodeGenerator.normalizar(it) }
        assertTrue(runCatching { flujo.aceptar(preparacion, false, true) }.isFailure)
        assertTrue(runCatching { flujo.aceptar(preparacion, true, false) }.isFailure)
        assertEquals(0, e.firmas)
        val frame = flujo.aceptar(preparacion, true, true)
        assertArrayEquals(frame, leer(QrLocal.generar(frame.toString(Charsets.UTF_8))).toByteArray())
        val documento = CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.PARENT, e.publica), Proposito.PAIR)
        assertEquals(preparacion.aceptacion, documento.mensaje)
        assertEquals(VinculoPersistido.PREPARADO, store.leer().vinculo)
        assertArrayEquals(frame, Base64.getUrlDecoder().decode(store.leer().pairAcceptanceFrameB64))
        assertTrue(preparacion.codigos.mostrar().isEmpty())
        val env = flujo.exportarEnvPublica()
        assertEquals(setOf("ACTIVE_PAIR_ID", "PARENT_PUBLIC_KEY_B64", "CHILD_PUBLIC_KEY_B64", "PUBLIC_ORIGIN"),
            env.lines().filter { it.isNotEmpty() }.map { it.substringBefore('=') }.toSet())
        for (codigo in codigos) { assertFalse(env.contains(codigo)); assertFalse(frame.toString(Charsets.UTF_8).contains(codigo)) }
        assertFalse(env.contains("recoveryHashes"))
        assertTrue(runCatching { flujo.preparar(e.oferta, "https://prueba.invalid") }.isFailure)
        assertTrue(runCatching { store.reservarIntencion(Accion.LOCK, null, "0") }.isFailure)
        store.cerrar()
        val reabierto = ParentStore(e.archivo, e.cifrador, backgroundScope)
        assertArrayEquals(frame, Base64.getUrlDecoder().decode(reabierto.leer().pairAcceptanceFrameB64))
        reabierto.cerrar()
    }

    @Test fun ofertaInvalidaOrigenInseguroYFallosFirmaNoPreparanVinculo() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val flujo = PairChild(store, { e.publica }, { error("Autenticación bloqueada ficticia") })
        assertTrue(runCatching { flujo.preparar("{}".toByteArray(), "https://prueba.invalid") }.isFailure)
        assertFalse(store.existe())
        for (origen in listOf("http://prueba.invalid", "https://prueba.invalid\nSECRETO=1", "https://prueba.invalid/ruta")) {
            assertTrue(runCatching { flujo.preparar(e.oferta, origen) }.isFailure)
        }
        val preparacion = flujo.preparar(e.oferta, "https://prueba.invalid")
        assertTrue(runCatching { flujo.aceptar(preparacion, true, true) }.isFailure)
        assertEquals(VinculoPersistido.SIN_VINCULAR, store.leer().vinculo)
        preparacion.codigos.descartar()
        store.cerrar()
    }

    @Test fun migracionPrivadaV1ConservaReservaContadorYEvidenciaSinCrearVinculo() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val datos = MetadatosPadre(parentPublicKeyB64 = e.publica,
            pairId = "00000000-0000-4000-8000-000000000002",
            childPublicKeyB64 = e.b64(e.hijo.public.encoded), relayOrigin = "https://prueba.invalid",
            vinculo = VinculoPersistido.VINCULADO)
        store.inicializar(datos)
        val reserva = store.reservarIntencion(Accion.LOCK, null, "7")
        store.cerrar()
        val v2 = e.cifrador.descifrar(e.archivo.bytes!!).toString(Charsets.UTF_8)
        val v1 = v2.replace("\"schemaVersion\":2", "\"schemaVersion\":1")
            .replace(",\"pairAcceptanceFrameB64\":null", "")
        e.archivo.bytes = e.cifrador.cifrar(v1.toByteArray())
        val recuperado = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val migrados = recuperado.leer()
        assertEquals(2, migrados.schemaVersion)
        assertEquals("8", migrados.contadorLocal)
        assertEquals(reserva, migrados.pendiente)
        assertEquals(VinculoPersistido.VINCULADO, migrados.vinculo)
        assertNull(migrados.pairAcceptanceFrameB64)
        assertTrue(e.cifrador.descifrar(e.archivo.bytes!!).toString(Charsets.UTF_8).contains("\"schemaVersion\":2"))
        recuperado.cerrar()
    }

    @Test fun propositoFalsoClaveDistintaYOfertaNoVigenteSeRechazanEnSimulador() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val flujo = PairChild(store, { e.publica }, e::firma)
        val preparacion = flujo.preparar(e.oferta, "https://prueba.invalid")
        val sobre = e.firma(preparacion.aceptacion)
        val frame = CodecProtocolo.codificarFrame(sobre)
        // Simulador exclusivamente de pruebas: no demuestra comportamiento del APK real del hijo.
        fun hijoAcepta(bytes: ByteArray, vigente: Boolean = true, configurado: Boolean = false): Boolean = runCatching {
            check(vigente && !configurado)
            val aceptacion = CriptografiaProtocolo.verificarSobre(bytes,
                ClaveVerificacion.desdeSpki(Rol.PARENT, e.publica), Proposito.PAIR).mensaje as AceptacionVinculo
            val oferta = CodecProtocolo.leerOferta(e.oferta)
            require(aceptacion.childPublicKeyB64 == oferta.childPublicKeyB64 &&
                aceptacion.enrollId == oferta.enrollId && aceptacion.enrollNonce == oferta.enrollNonce)
        }.isSuccess
        assertTrue(hijoAcepta(frame))
        assertFalse(hijoAcepta(frame, vigente = false))
        assertFalse(hijoAcepta(frame, configurado = true))
        assertFalse(hijoAcepta(CodecProtocolo.codificarFrame(sobre.copy(purpose = Proposito.MESSAGE))))
        assertFalse(hijoAcepta(CodecProtocolo.codificarFrame(e.firma(preparacion.aceptacion.copy(childPublicKeyB64 = e.publica)))))
        assertFalse(hijoAcepta(CodecProtocolo.codificarFrame(e.firma(preparacion.aceptacion.copy(enrollId = preparacion.aceptacion.pairId)))))
        val otroPadre = CriptografiaProtocolo.firmarSobre(preparacion.aceptacion, Proposito.PAIR,
            ClaveVerificacion.desdeSpki(Rol.PARENT, e.b64(e.hijo.public.encoded))) { bytes ->
            Signature.getInstance("SHA256withECDSA").run { initSign(e.hijo.private); update(bytes); sign() }
        }
        assertFalse(hijoAcepta(CodecProtocolo.codificarFrame(otroPadre)))
        assertTrue(runCatching { store.prepararVinculo(CodecProtocolo.codificarFrame(otroPadre)) }.isFailure)
        assertEquals(VinculoPersistido.SIN_VINCULAR, store.leer().vinculo)
        preparacion.codigos.descartar()
        store.cerrar()
    }

    @Test fun preparacionDescartadaNoFirmaAunqueLleguenConfirmacionesAntiguas() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val flujo = PairChild(store, { e.publica }, e::firma)
        val preparacion = flujo.preparar(e.oferta, "https://prueba.invalid")
        preparacion.codigos.descartar()
        assertTrue(runCatching { flujo.aceptar(preparacion, true, true) }.isFailure)
        assertEquals(0, e.firmas)
        assertEquals(VinculoPersistido.SIN_VINCULAR, store.leer().vinculo)
        assertNull(store.leer().pairAcceptanceFrameB64)
        store.cerrar()
    }

    @Test fun cambiarParejaOHashesDePreparacionNoFirmaCodigosNoConfirmados() = runTest {
        val e = Entorno()
        val store = ParentStore(e.archivo, e.cifrador, backgroundScope)
        val flujo = PairChild(store, { e.publica }, e::firma)
        val preparacion = flujo.preparar(e.oferta, "https://prueba.invalid")
        val otraPareja = PreparacionVinculo(preparacion.aceptacion.copy(pairId = java.util.UUID.randomUUID().toString()), preparacion.codigos)
        assertTrue(runCatching { flujo.aceptar(otraPareja, true, true) }.isFailure)
        val otrosHashes = PreparacionVinculo(preparacion.aceptacion.copy(recoveryHashes =
            preparacion.aceptacion.recoveryHashes.copy(MAINTENANCE = "A".repeat(43))), preparacion.codigos)
        assertTrue(runCatching { flujo.aceptar(otrosHashes, true, true) }.isFailure)
        assertEquals(0, e.firmas)
        assertEquals(VinculoPersistido.SIN_VINCULAR, store.leer().vinculo)
        preparacion.codigos.descartar()
        store.cerrar()
    }
}
