package dev.controlparental.parent.presentation

import dev.controlparental.parent.policy.VistaPoliticas
import dev.controlparental.protocol.*

internal fun duracionControl(texto: String): Int? =
    if (Regex("[0-9]{1,6}").matches(texto)) texto.toIntOrNull()?.takeIf { it in 60..604800 } else null

internal fun nombreAccion(accion: Accion) = when (accion) {
    Accion.LOCK -> "Bloquear"
    Accion.LOCK_FOR -> "Bloquear durante…"
    Accion.ALLOW -> "Permitir"
    Accion.ALLOW_FOR -> "Permitir durante…"
}

internal fun avisoAccion(accion: Accion) = when (accion) {
    Accion.LOCK -> "El bloqueo seguirá hasta una nueva orden válida o recuperación presencial."
    Accion.LOCK_FOR -> "Se permitirá el uso al terminar, si conserva conexión y no se reinicia."
    Accion.ALLOW -> "Hasta que bloquees, pierda conexión o reinicie."
    Accion.ALLOW_FOR -> "Volverá a bloquearse al terminar."
}

internal fun resumenControl(vista: VistaPoliticas): List<String> {
    val mensaje = vista.evidencia ?: return listOf("Estado del hijo: sin evidencia verificada")
    val mode: Modo; val reason: Motivo; val remaining: Int?; val resultado: String
    when (mensaje) {
        is EstadoDispositivo -> {
            mode = mensaje.mode; reason = mensaje.reason; remaining = mensaje.remainingSec
            resultado = when (mensaje.lastResult) {
                ResultadoEstado.NONE -> "sin orden aceptada"
                ResultadoEstado.APPLIED -> "aplicada según el hijo"
                ResultadoEstado.ACCEPTED_PENDING_ENFORCEMENT -> "aceptada, aplicación todavía pendiente"
                ResultadoEstado.ENFORCEMENT_FAILED -> "fallo de aplicación"
            }
        }
        is Confirmacion -> {
            mode = mensaje.mode; reason = mensaje.reason; remaining = mensaje.remainingSec
            resultado = when (mensaje.result) {
                ResultadoAck.APPLIED -> "aplicada según el hijo"
                ResultadoAck.ACCEPTED_PENDING_ENFORCEMENT -> "aceptada, aplicación todavía pendiente"
                ResultadoAck.ENFORCEMENT_FAILED -> "fallo de aplicación"
                ResultadoAck.REJECTED -> "rechazada; no acredita aplicación"
            }
        }
        else -> return listOf("Estado del hijo: sin evidencia verificada")
    }
    val modo = when (mode) {
        Modo.LOCKED -> "bloqueado"
        Modo.ALLOWED -> "permitido"
        Modo.MAINTENANCE -> "mantenimiento presencial"
        Modo.SETUP -> "configuración pendiente"
    }
    val causa = when (reason) {
        Motivo.NETWORK_LOST -> "falta de conexión"
        Motivo.CHANNEL_EXPIRED -> "canal del hijo caducado"
        Motivo.PARENT_LOCK -> "orden del padre"
        Motivo.SYSTEM_KEYGUARD, Motivo.BEFORE_USER_UNLOCK -> "bloqueo inicial de Android"
        Motivo.BOOT_AUTH_REQUIRED -> "nuevo arranque: requiere autorización"
        Motivo.ENFORCEMENT_ERROR -> "no se comprobaron las restricciones efectivas"
        Motivo.NOT_DEVICE_OWNER, Motivo.NOT_PROVISIONED -> "dispositivo no configurado correctamente"
        Motivo.TIMER_EXPIRED -> "tiempo terminado"
        Motivo.OK -> "sin bloqueo adicional informado"
        else -> "código del hijo ${reason.name}"
    }
    return buildList {
        add("${if (vista.fresca) "Estado del hijo verificado" else "Último estado confirmado"}: $modo")
        add("Causa: $causa")
        add("Última respuesta de orden: $resultado")
        if (mode == Modo.LOCKED && reason == Motivo.NETWORK_LOST && resultado == "aplicada según el hijo")
            add("Orden aplicada; bloqueo por falta de conexión")
        if (mensaje is EstadoDispositivo && !mensaje.isDeviceOwner)
            add("Dispositivo no configurado correctamente: sin administrador del dispositivo")
        add(vista.antiguedadSeg?.let { "Evidencia recibida hace $it s; no es un contador remoto en vivo" }
            ?: "Antigüedad desconocida tras reabrir; sincroniza para obtener evidencia actual")
        remaining?.let { add("Tiempo restante informado: $it s. No se descuenta sin otra respuesta del hijo.") }
    }
}
