package dev.controlparental.parent.network

import dev.controlparental.protocol.Rol
import dev.controlparental.protocol.cp2.*

internal data class SnapshotAuthCp2(val pairId: String, val publicaPadre: String, val revision: String,
    val compatibleAutorizado: Boolean, val protocol: String = "cp.v2", val role: Rol = Rol.PARENT)
internal enum class FaseAuthCp2 { NO_DISPONIBLE, DESCONECTADO, ESPERANDO_RETO, PRUEBA_LISTA, ESPERANDO_AUTH_OK, AUTENTICADO, ERROR }

// Ticket opaco de envío, ligado por identidad a la generación y bytes preparados.
internal class EnvioAuthCp2 internal constructor(val generacion: Long, bytes: ByteArray) {
    private val original = bytes.copyOf()
    fun bytes(): ByteArray = original.copyOf()
    override fun toString() = "EnvioAuthCp2(contenido oculto)"
}

// Borde puro sin Socket/Transporte/UI/store. Por defecto no tiene snapshot compatible.
// El futuro dueño del transporte debe cerrar al salir/fondo y confirmar envío real.
internal class AuthClienteCp2(private val reloj: () -> Long,
    private val firmar: (ByteArray, String) -> AuthVerificadaCp2,
    private val snapshot: () -> SnapshotAuthCp2? = { null }) {
    var fase = FaseAuthCp2.NO_DISPONIBLE; private set
    var generacion = 0L; private set
    private var contexto: SnapshotAuthCp2? = null
    private var envio: EnvioAuthCp2? = null
    private var reto: RetoAuthCp2? = null
    private var ultimo = 0L
    private var vence = 0L

    private fun siguiente() { check(generacion < Long.MAX_VALUE); generacion++ }
    private fun limpiar() { contexto = null; envio = null; reto = null; vence = 0 }
    @Synchronized fun cerrar() { siguiente(); limpiar(); fase = FaseAuthCp2.DESCONECTADO }
    @Synchronized fun segundoPlano() = cerrar()
    @Synchronized fun abrir(subprotocolo: String): Long {
        siguiente(); limpiar(); fase = FaseAuthCp2.NO_DISPONIBLE
        try {
            val s = snapshot() ?: return generacion
            check(s.compatibleAutorizado && s.protocol == "cp.v2" && s.role == Rol.PARENT && s.revision.isNotEmpty())
            check(subprotocolo == "cp.v2") // No fallback; no abrir red aquí.
            VinculacionCp2.publica(s.publicaPadre)
            AutenticacionCp2.payload(PruebaAuthCp2(s.pairId, Rol.PARENT, s.pairId, "A".repeat(43)))
            val ahora = reloj(); check(ahora in 0..Long.MAX_VALUE - 10_000)
            contexto = s; ultimo = ahora; vence = ahora + 10_000
            fase = FaseAuthCp2.ESPERANDO_RETO
        } catch (_: Exception) { limpiar(); fase = FaseAuthCp2.NO_DISPONIBLE }
        return generacion
    }
    private fun vigente(g: Long) {
        check(g == generacion && contexto != null && snapshot() == contexto)
        val ahora = reloj(); check(ahora >= ultimo)
        if (fase != FaseAuthCp2.AUTENTICADO) check(ahora < vence)
        ultimo = ahora
    }
    private fun fallar(g: Long) { if (g == generacion) { limpiar(); fase = FaseAuthCp2.ERROR } }
    @Synchronized fun recibirReto(g: Long, bytes: ByteArray): EnvioAuthCp2? {
        if (g != generacion || contexto == null) return null
        try {
            vigente(g); check(fase == FaseAuthCp2.ESPERANDO_RETO)
            val r = AutenticacionCp2.leerFrame(bytes) as? RetoAuthCp2 ?: error("Falta reto")
            reto = r
            val s = contexto!!
            val p = AutenticacionCp2.payload(PruebaAuthCp2(s.pairId, Rol.PARENT, r.connectionId, r.nonce))
            val d = firmar(p.copyOf(), s.publicaPadre)
            vigente(g); check(fase == FaseAuthCp2.ESPERANDO_RETO)
            val v = AutenticacionCp2.verificar(d.frame(), s.publicaPadre, Rol.PARENT)
            check(v.payload().contentEquals(p))
            fase = FaseAuthCp2.PRUEBA_LISTA
            return EnvioAuthCp2(g, v.frame()).also { envio = it }
        } catch (_: Exception) { fallar(g); return null }
    }
    @Synchronized fun confirmarEnvio(t: EnvioAuthCp2, enviado: Boolean): Boolean {
        if (t.generacion != generacion || contexto == null) return false
        return try {
            vigente(t.generacion); check(t === envio && fase == FaseAuthCp2.PRUEBA_LISTA && enviado)
            envio = null; fase = FaseAuthCp2.ESPERANDO_AUTH_OK; true
        } catch (_: Exception) { fallar(t.generacion); false }
    }
    @Synchronized fun recibirAceptada(g: Long, bytes: ByteArray): Boolean {
        if (g != generacion || contexto == null) return false
        return try {
            vigente(g); check(fase == FaseAuthCp2.ESPERANDO_AUTH_OK)
            val ok = AutenticacionCp2.leerFrame(bytes) as? AceptadaAuthCp2 ?: error("Falta AUTH_OK")
            check(ok.connectionId == reto?.connectionId)
            reto = null; fase = FaseAuthCp2.AUTENTICADO; true // Solo transporte, no hijo operativo.
        } catch (_: Exception) { fallar(g); false }
    }
    @Synchronized fun avanzarReloj(g: Long) {
        if (g != generacion || contexto == null) return
        try { vigente(g) } catch (_: Exception) { fallar(g) }
    }
}
