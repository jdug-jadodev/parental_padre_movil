package dev.controlparental.parent.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.controlparental.parent.data.*
import dev.controlparental.parent.network.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import dev.controlparental.parent.policy.*

@Composable
internal fun ConexionRelayScreen(conexion: RelayConnection, store: ParentStore, redValidada: Boolean,
    autenticar: () -> Unit, politicas: ControlPoliticas) {
    val estado by conexion.estado.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val resultado by politicas.resultado.collectAsStateWithLifecycle()
    var aviso by remember { mutableStateOf("Conexión explícita con vínculo guardado. La integración con servidor e hijo reales aún no está demostrada.") }
    DisposableEffect(conexion) { onDispose { conexion.desconectar() } }
    Text("Canal del padre", style = MaterialTheme.typography.titleMedium)
    Text(if (redValidada) "Red validada por Android; no demuestra acceso al relay"
        else "Red no validada por Android; no permite presentar funcionamiento normal")
    Text(if (estado.canalSano) "Latido vigente del relay; no demuestra estado ni restricciones del hijo"
        else "Sin latido vigente del relay")
    Text(when (estado.fase) {
        FaseRelay.DESCONECTADO -> "Relay: desconectado"
        FaseRelay.CONECTANDO -> "Relay: abriendo canal TLS"
        FaseRelay.ESPERANDO_RETO -> "Relay: esperando reto, no autenticado"
        FaseRelay.ESPERANDO_AUTH_OK -> "Relay: firma enviada, esperando autenticación"
        FaseRelay.AUTENTICADO -> "Relay: canal autenticado; no acredita estado del hijo ni órdenes aplicadas"
        FaseRelay.REINTENTO -> "Relay: desconectado, próximo reintento limitado"
        FaseRelay.AUTENTICACION_REQUERIDA -> "Autentica nuevamente al adulto o revisa identidad/recuperación antes de conectar"
        FaseRelay.ERROR_SEGURO -> "Conexión detenida por error TLS/protocolo/configuración; sin atajos de seguridad"
    })
    Text("Presencia del hijo: ${when (estado.hijoIndicativo) { true -> "indicada por relay (no estado verificado)"; false -> "no disponible según relay"; null -> "desconocida" }}")
    Text(aviso)
    OutlinedButton(onClick = autenticar) { Text("Autenticar para conectar") }
    Button(onClick = {
        scope.launch {
            try {
                val datos = store.leer()
                check(datos.vinculo == VinculoPersistido.PREPARADO || datos.vinculo == VinculoPersistido.VINCULADO)
                conexion.conectar(ConfiguracionRelay(datos.pairId!!, datos.relayOrigin!!, datos.parentPublicKeyB64))
            } catch (cancelacion: CancellationException) { throw cancelacion }
            catch (_: Exception) { aviso = "Sin vínculo válido disponible. No se crea identidad ni pareja para conectarse; usa vinculación/recuperación presencial." }
        }
    }) { Text("Conectar con vínculo guardado") }
    OutlinedButton(onClick = conexion::desconectar) { Text("Desconectar relay") }
    OutlinedButton(onClick = {
        scope.launch {
            try { politicas.sincronizar(); aviso = "Solicitud firmada enviada; esperando STATE verificable del hijo." }
            catch (cancelacion: CancellationException) { throw cancelacion }
            catch (_: Exception) { aviso = "No se pudo sincronizar. Revisa autoridad, vínculo, red validada y latido del canal; no se crean órdenes." }
        }
    }) { Text("Solicitar estado firmado del hijo") }
    Text("Sincronización: ${when (resultado.fase) {
        FasePolitica.SIN_ESTADO -> "sin estado fresco"
        FasePolitica.ESPERANDO_ESTADO -> "esperando respuesta firmada del hijo"
        FasePolitica.ESTADO_VERIFICADO -> "estado verificado"
        FasePolitica.ENVIADA_SIN_CONFIRMAR -> "orden sin confirmar"
        FasePolitica.PENDIENTE_APLICACION -> "aceptada, aplicación pendiente"
        FasePolitica.APLICADA -> "aplicada según evidencia del hijo"
        FasePolitica.FALLO_APLICACION -> "fallo de aplicación"
        FasePolitica.RECHAZADA -> "orden rechazada"
        FasePolitica.REQUIERE_CONFIRMACION -> "revisa la intención pendiente y confirma de nuevo"
    }}. No acredita recuperación local ni habilita órdenes automáticamente.")
}
