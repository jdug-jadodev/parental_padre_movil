package dev.controlparental.parent.presentation

import dev.controlparental.protocol.Modo
import dev.controlparental.protocol.Motivo
import dev.controlparental.protocol.ResultadoEstado

enum class PantallaPadre { PRIMER_INICIO, VINCULAR, CONTROL, RECUPERACION }
enum class EstadoVinculo { SIN_VINCULAR, VINCULADO, REQUIERE_RECUPERACION }
enum class ConexionRelay { DESCONECTADO, CONECTANDO, AUTENTICANDO, CONECTADO, ERROR }
enum class PresenciaHijo { DESCONOCIDA, PRESENTE, AUSENTE }
enum class IntencionPendiente { NINGUNA, LOCAL, ENVIADA_SIN_CONFIRMAR }
enum class TipoEvidencia { SIN_EVIDENCIA, CONFIRMADA, ULTIMA_CONOCIDA }

// Resumen de presentación; no acredita una firma ni aplica políticas.
// P05 tendrá que construirlo desde evidencia autenticada y correlacionada.
data class EvidenciaHijo(
    val modo: Modo,
    val motivo: Motivo,
    val resultadoUltimaOrden: ResultadoEstado,
    val antiguedadSeg: Long,
    val fresca: Boolean,
) {
    init { require(antiguedadSeg >= 0) }
}

data class DatosPadre(
    val vinculo: EstadoVinculo = EstadoVinculo.SIN_VINCULAR,
    val relay: ConexionRelay = ConexionRelay.DESCONECTADO,
    val presencia: PresenciaHijo = PresenciaHijo.DESCONOCIDA,
    val evidencia: EvidenciaHijo? = null,
    val pendiente: IntencionPendiente = IntencionPendiente.NINGUNA,
) {
    val tipoEvidencia: TipoEvidencia
        get() = when {
            evidencia == null -> TipoEvidencia.SIN_EVIDENCIA
            vinculo == EstadoVinculo.VINCULADO && relay == ConexionRelay.CONECTADO &&
                presencia == PresenciaHijo.PRESENTE && evidencia.fresca -> TipoEvidencia.CONFIRMADA
            else -> TipoEvidencia.ULTIMA_CONOCIDA
        }
}

data class ParentUiState(
    val pantalla: PantallaPadre = PantallaPadre.PRIMER_INICIO,
    val datos: DatosPadre = DatosPadre(),
    val puedeVolver: Boolean = false,
)
