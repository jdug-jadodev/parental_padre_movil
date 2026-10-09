package dev.controlparental.parent.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.controlparental.parent.pairing.*
import dev.controlparental.protocol.CodecProtocolo
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
internal fun CamaraOferta(onOferta: (String) -> Unit) {
    val contexto = LocalContext.current
    val propietario = LocalLifecycleOwner.current
    var abierta by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf("Apunta al QR CHILD_OFFER. Cámara solo mientras escaneas: no se guardan fotos ni se envían imágenes.") }
    val callback by rememberUpdatedState(onOferta)
    val permiso = rememberLauncherForActivityResult(RequestPermission()) { concedido ->
        abierta = concedido && propietario.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        if (!concedido) aviso = "Sin permiso de cámara. Puedes leer una imagen QR o volver a intentarlo; recuperación sigue disponible."
    }
    DisposableEffect(propietario) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_PAUSE) abierta = false
        }
        propietario.lifecycle.addObserver(observador)
        onDispose { propietario.lifecycle.removeObserver(observador) }
    }
    Text(aviso)
    if (!abierta) {
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) abierta = true
            else permiso.launch(Manifest.permission.CAMERA)
        }) { Text("Escanear oferta con cámara") }
    } else {
        OutlinedButton(onClick = { abierta = false }) { Text("Cerrar cámara") }
        val visor = remember { PreviewView(contexto).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
        } }
        AndroidView(factory = { visor }, modifier = Modifier.fillMaxWidth().height(300.dp))
        DisposableEffect(visor, propietario) {
            val activo = AtomicBoolean(true)
            val entregado = AtomicBoolean(false)
            val executor = Executors.newSingleThreadExecutor()
            val principal = ContextCompat.getMainExecutor(contexto)
            val preview = Preview.Builder().build().apply { setSurfaceProvider(visor.surfaceProvider) }
            val analisis = ImageAnalysis.Builder()
                .setResolutionSelector(ResolutionSelector.Builder().setResolutionStrategy(
                    ResolutionStrategy(Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER)).build())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            var proveedor: ProcessCameraProvider? = null
            analisis.setAnalyzer(executor) { imagen ->
                try {
                    if (activo.get() && !entregado.get()) {
                        val plano = imagen.planes[0]
                        val gris = LuminanciaQr.copiar(plano.buffer, imagen.width, imagen.height, plano.rowStride, plano.pixelStride)
                        val texto = QrLocal.leerLuminancia(gris.ancho, gris.alto, gris.bytes)
                        CodecProtocolo.leerOferta(texto.toByteArray()) // Nunca acepta otro tipo de QR.
                        if (entregado.compareAndSet(false, true)) principal.execute {
                            if (activo.get() && propietario.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                abierta = false
                                callback(texto)
                                aviso = "Oferta QR leída localmente. Comprueba huellas y que sigue vigente en el hijo."
                            }
                        }
                    }
                } catch (_: Exception) { /* Sin QR/oferta válida, continuar sin registrar imágenes ni contenido. */ }
                finally { imagen.close() }
            }
            val futuro = ProcessCameraProvider.getInstance(contexto)
            futuro.addListener({
                if (activo.get() && propietario.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) try {
                    val camaras = futuro.get()
                    proveedor = camaras
                    camaras.bindToLifecycle(propietario, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis)
                } catch (_: Exception) {
                    abierta = false
                    aviso = "Cámara no disponible. Usa la imagen QR como alternativa."
                }
            }, principal)
            onDispose {
                activo.set(false)
                analisis.clearAnalyzer()
                proveedor?.unbind(preview, analisis) // Solo nuestros casos, nunca unbindAll.
                executor.shutdown()
            }
        }
    }
}
