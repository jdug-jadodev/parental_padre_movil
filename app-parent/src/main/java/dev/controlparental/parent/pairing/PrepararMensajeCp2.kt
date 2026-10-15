package dev.controlparental.parent.pairing

import dev.controlparental.parent.security.AutoridadPadre
import dev.controlparental.protocol.cp2.*

// Puerto de confianza SIN implementación productiva en ORD01. No recibe QR/AUTH_OK/PONG.
// El futuro adaptador debe acreditar snapshot confirmado y STATE firmado/contextual vivo,
// incluida vigencia del reto; retornar null al perder conexión, contexto o autoridad.
internal interface FuentePreparacionMessageCp2 {
    fun snapshotConfirmado(): SnapshotMessageCp2?
    fun estadoVerificado(): ContextoStateMessageCp2?
}
internal data class SnapshotMessageCp2(val pairId: String, val publicaPadre: String, val revisionFuente: String,
    val confirmadoCompatible: Boolean, val protocol: String = "cp.v2")
internal data class ContextoStateMessageCp2(val pairId: String, val syncNonce: String, val action: AccionCp2,
    val bootId: String, val childSessionId: String, val revision: String, val commandNonce: String,
    val lastAcceptedSeq: String)

// Ticket RAM opaco ligado al caso: no reconstrucción tras reinicio ni renovación con STATE/PONG.
internal class TicketSyncMessageCp2 internal constructor(internal val dueno: Any, internal val inicio: Long,
    internal val snapshot: SnapshotMessageCp2, internal val sync: SyncRequestCp2)
internal class TicketPolicyMessageCp2 internal constructor(internal val sync: TicketSyncMessageCp2,
    internal val estado: ContextoStateMessageCp2, internal val policy: SetPolicyCp2)

// Preparación/firma solamente: NO almacena, reserva seq, envía, reintenta ni confirma APPLIED.
// No instanciar desde MAIN hasta implementar fuente real y persistencia de intención/secuencia.
internal class PrepararMensajeCp2(private val fuente: FuentePreparacionMessageCp2,
    private val autoridad: AutoridadPadre, private val ahora: () -> Long) {
    private var vigente: TicketSyncMessageCp2? = null
    fun cancelar() { vigente = null }
    fun iniciarSync(syncNonce: String, accion: AccionCp2?): TicketSyncMessageCp2 {
        cancelar()
        val inicio = ahora(); require(inicio >= 0); val s = exigirSnapshot()
        val sync = SyncRequestCp2(s.pairId, syncNonce, accion)
        MensajesPadreCp2.payload(sync)
        return TicketSyncMessageCp2(this, inicio, s, sync).also { vigente = it }
    }
    private fun exigirSnapshot(): SnapshotMessageCp2 {
        val s = requireNotNull(fuente.snapshotConfirmado())
        check(s.confirmadoCompatible && s.protocol == "cp.v2" && s.revisionFuente.isNotBlank())
        check(s.publicaPadre == autoridad.exportarPublica()); return s
    }
    private fun validar(t: TicketSyncMessageCp2) {
        check(t.dueno === this && vigente === t)
        val n = ahora(); check(n >= t.inicio && n - t.inicio in 0 until 20_000L)
        check(exigirSnapshot() == t.snapshot)
    }
    fun firmarSync(t: TicketSyncMessageCp2): MessageVerificadoCp2 = protegido {
        validar(t)
        autoridad.firmarMessageCp2(MensajesPadreCp2.payload(t.sync), t.snapshot.publicaPadre).also { validar(t) }
    }
    fun prepararPolicy(t: TicketSyncMessageCp2, commandId: String, seqPropuesta: String,
        minutos: Int?): TicketPolicyMessageCp2 = protegido {
        validar(t)
        val e = requireNotNull(fuente.estadoVerificado())
        check(e.pairId == t.snapshot.pairId && e.syncNonce == t.sync.syncNonce && e.action == t.sync.requestedAction)
        check(MensajesPadreCp2.contador(seqPropuesta, true) > MensajesPadreCp2.contador(e.lastAcceptedSeq))
        if (e.action == AccionCp2.ALLOW_FOR) require(minutos != null && minutos in 1..1440) else require(minutos == null)
        val p = SetPolicyCp2(e.pairId, e.bootId, e.childSessionId, commandId, seqPropuesta, e.revision,
            e.commandNonce, e.action, minutos?.times(60))
        MensajesPadreCp2.payload(p); validar(t)
        TicketPolicyMessageCp2(t, e, p)
    }
    // Confirmación explícita de la propuesta exacta; un conflicto retira todo el sync.
    fun confirmarYFirmar(t: TicketPolicyMessageCp2, confirmada: SetPolicyCp2): MessageVerificadoCp2 = protegido {
        validar(t.sync); check(t.policy == confirmada && fuente.estadoVerificado() == t.estado)
        autoridad.firmarMessageCp2(MensajesPadreCp2.payload(t.policy), t.sync.snapshot.publicaPadre).also {
            validar(t.sync); check(fuente.estadoVerificado() == t.estado); cancelar()
        }
    }
    private inline fun <T> protegido(bloque: () -> T): T = try { bloque() } catch (e: Exception) { cancelar(); throw e }
}
