package dev.controlparental.parent.recovery

import dev.controlparental.parent.data.*
import dev.controlparental.protocol.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

internal class PreparacionOffline internal constructor(
    val reto: RetoOffline,
    internal val datos: MetadatosPadre,
    internal val generacion: Long,
    internal val leidoEn: Long,
)

// Autoriza solo la función presencial del QR, nunca ALLOW ni un comando del relay.
internal class OfflineChallengeSigner(
    private val store: ParentStore,
    private val ahora: () -> Long,
    private val exigirAutoridad: () -> Unit,
    private val firmante: (RespuestaOffline) -> SobreFirmado,
) {
    private val generacion = AtomicLong()
    private val actual = AtomicReference<PreparacionOffline?>()

    @Synchronized fun descartar() { generacion.incrementAndGet(); actual.set(null) }
    fun exigirPresentacion() { exigirAutoridad() }
    fun tiempoRestante(preparada: PreparacionOffline): Long =
        if (generacion.get() == preparada.generacion) maxOf(0, MARGEN_LOCAL_MS - (ahora() - preparada.leidoEn)) else 0

    suspend fun preparar(frame: ByteArray): PreparacionOffline {
        require(frame.size <= CodecProtocolo.MAX_FRAME)
        val copia = frame.copyOf()
        descartar()
        val turno = generacion.get()
        val inicio = ahora()
        val datos = store.leer()
        check(datos.vinculo != VinculoPersistido.SIN_VINCULAR && datos.pairId != null && datos.childPublicKeyB64 != null)
        val documento = CriptografiaProtocolo.verificarSobre(copia,
            ClaveVerificacion.desdeSpki(Rol.CHILD, datos.childPublicKeyB64), Proposito.OFFLINE)
        val reto = documento.mensaje as? RetoOffline ?: error("No es desafío del hijo")
        require(reto.pairId == datos.pairId)
        val preparada = PreparacionOffline(reto, datos, turno, inicio)
        synchronized(this) {
            // Una lectura antigua no puede sobrescribir otra preparación ni una salida.
            vigente(preparada); actual.set(preparada)
        }
        return preparada
    }

    suspend fun responder(preparada: PreparacionOffline, retiroConfirmado: Boolean): ByteArray {
        vigente(preparada)
        require(preparada.reto.action != AccionOffline.RETIRE || retiroConfirmado) { "Confirma la advertencia de borrado" }
        exigirAutoridad()
        check(actual.compareAndSet(preparada, null)) { "Preparación descartada, ajena o ya utilizada" }
        val datos = store.leer()
        require(datos.vinculo != VinculoPersistido.SIN_VINCULAR && datos.pairId == preparada.datos.pairId &&
            datos.childPublicKeyB64 == preparada.datos.childPublicKeyB64 &&
            datos.parentPublicKeyB64 == preparada.datos.parentPublicKeyB64)
        vigente(preparada); exigirAutoridad()
        val reto = preparada.reto
        val respuesta = RespuestaOffline(reto.pairId, reto.bootId, reto.action, reto.nonce)
        val frame = CodecProtocolo.codificarFrame(firmante(respuesta))
        require(CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.PARENT, datos.parentPublicKeyB64), Proposito.OFFLINE).mensaje == respuesta)
        vigente(preparada); exigirAutoridad()
        return frame
    }

    private fun vigente(preparada: PreparacionOffline) {
        check(preparada.generacion == generacion.get() && ahora() - preparada.leidoEn in 0 until MARGEN_LOCAL_MS)
    }

    companion object {
        // El hijo decide los 5 minutos/boot/consumo en su contexto. El QR no lleva timestamp.
        const val MARGEN_LOCAL_MS = 240_000L
    }
}
