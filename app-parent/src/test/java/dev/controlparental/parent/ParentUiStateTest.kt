package dev.controlparental.parent

import dev.controlparental.parent.presentation.*
import dev.controlparental.protocol.*
import org.junit.Assert.*
import org.junit.Test

class ParentUiStateTest {
    private val evidencia = EvidenciaHijo(Modo.ALLOWED, Motivo.OK, ResultadoEstado.APPLIED, 4, true)
    private val confirmado = DatosPadre(EstadoVinculo.VINCULADO, ConexionRelay.CONECTADO, PresenciaHijo.PRESENTE, evidencia)

    @Test fun sinDatosNoInventaBloqueo() {
        val datos = DatosPadre()
        assertNull(datos.evidencia)
        assertEquals(TipoEvidencia.SIN_EVIDENCIA, datos.tipoEvidencia)
        assertEquals(PresenciaHijo.DESCONOCIDA, datos.presencia)
    }

    @Test fun relayPresenteNoPruebaEstadoDelHijo() {
        assertEquals(TipoEvidencia.SIN_EVIDENCIA, confirmado.copy(evidencia = null).tipoEvidencia)
    }

    @Test fun soloVinculoConexionPresenciaYFrescuraPermitenEstadoActual() {
        assertEquals(TipoEvidencia.CONFIRMADA, confirmado.tipoEvidencia)
        for (relay in ConexionRelay.entries.filter { it != ConexionRelay.CONECTADO }) {
            assertEquals(TipoEvidencia.ULTIMA_CONOCIDA, confirmado.copy(relay = relay).tipoEvidencia)
        }
        for (presencia in listOf(PresenciaHijo.AUSENTE, PresenciaHijo.DESCONOCIDA)) {
            assertEquals(TipoEvidencia.ULTIMA_CONOCIDA, confirmado.copy(presencia = presencia).tipoEvidencia)
        }
        assertEquals(TipoEvidencia.ULTIMA_CONOCIDA, confirmado.copy(vinculo = EstadoVinculo.SIN_VINCULAR).tipoEvidencia)
        assertEquals(TipoEvidencia.ULTIMA_CONOCIDA, confirmado.copy(evidencia = evidencia.copy(fresca = false)).tipoEvidencia)
    }

    @Test fun intencionPendienteNoModificaEvidencia() {
        for (pendiente in IntencionPendiente.entries) {
            val datos = confirmado.copy(pendiente = pendiente)
            assertEquals(evidencia, datos.evidencia)
            assertEquals(TipoEvidencia.CONFIRMADA, datos.tipoEvidencia)
        }
    }

    @Test fun antiguedadNegativaSeRechaza() {
        assertThrows(IllegalArgumentException::class.java) { evidencia.copy(antiguedadSeg = -1) }
    }
}
