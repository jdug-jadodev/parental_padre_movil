package dev.controlparental.protocol

import java.net.URI
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.X509EncodedKeySpec

internal object FormatosAvanzados {
    fun publicaP256(bytes: ByteArray) {
        try {
            val clave = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(bytes)) as ECPublicKey
            val referencia = AlgorithmParameters.getInstance("EC").apply {
                init(ECGenParameterSpec("secp256r1"))
            }.getParameterSpec(ECParameterSpec::class.java)
            val curva = clave.params
            exigir(curva.curve == referencia.curve && curva.generator == referencia.generator &&
                curva.order == referencia.order && curva.cofactor == referencia.cofactor, "La pública no es P-256")
            exigir(bytes.contentEquals(clave.encoded), "SPKI no canónico")
        } catch (_: java.security.GeneralSecurityException) {
            throw MensajeInvalido("Pública SPKI inválida")
        } catch (_: ClassCastException) {
            throw MensajeInvalido("La pública no es EC")
        }
    }

    // Solo comprueba la estructura DER de dos enteros positivos. No verifica ECDSA.
    fun firmaDer(bytes: ByteArray) {
        exigir(bytes.size in 8..72 && bytes[0] == 0x30.toByte(), "Firma DER inválida")
        exigir(bytes[1].toInt() == bytes.size - 2, "Longitud DER inválida")
        var posicion = 2
        repeat(2) {
            exigir(posicion + 2 < bytes.size && bytes[posicion++] == 0x02.toByte(), "Entero DER inválido")
            val longitud = bytes[posicion++].toInt()
            exigir(longitud in 1..33 && posicion + longitud <= bytes.size, "Longitud de entero DER inválida")
            val primero = bytes[posicion].toInt() and 255
            exigir(primero < 128, "Entero DER negativo")
            exigir(longitud == 1 || primero != 0 || bytes[posicion + 1].toInt() and 128 != 0, "Padding DER innecesario")
            exigir(longitud < 33 || primero == 0, "Entero DER mayor de 256 bits")
            exigir((posicion until posicion + longitud).any { bytes[it] != 0.toByte() }, "Entero DER cero")
            posicion += longitud
        }
        exigir(posicion == bytes.size, "Bytes extra en firma DER")
    }

    fun origenHttps(valor: String) {
        val uri = try { URI(valor) } catch (_: java.net.URISyntaxException) {
            throw MensajeInvalido("Origen HTTPS inválido")
        }
        exigir(uri.scheme == "https" && uri.host != null && uri.userInfo == null &&
            uri.rawPath.isNullOrEmpty() && uri.rawQuery == null && uri.rawFragment == null &&
            (uri.port == -1 || uri.port in 1..65535), "Se requiere un origen HTTPS exacto")
    }
}
