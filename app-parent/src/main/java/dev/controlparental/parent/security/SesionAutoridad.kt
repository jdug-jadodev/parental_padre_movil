package dev.controlparental.parent.security

class AutenticacionRequerida : IllegalStateException("Autentica al adulto nuevamente")

// Barrera de interfaz adicional; el permiso criptográfico lo impone Keystore.
class SesionAutoridad(private val ahora: () -> Long) {
    private var visible = false
    private var revision = 0L
    private var esperando = false
    private var autenticadaEn: Long? = null

    @Synchronized fun primerPlano() { visible = true }

    @Synchronized fun bloquear() {
        visible = false
        invalidar()
    }

    @Synchronized fun invalidar() {
        revision++
        esperando = false
        autenticadaEn = null
    }

    @Synchronized fun solicitar(): Long {
        invalidar()
        if (!visible) throw AutenticacionRequerida()
        esperando = true
        return revision
    }

    @Synchronized fun completar(ticket: Long): Boolean {
        if (!visible || !esperando || ticket != revision) return false
        esperando = false
        autenticadaEn = ahora()
        return true
    }

    @Synchronized fun exigirVigencia() {
        val inicio = autenticadaEn
        val transcurrido = if (inicio == null) -1 else ahora() - inicio
        if (!visible || transcurrido !in 0 until VENTANA_MILLIS) {
            invalidar()
            throw AutenticacionRequerida()
        }
    }

    companion object { const val VENTANA_MILLIS = 60_000L }
}
