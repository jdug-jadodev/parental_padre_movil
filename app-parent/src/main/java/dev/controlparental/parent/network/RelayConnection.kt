package dev.controlparental.parent.network

import dev.controlparental.protocol.*
import java.net.URI
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class FaseRelay { DESCONECTADO, CONECTANDO, ESPERANDO_RETO, ESPERANDO_AUTH_OK, AUTENTICADO, REINTENTO, AUTENTICACION_REQUERIDA, ERROR_SEGURO }
internal data class EstadoRelay(
    val fase: FaseRelay = FaseRelay.DESCONECTADO,
    val hijoIndicativo: Boolean? = null,
    val canalSano: Boolean = false,
)
internal data class ConfiguracionRelay(val pairId: String, val relayOrigin: String, val publicaPadre: String) {
    init {
        ClaveVerificacion.desdeSpki(Rol.PARENT, publicaPadre)
        CodecProtocolo.codificarPayload(PruebaAutenticacion(pairId, Rol.PARENT, pairId, "A".repeat(43)))
        val uri = URI(relayOrigin)
        require(uri.scheme == "https" && uri.host != null && uri.rawUserInfo == null && uri.rawPath.isEmpty() &&
            uri.rawQuery == null && uri.rawFragment == null && (uri.port == -1 || uri.port in 1..65535) &&
            uri.toASCIIString() == relayOrigin)
    }
    val endpoint get() = "wss://${relayOrigin.removePrefix("https://")}/ws"
}
internal interface SocketRelay { fun enviar(texto: String): Boolean; fun cancelar() }
internal interface EventosRelay {
    fun abierto(subprotocolo: String?)
    fun texto(texto: String)
    fun fallo(permanente: Boolean)
}
internal fun interface TransporteRelay { fun abrir(endpoint: String, eventos: EventosRelay): SocketRelay }

