package dev.controlparental.parent

import android.app.KeyguardManager
import android.os.Process
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import dev.controlparental.parent.security.*
import dev.controlparental.protocol.*
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.util.Properties
import java.util.UUID
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

// Opt-in de dos fases: requiere cerrar el proceso desde fuera entre ambas.
// Guarda solo alias de prueba, hash de pública y PID; nunca privadas ni credenciales.
class PersistenciaProcesoTest {
    @Test fun identidadSobreviveYLaSesionNoSobreviveAlProceso() {
        val fase = InstrumentationRegistry.getArguments().getString("persistenciaProceso")
        assumeTrue("Requiere autorización específica para cerrar/reabrir la app",
            fase == "preparar" || fase == "verificar")
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        assumeTrue(contexto.getSystemService(KeyguardManager::class.java).isDeviceSecure)
        val registro = File(contexto.noBackupFilesDir, "cp_test_persistencia.properties")
        val almacen = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (fase == "preparar") {
            check(!registro.exists()) { "Ya existe una prueba pendiente; no sobrescribir su identidad" }
            val alias = "cp.test.persist.${UUID.randomUUID()}"
            try {
                val publica = ParentKeyStore(contexto, alias).crearSiFalta()
                val datos = Properties().apply {
                    setProperty("aliasPrueba", alias)
                    setProperty("hashPublica", hash(publica))
                    setProperty("pidAnterior", Process.myPid().toString())
                }
                registro.outputStream().use { datos.store(it, "Solo prueba temporal de persistencia") }
            } catch (error: Exception) {
                almacen.deleteEntry(alias)
                if (registro.exists()) check(registro.delete())
                throw error
            }
        } else {
            check(registro.exists()) { "Primero ejecutar preparar en el proceso anterior" }
            val datos = Properties().apply { registro.inputStream().use { load(it) } }
            val alias = datos.getProperty("aliasPrueba")
            val prefijo = "cp.test.persist."
            check(alias != null && alias.startsWith(prefijo) &&
                UUID.fromString(alias.removePrefix(prefijo)).toString() == alias.removePrefix(prefijo))
            try {
                assertNotEquals("Debe ser un proceso distinto", datos.getProperty("pidAnterior").toInt(), Process.myPid())
                assertTrue("No recrear una clave ausente para aparentar persistencia", almacen.containsAlias(alias))
                val identidad = ParentKeyStore(contexto, alias)
                // Solo carga: no invoca crearSiFalta durante la verificación.
                assertEquals(datos.getProperty("hashPublica"), hash(identidad.publicaSpki()))
                val sesionNueva = SesionAutoridad(SystemClock::elapsedRealtime)
                sesionNueva.primerPlano()
                val autoridad = AutoridadPadre(identidad, sesionNueva)
                val mensaje = PruebaAutenticacion(
                    "00000000-0000-4000-8000-000000000002", Rol.PARENT,
                    "00000000-0000-4000-8000-000000000010", "A".repeat(43),
                )
                assertThrows(AutenticacionRequerida::class.java) { autoridad.firmar(mensaje, Proposito.AUTH) }
            } finally {
                // Solo el alias temporal validado y este registro de prueba.
                almacen.deleteEntry(alias)
                check(registro.delete()) { "No se pudo retirar el registro temporal" }
            }
        }
    }

    private fun hash(publica: String): String = MessageDigest.getInstance("SHA-256")
        .digest(publica.toByteArray(Charsets.US_ASCII)).joinToString("") { "%02x".format(it) }
}
