package dev.controlparental.parent.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.controlparental.parent.R
import dev.controlparental.parent.presentation.*
import dev.controlparental.protocol.Protocolo

@Composable
fun AplicacionPadre(
    estado: ParentUiState,
    navegar: (PantallaPadre) -> Unit,
    volver: () -> Unit,
    identidad: @Composable () -> Unit = {},
) {
    BackHandler(enabled = estado.puedeVolver, onBack = volver)
    Scaffold(bottomBar = {
        FlowRow(modifier = Modifier.fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .padding(horizontal = 12.dp)) {
            if (estado.puedeVolver) TextButton(onClick = volver) { Text(stringResource(R.string.volver)) }
            TextButton(
                onClick = { navegar(PantallaPadre.RECUPERACION) },
                modifier = Modifier.testTag("abrir_recuperacion"),
            ) { Text(stringResource(R.string.abrir_recuperacion)) }
        }
    }) { margenes ->
        // Cada destino abre desde arriba, no reutiliza el scroll de otra pantalla.
        key(estado.pantalla) {
            Column(
                modifier = Modifier.fillMaxSize().padding(margenes).verticalScroll(rememberScrollState())
                    .padding(20.dp).testTag("pantalla_${estado.pantalla.name}"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                when (estado.pantalla) {
                    PantallaPadre.PRIMER_INICIO -> {
                        PrimerInicio { navegar(PantallaPadre.VINCULAR) }
                        identidad()
                    }
                    PantallaPadre.VINCULAR -> Vincular { navegar(PantallaPadre.CONTROL) }
                    PantallaPadre.CONTROL -> Control(estado.datos)
                    PantallaPadre.RECUPERACION -> Recuperacion()
                }
            }
        }
    }
}

@Composable
private fun PrimerInicio(vincular: () -> Unit) {
    Text(stringResource(R.string.inicio_titulo), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(R.string.inicio_descripcion))
    Text(stringResource(R.string.inicio_aviso))
    Text(stringResource(R.string.inicio_protocolo, Protocolo.VERSION))
    Text(stringResource(R.string.inicio_seguridad))
    Button(onClick = vincular) { Text(stringResource(R.string.configurar_padre)) }
    Text(stringResource(R.string.recorrido_aviso))
}

@Composable
private fun Vincular(control: () -> Unit) {
    Text(stringResource(R.string.vincular_titulo), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(R.string.vincular_descripcion))
    Text(stringResource(R.string.vincular_pendiente))
    Button(onClick = {}, enabled = false) { Text(stringResource(R.string.escanear_qr)) }
    OutlinedButton(onClick = control) { Text(stringResource(R.string.ver_controles)) }
}

@Composable
private fun Control(datos: DatosPadre) {
    Text(stringResource(R.string.control_titulo), style = MaterialTheme.typography.headlineSmall)
    ResumenEstado(datos)
    Text(stringResource(R.string.acciones_no_operativas))
    for (etiqueta in listOf(R.string.bloquear, R.string.bloquear_durante, R.string.permitir, R.string.permitir_durante)) {
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(etiqueta))
        }
    }
    Text(stringResource(R.string.duracion_aviso))
}

@Composable
private fun Recuperacion() {
    Text(stringResource(R.string.recuperacion_titulo), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(R.string.recuperacion_descripcion))
    Text(stringResource(R.string.recuperacion_padre_perdido))
    Text(stringResource(R.string.recuperacion_retirada))
    Button(onClick = {}, enabled = false) { Text(stringResource(R.string.escanear_desafio)) }
    Text(stringResource(R.string.recuperacion_pendiente))
}
