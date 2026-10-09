package dev.controlparental.parent.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.controlparental.parent.data.*
import dev.controlparental.parent.pairing.*
import java.util.Base64
import kotlinx.coroutines.*

@Composable
internal fun PairingScreen(flujo: PairChild, store: ParentStore, autenticar: () -> Unit) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    var oferta by remember { mutableStateOf("") }
    var origen by remember { mutableStateOf("") }
    var preparacion by remember { mutableStateOf<PreparacionVinculo?>(null) }
    var frame by remember { mutableStateOf<String?>(null) }
    var env by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf("Lee una imagen QR CHILD_OFFER local. No es el QR de aprovisionamiento Android.") }
    var ocupado by remember { mutableStateOf(false) }
    var comparadas by remember { mutableStateOf(false) }
    var guardados by remember { mutableStateOf(false) }
    var bloqueado by remember { mutableStateOf(false) }
    var generacion by remember { mutableIntStateOf(0) }

    fun descartar() { preparacion?.codigos?.descartar(); preparacion = null; comparadas = false; guardados = false }
    fun ejecutar(operacion: suspend () -> Unit) {
        scope.launch {
            ocupado = true
            try { operacion() }
            catch (cancelacion: CancellationException) { throw cancelacion }
            catch (_: Exception) { aviso = "Operación rechazada. Comprueba oferta/origen e identidad y autentica de nuevo. Un almacén dañado o vínculo existente requiere recuperación; no se reemplaza." }
            finally { ocupado = false }
        }
    }
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) { generacion++; descartar() }
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador); descartar() }
    }
    LaunchedEffect(flujo) {
        try {
            if (store.existe()) {
                val datos = store.leer()
                bloqueado = datos.vinculo != VinculoPersistido.SIN_VINCULAR
                if (bloqueado) {
                    frame = datos.pairAcceptanceFrameB64?.let { Base64.getUrlDecoder().decode(it).toString(Charsets.UTF_8) }
                    env = flujo.exportarEnvPublica()
                    aviso = "Vínculo preparado o existente. No se cambian claves aquí. No se da por completado sin estado firmado del hijo y prueba de recuperación."
                }
            }
        } catch (_: Exception) { bloqueado = true; aviso = "Almacén no disponible: requiere recuperación presencial, no nueva vinculación." }
    }
    val selector = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) ejecutar {
            val texto = withContext(Dispatchers.IO) { ImagenQrAndroid.leer(contexto.contentResolver, uri) }
            oferta = texto
            aviso = "QR leído localmente. Comprueba que el hijo sigue mostrando esta oferta."
        }
    }

    Text("Vinculación presencial", style = MaterialTheme.typography.titleMedium)
    Text(aviso)
    if (!bloqueado && frame == null) {
        Button(enabled = !ocupado, onClick = { selector.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }) {
            Text(stringResource(dev.controlparental.parent.R.string.escanear_qr))
        }
        OutlinedTextField(oferta, { if (it.length <= 16384) { descartar(); oferta = it } },
            enabled = !ocupado, label = { Text("CHILD_OFFER (alternativa de prueba)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(origen, { descartar(); origen = it.take(512) }, enabled = !ocupado,
            label = { Text("Origen HTTPS del relay") }, modifier = Modifier.fillMaxWidth())
        Button(enabled = !ocupado && oferta.isNotEmpty(), onClick = {
            ejecutar {
                descartar()
                val turno = generacion
                val nueva = flujo.preparar(oferta.toByteArray(), origen)
                if (turno != generacion || !ciclo.currentState.isAtLeast(Lifecycle.State.STARTED)) nueva.codigos.descartar()
                else { preparacion = nueva; aviso = "Guarda los códigos fuera del teléfono. Desaparecen al salir o pasar a segundo plano." }
            }
        }) { Text("Preparar huellas y códigos") }
        preparacion?.let { preparada ->
            Text("Huella SHA-256 del padre: ${preparada.huellaPadre}")
            Text("Huella SHA-256 del hijo: ${preparada.huellaHijo}")
            Text("Mantenimiento: ${preparada.codigos.mostrar()[PropositoRecuperacion.MAINTENANCE]}")
            Text("Recuperar padre: ${preparada.codigos.mostrar()[PropositoRecuperacion.RECOVER_PARENT]}")
            Text("Retirar: ${preparada.codigos.mostrar()[PropositoRecuperacion.RETIRE]}")
            Row { Checkbox(comparadas, { comparadas = it }, enabled = !ocupado); Text("Comparé ambas huellas presencialmente; las compararé también en el hijo antes de aceptar.") }
            Row { Checkbox(guardados, { guardados = it }, enabled = !ocupado); Text("Guardé los tres códigos fuera del teléfono.") }
            Button(enabled = !ocupado, onClick = autenticar) { Text("Autenticar al adulto") }
            Button(enabled = !ocupado && comparadas && guardados, onClick = {
                ejecutar {
                    val firmado = flujo.aceptar(preparada, comparadas, guardados)
                    descartar()
                    frame = firmado.toString(Charsets.UTF_8)
                    bloqueado = true
                    env = flujo.exportarEnvPublica()
                    aviso = "Aceptación preparada, no vínculo completado. El hijo debe verificar oferta vigente, firma y huellas presencialmente."
                }
            }) { Text("Firmar aceptación y mostrar QR") }
        }
    }
    frame?.let { texto ->
        val bitmap = remember(texto) { ImagenQrAndroid.generar(texto) }
        Image(bitmap.asImageBitmap(), "QR PAIR_ACCEPT firmado, sin códigos", Modifier.fillMaxWidth().aspectRatio(1f))
        // Presentación de frame ya verificado por PairChild/ParentStore; no autorización.
        val sobre = dev.controlparental.protocol.CodecProtocolo.leerFrame(texto.toByteArray()) as dev.controlparental.protocol.SobreFirmado
        val aceptacion = dev.controlparental.protocol.CodecProtocolo.leerPayloadSinVerificar(
            dev.controlparental.protocol.CodecProtocolo.bytesPayload(sobre), dev.controlparental.protocol.Proposito.PAIR) as dev.controlparental.protocol.AceptacionVinculo
        Text("Huella padre: ${PreparacionVinculo.huella(aceptacion.parentPublicKeyB64)}")
        Text("Huella hijo: ${PreparacionVinculo.huella(aceptacion.childPublicKeyB64)}")
    }
    env?.let { Text("Solo configuración pública para Render (no despliega ni autentica el servicio):"); SelectionContainer { Text(it) } }
}
