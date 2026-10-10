package dev.controlparental.parent.pairing

import dev.controlparental.parent.data.VinculoPersistido
import dev.controlparental.protocol.*

// Solo sourceSet JVM test: no APK, puerto productivo, IO ni autoridad concedida.
internal class FrameRevision(bytes: ByteArray) {
    init { require(bytes.size <= CodecProtocolo.MAX_FRAME) { "Sobre de revisión demasiado grande" } }
    private val original = bytes.copyOf()
    fun copia() = original.copyOf()
    override fun toString() = "FrameRevision(contenido oculto)"
}
internal enum class ProcedenciaRevision { REGISTRO_INTEGRO, LEGADO, NO_VERIFICABLE }
internal enum class EstatusResumen { POST_COMMIT_READBACK, PROVISIONAL, NO_VERIFICABLE }
internal enum class OrigenObservaciones { FICTICIAS_JVM } // No existe opción física/productiva.
internal enum class ClaseDiagnostico { COHERENTE, INCOMPLETA, RECHAZADA }
internal enum class CausaRevision { CONSISTENCIA_FICTICIA, FALTAN_EVIDENCIAS, SNAPSHOT_INVALIDO,
    CONTEXTO_CAMBIADO, IDENTIDAD_DISTINTA, CRIPTOGRAFIA_INVALIDA, ACEPTACION_AJENA,
    RESUMEN_INVALIDO, RESUMEN_DISTINTO, ESTADO_AJENO, SECUENCIA_INVALIDA,
    RECUPERACION_AJENA, OBSERVACIONES_AJENAS, AUTORIDAD_AUSENTE, UI_INACTIVA,
    CANAL_NO_VIGENTE, RELOJ_INVALIDO, EVIDENCIA_VENCIDA }
internal data class DiagnosticoRevision(val clase: ClaseDiagnostico, val causa: CausaRevision)
internal data class SnapshotRevision(val pairId: String, val publicaPadre: String, val publicaHijo: String,
    val relayOrigin: String, val vinculo: VinculoPersistido = VinculoPersistido.PREPARADO,
    val version: Int = 3, val procedencia: ProcedenciaRevision = ProcedenciaRevision.REGISTRO_INTEGRO)
internal data class ResumenRevision(val pairId: String, val huellaPadre: String, val huellaHijo: String,
    val relayOrigin: String, val estatus: EstatusResumen)
internal data class ContextoRevision(val snapshot: SnapshotRevision, val hashPayloadPair: String,
    val generacion: Long, val bootId: String, val iniciadaEn: Long)
internal data class SolicitudRevision(val syncNonce: String, val ticket: Long, val bootId: String,
    val childSessionId: String, val solicitadaEn: Long, val ultimoStateSeq: String = "0",
    val ultimoAcceptedSeq: String = "0")
internal data class OfflineRevision(val reto: FrameRevision, val respuesta: FrameRevision,
    val leidoEn: Long, val respondidoEn: Long)
internal data class ObservacionesRevision(val hashPayloadPair: String, val generacion: Long, val bootId: String,
    val observadasEn: Long, val commitObservado: Boolean, val mantenimientoObservado: Boolean,
    val replayRechazado: Boolean, val emergenciaAccesible: Boolean, val codigosGuardados: Boolean,
    val origen: OrigenObservaciones = OrigenObservaciones.FICTICIAS_JVM)
internal data class SenalesRevision(val publicaIdentidadLocal: String?, val generacion: Long,
    val ticket: Long?, val ultimoPongEn: Long?, val autoridadVigente: Boolean,
    val visible: Boolean, val cancelada: Boolean, val canalSano: Boolean, val redValidada: Boolean)
internal data class EntradaRevision(val snapshot: SnapshotRevision, val revision: ContextoRevision,
    val senales: SenalesRevision, val evaluadaEn: Long, val aceptacion: FrameRevision?,
    val resumen: ResumenRevision?, val estado: FrameRevision?, val solicitud: SolicitudRevision?,
    val offline: OfflineRevision?, val observaciones: ObservacionesRevision?)

