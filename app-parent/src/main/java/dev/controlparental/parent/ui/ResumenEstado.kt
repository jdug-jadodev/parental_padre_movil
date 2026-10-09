package dev.controlparental.parent.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.controlparental.parent.R
import dev.controlparental.parent.presentation.*
import dev.controlparental.protocol.Modo
import dev.controlparental.protocol.Motivo
import dev.controlparental.protocol.ResultadoEstado

@Composable
internal fun ResumenEstado(datos: DatosPadre) {
    Text(stringResource(when (datos.vinculo) {
        EstadoVinculo.SIN_VINCULAR -> R.string.vinculo_pendiente
        EstadoVinculo.VINCULADO -> R.string.vinculo_listo
        EstadoVinculo.REQUIERE_RECUPERACION -> R.string.vinculo_recuperacion
    }))
    Text(stringResource(when (datos.relay) {
        ConexionRelay.DESCONECTADO -> R.string.relay_desconectado
        ConexionRelay.CONECTANDO -> R.string.relay_conectando
        ConexionRelay.AUTENTICANDO -> R.string.relay_autenticando
        ConexionRelay.CONECTADO -> R.string.relay_conectado
        ConexionRelay.ERROR -> R.string.relay_error
    }))
    Text(stringResource(when (datos.presencia) {
        PresenciaHijo.DESCONOCIDA -> R.string.hijo_desconocido
        PresenciaHijo.PRESENTE -> R.string.hijo_presente
        PresenciaHijo.AUSENTE -> R.string.hijo_ausente
    }))
    val evidencia = datos.evidencia
    if (evidencia == null) Text(stringResource(R.string.sin_evidencia)) else {
        val modo = stringResource(when (evidencia.modo) {
            Modo.LOCKED -> R.string.modo_bloqueado
            Modo.ALLOWED -> R.string.modo_permitido
            Modo.MAINTENANCE -> R.string.modo_mantenimiento
            Modo.SETUP -> R.string.modo_configuracion
        })
        Text(stringResource(if (datos.tipoEvidencia == TipoEvidencia.CONFIRMADA) R.string.estado_confirmado else R.string.ultimo_estado, modo))
        Text(stringResource(R.string.antiguedad_evidencia, evidencia.antiguedadSeg))
        Text(stringResource(when (evidencia.motivo) {
            Motivo.PARENT_LOCK -> R.string.causa_padre
            Motivo.NETWORK_LOST -> R.string.causa_red
            Motivo.CHANNEL_EXPIRED -> R.string.causa_canal
            Motivo.BOOT_AUTH_REQUIRED -> R.string.causa_arranque
            Motivo.ENFORCEMENT_ERROR -> R.string.causa_aplicacion
            Motivo.OK -> R.string.causa_ok
            else -> R.string.causa_otro
        }, evidencia.motivo.name))
        Text(stringResource(when (evidencia.resultadoUltimaOrden) {
            ResultadoEstado.NONE -> R.string.orden_ninguna
            ResultadoEstado.APPLIED -> R.string.orden_aplicada
            ResultadoEstado.ACCEPTED_PENDING_ENFORCEMENT -> R.string.orden_aceptada
            ResultadoEstado.ENFORCEMENT_FAILED -> R.string.orden_fallida
        }))
    }
    Text(stringResource(when (datos.pendiente) {
        IntencionPendiente.NINGUNA -> R.string.pendiente_ninguna
        IntencionPendiente.LOCAL -> R.string.pendiente_local
        IntencionPendiente.ENVIADA_SIN_CONFIRMAR -> R.string.pendiente_enviada
    }))
}
