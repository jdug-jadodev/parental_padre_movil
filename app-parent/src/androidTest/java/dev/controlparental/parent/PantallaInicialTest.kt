package dev.controlparental.parent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertIsNotEnabled
import org.junit.Rule
import org.junit.Test

class PantallaInicialTest {
    @get:Rule
    val pantalla = createAndroidComposeRule<MainActivity>()

    @Test
    fun muestraQueLaBaseTodaviaNoControlaUnDispositivo() {
        pantalla.onNodeWithText("Este teléfono será el administrador").assertIsDisplayed()
        pantalla.onNodeWithText(
            "Base inicial de desarrollo. Todavía no hay vinculación, conexión ni envío de órdenes.",
        ).assertIsDisplayed()
    }

    @Test
    fun recorreLasCuatroPantallasSinConfigurarAutoridadNiEnviarOrdenes() {
        pantalla.onNodeWithText("Configurar este teléfono como padre").performScrollTo().performClick()
        pantalla.onNodeWithText("Vincular dispositivo").assertIsDisplayed()
        pantalla.onNodeWithText("Escanear QR del hijo").performScrollTo().assertIsNotEnabled()
        pantalla.onNodeWithText("Ver controles").performScrollTo().performClick()
        pantalla.onNodeWithText("Vinculación: pendiente").assertIsDisplayed()
        pantalla.onNodeWithText("Estado del hijo: sin evidencia verificada").assertIsDisplayed()
        for (texto in listOf("Bloquear", "Bloquear durante…", "Permitir", "Permitir durante…")) {
            pantalla.onNodeWithText(texto).performScrollTo().assertIsNotEnabled()
        }
        pantalla.onNodeWithTag("abrir_recuperacion").performClick()
        pantalla.onNodeWithText("Recuperación").assertIsDisplayed()
        pantalla.onNodeWithText("Escanear desafío de recuperación").performScrollTo().assertIsNotEnabled()
        pantalla.onNodeWithText("Volver").performClick()
        pantalla.onNodeWithTag("pantalla_CONTROL").assertIsDisplayed()
    }

    @Test
    fun recuperacionAccesibleDesdePrimerInicioSinVinculoNiConexion() {
        pantalla.onNodeWithTag("abrir_recuperacion").performClick()
        pantalla.onNodeWithText("Recuperación").assertIsDisplayed()
        pantalla.onNodeWithText("Volver").performClick()
        pantalla.onNodeWithText("Este teléfono será el administrador").assertIsDisplayed()
    }
}
