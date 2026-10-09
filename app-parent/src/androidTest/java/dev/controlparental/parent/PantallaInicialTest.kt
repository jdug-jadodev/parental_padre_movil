package dev.controlparental.parent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
}