// Estados del canal, nunca evidencia de restricciones aplicadas. Reloj monotónico inyectado.
internal class RelayConnection(
    private val transporte: TransporteRelay,
    private val ahora: () -> Long,
    private val exigirAutoridad: () -> Unit,
    private val firmar: (PruebaAutenticacion) -> SobreFirmado,
    private val aleatorio: SecureRandom = SecureRandom(),
) {
    private val visible = AtomicBoolean(false)
    private val mutable = MutableStateFlow(EstadoRelay())
    val estado = mutable.asStateFlow()
    private var configuracion: ConfiguracionRelay? = null
    private var solicitado = false
    private var socket: SocketRelay? = null
    private var generacion = 0L
    private var limite = 0L
    private var connectionId: String? = null
    private var ping: String? = null
    private var proximoPing = 0L
    private var reintentoEn = 0L
    private var fallos = 0

    @Synchronized fun conectar(config: ConfiguracionRelay) {
        if (!visible.get()) return
        if (configuracion == config && socket != null) return
        cerrarSocket()
        configuracion = config; solicitado = true; fallos = 0
        iniciar()
    }
    @Synchronized fun primerPlano() { visible.set(true); if (solicitado && socket == null) iniciar() }
    fun segundoPlano() {
        visible.set(false) // Invalida envío/callback incluso si hay firma en curso.
        synchronized(this) { cerrarSocket(); mutable.value = EstadoRelay() }
    }
    @Synchronized fun desconectar() { solicitado = false; cerrarSocket(); mutable.value = EstadoRelay() }

    private fun iniciar() {
        if (!visible.get() || !solicitado || socket != null) return
        try { exigirAutoridad() }
        catch (_: Exception) { mutable.value = EstadoRelay(FaseRelay.AUTENTICACION_REQUERIDA); return }
        val config = configuracion ?: return
        val ticket = ++generacion
        mutable.value = EstadoRelay(FaseRelay.CONECTANDO)
        limite = ahora() + 10_000
        try {
            val abierto = transporte.abrir(config.endpoint, object : EventosRelay {
                override fun abierto(subprotocolo: String?) = synchronized(this@RelayConnection) {
                    if (actual(ticket)) {
                        if (subprotocolo != "cp.v1" || mutable.value.fase != FaseRelay.CONECTANDO) terminar(true)
                        else mutable.value = EstadoRelay(FaseRelay.ESPERANDO_RETO)
                    }
                }
                override fun texto(texto: String) = synchronized(this@RelayConnection) {
                    if (actual(ticket)) recibir(texto)
                }
                override fun fallo(permanente: Boolean) = synchronized(this@RelayConnection) {
                    if (actual(ticket)) terminar(permanente)
                }
            })
            // Un transporte puede fallar dentro de abrir; no conservar un socket huérfano.
            if (actual(ticket)) socket = abierto else abierto.cancelar()
        } catch (_: Exception) { terminar(true) }
    }
    private fun actual(ticket: Long) = visible.get() && solicitado && ticket == generacion

    private fun recibir(texto: String) {
        try {
            require(texto.length <= CodecProtocolo.MAX_FRAME)
            val mensaje = CodecProtocolo.leerFrame(texto.toByteArray(Charsets.UTF_8))
            when (mensaje) {
                is RetoConexion -> {
                    require(mutable.value.fase == FaseRelay.ESPERANDO_RETO && ahora() < limite)
                    val config = configuracion!!
                    connectionId = mensaje.connectionId
                    limite = ahora() + mensaje.timeoutSec * 1000L
                    val prueba = PruebaAutenticacion(config.pairId, Rol.PARENT, mensaje.connectionId, mensaje.nonce)
                    val sobre = try { exigirAutoridad(); firmar(prueba) }
                    catch (_: Exception) {
                        cerrarSocket(); mutable.value = EstadoRelay(FaseRelay.AUTENTICACION_REQUERIDA); return
                    }
                    val frame = CodecProtocolo.codificarFrame(sobre)
                    require(CriptografiaProtocolo.verificarSobre(frame,
                        ClaveVerificacion.desdeSpki(Rol.PARENT, config.publicaPadre), Proposito.AUTH).mensaje == prueba)
                    require(ahora() < limite)
                    try { exigirAutoridad() }
                    catch (_: Exception) {
                        cerrarSocket(); mutable.value = EstadoRelay(FaseRelay.AUTENTICACION_REQUERIDA); return
                    }
                    if (!visible.get()) return
                    mutable.value = EstadoRelay(FaseRelay.ESPERANDO_AUTH_OK)
                    enviar(frame.toString(Charsets.UTF_8))
                }
                is AutenticacionAceptada -> {
                    require(mutable.value.fase == FaseRelay.ESPERANDO_AUTH_OK && ahora() < limite && mensaje.connectionId == connectionId)
                    mutable.value = EstadoRelay(FaseRelay.AUTENTICADO)
                    proximoPing = ahora(); avanzarReloj()
                }
                is RespuestaLatido -> {
                    require(mutable.value.fase == FaseRelay.AUTENTICADO && mensaje.nonce == ping && ahora() < limite)
                    ping = null; fallos = 0; proximoPing = ahora() + 10_000
                    mutable.value = mutable.value.copy(canalSano = true)
                }
                is Presencia -> {
                    require(mutable.value.fase == FaseRelay.AUTENTICADO && mensaje.peerRole == Rol.CHILD)
                    mutable.value = mutable.value.copy(hijoIndicativo = mensaje.online)
                }
                is ReceptorAusente -> {
                    require(mutable.value.fase == FaseRelay.AUTENTICADO)
                    mutable.value = mutable.value.copy(hijoIndicativo = false)
                }
                // P05 incorporará STATE/ACK y sincronización. No aplicar ni presentar estados aquí.
                else -> terminar(true)
            }
        } catch (_: Exception) { terminar(true) }
    }
    @Synchronized fun avanzarReloj() {
        if (!visible.get() || !solicitado) return
        val fase = mutable.value.fase
        if (fase == FaseRelay.REINTENTO && ahora() >= reintentoEn) iniciar()
        else if (fase in listOf(FaseRelay.CONECTANDO, FaseRelay.ESPERANDO_RETO, FaseRelay.ESPERANDO_AUTH_OK) && ahora() >= limite) terminar(false)
        else if (fase == FaseRelay.AUTENTICADO) {
            if (ping != null && ahora() >= limite) terminar(false)
            else if (ping == null && ahora() >= proximoPing) {
                ping = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(aleatorio::nextBytes))
                limite = ahora() + 15_000
                enviar(CodecProtocolo.codificarFrame(Latido(ping!!)).toString(Charsets.UTF_8))
            }
        }
    }
    private fun enviar(texto: String) {
        if (visible.get() && socket?.enviar(texto) != true) terminar(false)
    }
    private fun terminar(permanente: Boolean) {
        cerrarSocket()
        if (!visible.get() || !solicitado) { mutable.value = EstadoRelay(); return }
        if (permanente) { solicitado = false; mutable.value = EstadoRelay(FaseRelay.ERROR_SEGURO) }
        else {
            val espera = minOf(30_000L, 1000L shl minOf(fallos++, 5)) + aleatorio.nextInt(251)
            reintentoEn = ahora() + espera
            mutable.value = EstadoRelay(FaseRelay.REINTENTO)
        }
    }
    private fun cerrarSocket() {
        generacion++
        val anterior = socket; socket = null; connectionId = null; ping = null
        anterior?.cancelar()
    }
}
