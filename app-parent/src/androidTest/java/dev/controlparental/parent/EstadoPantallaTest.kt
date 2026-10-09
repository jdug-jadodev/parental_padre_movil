package dev.controlparental.parent

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.controlparental.parent.presentation.*
import dev.controlparental.parent.ui.AplicacionPadre
import dev.controlparental.protocol.*
import org.junit.Rule
import org.junit.Test

class EstadoPantallaTest {
    @get:Rule val pantalla = createComposeRule()

    private fun mostrar(repositorio: RepositorioPadreFalso) {
        pantalla.setContent {
            val datos by repositorio.datos.collectAsState()
            MaterialTheme { AplicacionPadre(ParentUiState(PantallaPadre.CONTROL, datos), {}, {}) }
        }
    }

    @Test fun hijoAusenteNoSeMuestraBloqueado() {
        mostrar(RepositorioPadreFalso(DatosPadre(
            vinculo = EstadoVinculo.VINCULADO,
            relay = ConexionRelay.CONECTADO, presencia = PresenciaHijo.AUSENTE,
        )))
        pantalla.onNodeWithText("Relay: conectado").assertIsDisplayed()
        pantalla.onNodeWithText("Presencia del hijo: ausente").assertIsDisplayed()
        pantalla.onNodeWithText("Estado del hijo: sin evidencia verificada").assertIsDisplayed()
        pantalla.onAllNodesWithText("bloqueado", substring = true).assertCountEquals(0)
        pantalla.onNodeWithTag("abrir_recuperacion").assertIsDisplayed()
    }

    @Test fun desconexionMantieneUltimaEvidenciaYNoConfirmaIntencion() {
        val evidencia = EvidenciaHijo(Modo.ALLOWED, Motivo.OK, ResultadoEstado.APPLIED, 4, true)
        val confirmado = DatosPadre(
            EstadoVinculo.VINCULADO, ConexionRelay.CONECTADO, PresenciaHijo.PRESENTE,
            evidencia, IntencionPendiente.ENVIADA_SIN_CONFIRMAR,
        )
        val repositorio = RepositorioPadreFalso(confirmado)
        mostrar(repositorio)
        pantalla.onNodeWithText("Estado confirmado: permitido").assertIsDisplayed()
        pantalla.runOnIdle {
            repositorio.publicar(confirmado.copy(relay = ConexionRelay.DESCONECTADO, presencia = PresenciaHijo.AUSENTE))
        }
        pantalla.onNodeWithText("Relay: desconectado").assertIsDisplayed()
        pantalla.onNodeWithText("Último estado confirmado: permitido").assertIsDisplayed()
        pantalla.onNodeWithText("Estado confirmado: permitido").assertDoesNotExist()
        pantalla.onNodeWithText("Antigüedad de la evidencia: 4 s").assertIsDisplayed()
        pantalla.onNodeWithText("Intención pendiente: enviada sin confirmar").performScrollTo().assertIsDisplayed()
    }

    @Test fun aceptacionPendienteNoSePresentaComoAplicada() {
        mostrar(RepositorioPadreFalso(DatosPadre(
            EstadoVinculo.VINCULADO, ConexionRelay.CONECTADO, PresenciaHijo.PRESENTE,
            EvidenciaHijo(Modo.LOCKED, Motivo.PARENT_LOCK, ResultadoEstado.ACCEPTED_PENDING_ENFORCEMENT, 2, true),
            IntencionPendiente.LOCAL,
        )))
        pantalla.onNodeWithText("Última orden aceptada: pendiente de comprobar aplicación").performScrollTo().assertIsDisplayed()
        pantalla.onNodeWithText("Última orden aceptada: aplicada según su evidencia").assertDoesNotExist()
        pantalla.onNodeWithText("Intención pendiente: preparada localmente, no enviada").performScrollTo().assertIsDisplayed()
    }
}