// El llamador aporta snapshots fiables de señales/contexto. Esto NO comprueba
// Keystore, autenticación del adulto, procedencia real del resumen o hechos físicos.
internal object ValidadorRevisionVinculo {
    fun evaluar(e: EntradaRevision): DiagnosticoRevision {
        val s = e.snapshot; val r = e.revision; val l = e.senales; val ahora = e.evaluadaEn
        fun rechazo(causa: CausaRevision) = DiagnosticoRevision(ClaseDiagnostico.RECHAZADA, causa)
        fun incompleta() = DiagnosticoRevision(ClaseDiagnostico.INCOMPLETA, CausaRevision.FALTAN_EVIDENCIAS)
        if (s.vinculo != VinculoPersistido.PREPARADO || s.version != 3 ||
            s.procedencia != ProcedenciaRevision.REGISTRO_INTEGRO) return rechazo(CausaRevision.SNAPSHOT_INVALIDO)
        if (r.snapshot != s || r.generacion < 0 || l.generacion != r.generacion) return rechazo(CausaRevision.CONTEXTO_CAMBIADO)
        if (l.publicaIdentidadLocal != s.publicaPadre) return rechazo(CausaRevision.IDENTIDAD_DISTINTA)
        if (!l.visible || l.cancelada) return rechazo(CausaRevision.UI_INACTIVA)
        if (!l.autoridadVigente) return rechazo(CausaRevision.AUTORIDAD_AUSENTE)
        if (!l.canalSano || !l.redValidada || l.ticket == null || l.ticket <= 0 || l.ultimoPongEn == null)
            return rechazo(CausaRevision.CANAL_NO_VIGENTE)
        if (r.iniciadaEn < 0 || ahora < r.iniciadaEn || l.ultimoPongEn < 0 || l.ultimoPongEn > ahora)
            return rechazo(CausaRevision.RELOJ_INVALIDO)
        if (ahora - l.ultimoPongEn >= 45_000) return rechazo(CausaRevision.CANAL_NO_VIGENTE)
        val padre = clave(Rol.PARENT, s.publicaPadre) ?: return rechazo(CausaRevision.SNAPSHOT_INVALIDO)
        val hijo = clave(Rol.CHILD, s.publicaHijo) ?: return rechazo(CausaRevision.SNAPSHOT_INVALIDO)
        val pair = e.aceptacion ?: return incompleta()
        val docPair = verificar(pair, padre, Proposito.PAIR) ?: return rechazo(CausaRevision.CRIPTOGRAFIA_INVALIDA)
        val aceptacion = docPair.mensaje as? AceptacionVinculo ?: return rechazo(CausaRevision.CRIPTOGRAFIA_INVALIDA)
        if (aceptacion.pairId != s.pairId || aceptacion.parentPublicKeyB64 != s.publicaPadre ||
            aceptacion.childPublicKeyB64 != s.publicaHijo || aceptacion.relayOrigin != s.relayOrigin)
            return rechazo(CausaRevision.ACEPTACION_AJENA)
        if (docPair.payloadSha256B64 != r.hashPayloadPair) return rechazo(CausaRevision.CONTEXTO_CAMBIADO)
        val resumen = e.resumen ?: return incompleta()
        val formato = Regex("[0-9A-F]{4}( [0-9A-F]{4}){15}")
        if (resumen.estatus != EstatusResumen.POST_COMMIT_READBACK || !formato.matches(resumen.huellaPadre) ||
            !formato.matches(resumen.huellaHijo)) return rechazo(CausaRevision.RESUMEN_INVALIDO)
        if (resumen.pairId != s.pairId || resumen.relayOrigin != s.relayOrigin ||
            resumen.huellaPadre != PreparacionVinculo.huella(s.publicaPadre) ||
            resumen.huellaHijo != PreparacionVinculo.huella(s.publicaHijo)) return rechazo(CausaRevision.RESUMEN_DISTINTO)
        val frameEstado = e.estado ?: return incompleta()
        val docEstado = verificar(frameEstado, hijo, Proposito.MESSAGE) ?: return rechazo(CausaRevision.CRIPTOGRAFIA_INVALIDA)
        val estado = docEstado.mensaje as? EstadoDispositivo ?: return rechazo(CausaRevision.CRIPTOGRAFIA_INVALIDA)
        val solicitud = e.solicitud ?: return incompleta()
        if (solicitud.ticket != l.ticket || estado.pairId != s.pairId || estado.syncNonce != solicitud.syncNonce ||
            estado.bootId != r.bootId || estado.bootId != solicitud.bootId || estado.childSessionId != solicitud.childSessionId)
            return rechazo(CausaRevision.ESTADO_AJENO)
        if (solicitud.solicitadaEn < r.iniciadaEn || solicitud.solicitadaEn > ahora) return rechazo(CausaRevision.RELOJ_INVALIDO)
        if (ahora - solicitud.solicitadaEn >= 20_000) return rechazo(CausaRevision.EVIDENCIA_VENCIDA)
        val anteriorSeq = secuencia(solicitud.ultimoStateSeq) ?: return rechazo(CausaRevision.SECUENCIA_INVALIDA)
        val anteriorAccepted = secuencia(solicitud.ultimoAcceptedSeq) ?: return rechazo(CausaRevision.SECUENCIA_INVALIDA)
        if (estado.stateSeq.toLong() <= anteriorSeq || estado.lastAcceptedSeq.toLong() < anteriorAccepted)
            return rechazo(CausaRevision.SECUENCIA_INVALIDA)
        val offline = e.offline ?: return incompleta()
        val reto = verificar(offline.reto, hijo, Proposito.OFFLINE)?.mensaje as? RetoOffline
            ?: return rechazo(CausaRevision.CRIPTOGRAFIA_INVALIDA)
        val respuesta = verificar(offline.respuesta, padre, Proposito.OFFLINE)?.mensaje as? RespuestaOffline
            ?: return rechazo(CausaRevision.CRIPTOGRAFIA_INVALIDA)
        if (reto.action != AccionOffline.MAINTENANCE || reto.pairId != s.pairId || reto.bootId != r.bootId ||
            respuesta != RespuestaOffline(reto.pairId, reto.bootId, reto.action, reto.nonce)) return rechazo(CausaRevision.RECUPERACION_AJENA)
        if (offline.leidoEn < r.iniciadaEn || offline.respondidoEn < offline.leidoEn || offline.respondidoEn > solicitud.solicitadaEn)
            return rechazo(CausaRevision.RELOJ_INVALIDO)
        // Margen local al responder, no timestamp CP/1 ni ventana nueva al recibir.
        if (offline.respondidoEn - offline.leidoEn >= 240_000) return rechazo(CausaRevision.EVIDENCIA_VENCIDA)
        val o = e.observaciones ?: return incompleta()
        if (o.hashPayloadPair != r.hashPayloadPair || o.generacion != r.generacion || o.bootId != r.bootId)
            return rechazo(CausaRevision.OBSERVACIONES_AJENAS)
        if (o.observadasEn < offline.respondidoEn || o.observadasEn > solicitud.solicitadaEn)
            return rechazo(CausaRevision.RELOJ_INVALIDO)
        if (!o.commitObservado || !o.mantenimientoObservado || !o.replayRechazado || !o.emergenciaAccesible || !o.codigosGuardados)
            return incompleta()
        // Los flags STATE (Device Owner/APPLIED/MAINTENANCE) no acreditan hechos físicos.
        return DiagnosticoRevision(ClaseDiagnostico.COHERENTE, CausaRevision.CONSISTENCIA_FICTICIA)
    }
    private fun clave(rol: Rol, publica: String) = runCatching { ClaveVerificacion.desdeSpki(rol, publica) }.getOrNull()
    private fun verificar(frame: FrameRevision, clave: ClaveVerificacion, proposito: Proposito) =
        runCatching { CriptografiaProtocolo.verificarSobre(frame.copia(), clave, proposito) }.getOrNull()
    private fun secuencia(valor: String): Long? = if (Regex("0|[1-9][0-9]{0,18}").matches(valor)) valor.toLongOrNull() else null
}
