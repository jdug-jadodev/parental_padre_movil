package dev.controlparental.parent

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.controlparental.parent.presentation.ParentViewModel
import dev.controlparental.parent.ui.AplicacionPadre
import dev.controlparental.parent.ui.IdentidadPadre
import dev.controlparental.parent.security.*

class MainActivity : ComponentActivity() {
    private val sesion = SesionAutoridad(SystemClock::elapsedRealtime)
    private lateinit var autoridad: AutoridadPadre
    private lateinit var autenticacion: LocalAuthentication
    private var aviso by mutableStateOf("Identidad sin comprobar. Crear o cargar exige bloqueo seguro del sistema.")
    private var publica by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        autoridad = AutoridadPadre(ParentKeyStore(applicationContext), sesion)
        autenticacion = LocalAuthentication(this, sesion)
        val modelo = ViewModelProvider(this, AppContainer().fabrica)[ParentViewModel::class.java]
        setContent {
            val estado by modelo.estado.collectAsStateWithLifecycle()
            MaterialTheme {
                AplicacionPadre(estado, modelo::navegar, modelo::volver) {
                    IdentidadPadre(aviso, publica, ::crearIdentidad, ::autenticar)
                }
            }
        }
    }

    private fun crearIdentidad() {
        autenticacion.cancelar()
        try {
            publica = autoridad.crearIdentidad()
            aviso = "Identidad protegida disponible. Autentica al adulto antes de firmar; aún no hay órdenes operativas."
        } catch (_: Exception) {
            publica = null
            aviso = "No se pudo cargar la identidad. Comprueba el PIN, patrón o contraseña del sistema. Una clave dañada requiere recuperación presencial; no se reemplaza automáticamente."
        }
    }

    private fun autenticar() {
        aviso = "Autenticación del sistema en curso."
        autenticacion.solicitar { correcta ->
            aviso = if (correcta) "Última autenticación completada. Se comprobará su vigencia de 60 segundos y el permiso de Keystore antes de cada firma."
                else "Autenticación cancelada o no disponible. La autoridad permanece bloqueada."
        }
    }

    override fun onStart() {
        super.onStart()
        sesion.primerPlano()
    }

    override fun onStop() {
        sesion.bloquear()
        autenticacion.cancelar()
        aviso = "Autoridad bloqueada al pasar a segundo plano. Autentica nuevamente antes de firmar."
        super.onStop()
    }
}
