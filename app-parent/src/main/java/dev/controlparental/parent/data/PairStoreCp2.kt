package dev.controlparental.parent.data

import dev.controlparental.protocol.cp2.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.Base64

@Serializable
internal data class RegistroPairCp2(val schemaVersion: Int = 1, val protocol: String = "cp.v2",
    val etapa: String = "PENDIENTE_CONFIRMACION_HIJO", val legadoSha256B64: String,
    val frameB64: String, val payloadSha256B64: String)

// Documento exclusivamente público. Nunca códigos/privadas ni estado ACTIVO.
internal class PairStoreCp2(private val archivo: ArchivoMetadatos) {
    private val mutex = Mutex()
    private var incierto = false
    private var visto = false
    private val json = Json { encodeDefaults = true }
    suspend fun ocupadaOIncierta(): Boolean = mutex.withLock {
        withContext(Dispatchers.IO) { incierto || visto || archivo.existe() }
    }
    private fun comprobar(r: RegistroPairCp2, publica: String, legado: String): PairVerificadoCp2 {
        check(r.schemaVersion == 1 && r.protocol == "cp.v2" && r.etapa == "PENDIENTE_CONFIRMACION_HIJO")
        check(r.legadoSha256B64 == legado)
        val f = Base64.getUrlDecoder().decode(r.frameB64)
        check(f.size <= VinculacionCp2.MAX_FRAME && VinculacionCp2.b64(f) == r.frameB64)
        return VinculacionCp2.verificar(f, publica).also { check(it.payloadSha256B64 == r.payloadSha256B64) }
    }
    private fun cargar(publica: String, legado: String): RegistroPairCp2? {
        check(!incierto) { "Área CP/2 incierta: no nueva alta" }
        try {
            if (!archivo.existe()) { check(!visto) { "Área CP/2 desaparecida" }; return null }
            visto = true
            val bytes = archivo.leer(); check(bytes.size in 1..32768)
            val r = json.decodeFromString<RegistroPairCp2>(bytes.toString(Charsets.UTF_8))
            check(json.encodeToString(r).toByteArray(Charsets.UTF_8).contentEquals(bytes))
            comprobar(r, publica, legado); return r
        } catch (e: Exception) { incierto = true; throw AlmacenNoDisponible(e) }
    }
    suspend fun leer(publica: String, legado: String): RegistroPairCp2? = mutex.withLock {
        withContext(Dispatchers.IO) { cargar(publica, legado) }
    }
    suspend fun guardarNuevo(d: PairVerificadoCp2, publica: String, legado: String): RegistroPairCp2 = mutex.withLock {
        // Si el caller se cancela en escritura, terminar readback antes de liberar escritor.
        withContext(NonCancellable + Dispatchers.IO) {
            check(cargar(publica, legado) == null) { "Ya existe una aceptación pendiente; no reemplazar" }
            val r = RegistroPairCp2(legadoSha256B64 = legado, frameB64 = VinculacionCp2.b64(d.frame()), payloadSha256B64 = d.payloadSha256B64)
            comprobar(r, publica, legado)
            val bytes = json.encodeToString(r).toByteArray(Charsets.UTF_8)
            try {
                visto = true; archivo.escribir(bytes)
                check(MessageDigest.isEqual(bytes, archivo.leer())) { "Readback CP/2 distinto" }
                check(cargar(publica, legado) == r); r
            } catch (e: Exception) { incierto = true; throw AlmacenNoDisponible(e) }
        }
    }
}
