package dev.controlparental.parent.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.controlparental.parent.data.*
import dev.controlparental.parent.pairing.ImagenQrAndroid
import dev.controlparental.parent.recovery.*
import dev.controlparental.protocol.AccionOffline
import kotlinx.coroutines.*

@Composable
internal fun RecoveryScreen(flujo: OfflineChallengeSigner, store: ParentStore, autenticar: () -> Unit) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    var texto by remember { mutableStateOf("") }
    var preparada by remember { mutableStateOf<PreparacionOffline?>(null) }
    var respuesta by remember { mutableStateOf<String?>(null) }
    var retiro by remember { mutableStateOf(false) }
    var conocida by remember { mutableStateOf(false) }
    var ocupado by remember { mutableStateOf(false) }
    var revision by remember { mutableIntStateOf(0) }
    var trabajo by remember { mutableStateOf<Job?>(null) }
    var aviso by remember { mutableStateOf("QR presencial firmado del hijo vinculado; no permite uso normal sin internet. No se envía al relay.") }
    fun descartar() { revision++; flujo.descartar(); preparada = null; respuesta = null; retiro = false }
    fun ejecutar(operacion: suspend () -> Unit) {
        if (ocupado) return
        ocupado = true
        trabajo = scope.launch {
            try { operacion() }
            catch (cancelacion: CancellationException) { throw cancelacion }
            catch (_: Exception) {
                descartar()
                aviso = "Desafío o firma rechazados. Comprueba pareja, QR vigente e identidad y autentica de nuevo. No se reemplaza la clave ni se abre uso normal."
            } finally { ocupado = false }
        }
    }
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) { descartar(); texto = ""; trabajo?.cancel() }
        }
        ciclo.addObserver(observador)
        onDispose { descartar(); trabajo?.cancel(); ciclo.removeObserver(observador) }
    }
    LaunchedEffect(store) {
        conocida = runCatching { store.leer().let { it.pairId != null && it.childPublicKeyB64 != null } }.getOrDefault(false)
    }
    LaunchedEffect(preparada) {
        preparada?.let { delay(flujo.tiempoRestante(it)); descartar() }
    }
    LaunchedEffect(respuesta) {
        if (respuesta != null) while (true) {
            if (runCatching { flujo.exigirPresentacion() }.isFailure) { descartar(); break }
            delay(500)
        }
    }
    val selector = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) ejecutar {
            val turno = revision
            val nuevo = withContext(Dispatchers.IO) { ImagenQrAndroid.leer(contexto.contentResolver, uri) }
            if (turno == revision && ciclo.currentState.isAtLeast(Lifecycle.State.STARTED)) { descartar(); texto = nuevo }
        }
    }
    Text("Autorización presencial sin internet", style = MaterialTheme.typography.titleMedium)
    Text(aviso)
    if (!conocida) Text("Sin pareja guardada verificable. Usa el código físico en el hijo para recuperar el padre; no se crea identidad alternativa aquí.")
    if (!ocupado && respuesta == null) CamaraRetoOffline({ nuevo -> descartar(); texto = nuevo }, conocida)
    OutlinedButton(enabled = conocida && !ocupado && respuesta == null, onClick = {
        selector.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }) { Text("Leer imagen del desafío firmado") }
    OutlinedTextField(texto, { if (it.length <= 16384) { descartar(); texto = it } },
        enabled = conocida && !ocupado && respuesta == null, label = { Text("Desafío firmado (alternativa local)") },
        modifier = Modifier.fillMaxWidth().testTag("reto_offline"))
    Button(enabled = conocida && !ocupado && texto.isNotEmpty() && respuesta == null, onClick = {
        ejecutar {
            descartar(); val turno = revision
            val nueva = flujo.preparar(texto.toByteArray(Charsets.UTF_8))
            if (turno == revision && ciclo.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                preparada = nueva
                aviso = "Firma y pareja verificadas; confirma que el hijo sigue mostrando ese desafío. La vigencia y consumo final los comprueba el hijo."
            } else flujo.descartar()
        }
    }) { Text("Verificar desafío del hijo") }
    preparada?.let { p ->
        val esRetiro = p.reto.action == AccionOffline.RETIRE
        Text(if (esRetiro) "RETIRE: autoriza retirada presencial. Puede restablecer el hijo y borrar sus datos; no es un desbloqueo."
            else "MAINTENANCE: solo herramientas propias de red/actualización durante cinco minutos. No habilita uso normal ni ALLOW.")
        if (esRetiro) Row {
            Checkbox(checked = retiro, onCheckedChange = { retiro = it }, enabled = !ocupado && respuesta == null,
                modifier = Modifier.testTag("retiro_confirmado"))
            Text("Comprendo el posible borrado y autorizaré la retirada presencial; el hijo exigirá otra confirmación y BORRAR.")
        }
        OutlinedButton(enabled = !ocupado && respuesta == null, onClick = autenticar) { Text("Autenticar para respuesta offline") }
        Button(enabled = !ocupado && respuesta == null && (!esRetiro || retiro), onClick = {
            ejecutar {
                val turno = revision
                val frame = flujo.responder(p, retiro)
                if (turno == revision && ciclo.currentState.isAtLeast(Lifecycle.State.STARTED)) respuesta = frame.toString(Charsets.UTF_8)
            }
        }) { Text("Firmar respuesta offline y mostrar QR") }
    }
    respuesta?.let { frame ->
        val bitmap = remember(frame) { ImagenQrAndroid.generar(frame) }
        DisposableEffect(bitmap) { onDispose { bitmap.recycle() } }
        Image(bitmap.asImageBitmap(), "QR de respuesta offline para el hijo", Modifier.fillMaxWidth().aspectRatio(1f))
        Text("Muestra este QR solo al hijo presente. No prueba función aplicada. Reinicio, consumo o vencimiento lo invalidan en el hijo; RETIRE todavía exige confirmar BORRAR allí.")
    }
    OutlinedButton(enabled = !ocupado, onClick = { descartar(); texto = "" }) { Text("Descartar desafío y respuesta") }
}
