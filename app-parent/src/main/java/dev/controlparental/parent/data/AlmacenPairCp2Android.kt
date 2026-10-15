package dev.controlparental.parent.data

import android.content.Context
import java.io.File

internal object AlmacenPairCp2Android {
    private var instancia: PairStoreCp2? = null
    @Synchronized fun obtener(c: Context): PairStoreCp2 = instancia ?: PairStoreCp2(
        ArchivoAtomicoMetadatos(File(c.applicationContext.noBackupFilesDir, "pair-cp2-public-v1.json"))
    ).also { instancia = it }
}
