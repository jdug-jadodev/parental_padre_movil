package dev.controlparental.parent

import android.util.AtomicFile
import androidx.test.platform.app.InstrumentationRegistry
import dev.controlparental.parent.data.*
import dev.controlparental.protocol.cp2.*
import java.io.File
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

// Solo archivo temporal propio noBackup. Compilado, NO ejecutado en PAIR02.
class PairStoreCp2AndroidTest {
    @Test fun atomicFileReadbackReaperturaEInterrupcionConDocumentoPublico() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val directorio = File(contexto.noBackupFilesDir, "cp.test.pair2.${UUID.randomUUID()}")
        check(directorio.mkdir())
        val ruta = File(directorio, "publico.json")
        try {
            val k = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
            val publica = VinculacionCp2.b64(k.public.encoded)
            val id = "00000000-0000-4000-8000-000000000001"
            val h = "A".repeat(43)
            val p = VinculacionCp2.payload(AceptacionCp2(id, id, h, publica, publica, "https://prueba.invalid", RecuperacionCp2(h, h, h)))
            val d = VinculacionCp2.firmar(p, publica) { b ->
                Signature.getInstance("SHA256withECDSA").run { initSign(k.private); update(b); sign() }
            }
            val area = PairStoreCp2(ArchivoAtomicoMetadatos(ruta))
            val r = area.guardarNuevo(d, publica, h)
            assertEquals(r, PairStoreCp2(ArchivoAtomicoMetadatos(ruta)).leer(publica, h))
            assertTrue(ruta.canonicalPath.startsWith(contexto.noBackupFilesDir.canonicalPath + File.separator))
            // Interrupción sintética antes de finishWrite, no corte de energía real.
            AtomicFile(ruta).startWrite().use { it.write(byteArrayOf(1, 2, 3)) }
            assertEquals(r, PairStoreCp2(ArchivoAtomicoMetadatos(ruta)).leer(publica, h))
            ruta.writeBytes(byteArrayOf(0))
            var rechazo = false
            try { PairStoreCp2(ArchivoAtomicoMetadatos(ruta)).leer(publica, h) } catch (_: Exception) { rechazo = true }
            assertTrue(rechazo)
        } finally {
            for (sufijo in listOf("", ".bak", ".new")) {
                val f = File(ruta.path + sufijo)
                if (f.exists()) check(f.delete())
            }
            check(directorio.delete())
        }
    }
}
