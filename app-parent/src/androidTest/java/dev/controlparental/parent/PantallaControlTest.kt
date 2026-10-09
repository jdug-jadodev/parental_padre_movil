package dev.controlparental.parent

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.controlparental.parent.data.*
import dev.controlparental.parent.policy.*
import dev.controlparental.parent.presentation.*
import dev.controlparental.parent.ui.*
import dev.controlparental.protocol.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

class PantallaControlTest {
    @get:Rule val pantalla = createComposeRule()

    // Doble de presentación exclusivamente instrumentado: no claves, almacén ni red reales.
    private class AccionesFalsas(preparado: Boolean = false, local: Boolean = false) : AccionesControl {
        val id = "00000000-0000-4000-8000-000000000002"
        var guardadas = 0
        var envios = 0
        var cierres = 0
        var actual = VistaPoliticas(MetadatosPadre(parentPublicKeyB64 = "Doble de UI, no persistir",
            vinculo = if (preparado) VinculoPersistido.PREPARADO else VinculoPersistido.VINCULADO,
            intencionLocal = if (local) IntencionLocal(id, Accion.ALLOW_FOR, 60) else null),
            Confirmacion(id, id, id, id, "1", "A".repeat(43), ResultadoAck.APPLIED, Modo.LOCKED, Motivo.NETWORK_LOST, 60),
            true, 4, !preparado)
        override suspend fun vista() = actual
        override suspend fun guardar(action: Accion, durationSec: Int?, reemplazarId: String?): IntencionLocal {
            guardadas++
            return IntencionLocal(id, action, durationSec).also { actual = actual.copy(datos = actual.datos.copy(intencionLocal = it)) }
        }
        override suspend fun preparar(local: IntencionLocal) = ConfirmacionIntencion(local.action, local.durationSec, local.id)
        override suspend fun enviar(confirmacion: ConfirmacionIntencion) {
            envios++
            actual = actual.copy(fresca = false, puedeEnviar = false,
                datos = actual.datos.copy(intencionLocal = null,
                    pendiente = PendingIntentRecord(id, confirmacion.action, confirmacion.durationSec, "1")))
        }
        override suspend fun descartarLocal(id: String) { actual = actual.copy(datos = actual.datos.copy(intencionLocal = null)) }
        override suspend fun descartarOrden(id: String) { error("No borrar una orden en este doble") }
        override suspend fun cerrarConfirmacion() { cierres++ }
    }

    private fun mostrar(acciones: AccionesFalsas, textoGrande: Boolean = false) {
        pantalla.setContent {
            val densidad = LocalDensity.current
            MaterialTheme {
                CompositionLocalProvider(LocalDensity provides Density(densidad.density, if (textoGrande) 2f else densidad.fontScale)) {
                    AplicacionPadre(ParentUiState(PantallaPadre.CONTROL), {}, {},
                        controles = { ControlScreen(acciones, autenticar = {}) })
                }
            }
        }
        pantalla.waitUntil(5000) { pantalla.onAllNodesWithTag("accion_LOCK").fetchSemanticsNodes().isNotEmpty() }
        if (acciones.actual.datos.vinculo == VinculoPersistido.VINCULADO) pantalla.waitUntil(5000) {
            pantalla.onNodeWithTag("accion_LOCK").fetchSemanticsNode().config.contains(SemanticsProperties.Disabled).not()
        }
        pantalla.waitForIdle()
    }

    @Test fun duracionInvalidaNoGuardaYGuardarNoEnvia() {
        val acciones = AccionesFalsas(); mostrar(acciones)
        pantalla.onNodeWithTag("accion_LOCK_FOR").performScrollTo().performClick()
        pantalla.onNodeWithTag("duracion_control").performScrollTo().performTextReplacement("59")
        pantalla.onNodeWithText("Guardar intención local").performScrollTo().assertIsNotEnabled()
        pantalla.onNodeWithTag("duracion_control").performTextReplacement("60")
        pantalla.onNodeWithText("Guardar intención local").performScrollTo().performClick()
        pantalla.waitForIdle()
        assertEquals(1, acciones.guardadas); assertEquals(0, acciones.envios)
        pantalla.onNodeWithText("Intención pendiente local:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test fun permitirRequiereConfirmacionYEnvioNoPintaUsoPermitido() {
        val acciones = AccionesFalsas(local = true); mostrar(acciones)
        pantalla.onNodeWithText("Revisar y enviar intención").performScrollTo().performClick()
        pantalla.onNodeWithText("Confirmar envío a este dispositivo").assertIsDisplayed()
        assertEquals(0, acciones.envios)
        pantalla.onNodeWithTag("confirmar_orden").performClick()
        pantalla.waitForIdle(); assertEquals(1, acciones.envios)
        pantalla.onNodeWithText("Último estado confirmado: bloqueado").performScrollTo().assertIsDisplayed()
        pantalla.onNodeWithText("Estado del hijo verificado: permitido").assertDoesNotExist()
    }

    @Test fun volverSinEnviarConservaIntencionYNoEmiteOrden() {
        val acciones = AccionesFalsas(local = true); mostrar(acciones)
        pantalla.onNodeWithText("Revisar y enviar intención").performScrollTo().performClick()
        pantalla.onNodeWithText("Volver sin enviar").performClick()
        pantalla.waitForIdle()
        assertEquals(0, acciones.envios); assertEquals(1, acciones.cierres)
        pantalla.onNodeWithText("Intención pendiente local:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test fun preparadoNoHabilitaAccionesYRecuperacionPermaneceAccesible() {
        val acciones = AccionesFalsas(preparado = true); mostrar(acciones)
        for (accion in Accion.entries) pantalla.onNodeWithTag("accion_${accion.name}").performScrollTo().assertIsNotEnabled()
        pantalla.onNodeWithTag("abrir_recuperacion").assertIsDisplayed()
        assertEquals(0, acciones.envios)
    }

    @Test fun textoGrandeMantieneFormularioDesplazableYRecuperacionAccesible() {
        val acciones = AccionesFalsas(); mostrar(acciones, textoGrande = true)
        pantalla.onNodeWithTag("accion_ALLOW_FOR").performScrollTo().performClick()
        pantalla.onNodeWithTag("duracion_control").performScrollTo().performTextReplacement("604800")
        pantalla.onNodeWithText("Guardar intención local").performScrollTo().assertIsEnabled()
        pantalla.onNodeWithTag("abrir_recuperacion").assertIsDisplayed()
        assertEquals(0, acciones.envios)
    }
}
