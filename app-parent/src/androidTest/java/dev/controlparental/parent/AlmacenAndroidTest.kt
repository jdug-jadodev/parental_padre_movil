package dev.controlparental.parent

import android.util.AtomicFile
import androidx.test.platform.app.InstrumentationRegistry
import dev.controlparental.parent.data.*
import dev.controlparental.parent.security.*
import dev.controlparental.protocol.Accion
import java.io.File
import java.security.KeyStore
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

// Solo directorio/alias temporales propios; no modifica identidad ni vínculo operativos.
class AlmacenAndroidTest {
    private fun datos(): MetadatosPadre {
        val contexto = InstrumentationRegistry.getInstrumentation().context
        val claves = contexto.assets.open("vectores_crypto.json").use {
            Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject.getValue("publicKeys").jsonObject
        }
        return MetadatosPadre(parentPublicKeyB64 = claves.getValue("PARENT").jsonPrimitive.content,
            pairId = "00000000-0000-4000-8000-000000000002", childPublicKeyB64 = claves.getValue("CHILD").jsonPrimitive.content,
            relayOrigin = "https://prueba.invalid", vinculo = VinculoPersistido.VINCULADO)
    }

    private fun temporal(prueba: suspend (File, MetadataKeyStore, CoroutineScope) -> Unit) = runBlocking {
        val id = UUID.randomUUID().toString()
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val directorio = File(contexto.noBackupFilesDir, "cp.test.store.$id")
        check(directorio.mkdir())
        val ruta = File(directorio, "estado.enc")
        val alias = "cp.test.metadata.$id"
        val claves = MetadataKeyStore(alias)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try { prueba(ruta, claves, scope) }
        finally {
            scope.cancel()
            KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
            for (sufijo in listOf("", ".bak", ".new")) {
                val archivo = File(ruta.path + sufijo)
                if (archivo.exists()) check(archivo.delete())
            }
            check(directorio.delete())
        }
    }

    private fun abrir(ruta: File, claves: MetadataKeyStore, scope: CoroutineScope) =
        ParentStore(ArchivoAtomicoMetadatos(ruta), CifradoMetadatos(claves::preparar, claves::cargar), scope)

    @Test fun cifradoRealNonceNuevoYReservaPersistenteSinTextoClaro() = temporal { ruta, claves, scope ->
        val store = abrir(ruta, claves, scope)
        store.inicializar(datos())
        val inicial = ruta.readBytes()
        val reserva = store.reservarIntencion(Accion.LOCK, null, "5")
        val reservado = ruta.readBytes()
        assertFalse(inicial.copyOfRange(8, 20).contentEquals(reservado.copyOfRange(8, 20)))
        assertFalse(reservado.toString(Charsets.UTF_8).contains("prueba.invalid"))
        assertNull(claves.cargar().encoded)
        store.cerrar()
        val reabierto = abrir(ruta, claves, scope)
        assertEquals(reserva, reabierto.leer().pendiente)
        reabierto.cancelarIntencion(reserva.commandId)
        assertEquals("6", reabierto.leer().contadorLocal)
        reabierto.cerrar()
    }

    @Test fun escrituraInterrumpidaNoSustituyeElDocumentoAnterior() = temporal { ruta, claves, scope ->
        val store = abrir(ruta, claves, scope)
        store.inicializar(datos())
        val reserva = store.reservarIntencion(Accion.LOCK, null, "0")
        store.cerrar()
        // Simula .new sin finishWrite, no un corte físico de alimentación.
        AtomicFile(ruta).startWrite().use { it.write(byteArrayOf(1, 2, 3)) }
        val recuperado = abrir(ruta, claves, scope)
        assertEquals(reserva, recuperado.leer().pendiente)
        assertEquals("1", recuperado.leer().contadorLocal)
        recuperado.cerrar()
    }

    @Test fun corrupcionCifradaNoReabreNiSobrescribeElAlmacen() = temporal { ruta, claves, scope ->
        val store = abrir(ruta, claves, scope)
        store.inicializar(datos())
        store.cerrar()
        val alterado = ruta.readBytes().apply { this[lastIndex] = (this[lastIndex].toInt() xor 1).toByte() }
        ruta.writeBytes(alterado)
        val recuperado = abrir(ruta, claves, scope)
        assertTrue(runCatching { recuperado.leer() }.exceptionOrNull() is AlmacenNoDisponible)
        assertTrue(runCatching { recuperado.inicializar(datos()) }.isFailure)
        assertArrayEquals(alterado, ruta.readBytes())
        recuperado.cerrar()
    }
}
