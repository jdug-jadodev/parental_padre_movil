package dev.controlparental.parent.data

import dev.controlparental.protocol.*
import kotlinx.serialization.Serializable

@Serializable enum class EtapaIntencion { RESERVADA, FIRMADA, ENVIADA_SIN_CONFIRMAR }
@Serializable enum class VinculoPersistido { SIN_VINCULAR, PREPARADO, VINCULADO, REQUIERE_RECUPERACION }

@Serializable
data class PendingIntentRecord(
    val commandId: String,
    val action: Accion,
    val durationSec: Int?,
    val seq: String,
    val etapa: EtapaIntencion = EtapaIntencion.RESERVADA,
    val frameB64: String? = null,
)

// Formato privado local, no modifica CP/1 ni serializa privadas/códigos.
@Serializable
data class MetadatosPadre(
    val schemaVersion: Int = 1,
    val signingAlias: String = "cp.parent.sign.v1",
    val parentPublicKeyB64: String,
    val pairId: String? = null,
    val childPublicKeyB64: String? = null,
    val relayOrigin: String? = null,
    val contadorLocal: String = "0",
    val vinculo: VinculoPersistido = VinculoPersistido.SIN_VINCULAR,
    val pendiente: PendingIntentRecord? = null,
    val ultimaEvidenciaFrameB64: String? = null,
)

internal fun secuenciaLocal(valor: String): Long {
    require(Regex("^(0|[1-9][0-9]{0,18})$").matches(valor)) { "Secuencia decimal inválida" }
    return valor.toLongOrNull() ?: throw IllegalArgumentException("Secuencia agotada o inválida")
}

internal fun validarMetadatos(datos: MetadatosPadre) {
    require(datos.schemaVersion == 1 && datos.signingAlias == "cp.parent.sign.v1")
    ClaveVerificacion.desdeSpki(Rol.PARENT, datos.parentPublicKeyB64)
    val contador = secuenciaLocal(datos.contadorLocal)
    if (datos.vinculo == VinculoPersistido.SIN_VINCULAR) {
        require(datos.pairId == null && datos.childPublicKeyB64 == null && datos.relayOrigin == null)
        require(contador == 0L && datos.pendiente == null && datos.ultimaEvidenciaFrameB64 == null)
    } else {
        require(datos.pairId != null && datos.childPublicKeyB64 != null && datos.relayOrigin != null)
        ClaveVerificacion.desdeSpki(Rol.CHILD, datos.childPublicKeyB64)
        // El codec aplica UUID y origen HTTPS del contrato sin duplicar su esquema.
        CodecProtocolo.codificarPayload(SolicitudSincronizacion(datos.pairId, "A".repeat(43)))
        val origen = java.net.URI(datos.relayOrigin)
        require(origen.scheme == "https" && origen.rawUserInfo == null && origen.host != null &&
            origen.rawPath.isEmpty() && origen.rawQuery == null && origen.rawFragment == null &&
            (origen.port == -1 || origen.port in 1..65535) && origen.toASCIIString() == datos.relayOrigin)
    }
    datos.pendiente?.let { pendiente ->
        require(datos.vinculo == VinculoPersistido.VINCULADO)
        require(secuenciaLocal(pendiente.seq) > 0 && secuenciaLocal(pendiente.seq) == contador)
        CodecProtocolo.codificarPayload(OrdenPolitica(datos.pairId!!,
            "00000000-0000-4000-8000-000000000001", "00000000-0000-4000-8000-000000000002",
            pendiente.commandId, "A".repeat(43), pendiente.seq, pendiente.action, pendiente.durationSec))
        require((pendiente.etapa == EtapaIntencion.RESERVADA) == (pendiente.frameB64 == null))
        pendiente.frameB64?.let { frame ->
            val documento = CriptografiaProtocolo.verificarSobre(bytesFrameLocal(frame),
                ClaveVerificacion.desdeSpki(Rol.PARENT, datos.parentPublicKeyB64), Proposito.MESSAGE)
            val orden = documento.mensaje as? OrdenPolitica ?: error("No es una orden")
            require(orden.pairId == datos.pairId && orden.commandId == pendiente.commandId &&
                orden.seq == pendiente.seq && orden.action == pendiente.action && orden.durationSec == pendiente.durationSec)
        }
    }
    datos.ultimaEvidenciaFrameB64?.let { frame ->
        val documento = CriptografiaProtocolo.verificarSobre(bytesFrameLocal(frame),
            ClaveVerificacion.desdeSpki(Rol.CHILD, datos.childPublicKeyB64 ?: error("Sin clave del hijo")), Proposito.MESSAGE)
        val pairId = when (val mensaje = documento.mensaje) {
            is EstadoDispositivo -> mensaje.pairId
            is Confirmacion -> mensaje.pairId
            else -> error("No es evidencia del hijo")
        }
        require(pairId == datos.pairId)
    }
}

private fun bytesFrameLocal(valor: String): ByteArray {
    require(valor.length <= (CodecProtocolo.MAX_FRAME * 4 + 2) / 3)
    val bytes = java.util.Base64.getUrlDecoder().decode(valor)
    require(java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) == valor)
    return bytes
}
