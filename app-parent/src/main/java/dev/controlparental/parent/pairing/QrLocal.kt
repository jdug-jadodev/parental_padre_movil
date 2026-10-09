package dev.controlparental.parent.pairing

import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import dev.controlparental.protocol.CodecProtocolo

// Sin servicios remotos ni Google Play. Solo QR CP/1 en UTF-8.
internal object QrLocal {
    fun generar(texto: String): BitMatrix {
        require(texto.toByteArray(Charsets.UTF_8).size <= 2500)
        return QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, 768, 768,
            mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.MARGIN to 4))
    }
    fun leer(ancho: Int, alto: Int, pixeles: IntArray): String {
        require(ancho in 1..2048 && alto in 1..2048 && pixeles.size == ancho * alto)
        val imagen = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(ancho, alto, pixeles)))
        return decodificar(imagen)
    }
    fun leerLuminancia(ancho: Int, alto: Int, bytes: ByteArray): String {
        require(ancho in 1..2048 && alto in 1..2048 && bytes.size == ancho * alto)
        return decodificar(BinaryBitmap(HybridBinarizer(
            PlanarYUVLuminanceSource(bytes, ancho, alto, 0, 0, ancho, alto, false))))
    }
    private fun decodificar(imagen: BinaryBitmap): String {
        val resultado = MultiFormatReader().decode(imagen, mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.CHARACTER_SET to "UTF-8", DecodeHintType.TRY_HARDER to true))
        return resultado.text.also { require(it.toByteArray(Charsets.UTF_8).size <= CodecProtocolo.MAX_FRAME) }
    }
}
