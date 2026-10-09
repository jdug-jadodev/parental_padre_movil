package dev.controlparental.parent.presentation

import dev.controlparental.parent.data.*
import dev.controlparental.parent.policy.*
import dev.controlparental.protocol.Accion

internal interface AccionesControl {
    suspend fun vista(): VistaPoliticas
    suspend fun guardar(action: Accion, durationSec: Int?, reemplazarId: String?): IntencionLocal
    suspend fun preparar(local: IntencionLocal): ConfirmacionIntencion
    suspend fun enviar(confirmacion: ConfirmacionIntencion)
    suspend fun descartarLocal(id: String)
    suspend fun descartarOrden(id: String)
    suspend fun cerrarConfirmacion()
}

internal class AccionesControlReal(private val store: ParentStore, private val politicas: ControlPoliticas) : AccionesControl {
    override suspend fun vista() = politicas.vista()
    override suspend fun guardar(action: Accion, durationSec: Int?, reemplazarId: String?) =
        store.guardarIntencionLocal(action, durationSec, reemplazarId)
    override suspend fun preparar(local: IntencionLocal): ConfirmacionIntencion =
        politicas.prepararIntencion(local.action, local.durationSec).also { check(it.intencionLocalId == local.id) }
    override suspend fun enviar(confirmacion: ConfirmacionIntencion) = politicas.enviarConfirmada(confirmacion)
    override suspend fun descartarLocal(id: String) = store.descartarIntencionLocal(id)
    override suspend fun descartarOrden(id: String) = politicas.descartarPendiente(id)
    override suspend fun cerrarConfirmacion() = politicas.descartarConfirmacion()
}
