package dev.controlparental.parent

import dev.controlparental.parent.data.MetadatosPadre
import dev.controlparental.parent.policy.VistaPoliticas
import dev.controlparental.parent.presentation.*
import dev.controlparental.protocol.*
import org.junit.Assert.*
import org.junit.Test

class PresentacionControlTest {
    private val id = "00000000-0000-4000-8000-000000000002"
    private fun vista(mensaje: Mensaje, fresca: Boolean = true) = VistaPoliticas(
        MetadatosPadre(parentPublicKeyB64 = "Solo presentación, no almacenar"), mensaje, fresca, 4, false)
    private fun ack(result: ResultadoAck = ResultadoAck.APPLIED, mode: Modo = Modo.LOCKED,
        reason: Motivo = Motivo.NETWORK_LOST) = Confirmacion(id, id, id, id, "1", "A".repeat(43), result, mode, reason, 60)

    @Test fun duracionesLimitesYEntradasInvalidasSinConversionSilenciosa() {
        assertEquals(60, duracionControl("60")); assertEquals(604800, duracionControl("604800"))
        for (valor in listOf("", "59", "604801", "-60", "60.5", " 60", "1e3", "999999999999"))
            assertNull(valor, duracionControl(valor))
    }

    @Test fun advertenciasDeLasCuatroAccionesNoPrometenAperturaIncondicional() {
        assertTrue(avisoAccion(Accion.LOCK_FOR).contains("si conserva conexión y no se reinicia"))
        assertTrue(avisoAccion(Accion.ALLOW).contains("pierda conexión o reinicie"))
        assertTrue(avisoAccion(Accion.ALLOW_FOR).contains("Volverá a bloquearse"))
        assertEquals(4, Accion.entries.map(::nombreAccion).distinct().size)
    }

    @Test fun appliedConBloqueoDeRedNoSePresentaComoPermitido() {
        val textos = resumenControl(vista(ack()))
        assertTrue(textos.contains("Estado del hijo verificado: bloqueado"))
        assertTrue(textos.contains("Orden aplicada; bloqueo por falta de conexión"))
        assertFalse(textos.any { it.contains(": permitido") })
    }

    @Test fun desconectarMuestraUltimaEvidenciaYTiempoInformadoSinDescontarlo() {
        val textos = resumenControl(vista(ack(mode = Modo.ALLOWED, reason = Motivo.OK), false))
        assertTrue(textos.contains("Último estado confirmado: permitido"))
        assertTrue(textos.any { it.contains("60 s. No se descuenta") })
        assertFalse(textos.any { it.startsWith("Estado del hijo verificado") })
    }

    @Test fun pendienteFalloYRechazoNoSePintanComoAplicados() {
        for (result in listOf(ResultadoAck.ACCEPTED_PENDING_ENFORCEMENT, ResultadoAck.ENFORCEMENT_FAILED, ResultadoAck.REJECTED)) {
            assertFalse(resumenControl(vista(ack(result))).any { it.contains("aplicada según el hijo") })
        }
    }

    @Test fun sinDeviceOwnerNoMuestraProtegidoYAntiguedadDesconocidaNoSeInventa() {
        val estado = EstadoDispositivo(id, "A".repeat(43), id, id, "1", "0", null, null, ResultadoEstado.NONE,
            Modo.SETUP, Motivo.NOT_DEVICE_OWNER, null, true, true, false, false, "A".repeat(43))
        val textos = resumenControl(vista(estado, false).copy(antiguedadSeg = null))
        assertTrue(textos.any { it.contains("sin administrador del dispositivo") })
        assertTrue(textos.any { it.contains("Antigüedad desconocida") })
    }
}
