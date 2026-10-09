package dev.controlparental.parent

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import dev.controlparental.parent.data.*
import dev.controlparental.parent.ui.PairingScreen
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PantallaVinculoTest {
    @get:Rule val pantalla = createAndroidComposeRule<ComponentActivity>()
    private var entorno: EntornoVinculoAndroid? = null
    private lateinit var store: ParentStore
    private val visible = mutableStateOf(true)
    private fun iniciar(falloFirma: Boolean = false) {
        val e = EntornoVinculoAndroid().also { entorno = it }
        store = e.abrir()
        val flujo = e.flujo(store, falloFirma)
        pantalla.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (visible.value) PairingScreen(flujo, store, {})
                }
            }
        }
        pantalla.waitForIdle()
        pantalla.onNodeWithTag("oferta_vinculo").performScrollTo().performTextReplacement(e.oferta)
        pantalla.onNodeWithTag("origen_vinculo").performScrollTo().performTextReplacement("https://prueba.invalid")
        pantalla.onNodeWithText("Preparar huellas y códigos").performScrollTo().performClick()
        pantalla.waitUntil(10000) { pantalla.onAllNodesWithTag("codigos_vinculo").fetchSemanticsNodes().size == 1 }
    }
    @After fun limpiar() {
        val e = entorno ?: return
        try {
            pantalla.runOnIdle { visible.value = false }
            pantalla.waitForIdle()
        } finally { e.close(); entorno = null }
    }

    @Test fun exigeAmbasConfirmacionesYFalloDeFirmaNoPreparaVinculo() {
        iniciar(falloFirma = true)
        pantalla.onNodeWithText("Firmar aceptación y mostrar QR").performScrollTo().assertIsNotEnabled()
        pantalla.onNodeWithTag("huellas_comparadas").performScrollTo().performClick()
        pantalla.onNodeWithText("Firmar aceptación y mostrar QR").performScrollTo().assertIsNotEnabled()
        pantalla.onNodeWithTag("codigos_guardados").performScrollTo().performClick()
        pantalla.onNodeWithText("Firmar aceptación y mostrar QR").performScrollTo().assertIsEnabled().performClick()
        pantalla.waitUntil(10000) {
            pantalla.onAllNodesWithText("Operación rechazada.", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(VinculoPersistido.SIN_VINCULAR, runBlocking { store.leer().vinculo })
        pantalla.onNodeWithContentDescription("QR PAIR_ACCEPT firmado, sin códigos").assertDoesNotExist()
    }

    @Test fun segundoPlanoDescartaCodigosSinCambiarVinculo() {
        iniciar()
        pantalla.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        pantalla.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        pantalla.onNodeWithTag("codigos_vinculo").assertDoesNotExist()
        pantalla.onNodeWithText("Firmar aceptación y mostrar QR").assertDoesNotExist()
        assertEquals(VinculoPersistido.SIN_VINCULAR, runBlocking { store.leer().vinculo })
    }

    @Test fun cambiarOfertaOOrigenDescartaCodigosYConfirmacionesAnteriores() {
        iniciar()
        pantalla.onNodeWithTag("origen_vinculo").performScrollTo().performTextReplacement("https://otra-prueba.invalid")
        pantalla.onNodeWithTag("codigos_vinculo").assertDoesNotExist()
        pantalla.onNodeWithText("Preparar huellas y códigos").performScrollTo().performClick()
        pantalla.waitUntil(10000) { pantalla.onAllNodesWithTag("codigos_vinculo").fetchSemanticsNodes().size == 1 }
        pantalla.onNodeWithTag("oferta_vinculo").performScrollTo().performTextReplacement("{}")
        pantalla.onNodeWithTag("codigos_vinculo").assertDoesNotExist()
        pantalla.onNodeWithText("Firmar aceptación y mostrar QR").assertDoesNotExist()
    }

    @Test fun salirDePantallaNoRestauraCodigosDesdeMemoriaPersistida() {
        iniciar()
        pantalla.runOnIdle { visible.value = false }
        pantalla.waitForIdle()
        pantalla.runOnIdle { visible.value = true }
        pantalla.onNodeWithTag("codigos_vinculo").assertDoesNotExist()
        assertEquals("", pantalla.onNodeWithTag("oferta_vinculo").fetchSemanticsNode().config[SemanticsProperties.EditableText].text)
        assertEquals(VinculoPersistido.SIN_VINCULAR, runBlocking { store.leer().vinculo })
    }
}
