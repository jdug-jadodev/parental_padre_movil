package dev.controlparental.parent

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import dev.controlparental.parent.pairing.ImagenQrAndroid
import dev.controlparental.protocol.CodecProtocolo
import java.io.File
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test

class ImagenQrAndroidTest {
    @Test fun imagenLocalQrSinRedYContenidoInvalidoNoGeneranOferta() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val ruta = File(contexto.noBackupFilesDir, "cp.test.qr.${UUID.randomUUID()}.png")
        // Texto sin secretos: comprueba lector de imagen, no identidad ni oferta válida.
        val texto = "QR local de prueba, sin secretos"
        val bitmap = ImagenQrAndroid.generar(texto)
        try {
            ruta.outputStream().use { assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)) }
            val leido = ImagenQrAndroid.leer(contexto.contentResolver, Uri.fromFile(ruta))
            assertEquals(texto, leido)
            assertTrue(runCatching { CodecProtocolo.leerOferta(leido.toByteArray()) }.isFailure)
            ruta.writeBytes(byteArrayOf(1, 2, 3))
            assertTrue(runCatching { ImagenQrAndroid.leer(contexto.contentResolver, Uri.fromFile(ruta)) }.isFailure)
        } finally { bitmap.recycle(); if (ruta.exists()) check(ruta.delete()) }
    }
}
