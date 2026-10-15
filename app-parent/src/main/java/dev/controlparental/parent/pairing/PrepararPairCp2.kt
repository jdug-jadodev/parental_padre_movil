package dev.controlparental.parent.pairing

import dev.controlparental.protocol.cp2.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

// Snapshot de la política de alta validada, nunca de datos del QR.
internal data class AltaCp2(val publica: String, val generacion: String, val boot: String,
    val ausenciaCoherente: Boolean, val autorizada: Boolean)

internal class TicketPairCp2 internal constructor(internal val turno: Long, internal val alta: AltaCp2,
    internal val vence: Long, internal val aceptacion: AceptacionCp2,
    internal val codigos: MutableMap<PropositoRecuperacion, String>, payload: ByteArray) {
    private val original = payload.copyOf()
    fun payload(): ByteArray = original.copyOf()
    fun mostrarCodigos(): Map<PropositoRecuperacion, String> = codigos.toMap()
    internal fun borrar() { codigos.clear() }
    override fun toString() = "TicketPairCp2(contenido oculto)"
}

// Solo RAM; sin referencia a ParentStore ni método commit, nunca promoción de vínculo.
internal class PrepararPairCp2(private val reloj: () -> Long, private val snapshot: () -> AltaCp2,
    private val autenticarVigente: () -> Unit, private val firmante: (ByteArray) -> ByteArray,
    private val aleatorio: SecureRandom = SecureRandom()) {
    private var turno = 0L
    private var ticket: TicketPairCp2? = null
    @Synchronized fun descartar() { turno++; ticket?.borrar(); ticket = null }
    @Synchronized fun preparar(oferta: ByteArray, origen: String, recibidaEn: Long = reloj()): TicketPairCp2 {
        descartar()
        val alta = snapshot()
        check(alta.ausenciaCoherente && alta.autorizada) { "REQUIERE_MIGRACION o alta no autorizada" }
        VinculacionCp2.publica(alta.publica)
        val o = VinculacionCp2.leerOferta(oferta); VinculacionCp2.origen(origen)
        val pairId = UUID.randomUUID().toString()
        val codigos = mutableMapOf<PropositoRecuperacion, String>()
        try {
            for (p in PropositoRecuperacion.entries) {
                var c: String; var intentos = 0
                do {
                    check(intentos++ < 8) { "Fuente aleatoria sin unicidad" }
                    val bytes = ByteArray(20); aleatorio.nextBytes(bytes)
                    c = RecoveryCodeGenerator.base32(bytes); bytes.fill(0)
                } while (c in codigos.values)
                codigos[p] = c
            }
            fun hash(p: PropositoRecuperacion) = hashRecuperacion(pairId, p, codigos.getValue(p))
            val a = AceptacionCp2(pairId, o.enrollId, o.enrollNonce, alta.publica, o.childPublicKeyB64, origen,
                RecuperacionCp2(hash(PropositoRecuperacion.MAINTENANCE), hash(PropositoRecuperacion.RECOVER_PARENT), hash(PropositoRecuperacion.RETIRE)))
            val ahora = reloj(); check(recibidaEn in 0..Long.MAX_VALUE - 300_000 && ahora in recibidaEn until recibidaEn + 300_000)
            return TicketPairCp2(turno, alta, recibidaEn + 300_000, a, codigos, VinculacionCp2.payload(a)).also { ticket = it }
        } catch (e: Exception) { codigos.clear(); throw e }
    }
    private fun exigir(t: TicketPairCp2) {
        check(t === ticket && t.turno == turno && reloj() in (t.vence - 300_000) until t.vence && snapshot() == t.alta &&
            t.alta.autorizada && t.alta.ausenciaCoherente && t.codigos.size == 3) { "Preparación caducada o contexto distinto" }
        val h = t.aceptacion.recoveryHashes
        check(listOf(h.MAINTENANCE, h.RECOVER_PARENT, h.RETIRE) == PropositoRecuperacion.entries.map {
            hashRecuperacion(t.aceptacion.pairId, it, t.codigos.getValue(it))
        })
    }
    @Synchronized fun firmar(t: TicketPairCp2, huellas: Boolean, origen: Boolean, guardados: Boolean): PairVerificadoCp2 {
        try {
            exigir(t); check(huellas && origen && guardados)
            autenticarVigente(); exigir(t)
            val verificado = VinculacionCp2.firmar(t.payload(), t.alta.publica) { bytes ->
                autenticarVigente(); exigir(t); firmante(bytes)
            }
            autenticarVigente(); exigir(t)
            return verificado
        } finally { descartar() }
    }
    companion object {
        fun hashRecuperacion(pairId: String, p: PropositoRecuperacion, codigo: String): String =
            VinculacionCp2.b64(MessageDigest.getInstance("SHA-256").digest(
                "CPv2/RECOVERY\n$pairId\n${p.name}\n${RecoveryCodeGenerator.normalizar(codigo)}".toByteArray(Charsets.UTF_8)))
    }
}
