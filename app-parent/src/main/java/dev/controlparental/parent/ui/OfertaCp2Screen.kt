package dev.controlparental.parent.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.controlparental.parent.data.ParentStore
import dev.controlparental.parent.pairing.PreparacionVinculo
import dev.controlparental.protocol.cp2.OfertaCp2
import dev.controlparental.protocol.cp2.VinculacionCp2
import kotlinx.coroutines.delay

@Composable
internal fun OfertaCp2Screen(store: ParentStore) {
    var oferta by remember { mutableStateOf<OfertaCp2?>(null) }
    var padre by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf("Solo lectura CP/2. La oferta no está firmada ni acredita identidad o estado operativo.") }
    var generacion by remember { mutableIntStateOf(0) }
    var activa by remember { mutableStateOf(true) }
    val ciclo = LocalLifecycleOwner.current.lifecycle
    fun borrar() { oferta = null; generacion++; activa = false }
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_STOP) borrar()
            if (e == Lifecycle.Event.ON_START) activa = true
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador); borrar() }
    }
    LaunchedEffect(store) {
        try {
            if (store.existe()) {
                padre = store.leer().parentPublicKeyB64.also { VinculacionCp2.publica(it) }
                aviso = "REQUIERE_MIGRACION: almacén de perfil anterior conservado. Lectura permitida; firma CP/2 bloqueada."
            } else aviso = "Sin snapshot CP/2 autorizado: lectura permitida; firma bloqueada. No se crea identidad aquí."
        } catch (_: Exception) { padre = null; aviso = "REQUIERE_MIGRACION: estado incierto. No se modifica el almacén ni la identidad." }
    }
    LaunchedEffect(generacion) {
        if (oferta != null) {
            delay(300_000); borrar(); aviso = "Oferta local caducada; vuelve a escanear y comparar. El hijo valida su propia ventana."
        }
    }
    Text("NUEVO PERFIL CP/2 — lectura presencial")
    Text(aviso)
    if (activa) CamaraOfertaCp2 { texto ->
        oferta = VinculacionCp2.leerOferta(texto.toByteArray(Charsets.UTF_8)); generacion++
    }
    oferta?.let {
        Text("Oferta CP/2 leída; NO firma ni vínculo confirmado.")
        Text("Huella SHA-256 hijo: ${PreparacionVinculo.huella(it.childPublicKeyB64)}")
        padre?.let { p -> Text("Huella SHA-256 padre existente: ${PreparacionVinculo.huella(p)}") }
        Text("Compara ambas huellas presencialmente. La vigencia y nonce de la oferta los valida el hijo al aceptar.")
    }
    Button(onClick = {}, enabled = false) { Text("Firma CP/2 bloqueada: falta snapshot/alta compatible autorizada") }
    Text("Sin commit, migración ni códigos nuevos. PREPARADO no significa vínculo operativo.")
}
