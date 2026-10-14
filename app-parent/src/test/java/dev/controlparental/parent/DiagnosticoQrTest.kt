package dev.controlparental.parent

import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.detector.Detector
import com.google.zxing.qrcode.encoder.Encoder
import dev.controlparental.parent.pairing.QrLocal
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

// Opt-in -PdiagnosticoQr=true. Presupuesto fijo, no reintentos para pasar tests.
// Logs solo métricas y, como máximo, un sobre/pública ficticios (nunca privadas).
class DiagnosticoQrTest {
    private data class Caso(val indice: Int, val etapa: String, val frame: ByteArray, val publica: String, val rol: Rol)
    private data class Generacion(val etapa: String, val identidad: java.security.KeyPair, val mensaje: Mensaje, val rol: Rol)
    private fun pixeles(m: BitMatrix) = IntArray(m.width * m.height) {
        if (m[it % m.width, it / m.width]) 0xff000000.toInt() else 0xffffffff.toInt()
    }
    private fun verificar(c: Caso, texto: String) {
        assertArrayEquals(c.frame, texto.toByteArray(Charsets.UTF_8))
        CriptografiaProtocolo.verificarSobre(texto.toByteArray(Charsets.UTF_8),
            ClaveVerificacion.desdeSpki(c.rol, c.publica), Proposito.OFFLINE)
    }
    private fun comprobar(c: Caso, m: BitMatrix, variante: String): Boolean {
        val texto = try { QrLocal.leer(m.width, m.height, pixeles(m)) }
        catch (_: NotFoundException) {
            println("CASO indice=${c.indice} etapa=${c.etapa} variante=$variante resultado=NotFoundException")
            return false
        }
        verificar(c, texto)
        println("CASO indice=${c.indice} etapa=${c.etapa} variante=$variante resultado=OK_BYTES_FIRMA")
        return true
    }
    private fun variantes(c: Caso) {
        val texto = c.frame.toString(Charsets.UTF_8)
        verificar(c, texto)
        val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
        val qr = Encoder.encode(texto, ErrorCorrectionLevel.L, hints)
        val dimension = qr.version.dimensionForVersion
        val matriz = QrLocal.generar(texto)
        // Control distinto, SOLO diagnóstico: evita detector, no corrige QrLocal.
        val imagen = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(matriz.width, matriz.height, pixeles(matriz))))
        verificar(c, QRCodeReader().decode(imagen, mapOf(DecodeHintType.PURE_BARCODE to true,
            DecodeHintType.CHARACTER_SET to "UTF-8")).text)
        println("CONTROL PURE_BARCODE resultado=OK_BYTES_FIRMA version=${qr.version.versionNumber} mascara=${qr.maskPattern}")
        repeat(3) { comprobar(c, matriz, "repeticion-identica-${it + 1}") }
        val configuraciones = listOf(752 to 4, (dimension + 8) * 6 to 4,
            (dimension + 8) * 8 to 4, 768 to 8, (dimension + 16) * 8 to 8)
        for ((tamano, margen) in configuraciones) {
            val m = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamano, tamano,
                hints + (EncodeHintType.MARGIN to margen))
            comprobar(c, m, "tamano-$tamano-margen-$margen")
        }
        for (mascara in 0..7) {
            val m = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, 768, 768,
                hints + mapOf(EncodeHintType.MARGIN to 4, EncodeHintType.QR_MASK_PATTERN to mascara))
            comprobar(c, m, "mascara-$mascara")
        }
    }
    @Test fun casoPublicoFijoExigeLecturaCorregidaSinFirmarDeNuevo() {
        val json = javaClass.getResourceAsStream("/qr01_respuesta_publica.json")!!.use {
            Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject
        }
        val c = Caso(39, "respuesta", Base64.getUrlDecoder().decode(json.getValue("frameB64").jsonPrimitive.content),
            json.getValue("publica").jsonPrimitive.content, Rol.PARENT)
        val texto = c.frame.toString(Charsets.UTF_8)
        verificar(c, texto)
        val qr = Encoder.encode(texto, ErrorCorrectionLevel.L, mapOf(EncodeHintType.CHARACTER_SET to "UTF-8"))
        assertEquals(15, qr.version.versionNumber); assertEquals(4, qr.maskPattern)
        val matriz = QrLocal.generar(texto)
        assertTrue("El lector productivo debe preservar bytes y firma", comprobar(c, matriz, "fixture-corregido"))
        val imagen = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(matriz.width, matriz.height, pixeles(matriz))))
        val puntos = mutableListOf<ResultPoint>()
        assertThrows(ChecksumException::class.java) {
            QRCodeReader().decode(imagen, mapOf(DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8", DecodeHintType.NEED_RESULT_POINT_CALLBACK to ResultPointCallback { puntos.add(it) }))
        }
        val centros = puntos.joinToString { "(${it.x},${it.y})" }
        println("LECTOR_QR_DIRECTO resultado=ChecksumException candidatos=${puntos.size} centros=$centros")
        val detectado = Detector(imagen.blackMatrix).detect(mapOf(DecodeHintType.TRY_HARDER to true))
        val seleccionados = detectado.points.joinToString { "(${it.x},${it.y})" }
        println("GEOMETRIA seleccionados=$seleccionados inferiorIzquierdoReal=(68.5,698.5)")
        assertFalse(detectado.points.take(3).any { it.x == 68.5f && it.y == 698.5f })
        verificar(c, QRCodeReader().decode(imagen, mapOf(DecodeHintType.PURE_BARCODE to true,
            DecodeHintType.CHARACTER_SET to "UTF-8")).text)
        println("FIXTURE_CONTROL resultado=OK_BYTES_FIRMA")
    }
    @Test fun presupuestoSesentaYCuatroParesYDieciseisVariantesMaximas() {
        var fallosReto = 0; var fallosRespuesta = 0; var primero: Caso? = null
        for (indice in 0 until 64) {
            // Identidades nuevas por par como en el test anterior; firma DER variable.
            val hijo = par(); val padre = par()
            for ((etapa, identidad, mensaje, rol) in listOf(
                Generacion("reto", hijo, RetoOffline(ID, BOOT, AccionOffline.MAINTENANCE, NONCE), Rol.CHILD),
                Generacion("respuesta", padre, RespuestaOffline(ID, BOOT, AccionOffline.MAINTENANCE, NONCE), Rol.PARENT))) {
                val clave = identidad
                val publica = Base64.getUrlEncoder().withoutPadding().encodeToString(clave.public.encoded)
                val sobre = CriptografiaProtocolo.firmarSobre(mensaje, Proposito.OFFLINE,
                    ClaveVerificacion.desdeSpki(rol, publica)) { bytes ->
                    Signature.getInstance("SHA256withECDSA").run { initSign(clave.private); update(bytes); sign() }
                }
                val c = Caso(indice, etapa, CodecProtocolo.codificarFrame(sobre), publica, rol)
                val texto = c.frame.toString(Charsets.UTF_8)
                val qr = Encoder.encode(texto, ErrorCorrectionLevel.L, mapOf(EncodeHintType.CHARACTER_SET to "UTF-8"))
                println("METRICA indice=$indice etapa=$etapa bytes=${c.frame.size} der=${Base64.getUrlDecoder().decode(sobre.sigB64).size} " +
                    "version=${qr.version.versionNumber} mascara=${qr.maskPattern} tamano=768 margen=4 escala=${768 / (qr.version.dimensionForVersion + 8)}")
                if (!comprobar(c, QrLocal.generar(texto), "original")) {
                    if (rol == Rol.CHILD) fallosReto++ else fallosRespuesta++
                    if (primero == null) primero = c
                }
            }
        }
        println("RESUMEN pares=64 frames=128 fallosReto=$fallosReto fallosRespuesta=$fallosRespuesta")
        primero?.let { c ->
            println("FIXTURE_PUBLICO " + buildJsonObject {
                put("indice", c.indice); put("etapa", c.etapa); put("rol", c.rol.name)
                put("publica", c.publica); put("frameB64", Base64.getUrlEncoder().withoutPadding().encodeToString(c.frame))
            })
            variantes(c)
        }
        // Una muestra fallida hace fallar el diagnóstico; nunca se oculta como éxito.
        assertEquals("Fallos de lector original dentro del presupuesto", 0, fallosReto + fallosRespuesta)
    }
    companion object {
        const val ID = "00000000-0000-4000-8000-000000000002"
        const val BOOT = "00000000-0000-4000-8000-000000000003"
        val NONCE = "A".repeat(43)
        fun par() = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    }
}
