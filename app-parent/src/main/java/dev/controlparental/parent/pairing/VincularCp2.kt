package dev.controlparental.parent.pairing

import dev.controlparental.parent.data.*
import dev.controlparental.protocol.cp2.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.UUID

internal data class EstadoAltaPairCp2(val publica: String, val legadoHash: String, val pendiente: RegistroPairCp2?)

// Orquesta la fuente real legada de solo lectura; no traduce aceptación/hashes CP1.
internal class VincularCp2(private val legado: ParentStore, private val area: PairStoreCp2,
    private val publica: () -> String, private val autoridadVigente: () -> Unit,
    private val firmante: (ByteArray, String) -> ByteArray, private val reloj: () -> Long) {
    private val mutex = Mutex()
    private val proceso = UUID.randomUUID().toString()
    private val json = Json { encodeDefaults = true }
    private var revision = 0L
    private var alta: AltaCp2? = null
    private val preparador = PrepararPairCp2(reloj, { alta ?: error("Alta no autorizada") }, autoridadVigente,
        { firmante(it, alta?.publica ?: error("Alta descartada")) })
    private var ticket: TicketPairCp2? = null
    fun descartar() { revision++; preparador.descartar(); alta = null; ticket = null }
    private suspend fun fuente(): EstadoAltaPairCp2 {
        // Ausencia de archivo NO es un estado virgen autorizado.
        val datos = legado.inspeccionarSinMigrar()
        val p = publica(); VinculacionCp2.publica(p)
        check(p == datos.parentPublicKeyB64) { "Identidad distinta: requiere recuperación" }
        validarMetadatos(datos)
        check(datos.vinculo == VinculoPersistido.SIN_VINCULAR || datos.vinculo == VinculoPersistido.PREPARADO) { "Vínculo activo/incierto: requiere migración acordada" }
        check(datos.contadorLocal == "0" && datos.pendiente == null && datos.ultimaEvidenciaFrameB64 == null && datos.intencionLocal == null) { "Evidencia/órdenes legadas: alta bloqueada" }
        check(datos.vinculo != VinculoPersistido.PREPARADO || datos.pairAcceptanceFrameB64 != null) { "Preparación legada incompleta" }
        val hash = VinculacionCp2.b64(MessageDigest.getInstance("SHA-256").digest(json.encodeToString(datos).toByteArray(Charsets.UTF_8)))
        return EstadoAltaPairCp2(p, hash, area.leer(p, hash))
    }
    suspend fun inspeccionar(): EstadoAltaPairCp2 = mutex.withLock { fuente() }
    suspend fun preparar(oferta: ByteArray, origen: String, recibidaEn: Long, confirmoAltaNueva: Boolean): TicketPairCp2 = mutex.withLock {
        descartar(); val turno = revision
        try {
            check(confirmoAltaNueva) { "Autoriza explícitamente nuevo enrolamiento CP/2" }
            autoridadVigente()
            val f = fuente(); check(f.pendiente == null) { "Aceptación CP/2 pendiente: no repetir ni reemplazar" }
            check(turno == revision); autoridadVigente()
            alta = AltaCp2(f.publica, f.legadoHash, proceso, true, true)
            preparador.preparar(oferta, origen, recibidaEn).also { ticket = it }
        } catch (e: Exception) { descartar(); throw e }
    }
    suspend fun firmar(t: TicketPairCp2, huellas: Boolean, origen: Boolean, codigosGuardados: Boolean): PairVerificadoCp2 = mutex.withLock {
        val turno = revision
        try {
            check(ticket === t)
            val f = fuente()
            check(f.pendiente == null && f.publica == t.alta.publica && f.legadoHash == t.alta.generacion && turno == revision)
            val d = preparador.firmar(t, huellas, origen, codigosGuardados)
            QrLocal.generar(d.frame().toString(Charsets.UTF_8)) // Capacidad antes de escribir/exponer.
            check(turno == revision && reloj() in (t.vence - 300_000) until t.vence)
            val antes = fuente(); check(antes == f); autoridadVigente()
            check(turno == revision)
            area.guardarNuevo(d, f.publica, f.legadoHash)
            val despues = fuente()
            check(despues.publica == f.publica && despues.legadoHash == f.legadoHash && despues.pendiente != null)
            autoridadVigente(); check(turno == revision && reloj() in (t.vence - 300_000) until t.vence)
            d // Única salida que permite mostrar QR, tras commit público/readback.
        } finally { descartar() }
    }
    suspend fun exportarPublico(): String = mutex.withLock {
        val f = fuente(); val r = f.pendiente ?: error("Sin snapshot CP/2 pendiente")
        val a = VinculacionCp2.verificar(java.util.Base64.getUrlDecoder().decode(r.frameB64), f.publica).aceptacion
        "CP2_STAGE=PENDIENTE_CONFIRMACION_HIJO\nPROTOCOL=cp.v2\nPAIR_ID=${a.pairId}\nPARENT_PUBLIC_KEY_B64=${a.parentPublicKeyB64}\nCHILD_PUBLIC_KEY_B64=${a.childPublicKeyB64}\nPUBLIC_ORIGIN=${a.relayOrigin}\n"
    }
}
