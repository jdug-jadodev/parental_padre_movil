package dev.controlparental.parent.data

import android.content.Context
import dev.controlparental.parent.security.CifradoMetadatos
import dev.controlparental.parent.security.MetadataKeyStore
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Un archivo y un escritor por proceso. Obtenerlo no inicializa identidad/vínculo.
internal object AlmacenPadreAndroid {
    private var instancia: ParentStore? = null
    @Synchronized fun obtener(contexto: Context): ParentStore = instancia ?: run {
        val claves = MetadataKeyStore()
        ParentStore(ArchivoAtomicoMetadatos(File(contexto.applicationContext.noBackupFilesDir, "parent-state.enc")),
            CifradoMetadatos(claves::preparar, claves::cargar),
            CoroutineScope(SupervisorJob() + Dispatchers.IO)).also { instancia = it }
    }
}
