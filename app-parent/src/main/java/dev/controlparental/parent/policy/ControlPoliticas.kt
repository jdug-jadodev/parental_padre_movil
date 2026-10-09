package dev.controlparental.parent.policy

import dev.controlparental.parent.data.*
import dev.controlparental.protocol.*
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal interface CanalPoliticas {
    // Ticket de conexión autenticada, sana y visible; null no permite enviar.
    fun actual(): Long?
    fun enviar(ticket: Long, frame: ByteArray): Boolean
}

internal enum class FasePolitica { SIN_ESTADO, ESPERANDO_ESTADO, ESTADO_VERIFICADO, ENVIADA_SIN_CONFIRMAR,
    PENDIENTE_APLICACION, APLICADA, FALLO_APLICACION, RECHAZADA, REQUIERE_CONFIRMACION }
internal data class ResultadoPolitica(
    val fase: FasePolitica = FasePolitica.SIN_ESTADO,
    val mensajeHijo: Mensaje? = null,
)
internal class ConfirmacionIntencion internal constructor(val action: Accion, val durationSec: Int?)

// Casos de uso serializados. La firma acredita bytes; el contexto se valida aquí.
// No promociona PREPARADO ni certifica recuperación; P06 aportará la UI final.
internal class ControlPoliticas(
    private val store: ParentStore,
    private val canal: CanalPoliticas,
    private val ahora: () -> Long,
    private val exigirAutoridad: () -> Unit,
    private val firmar: (Mensaje) -> SobreFirmado,
    private val aleatorio: SecureRandom = SecureRandom(),
) {
    private class Contexto(val ticket: Long, val datos: MetadatosPadre, val nonce: String, val inicio: Long) {
        var estado: EstadoDispositivo? = null
        var utilizable = true
    }
    private class EnVuelo(val ticket: Long, val orden: OrdenPolitica, val frame: ByteArray, val hash: String, var plazo: Long) {
        var reintentos = 0
        var aceptada = false
    }
    private val mutex = Mutex()
    private var contexto: Contexto? = null
    private var intencion: ConfirmacionIntencion? = null
    private var vuelo: EnVuelo? = null
    private var ultimoEstado: EstadoDispositivo? = null
    private val mutable = MutableStateFlow(ResultadoPolitica())
    val resultado = mutable.asStateFlow()

    private fun vigente(c: Contexto) {
        mismaConexion(c)
        check(ahora() - c.inicio in 0 until VIGENCIA_LOCAL_MS) { "Estado/desafío local caducado" }
    }
    private fun mismaConexion(c: Contexto) {
        check(canal.actual() == c.ticket) { "Canal cambiado; sincroniza nuevamente" }
    }
    private fun identidad(datos: MetadatosPadre, c: Contexto) {
        check(datos.pairId == c.datos.pairId && datos.childPublicKeyB64 == c.datos.childPublicKeyB64 &&
            datos.parentPublicKeyB64 == c.datos.parentPublicKeyB64 && datos.vinculo == c.datos.vinculo)
    }
    private fun firmarExacto(mensaje: Mensaje, c: Contexto): ByteArray {
        vigente(c); exigirAutoridad()
        val frame = CodecProtocolo.codificarFrame(firmar(mensaje))
        check(CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.PARENT, c.datos.parentPublicKeyB64), Proposito.MESSAGE).mensaje == mensaje)
        vigente(c); exigirAutoridad()
        return frame
    }

    suspend fun sincronizar() = mutex.withLock { solicitarEstado() }
    private suspend fun solicitarEstado() {
        intencion = null; vuelo = null; contexto = null
        mutable.value = ResultadoPolitica()
        val ticket = canal.actual() ?: error("Sin canal sano en primer plano")
        exigirAutoridad()
        val datos = store.leer()
        check(datos.vinculo == VinculoPersistido.PREPARADO || datos.vinculo == VinculoPersistido.VINCULADO)
        val nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(aleatorio::nextBytes))
        val c = Contexto(ticket, datos, nonce, ahora())
        val frame = firmarExacto(SolicitudSincronizacion(datos.pairId!!, nonce), c)
        contexto = c
        mutable.value = ResultadoPolitica(FasePolitica.ESPERANDO_ESTADO)
        check(canal.enviar(ticket, frame)) { "No se encoló sincronización" }
    }

    suspend fun recibir(ticket: Long, frame: ByteArray) = mutex.withLock {
        val c = contexto ?: error("No hay sincronización actual")
        mismaConexion(c); check(ticket == c.ticket)
        val doc = CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.CHILD, c.datos.childPublicKeyB64!!), Proposito.MESSAGE)
        when (val mensaje = doc.mensaje) {
            is EstadoDispositivo -> {
                vigente(c)
                check(mensaje.pairId == c.datos.pairId && mensaje.syncNonce == c.nonce)
                c.estado?.let { anterior ->
                    check(mensaje.bootId == anterior.bootId && mensaje.childSessionId == anterior.childSessionId)
                }
                ultimoEstado?.takeIf { it.pairId == mensaje.pairId && it.bootId == mensaje.bootId &&
                    it.childSessionId == mensaje.childSessionId }?.let { anterior ->
                    check(secuenciaLocal(mensaje.stateSeq) > secuenciaLocal(anterior.stateSeq) &&
                        secuenciaLocal(mensaje.lastAcceptedSeq) >= secuenciaLocal(anterior.lastAcceptedSeq))
                }
                val datos = store.leer(); identidad(datos, c); vigente(c)
                val pendiente = datos.pendiente
                val original = pendiente?.frameB64?.let { verificarOrden(it, c) }
                val coincide = original != null && mensaje.lastCommandId == pendiente?.commandId &&
                    mensaje.lastAcceptedSeq == pendiente?.seq && mensaje.lastCommandPayloadSha256B64 == original.payloadSha256B64
                if (coincide) store.resolverIntencion(doc, mensaje.lastResult != ResultadoEstado.ACCEPTED_PENDING_ENFORCEMENT)
                else store.retenerEvidencia(doc)
                vigente(c)
                intencion = null
                if (coincide) vuelo = null
                c.estado = mensaje
                ultimoEstado = mensaje
                // Una actualización no renueva la ventana ni rearma un desafío consumido.
                mutable.value = ResultadoPolitica(if (coincide) fase(mensaje.lastResult)
                    else if (pendiente != null) FasePolitica.REQUIERE_CONFIRMACION else FasePolitica.ESTADO_VERIFICADO, mensaje)
            }
            is Confirmacion -> {
                val v = vuelo ?: error("ACK sin envío en la conexión actual")
                check(v.ticket == ticket && mensaje.pairId == v.orden.pairId && mensaje.bootId == v.orden.bootId &&
                    mensaje.childSessionId == v.orden.childSessionId && mensaje.commandId == v.orden.commandId &&
                    mensaje.seq == v.orden.seq && mensaje.commandPayloadSha256B64 == v.hash)
                store.resolverIntencion(doc, mensaje.result != ResultadoAck.ACCEPTED_PENDING_ENFORCEMENT)
                mismaConexion(c)
                v.aceptada = true
                if (mensaje.result != ResultadoAck.ACCEPTED_PENDING_ENFORCEMENT) vuelo = null
                mutable.value = ResultadoPolitica(when (mensaje.result) {
                    ResultadoAck.APPLIED -> FasePolitica.APLICADA
                    ResultadoAck.ACCEPTED_PENDING_ENFORCEMENT -> FasePolitica.PENDIENTE_APLICACION
                    ResultadoAck.ENFORCEMENT_FAILED -> FasePolitica.FALLO_APLICACION
                    ResultadoAck.REJECTED -> FasePolitica.RECHAZADA
                }, mensaje)
            }
            else -> error("No es STATE/ACK del hijo")
        }
    }

    suspend fun prepararIntencion(action: Accion, durationSec: Int?): ConfirmacionIntencion = mutex.withLock {
        val c = contexto ?: error("Sin estado fresco")
        comprobarOrden(c)
        exigirAutoridad()
        val datos = store.leer(); identidad(datos, c)
        check(datos.vinculo == VinculoPersistido.VINCULADO && datos.pendiente == null)
        val e = c.estado!!
        CodecProtocolo.codificarPayload(OrdenPolitica(datos.pairId!!, e.bootId, e.childSessionId,
            UUID.randomUUID().toString(), e.commandNonce, "1", action, durationSec))
        vigente(c)
        ConfirmacionIntencion(action, durationSec).also { intencion = it }
    }

    private fun comprobarOrden(c: Contexto) {
        vigente(c)
        val e = c.estado ?: error("Sin STATE verificado")
        check(c.utilizable && e.isDeviceOwner && e.networkValidated && e.channelHealthy &&
            e.mode in listOf(Modo.LOCKED, Modo.ALLOWED) &&
            e.reason !in listOf(Motivo.NOT_PROVISIONED, Motivo.BEFORE_USER_UNLOCK, Motivo.BOOT_AUTH_REQUIRED,
                Motivo.STATE_CORRUPT, Motivo.ENFORCEMENT_ERROR, Motivo.KEY_UNAVAILABLE)) { "Hijo no apto; sincroniza/revisa configuración" }
    }

    suspend fun enviarConfirmada(confirmacion: ConfirmacionIntencion) = mutex.withLock {
        check(intencion === confirmacion) { "Confirmación descartada o ajena" }
        val c = contexto ?: error("Sin sincronización")
        comprobarOrden(c); exigirAutoridad()
        intencion = null; c.utilizable = false
        val e = c.estado!!
        val reserva = store.reservarIntencion(confirmacion.action, confirmacion.durationSec, e.lastAcceptedSeq)
        vigente(c)
        val orden = OrdenPolitica(c.datos.pairId!!, e.bootId, e.childSessionId, reserva.commandId,
            e.commandNonce, reserva.seq, reserva.action, reserva.durationSec)
        val frame = firmarExacto(orden, c)
        store.guardarComandoFirmado(reserva.commandId, frame)
        vigente(c); exigirAutoridad()
        // Registro conservador ANTES de encolar: un cierre no permite suponer no entrega.
        store.marcarEnviada(reserva.commandId)
        vigente(c); exigirAutoridad()
        val doc = CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.PARENT, c.datos.parentPublicKeyB64), Proposito.MESSAGE)
        vuelo = EnVuelo(c.ticket, orden, frame.copyOf(), doc.payloadSha256B64, ahora() + ESPERA_ACK_MS)
        mutable.value = ResultadoPolitica(FasePolitica.ENVIADA_SIN_CONFIRMAR)
        vigente(c); exigirAutoridad()
        if (!canal.enviar(c.ticket, frame)) vuelo = null // La intención duradera no se borra.
    }

    suspend fun avanzarReloj() = mutex.withLock {
        val c = contexto ?: return@withLock
        val v = vuelo
        if (canal.actual() != c.ticket || (ahora() - c.inicio !in 0 until VIGENCIA_LOCAL_MS &&
                (v == null || v.aceptada))) {
            contexto = null; intencion = null; vuelo = null
            mutable.value = mutable.value.copy(fase = FasePolitica.SIN_ESTADO)
            return@withLock
        }
        if (v == null) return@withLock
        if (v.aceptada || ahora() < v.plazo) return@withLock
        exigirAutoridad()
        val datos = store.leer(); identidad(datos, c); mismaConexion(c)
        val pendiente = datos.pendiente ?: error("Sin intención duradera para reintentar")
        check(pendiente.commandId == v.orden.commandId &&
            pendiente.frameB64 == Base64.getUrlEncoder().withoutPadding().encodeToString(v.frame))
        exigirAutoridad()
        if (v.reintentos >= 2) solicitarEstado()
        else {
            v.reintentos++; v.plazo = ahora() + ESPERA_ACK_MS
            // Los mismos bytes, nunca una nueva firma, ID, secuencia o duración.
            if (!canal.enviar(v.ticket, v.frame.copyOf())) vuelo = null
        }
    }

    suspend fun descartarPendiente(commandId: String) = mutex.withLock {
        exigirAutoridad()
        store.cancelarIntencion(commandId)
        intencion = null; vuelo = null; contexto = null
        mutable.value = ResultadoPolitica(FasePolitica.REQUIERE_CONFIRMACION)
    }

    private fun verificarOrden(frameB64: String, c: Contexto) = CriptografiaProtocolo.verificarSobre(
        Base64.getUrlDecoder().decode(frameB64), ClaveVerificacion.desdeSpki(Rol.PARENT, c.datos.parentPublicKeyB64), Proposito.MESSAGE)

    private fun fase(result: ResultadoEstado) = when (result) {
        ResultadoEstado.APPLIED -> FasePolitica.APLICADA
        ResultadoEstado.ACCEPTED_PENDING_ENFORCEMENT -> FasePolitica.PENDIENTE_APLICACION
        ResultadoEstado.ENFORCEMENT_FAILED -> FasePolitica.FALLO_APLICACION
        ResultadoEstado.NONE -> error("No hay aceptación reconciliable")
    }

    companion object {
        // Margen local conservador medido desde la petición, no al recibir STATE.
        // La validez de 30 s la decide siempre el hijo con su reloj; no se inventa timestamp CP/1.
        const val VIGENCIA_LOCAL_MS = 20_000L
        const val ESPERA_ACK_MS = 5_000L
    }
}
