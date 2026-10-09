package dev.controlparental.parent.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import dev.controlparental.parent.data.*
import dev.controlparental.parent.policy.*
import dev.controlparental.parent.presentation.*
import dev.controlparental.protocol.Accion
import kotlinx.coroutines.*

@Composable
internal fun ControlScreen(acciones: AccionesControl, autenticar: () -> Unit, canalDisponible: Boolean = true) {
    val ciclo = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    var base by remember { mutableStateOf<VistaPoliticas?>(null) }
    var disponible by remember { mutableStateOf(false) }
    var elegido by remember { mutableStateOf<Accion?>(null) }
    var duracion by remember { mutableStateOf("60") }
    var ocupado by remember { mutableStateOf(false) }
    var activo by remember { mutableStateOf(ciclo.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    var trabajo by remember { mutableStateOf<Job?>(null) }
    var confirmacion by remember { mutableStateOf<ConfirmacionIntencion?>(null) }
    var reemplazar by remember { mutableStateOf(false) }
    var descartar by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf("Seleccionar o guardar no envía una orden. Autentica y revisa antes de confirmar.") }

    suspend fun actualizar() {
        try {
            val nueva = acciones.vista()
            if (activo) { base = nueva; disponible = true }
        } catch (cancelacion: CancellationException) { throw cancelacion }
        catch (_: Exception) {
            disponible = false; base = base?.copy(fresca = false, puedeEnviar = false)
        }
    }
    fun ejecutar(operacion: suspend () -> Unit) {
        if (ocupado || !activo) return
        ocupado = true // Guardia síncrona, antes de lanzar coroutine.
        trabajo = scope.launch {
            try { operacion(); actualizar() }
            catch (cancelacion: CancellationException) { throw cancelacion }
            catch (_: Exception) {
                confirmacion = null
                aviso = "Operación rechazada o sin confirmar. Autentica, sincroniza y revisa la intención guardada; no se sustituye identidad ni vínculo."
                actualizar()
            } finally { ocupado = false }
        }
    }
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_START) activo = true
            if (evento == Lifecycle.Event.ON_STOP) {
                activo = false; confirmacion = null; reemplazar = false; descartar = false; elegido = null
                disponible = false; base = base?.copy(fresca = false, puedeEnviar = false)
                trabajo?.cancel()
            }
        }
        ciclo.addObserver(observador)
        onDispose { activo = false; trabajo?.cancel(); ciclo.removeObserver(observador) }
    }
    LaunchedEffect(acciones, ciclo) {
        ciclo.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) { actualizar(); delay(500) }
        }
    }

    Text("Controles del hijo", style = MaterialTheme.typography.headlineSmall)
    // La señal reactiva del canal degrada evidencia inmediatamente, sin esperar al sondeo.
    val vista = base?.let { if (canalDisponible) it else it.copy(fresca = false, puedeEnviar = false) }
    val datos = vista?.datos
    Text(when (datos?.vinculo) {
        VinculoPersistido.VINCULADO -> "Vinculación: guardada; las órdenes exigen contexto válido y confirmación"
        VinculoPersistido.PREPARADO -> "Vinculación preparada, no operativa: falta comprobar recuperación presencial y vínculo del hijo"
        VinculoPersistido.REQUIERE_RECUPERACION -> "Vinculación: requiere recuperación presencial"
        else -> "Vinculación: pendiente"
    })
    if (!disponible) Text("Almacén o vínculo no disponible. Puedes abrir Recuperación; no se crea ni reemplaza identidad.")
    (vista?.let(::resumenControl) ?: listOf("Estado del hijo: sin evidencia verificada")).forEach { Text(it) }
    Text(aviso, modifier = Modifier.testTag("aviso_control"))
    OutlinedButton(onClick = autenticar, enabled = !ocupado) { Text("Autenticar para revisar la orden") }
    val habilitado = disponible && datos?.vinculo == VinculoPersistido.VINCULADO && datos.pendiente == null && !ocupado
    for (accion in Accion.entries) {
        Button(onClick = { elegido = accion }, enabled = habilitado,
            modifier = Modifier.fillMaxWidth().testTag("accion_${accion.name}")) { Text(nombreAccion(accion)) }
    }
    elegido?.let { accion ->
        Text("Seleccionado: ${nombreAccion(accion)}")
        Text(avisoAccion(accion))
        val temporal = accion == Accion.LOCK_FOR || accion == Accion.ALLOW_FOR
        if (temporal) OutlinedTextField(value = duracion, onValueChange = { duracion = it },
            label = { Text("Duración en segundos (60–604800)") }, singleLine = true,
            isError = duracionControl(duracion) == null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("duracion_control"))
        Text("El tiempo empieza con la aceptación en el hijo, no al guardar ni al pulsar enviar.")
        Button(enabled = habilitado && (!temporal || duracionControl(duracion) != null), onClick = {
            if (datos?.intencionLocal != null) reemplazar = true
            else ejecutar {
                acciones.guardar(accion, if (temporal) duracionControl(duracion) else null, null)
                elegido = null; aviso = "Intención pendiente local guardada; todavía no enviada."
            }
        }) { Text("Guardar intención local") }
    }

    datos?.intencionLocal?.let { local ->
        Text("Intención pendiente local: ${nombreAccion(local.action)}${local.durationSec?.let { " $it s" } ?: ""}. Todavía no enviada.")
        Text("Hay una intención pendiente. ¿Enviar ahora a este dispositivo? Se pedirá confirmación explícita.")
        Button(enabled = disponible && vista?.puedeEnviar == true && !ocupado, onClick = {
            ejecutar { confirmacion = acciones.preparar(local) }
        }) { Text("Revisar y enviar intención") }
        if (vista?.puedeEnviar != true) Text("Para enviar: autentica, conecta y solicita estado firmado fresco. No se enviará al reconectar sin tu confirmación.")
        OutlinedButton(enabled = disponible && !ocupado, onClick = { descartar = true }) { Text("Descartar intención local") }
    }
    datos?.pendiente?.let { pendiente ->
        Text("Orden pendiente: ${nombreAccion(pendiente.action)}; ${when (pendiente.etapa) {
            EtapaIntencion.RESERVADA -> "reserva local, sin comando firmado"
            EtapaIntencion.FIRMADA -> "comando guardado, entrega no confirmada"
            EtapaIntencion.ENVIADA_SIN_CONFIRMAR -> "envío o aplicación sin confirmar"
        }}")
        Text("Antes de cambiarla, consulta estado firmado. No repitas la duración por falta de ACK.")
        OutlinedButton(enabled = disponible && vista?.fresca == true && vista?.evidencia is dev.controlparental.protocol.EstadoDispositivo && !ocupado,
            onClick = { descartar = true }) { Text("Descartar solo pendiente local") }
    }
    if (!disponible) Text("Intención pendiente: estado local no disponible; no se asume que esté vacía")
    else if (datos?.intencionLocal == null && datos?.pendiente == null) Text("Intención pendiente: ninguna")

    if (reemplazar) AlertDialog(onDismissRequest = { reemplazar = false }, title = { Text("¿Reemplazar intención local?") },
        text = { Text("Se descarta la selección anterior sin enviar ninguna orden. Solo se guardará una intención.") },
        confirmButton = { TextButton(enabled = !ocupado, onClick = {
            val accion = elegido ?: return@TextButton
            val local = datos?.intencionLocal ?: return@TextButton
            reemplazar = false
            ejecutar {
                acciones.guardar(accion, if (accion == Accion.LOCK_FOR || accion == Accion.ALLOW_FOR) duracionControl(duracion) else null, local.id)
                elegido = null; aviso = "Selección local reemplazada; no enviada."
            }
        }) { Text("Reemplazar selección") } }, dismissButton = { TextButton(onClick = { reemplazar = false }) { Text("Volver sin reemplazar") } })

    confirmacion?.let { ticket ->
        AlertDialog(onDismissRequest = { confirmacion = null; ejecutar { acciones.cerrarConfirmacion() } },
            title = { Text("Confirmar envío a este dispositivo") }, text = {
                Text("${nombreAccion(ticket.action)}${ticket.durationSec?.let { " $it s" } ?: ""}. ${avisoAccion(ticket.action)} El envío no demuestra aplicación.")
            }, confirmButton = { TextButton(enabled = disponible && vista?.puedeEnviar == true && !ocupado, onClick = {
                confirmacion = null
                ejecutar { acciones.enviar(ticket); aviso = "Orden guardada para envío; entrega y aplicación requieren confirmación firmada del hijo." }
            }, modifier = Modifier.testTag("confirmar_orden")) { Text("Confirmar y enviar") } },
            dismissButton = { TextButton(onClick = { confirmacion = null; ejecutar { acciones.cerrarConfirmacion() } }) { Text("Volver sin enviar") } })
    }
    if (descartar) AlertDialog(onDismissRequest = { descartar = false }, title = { Text("¿Descartar solo la intención local?") },
        text = { Text("Esto NO revoca una orden que el hijo ya recibió. No se asegura una cancelación remota. Para cambiar una orden aceptada hace falta otra orden confirmada con estado nuevo.") },
        confirmButton = { TextButton(enabled = !ocupado, onClick = {
            descartar = false
            ejecutar {
                datos?.intencionLocal?.let { acciones.descartarLocal(it.id) }
                    ?: datos?.pendiente?.let { acciones.descartarOrden(it.commandId) }
                aviso = "Solo pendiente local descartada; ninguna orden remota revocada. Sincroniza antes de una nueva intención."
            }
        }) { Text("Descartar solo selección local") } }, dismissButton = { TextButton(onClick = { descartar = false }) { Text("Conservar pendiente") } })
}
