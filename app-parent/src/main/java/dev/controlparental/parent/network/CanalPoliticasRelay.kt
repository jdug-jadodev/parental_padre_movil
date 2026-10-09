package dev.controlparental.parent.network

import dev.controlparental.parent.policy.CanalPoliticas

internal class CanalPoliticasRelay(
    private val conexion: RelayConnection,
    private val redValidada: () -> Boolean,
) : CanalPoliticas {
    override fun actual(): Long? = if (redValidada()) conexion.sesionPoliticas() else null
    override fun enviar(ticket: Long, frame: ByteArray): Boolean =
        actual() == ticket && conexion.enviarPolitica(ticket, frame)
}
