package dev.controlparental.parent.pairing

import java.nio.ByteBuffer

internal data class LuminanciaQr(val ancho: Int, val alto: Int, val bytes: ByteArray) {
    companion object {
        // Respeta posición/límite, rowStride y pixelStride del plano Y sin mutarlo.
        fun copiar(buffer: ByteBuffer, ancho: Int, alto: Int, rowStride: Int, pixelStride: Int): LuminanciaQr {
            require(ancho in 1..4096 && alto in 1..4096 && pixelStride in 1..4)
            require(rowStride >= (ancho - 1) * pixelStride + 1)
            val inicio = buffer.position()
            val ultimo = inicio.toLong() + (alto - 1).toLong() * rowStride + (ancho - 1).toLong() * pixelStride
            require(ultimo < buffer.limit())
            val paso = if (maxOf(ancho, alto) > 2048) 2 else 1
            val w = (ancho + paso - 1) / paso
            val h = (alto + paso - 1) / paso
            val bytes = ByteArray(w * h)
            for (y in 0 until h) for (x in 0 until w) {
                bytes[y * w + x] = buffer.get(inicio + y * paso * rowStride + x * paso * pixelStride)
            }
            return LuminanciaQr(w, h, bytes)
        }
    }
}
