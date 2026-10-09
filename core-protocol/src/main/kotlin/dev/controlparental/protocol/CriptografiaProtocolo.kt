package dev.controlparental.protocol

import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class FirmaInvalida : IllegalArgumentException("La firma no corresponde a los bytes y clave esperados")

// La aplicación aporta este firmante desde Keystore/autenticación (P01).
// El núcleo no almacena privadas ni crea una clave alternativa ante errores.
fun interface FirmanteMensaje {
    fun firmar(bytes: ByteArray): ByteArray
}

class ClaveVerificacion private constructor(val rol: Rol, internal val publica: PublicKey) {
    companion object {
        // Usar únicamente la pública ya vinculada o fijada en configuración confiable.
        // Nunca invocar con una clave seleccionada por el propio comando recibido.
        fun desdeSpki(rol: Rol, publicaB64: String): ClaveVerificacion {
            val bytes = Formatos.base64(publicaB64, 150)
            FormatosAvanzados.publicaP256(bytes)
            return ClaveVerificacion(rol, KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(bytes)))
        }
    }
}

// Firma y estructura comprobadas. NO significa autorización contextual ni APPLIED.
class DocumentoVerificado internal constructor(
    val mensaje: Mensaje,
    val proposito: Proposito,
    val rol: Rol,
    payload: ByteArray,
    frame: ByteArray,
) {
    private val payloadOriginal = payload.copyOf()
    private val frameOriginal = frame.copyOf()
    val payloadSha256B64: String = Formatos.codificar(MessageDigest.getInstance("SHA-256").digest(payloadOriginal))
    fun bytesPayload(): ByteArray = payloadOriginal.copyOf()
    fun bytesFrame(): ByteArray = frameOriginal.copyOf()
}

object CriptografiaProtocolo {
    fun verificarSobre(bytesFrame: ByteArray, clave: ClaveVerificacion, propositoEsperado: Proposito): DocumentoVerificado {
        val frame = bytesFrame.copyOf()
        val sobre = CodecProtocolo.leerFrame(frame) as? SobreFirmado
            ?: throw MensajeInvalido("Se esperaba un contenedor firmado")
        exigir(sobre.purpose == propositoEsperado, "Propósito distinto del esperado")
        val payload = CodecProtocolo.bytesPayload(sobre)
        val firma = Formatos.base64(sobre.sigB64, 75)
        verificarFirma(bytesFirma(sobre.purpose, payload), firma, clave)
        // Solo después de verificar los bytes originales se interpreta el payload.
        val mensaje = CodecProtocolo.leerPayloadSinVerificar(payload, sobre.purpose)
        comprobarRol(mensaje, clave.rol)
        return DocumentoVerificado(mensaje, sobre.purpose, clave.rol, payload, frame)
    }

    fun firmarSobre(mensaje: Mensaje, proposito: Proposito, clave: ClaveVerificacion, firmante: FirmanteMensaje): SobreFirmado {
        val payload = CodecProtocolo.codificarPayload(mensaje)
        CodecProtocolo.leerPayloadSinVerificar(payload, proposito)
        comprobarRol(mensaje, clave.rol)
        val bytes = bytesFirma(proposito, payload)
        val firma = firmante.firmar(bytes.copyOf()).copyOf()
        FormatosAvanzados.firmaDer(firma)
        verificarFirma(bytes, firma, clave)
        return SobreFirmado(proposito, Formatos.codificar(payload), Formatos.codificar(firma))
    }

    private fun bytesFirma(proposito: Proposito, payload: ByteArray): ByteArray =
        "CPv1/${proposito.name}\n".toByteArray(Charsets.UTF_8) + payload

    private fun verificarFirma(bytes: ByteArray, firma: ByteArray, clave: ClaveVerificacion) {
        val valida = try {
            Signature.getInstance("SHA256withECDSA").run {
                initVerify(clave.publica)
                update(bytes)
                verify(firma)
            }
        } catch (_: java.security.GeneralSecurityException) {
            false
        }
        if (!valida) throw FirmaInvalida()
    }

    private fun comprobarRol(mensaje: Mensaje, rol: Rol) {
        val esperado = when (mensaje) {
            is PruebaAutenticacion -> mensaje.role
            is SolicitudSincronizacion, is OrdenPolitica, is AceptacionVinculo, is RespuestaOffline -> Rol.PARENT
            is Confirmacion, is EstadoDispositivo, is RetoOffline -> Rol.CHILD
            else -> throw MensajeInvalido("Tipo de payload no firmable")
        }
        exigir(rol == esperado, "La clave configurada no corresponde al rol del payload")
    }
}
