package dev.controlparental.parent

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.controlparental.parent.data.*
import dev.controlparental.parent.presentation.*
import dev.controlparental.parent.recovery.OfflineChallengeSigner
import dev.controlparental.parent.ui.*
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PantallaRecuperacionTest {
    @get:Rule val pantalla = createComposeRule()
    // Todo en RAM/JCA y autoridad ficticia. No acredita Keystore/prompt físicos,
    // no crea archivos/alias ni opera un hijo o su retirada.
    private class Entorno : AutoCloseable {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val padre = par(); val hijo = par()
        var firmas = 0
        val archivo = object : ArchivoMetadatos {
            var bytes: ByteArray? = null
            override fun existe() = bytes != null
            override fun leer() = bytes!!.copyOf()
            override fun escribir(bytes: ByteArray) { this.bytes = bytes.copyOf() }
        }
        val store = ParentStore(archivo, object : CifradorMetadatos {
            override fun prepararClave() {}
            override fun cifrar(bytes: ByteArray) = bytes.copyOf()
            override fun descifrar(bytes: ByteArray) = bytes.copyOf()
        }, scope)
        val id = "00000000-0000-4000-8000-000000000002"
        val flujo = OfflineChallengeSigner(store, { 0L }, {}, { firmas++; firmar(it, Rol.PARENT) })
        init {
            runBlocking { store.inicializar(MetadatosPadre(parentPublicKeyB64 = publica(Rol.PARENT), pairId = id,
                childPublicKeyB64 = publica(Rol.CHILD), relayOrigin = "https://prueba.invalid", vinculo = VinculoPersistido.PREPARADO)) }
        }
        fun publica(rol: Rol) = Base64.getUrlEncoder().withoutPadding().encodeToString((if (rol == Rol.PARENT) padre else hijo).public.encoded)
        fun firmar(mensaje: Mensaje, rol: Rol): SobreFirmado = CriptografiaProtocolo.firmarSobre(mensaje,
            Proposito.OFFLINE, ClaveVerificacion.desdeSpki(rol, publica(rol))) { bytes ->
            Signature.getInstance("SHA256withECDSA").run { initSign((if (rol == Rol.PARENT) padre else hijo).private); update(bytes); sign() }
        }
        fun reto(action: AccionOffline): String = CodecProtocolo.codificarFrame(firmar(
            RetoOffline(id, id, action, "A".repeat(43)), Rol.CHILD)).toString(Charsets.UTF_8)
        override fun close() { runBlocking { store.cerrar() }; scope.cancel() }
        companion object { fun par() = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair() }
    }

    private fun mostrar(e: Entorno) {
        pantalla.setContent { MaterialTheme {
            AplicacionPadre(ParentUiState(PantallaPadre.RECUPERACION), {}, {},
                recuperacion = { RecoveryScreen(e.flujo, e.store, {}) })
        } }
        pantalla.waitUntil(5000) {
            val nodos = pantalla.onAllNodesWithTag("reto_offline").fetchSemanticsNodes()
            nodos.isNotEmpty() && !nodos.first().config.contains(SemanticsProperties.Disabled)
        }
    }

    @Test fun retiroNoFirmaSinAdvertenciaYRespuestaNoAfirmaBorradoAplicado() {
        Entorno().use { e ->
            mostrar(e)
            pantalla.onNodeWithTag("reto_offline").performScrollTo().performTextReplacement(e.reto(AccionOffline.RETIRE))
            pantalla.onNodeWithText("Verificar desafío del hijo").performScrollTo().performClick()
            pantalla.onNodeWithText("Firmar respuesta offline y mostrar QR").performScrollTo().assertIsNotEnabled()
            assertEquals(0, e.firmas)
            pantalla.onNodeWithTag("retiro_confirmado").performScrollTo().performClick()
            pantalla.onNodeWithText("Firmar respuesta offline y mostrar QR").performScrollTo().performClick()
            pantalla.onNodeWithContentDescription("QR de respuesta offline para el hijo").performScrollTo().assertIsDisplayed()
            assertEquals(1, e.firmas)
            pantalla.onNodeWithText("No prueba función aplicada.", substring = true).performScrollTo().assertIsDisplayed()
        }
    }

    @Test fun cambiarEntradaDescartaPreparacionYConservaAccesoARecuperacion() {
        Entorno().use { e ->
            mostrar(e)
            pantalla.onNodeWithTag("reto_offline").performScrollTo().performTextReplacement(e.reto(AccionOffline.MAINTENANCE))
            pantalla.onNodeWithText("Verificar desafío del hijo").performScrollTo().performClick()
            pantalla.onNodeWithText("Firmar respuesta offline y mostrar QR").performScrollTo().assertIsEnabled()
            pantalla.onNodeWithTag("reto_offline").performScrollTo().performTextReplacement("QR inválido")
            pantalla.onNodeWithText("Firmar respuesta offline y mostrar QR").assertDoesNotExist()
            pantalla.onNodeWithTag("abrir_recuperacion").assertIsDisplayed()
            assertEquals(0, e.firmas)
        }
    }
}
