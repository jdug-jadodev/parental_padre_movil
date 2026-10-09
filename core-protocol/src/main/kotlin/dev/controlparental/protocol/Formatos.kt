package dev.controlparental.protocol

import java.util.Base64

internal object Formatos {
    private val alfabeto = Regex("^[A-Za-z0-9_-]+$")
    private val contador = Regex("^(0|[1-9][0-9]{0,18})$")

    fun base64(valor: String, maximoBytes: Int, longitud: Int? = null): ByteArray {
        exigir(valor.length <= (maximoBytes * 4 + 2) / 3, "Base64url demasiado largo")
        exigir(alfabeto.matches(valor), "Base64url inválido")
        val bytes = try {
            Base64.getUrlDecoder().decode(valor)
        } catch (_: IllegalArgumentException) {
            throw MensajeInvalido("Base64url inválido")
        }
        exigir(bytes.size <= maximoBytes, "Contenido decodificado demasiado grande")
        exigir(longitud == null || bytes.size == longitud, "Longitud decodificada incorrecta")
        exigir(codificar(bytes) == valor, "Base64url no canónico")
        return bytes
    }

    fun codificar(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    fun secuencia(valor: String, permiteCero: Boolean = false): Long {
        exigir(contador.matches(valor), "Secuencia decimal inválida")
        val numero = valor.toLongOrNull() ?: throw MensajeInvalido("Secuencia fuera de 64 bits")
        exigir(permiteCero || numero > 0, "La secuencia debe ser positiva")
        return numero
    }
}
