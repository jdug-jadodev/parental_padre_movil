package dev.controlparental.parent.pairing

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

internal object ImagenQrAndroid {
    fun leer(resolver: ContentResolver, uri: Uri): String {
        val bytes = resolver.openInputStream(uri)?.use { entrada ->
            val salida = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val cantidad = entrada.read(buffer)
                if (cantidad < 0) break
                require(salida.size() + cantidad <= 8 * 1024 * 1024)
                salida.write(buffer, 0, cantidad)
            }
            salida.toByteArray()
        } ?: error("Imagen no disponible")
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)
        require(opciones.outWidth in 1..16384 && opciones.outHeight in 1..16384)
        var escala = 1
        while (opciones.outWidth / escala > 2048 || opciones.outHeight / escala > 2048) escala *= 2
        opciones.inJustDecodeBounds = false
        opciones.inSampleSize = escala
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones) ?: error("Imagen ilegible")
        return try {
            val pixeles = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixeles, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            QrLocal.leer(bitmap.width, bitmap.height, pixeles)
        } finally { bitmap.recycle() }
    }

    fun generar(texto: String): Bitmap {
        val matriz = QrLocal.generar(texto)
        val pixeles = IntArray(matriz.width * matriz.height) { indice ->
            if (matriz[indice % matriz.width, indice / matriz.width]) 0xff000000.toInt() else 0xffffffff.toInt()
        }
        return Bitmap.createBitmap(pixeles, matriz.width, matriz.height, Bitmap.Config.ARGB_8888)
    }
}
