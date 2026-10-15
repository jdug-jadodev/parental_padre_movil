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
import dev.controlparental.parent.ui.PairingScreen
import dev.controlparental.parent.ui.OfertaCp2Screen
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import dev.controlparental.parent.pairing.PairChild
import dev.controlparental.parent.data.AlmacenPadreAndroid
import dev.controlparental.protocol.Proposito
import dev.controlparental.parent.security.*
import dev.controlparental.parent.network.*
import dev.controlparental.parent.ui.ConexionRelayScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import dev.controlparental.parent.policy.ControlPoliticas
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import dev.controlparental.parent.presentation.AccionesControlReal
import dev.controlparental.parent.ui.ControlScreen
import dev.controlparental.parent.ui.RecoveryScreen
import dev.controlparental.parent.recovery.OfflineChallengeSigner

class MainActivity : ComponentActivity() {
    private val sesion = SesionAutoridad(SystemClock::elapsedRealtime)
    private lateinit var autoridad: AutoridadPadre
    private lateinit var autenticacion: LocalAuthentication
    private lateinit var transporte: TransporteOkHttp
    private lateinit var conexion: RelayConnection
    private lateinit var red: RedAndroid
    private lateinit var politicas: ControlPoliticas
    private var aviso by mutableStateOf("Identidad sin comprobar. Crear o cargar exige bloqueo seguro del sistema.")
    private var publica by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        autoridad = AutoridadPadre(ParentKeyStore(applicationContext), sesion)
        autenticacion = LocalAuthentication(this, sesion)
        transporte = TransporteOkHttp()
        red = RedAndroid(applicationContext)
        conexion = RelayConnection(transporte, SystemClock::elapsedRealtime, sesion::exigirVigencia,
            { autoridad.firmar(it, Proposito.AUTH) })
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        val store = AlmacenPadreAndroid.obtener(applicationContext)
        politicas = ControlPoliticas(store, CanalPoliticasRelay(conexion) { red.validada.value },
            SystemClock::elapsedRealtime, sesion::exigirVigencia, { autoridad.firmar(it, Proposito.MESSAGE) })
        val accionesControl = AccionesControlReal(store, politicas)
        val recuperacion = OfflineChallengeSigner(store, SystemClock::elapsedRealtime, sesion::exigirVigencia,
            { autoridad.firmar(it, Proposito.OFFLINE) })
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(conexion.estado, red.validada) { _, validada ->
                        if (validada) conexion.sesionPoliticas() else null
                    }.distinctUntilChanged().collect { ticket ->
                        if (ticket != null) {
                            try { politicas.sincronizar() }
                            catch (cancelacion: CancellationException) { throw cancelacion }
                            catch (_: Exception) { /* Solicitud manual disponible sin bajar barreras. */ }
                        }
                    }
                }
                launch {
                    conexion.mensajesHijo.collect { trama ->
                        try { politicas.recibir(trama.ticket, trama.bytes()) }
                        catch (cancelacion: CancellationException) { throw cancelacion }
                        catch (_: Exception) { /* Sin evidencia aceptada; no confirmar por fallo. */ }
                    }
                }
                while (true) {
                    conexion.avanzarReloj()
                    try { politicas.avanzarReloj() }
                    catch (cancelacion: CancellationException) { throw cancelacion }
                    catch (_: Exception) { /* Pendiente conservada; no firmar por vías alternativas. */ }
                    delay(250)
                }
            }
        }
        val vinculacion = PairChild(store, autoridad::exportarPublica, { autoridad.firmar(it, Proposito.PAIR) })
        val areaCp2 = dev.controlparental.parent.data.AlmacenPairCp2Android.obtener(applicationContext)
        val vincularCp2 = dev.controlparental.parent.pairing.VincularCp2(store, areaCp2, autoridad::exportarPublica,
            sesion::exigirVigencia, autoridad::firmarPairCp2, SystemClock::elapsedRealtime)
        val modelo = ViewModelProvider(this, AppContainer().fabrica)[ParentViewModel::class.java]
        setContent {
            val estado by modelo.estado.collectAsStateWithLifecycle()
            val canal by conexion.estado.collectAsStateWithLifecycle()
            val redValidada by red.validada.collectAsStateWithLifecycle()
            val relayUi = when (canal.fase) {
                FaseRelay.CONECTANDO -> dev.controlparental.parent.presentation.ConexionRelay.CONECTANDO
                FaseRelay.ESPERANDO_RETO, FaseRelay.ESPERANDO_AUTH_OK -> dev.controlparental.parent.presentation.ConexionRelay.AUTENTICANDO
                FaseRelay.AUTENTICADO -> dev.controlparental.parent.presentation.ConexionRelay.CONECTADO
                FaseRelay.ERROR_SEGURO -> dev.controlparental.parent.presentation.ConexionRelay.ERROR
                else -> dev.controlparental.parent.presentation.ConexionRelay.DESCONECTADO
            }
            val presenciaUi = when (canal.hijoIndicativo) {
                true -> dev.controlparental.parent.presentation.PresenciaHijo.PRESENTE
                false -> dev.controlparental.parent.presentation.PresenciaHijo.AUSENTE
                null -> dev.controlparental.parent.presentation.PresenciaHijo.DESCONOCIDA
            }
            val presentacion = estado.copy(datos = estado.datos.copy(relay = relayUi, presencia = presenciaUi))
            var cp2 by remember { mutableStateOf(false) }
            var legadoPermitido by remember { mutableStateOf(false) }
            LaunchedEffect(cp2) { legadoPermitido = !areaCp2.ocupadaOIncierta() }
            MaterialTheme {
                AplicacionPadre(presentacion, modelo::navegar, modelo::volver,
                    identidad = { if (!cp2 && legadoPermitido) IdentidadPadre(aviso, publica, ::crearIdentidad, ::autenticar) },
                    vinculacion = {
                        androidx.compose.material3.TextButton(onClick = {
                            autenticacion.cancelar(); vincularCp2.descartar(); legadoPermitido = false; cp2 = !cp2
                        }) {
                            androidx.compose.material3.Text(if (cp2) "Volver al perfil legado CP/1" else "Abrir NUEVO PERFIL CP/2 (sin migrar)")
                        }
                        if (cp2) OfertaCp2Screen(vincularCp2, { resultado -> autenticacion.solicitar(resultado) }, SystemClock::elapsedRealtime)
                        else if (legadoPermitido) PairingScreen(vinculacion, store, ::autenticar)
                        else androidx.compose.material3.Text("Área CP/2 pendiente o incierta: nueva preparación CP/1 bloqueada; datos legados conservados.")
                    },
                    conexion = { ConexionRelayScreen(conexion, store, redValidada, ::autenticar, politicas) },
                    controles = { ControlScreen(accionesControl, ::autenticar,
                        redValidada && canal.fase == FaseRelay.AUTENTICADO && canal.canalSano) },
                    recuperacion = { RecoveryScreen(recuperacion, store, ::autenticar) })
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
        red.iniciar()
        conexion.primerPlano()
    }

    override fun onStop() {
        sesion.bloquear()
        conexion.segundoPlano()
        red.detener()
        autenticacion.cancelar()
        aviso = "Autoridad bloqueada al pasar a segundo plano. Autentica nuevamente antes de firmar."
        super.onStop()
    }
    override fun onDestroy() {
        conexion.desconectar()
        transporte.liberar()
        super.onDestroy()
    }
}
