package dev.controlparental.parent.data

import android.util.AtomicFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

internal class ArchivoAtomicoMetadatos(ruta: File) : ArchivoMetadatos {
    private val atomico = AtomicFile(ruta)
    override fun existe() = atomico.baseFile.exists() || File("${atomico.baseFile}.bak").exists() ||
        File("${atomico.baseFile}.new").exists()

    override fun leer(): ByteArray = atomico.openRead().use { entrada ->
        val salida = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (true) {
            val cantidad = entrada.read(buffer)
            if (cantidad == -1) break
            require(salida.size() + cantidad <= ParentStore.MAX_BYTES + 36)
            salida.write(buffer, 0, cantidad)
        }
        salida.toByteArray()
    }

    override fun escribir(bytes: ByteArray) {
        require(bytes.size <= ParentStore.MAX_BYTES + 36)
        var escritura: FileOutputStream? = null
        try {
            escritura = atomico.startWrite()
            escritura.write(bytes)
            atomico.finishWrite(escritura)
            escritura = null
            check(MessageDigest.isEqual(bytes, leer())) { "No se pudo confirmar la escritura atómica" }
        } catch (error: Exception) {
            escritura?.let { atomico.failWrite(it) }
            throw error
        }
    }
}
