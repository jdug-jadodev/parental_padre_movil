package dev.controlparental.parent

import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import dev.controlparental.parent.pairing.QrLocal
import dev.controlparental.protocol.*
import java.util.Base64
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class QrLocalRegresionTest {
    private val json = javaClass.getResourceAsStream("/qr01_respuesta_publica.json")!!.use {
        Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject
    }
    private val frame = Base64.getUrlDecoder().decode(json.getValue("frameB64").jsonPrimitive.content)
    private val publica = ClaveVerificacion.desdeSpki(Rol.PARENT, json.getValue("publica").jsonPrimitive.content)
    private val texto = frame.toString(Charsets.UTF_8)
    private fun gris(m: BitMatrix) = ByteArray(m.width * m.height) { if (m[it % m.width, it / m.width]) 0 else -1 }
    private fun rgb(bytes: ByteArray) = IntArray(bytes.size) { if (bytes[it] == 0.toByte()) 0xff000000.toInt() else -1 }
    private fun exacto(leido: String) {
        assertArrayEquals(frame, leido.toByteArray(Charsets.UTF_8))
        CriptografiaProtocolo.verificarSobre(leido.toByteArray(Charsets.UTF_8), publica, Proposito.OFFLINE)
    }
    private fun comprobar(w: Int, h: Int, bytes: ByteArray) {
        val copia = bytes.copyOf()
        exacto(QrLocal.leerLuminancia(w, h, bytes))
        exacto(QrLocal.leer(w, h, rgb(bytes)))
        assertArrayEquals("El lector no modifica el plano recibido", copia, bytes)
    }

    @Test fun fixtureFijoOriginalYCuatroRotacionesPorAmbasEntradas() {
        val matriz = QrLocal.generar(texto); val lado = matriz.width
        var bytes = gris(matriz)
        repeat(4) {
            comprobar(lado, lado, bytes)
            val anterior = bytes
            bytes = ByteArray(bytes.size) { anterior[(lado - 1 - it % lado) * lado + it / lado] }
        }
    }

    @Test fun presupuestoCerradoDeCuarentaYOchoVariantesSinCambiarContenido() {
        val fallos = mutableListOf<String>(); var lecturas = 0
        val inicio = System.nanoTime()
        for ((lado, margen) in listOf(768 to 4, 752 to 4, 510 to 4, 680 to 4, 768 to 8, 744 to 8)) {
            for (mascara in 0..7) {
                val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, lado, lado, mapOf(
                    EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.MARGIN to margen,
                    EncodeHintType.QR_MASK_PATTERN to mascara))
                for (entrada in listOf("Y", "RGB")) {
                    try {
                        val bytes = gris(matriz)
                        exacto(if (entrada == "Y") QrLocal.leerLuminancia(lado, lado, bytes)
                            else QrLocal.leer(lado, lado, rgb(bytes)))
                    } catch (e: Exception) {
                        fallos += "lado=$lado margen=$margen mascara=$mascara entrada=$entrada ${e.javaClass.simpleName}"
                    }
                    lecturas++
                }
            }
        }
        println("REGRESION casos=48 lecturas=$lecturas fallos=${fallos.size} transcurridoMs=${(System.nanoTime() - inicio) / 1_000_000}")
        fallos.forEach { println("FALLO $it") }
        assertEquals("Todos los fallos del presupuesto: $fallos", 0, fallos.size)
    }

    @Test fun planoRectangularConTraslacionYCizallaSinteticaAcotada() {
        val m = QrLocal.generar(texto)
        for (cizalla in listOf(-0.06, 0.06)) {
            val w = 900; val h = 850
            val bytes = ByteArray(w * h) { i ->
                val y = i / w - 40; val x = (i % w - 65 - y * cizalla).toInt()
                if (x in 0 until m.width && y in 0 until m.height && m[x, y]) 0 else -1
            }
            comprobar(w, h, bytes)
        }
    }

    @Test fun imagenBlancaYRuidoSinQrNoFabricanContenido() {
        assertThrows(ReaderException::class.java) { QrLocal.leerLuminancia(256, 256, ByteArray(256 * 256) { -1 }) }
        val ruido = ByteArray(256 * 256) { i -> if ((i * 31 + i / 256 * 17) % 7 < 3) 0 else -1 }
        assertThrows(ReaderException::class.java) { QrLocal.leer(256, 256, rgb(ruido)) }
    }

    @Test fun simboloDanadoNoDevuelvePayloadAceptadoPorChecksum() {
        val m = QrLocal.generar(texto)
        val bytes = gris(m)
        // Conservar buscadores exteriores, destruir área de datos ampliamente.
        for (y in 180 until 620) for (x in 180 until 620) bytes[y * m.width + x] = -1
        assertThrows(ReaderException::class.java) { QrLocal.leerLuminancia(m.width, m.height, bytes) }
        val reemplazo = QrLocal.generar("texto\uFFFD")
        assertThrows(IllegalArgumentException::class.java) {
            QrLocal.leerLuminancia(reemplazo.width, reemplazo.height, gris(reemplazo))
        }
    }

    @Test fun limitesDimensionYBufferDeAmbasEntradas() {
        for ((w, h, n) in listOf(Triple(0, 1, 0), Triple(2049, 1, 2049), Triple(1, 2049, 2049), Triple(20, 20, 399))) {
            assertThrows(IllegalArgumentException::class.java) { QrLocal.leer(w, h, IntArray(n)) }
            assertThrows(IllegalArgumentException::class.java) { QrLocal.leerLuminancia(w, h, ByteArray(n)) }
        }
        assertThrows(ReaderException::class.java) { QrLocal.leerLuminancia(2048, 1, ByteArray(2048) { -1 }) }
    }

    @Test fun textoNoProtocoloYFirmaAlteradaSiguenRechazadosPorBordeReal() {
        val m = QrLocal.generar("Esto no es un sobre CP/1")
        val leido = QrLocal.leerLuminancia(m.width, m.height, gris(m))
        assertThrows(Exception::class.java) { CodecProtocolo.leerOferta(leido.toByteArray(Charsets.UTF_8)) }
        // Mismo payload público, firma pública alterada SOLO en imagen de test.
        val sobre = Json.parseToJsonElement(texto).jsonObject
        val invalido = JsonObject(sobre + ("sigB64" to JsonPrimitive("A".repeat(43)))).toString()
        val firmadoMal = QrLocal.generar(invalido)
        val decodificado = QrLocal.leerLuminancia(firmadoMal.width, firmadoMal.height, gris(firmadoMal))
        assertEquals(invalido, decodificado)
        assertThrows(Exception::class.java) {
            CriptografiaProtocolo.verificarSobre(decodificado.toByteArray(Charsets.UTF_8), publica, Proposito.OFFLINE)
        }
    }

    @Test fun dosCandidatosNoSeConcatenanNiInventanFirma() {
        val a = QrLocal.generar(texto); val b = QrLocal.generar("No es una autorización CP/1")
        val w = 1600; val h = 800
        val plano = ByteArray(w * h) { i ->
            val x = i % w; val y = i / w - 16
            val negro = when {
                y !in 0 until 768 -> false
                x in 16 until 784 -> a[x - 16, y]
                x in 816 until 1584 -> b[x - 816, y]
                else -> false
            }
            if (negro) 0 else -1
        }
        // Lector de una sola región: no promete elección de QR en una escena ambigua.
        // Puede rechazar escena o entregar un candidato exacto, jamás mezcla de ambos.
        val leido = try { QrLocal.leerLuminancia(w, h, plano) } catch (_: ReaderException) { return }
        assertTrue(leido == texto || leido == "No es una autorización CP/1")
        if (leido == texto) exacto(leido) else assertThrows(Exception::class.java) {
            CriptografiaProtocolo.verificarSobre(leido.toByteArray(Charsets.UTF_8), publica, Proposito.OFFLINE)
        }
    }

    @Test fun textoUtf8NoSeRecortaONormalizaYFrameExcesivoSeRechaza() {
        val contenido = "  áé漢字\n "
        val m = QrLocal.generar(contenido)
        assertEquals(contenido, QrLocal.leerLuminancia(m.width, m.height, gris(m)))
        assertThrows(IllegalArgumentException::class.java) { QrLocal.generar("a".repeat(2501)) }
        // QR version40 tiene menos capacidad que MAX_FRAME: el límite de frame se
        // mantiene en producción, pero no se fabrica un QR imposible para probarlo.
        assertEquals(16384, CodecProtocolo.MAX_FRAME)
    }
}
