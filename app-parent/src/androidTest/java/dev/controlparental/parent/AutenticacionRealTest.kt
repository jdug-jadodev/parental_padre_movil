package dev.controlparental.parent

import android.app.KeyguardManager
import android.os.SystemClock
import android.security.keystore.UserNotAuthenticatedException
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.controlparental.parent.security.*
import dev.controlparental.protocol.*
import java.security.KeyStore
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

// Opt-in: requiere interacción física; nunca inyectar éxito biométrico.
class AutenticacionRealTest {
    @Test fun adultoAutenticaOCancelaConPromptDelSistema() {
        val opcion = InstrumentationRegistry.getArguments().getString("autenticacionReal")
        assumeTrue("Solo ejecutar tras habilitar ADB y acordar la interacción física",
            opcion == "aceptar" || opcion == "cancelar")
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        assumeTrue(contexto.getSystemService(KeyguardManager::class.java).isDeviceSecure)
        val alias = "cp.test.auth.${UUID.randomUUID()}"
        val sesion = SesionAutoridad(SystemClock::elapsedRealtime)
        val almacen = ParentKeyStore(contexto, alias)
        var prompt: LocalAuthentication? = null
        var correcta = false
        val respuesta = CountDownLatch(1)
        val mensaje = PruebaAutenticacion(
            "00000000-0000-4000-8000-000000000002", Rol.PARENT,
            "00000000-0000-4000-8000-000000000010", "A".repeat(43),
        )
        try {
            val publica = almacen.crearSiFalta()
            val autoridad = AutoridadPadre(almacen, sesion)
            ActivityScenario.launch(MainActivity::class.java).use { escenario ->
                escenario.onActivity { activity ->
                    activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onStart(owner: LifecycleOwner) { sesion.primerPlano() }
                        override fun onStop(owner: LifecycleOwner) {
                            sesion.bloquear()
                            prompt?.cancelar()
                            respuesta.countDown()
                        }
                    })
                    sesion.primerPlano()
                    prompt = LocalAuthentication(activity, sesion).also { autenticacion ->
                        autenticacion.solicitar { exito -> correcta = exito; respuesta.countDown() }
                    }
                }
                assertTrue("El adulto no respondió en 120 segundos", respuesta.await(120, TimeUnit.SECONDS))
                if (opcion == "aceptar") {
                    assertTrue("El sistema no confirmó autenticación", correcta)
                    val sobre = autoridad.firmar(mensaje, Proposito.AUTH)
                    val clave = ClaveVerificacion.desdeSpki(Rol.PARENT, publica)
                    assertEquals(mensaje, CriptografiaProtocolo.verificarSobre(
                        CodecProtocolo.codificarFrame(sobre), clave, Proposito.AUTH).mensaje)
                    // El propietario no debe desbloquear/autenticar de nuevo durante esta espera.
                    SystemClock.sleep(61_000)
                    assertThrows(UserNotAuthenticatedException::class.java) { almacen.firmar(byteArrayOf(1)) }
                    assertThrows(AutenticacionRequerida::class.java) { autoridad.firmar(mensaje, Proposito.AUTH) }
                } else {
                    assertFalse("Se esperaba cancelar físicamente el prompt", correcta)
                    assertThrows(AutenticacionRequerida::class.java) { autoridad.firmar(mensaje, Proposito.AUTH) }
                }
                sesion.bloquear()
                assertThrows(AutenticacionRequerida::class.java) { autoridad.firmar(mensaje, Proposito.AUTH) }
                assertEquals(publica, ParentKeyStore(contexto, alias).publicaSpki())
                escenario.onActivity { prompt?.cancelar() }
            }
        } finally {
            sesion.bloquear()
            KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
        }
    }
}
