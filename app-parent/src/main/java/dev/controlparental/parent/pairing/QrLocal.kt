package dev.controlparental.parent.pairing

import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.QRCodeReader
import dev.controlparental.protocol.CodecProtocolo
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction

// Sin servicios remotos ni Google Play. Solo QR CP/1 en UTF-8.
internal object QrLocal {
    fun generar(texto: String): BitMatrix {
        require(texto.toByteArray(Charsets.UTF_8).size <= 2500)
        return QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, 768, 768,
            mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.MARGIN to 4))
    }
    fun leer(ancho: Int, alto: Int, pixeles: IntArray): String {
        require(ancho in 1..2048 && alto in 1..2048 && pixeles.size == ancho * alto)
        return decodificar(ancho, alto, RGBLuminanceSource(ancho, alto, pixeles).matrix)
    }
    fun leerLuminancia(ancho: Int, alto: Int, bytes: ByteArray): String {
        require(ancho in 1..2048 && alto in 1..2048 && bytes.size == ancho * alto)
        return decodificar(ancho, alto, bytes)
    }
    private fun decodificar(ancho: Int, alto: Int, bytes: ByteArray): String {
        var w = ancho; var h = alto; var gris = bytes
        var fallo: ReaderException = NotFoundException.getNotFoundInstance()
        val inicio = System.nanoTime()
        // Cuatro orientaciones como máximo, sin PURE_BARCODE ni geometría presupuesta.
        // Rotar el plano cambia el barrido del finder; no los módulos/contenido del QR.
        repeat(4) { intento ->
            // Presupuesto cooperativo: no iniciar otra detección tras 400 ms.
            // ZXing síncrono no permite interrumpir una detección ya iniciada.
            if (intento > 0 && System.nanoTime() - inicio >= 400_000_000L) throw fallo
            val imagen = BinaryBitmap(HybridBinarizer(
                PlanarYUVLuminanceSource(gris, w, h, 0, 0, w, h, false)))
            val resultado = try {
                QRCodeReader().decode(imagen, mapOf(DecodeHintType.CHARACTER_SET to "UTF-8",
                    DecodeHintType.TRY_HARDER to true))
            } catch (e: ReaderException) {
                // Preservar checksum/formato si hubo detección, nunca fabricar texto.
                if (fallo is NotFoundException || e !is NotFoundException) fallo = e
                null
            }
            if (resultado != null) {
                // rawBytes son codewords QR, NO bytes del sobre. text es el contenido
                // lógico (también para segmentos mixtos); no trim/normalización.
                val texto = resultado.text
                require('\uFFFD' !in texto) { "QR con texto no decodificable exactamente" }
                val codificador = Charsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                require(codificador.encode(CharBuffer.wrap(texto)).remaining() <= CodecProtocolo.MAX_FRAME)
                return texto // El borde PAIR/OFFLINE valida codec, firma y contexto después.
            }
            if (intento < 3) {
                val anterior = gris; val aw = w; val ah = h
                gris = ByteArray(anterior.size) { i -> anterior[(ah - 1 - i % ah) * aw + i / ah] }
                w = ah; h = aw
            }
        }
        throw fallo
    }
}
