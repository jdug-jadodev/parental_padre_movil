package dev.controlparental.parent.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.controlparental.parent.pairing.*
import dev.controlparental.protocol.cp2.VinculacionCp2
import kotlinx.coroutines.*

@Composable
internal fun OfertaCp2Screen(flujo: VincularCp2, autenticar: ((Boolean) -> Unit) -> Unit, reloj: () -> Long) {
    var oferta by remember { mutableStateOf<String?>(null) }
    var recibidaEn by remember { mutableLongStateOf(0L) }
    var origen by remember { mutableStateOf("") }
    var padre by remember { mutableStateOf<String?>(null) }
    var ticket by remember { mutableStateOf<TicketPairCp2?>(null) }
    var frame by remember { mutableStateOf<String?>(null) }
    var env by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf("Comprobando identidad y legado sin modificarlos…") }
    var permitida by remember { mutableStateOf(false) }
    var pendiente by remember { mutableStateOf(false) }
    var altaNueva by remember { mutableStateOf(false) }
    var huellas by remember { mutableStateOf(false) }
    var origenConfirmado by remember { mutableStateOf(false) }
    var guardados by remember { mutableStateOf(false) }
    var generacion by remember { mutableIntStateOf(0) }
    var activa by remember { mutableStateOf(true) }
    var ocupado by remember { mutableStateOf(false) }
    val ciclo = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    fun descartar(borrarOferta: Boolean = false) {
        flujo.descartar(); generacion++; ticket = null; frame = null; env = null
        altaNueva = false; huellas = false; origenConfirmado = false; guardados = false
        if (borrarOferta) oferta = null
    }
    fun errorSeguro(e: Exception) = (e as? AltaPairBloqueada)?.message
        ?: "Operación rechazada o preparación caducada. Reautentica y comprueba contexto; no se reemplaza un pendiente."
    fun ejecutar(operacion: suspend (Int) -> Unit) {
        if (ocupado) return
        ocupado = true; val turno = generacion
        scope.launch {
            try { operacion(turno) }
            catch (e: CancellationException) { descartar(true); throw e }
            catch (e: Exception) {
                descartar(); aviso = errorSeguro(e); permitida = false
                try { val f = flujo.inspeccionar(); pendiente = f.pendiente != null; permitida = !pendiente }
                catch (_: Exception) { /* Fallo cerrado; no reabrir ni reemplazar el área. */ }
            }
            finally { ocupado = false }
        }
    }
    DisposableEffect(ciclo, flujo) {
        val observador = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_STOP) { descartar(true); activa = false; permitida = false }
            if (e == Lifecycle.Event.ON_START) activa = true
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador); descartar(true) }
    }
    LaunchedEffect(activa, flujo) {
        if (activa) try {
            val f = flujo.inspeccionar(); padre = f.publica; pendiente = f.pendiente != null; permitida = !pendiente
            aviso = if (pendiente) "PENDIENTE_CONFIRMACION_HIJO: aceptación pública conservada. No se restaura QR ni códigos; no repetir enrolamiento sin reconciliar."
                else "Identidad estable y legado no operativo coherentes. Nuevo enrolamiento CP/2 solo con autorización adulta; no convierte ni borra CP/1."
        } catch (e: Exception) { permitida = false; padre = null; aviso = errorSeguro(e) }
    }
    LaunchedEffect(generacion) {
        if (oferta != null) {
            val restante = 300_000 - (reloj() - recibidaEn)
            if (restante > 0) delay(restante)
            descartar(true); aviso = "Ventana local original de 5 minutos terminada: QR y códigos retirados. Un pendiente público no se elimina."
        }
    }
    Text("NUEVO PERFIL CP/2 — enrolamiento presencial")
    Text(aviso)
    Text("No acredita vínculo operativo. El hijo valida su oferta viva, nonce y confirmación local; aquí no se recibe confirmación del hijo.")
    if (activa && !ocupado && !pendiente && frame == null) CamaraOfertaCp2 { texto ->
        descartar(true); oferta = texto; recibidaEn = reloj()
    }
    oferta?.let { texto ->
        val o = remember(texto) { VinculacionCp2.leerOferta(texto.toByteArray(Charsets.UTF_8)) }
        Text("Oferta CP/2 leída; oferta no firmada, no vínculo confirmado.")
        Text("Huella SHA-256 hijo: ${PreparacionVinculo.huella(o.childPublicKeyB64)}")
        padre?.let { Text("Huella SHA-256 padre existente: ${PreparacionVinculo.huella(it)}") }
        if (frame == null && !pendiente) {
            OutlinedTextField(origen, { descartar(); origen = it.take(200) }, enabled = !ocupado,
                label = { Text("Origen HTTPS exacto del relay, sin ruta") })
            Row { Checkbox(altaNueva, { descartar(); altaNueva = it }, enabled = permitida && !ocupado); Text("Autorizo nuevo enrolamiento CP/2 con la identidad existente; conservo íntegro el legado y NO activo el vínculo.") }
            Button(enabled = permitida && !ocupado, onClick = {
                descartar(); ocupado = true; val turno = generacion
                autenticar { correcta ->
                    ocupado = false
                    if (turno == generacion && activa) {
                        if (!correcta) descartar(true)
                        aviso = if (correcta) "Autenticación realizada: confirma alta nueva y prepara. La firma exige autoridad todavía vigente." else "Autenticación cancelada; preparación descartada."
                    }
                }
            }) { Text("Autenticar al adulto (sin crear identidad)") }
            Button(enabled = permitida && !ocupado && altaNueva && ticket == null, onClick = {
                ejecutar { turno ->
                    val t = flujo.preparar(texto.toByteArray(Charsets.UTF_8), origen, recibidaEn, altaNueva)
                    if (turno == generacion && activa) { ticket = t; aviso = "Guarda los tres códigos fuera del teléfono. Solo RAM; se retiran al salir, fondo, error o vencimiento." }
                    else flujo.descartar()
                }
            }) { Text("Preparar huellas y códigos CP/2") }
            ticket?.let { t ->
                t.mostrarCodigos().forEach { (p, c) -> Text("${p.name}: ${c.chunked(4).joinToString("-")}") }
                Row { Checkbox(huellas, { huellas = it }, enabled = !ocupado); Text("Comparé ambas huellas presencialmente.") }
                Row { Checkbox(origenConfirmado, { origenConfirmado = it }, enabled = !ocupado); Text("Confirmé el origen HTTPS exacto con el otro equipo.") }
                Row { Checkbox(guardados, { guardados = it }, enabled = !ocupado); Text("Guardé los tres códigos fuera del teléfono.") }
                Button(enabled = !ocupado && huellas && origenConfirmado && guardados, onClick = {
                    ejecutar { turno ->
                        val d = flujo.firmar(t, huellas, origenConfirmado, guardados)
                        if (turno == generacion && activa && reloj() < t.vence) {
                            ticket = null; frame = d.frame().toString(Charsets.UTF_8); pendiente = true; permitida = false
                            aviso = "PENDIENTE_CONFIRMACION_HIJO: persistido y releído. QR solo durante esta ventana, no ACTIVO."
                        }
                    }
                }) { Text("Firmar aceptación CP/2 y mostrar QR") }
            }
        }
    }
    frame?.let {
        val imagen = remember(it) { ImagenQrAndroid.generar(it) }
        Image(imagen.asImageBitmap(), "PAIR_ACCEPT CP/2 firmado, solo hashes de recuperación", Modifier.fillMaxWidth().aspectRatio(1f))
    }
    if (pendiente) Button(enabled = !ocupado, onClick = {
        ejecutar { turno -> val publico = flujo.exportarPublico(); if (turno == generacion && activa) env = publico }
    }) { Text("Mostrar configuración pública CP/2 PENDIENTE (no despliega)") }
    env?.let { Text(it) }
    TextButton(onClick = { descartar(true); aviso = "Ceremonia local cancelada; el registro público pendiente, si existe, se conserva." }, enabled = !ocupado) { Text("Cancelar y retirar QR/códigos") }
}
