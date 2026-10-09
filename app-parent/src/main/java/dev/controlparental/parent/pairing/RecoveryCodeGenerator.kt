package dev.controlparental.parent.pairing

import dev.controlparental.protocol.HashesRecuperacion
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal enum class PropositoRecuperacion { MAINTENANCE, RECOVER_PARENT, RETIRE }

// Nunca serializar, registrar ni exportar esta instancia. Solo memoria del flujo presencial.
internal class CodigosRecuperacion(private val valores: MutableMap<PropositoRecuperacion, String>) {
    fun mostrar(): Map<PropositoRecuperacion, String> = valores.mapValues { it.value.chunked(4).joinToString("-") }
    fun hashes(pairId: String): HashesRecuperacion {
        check(valores.size == 3)
        fun hash(proposito: PropositoRecuperacion) = RecoveryCodeGenerator.hash(pairId, proposito, valores.getValue(proposito))
        return HashesRecuperacion(hash(PropositoRecuperacion.MAINTENANCE),
            hash(PropositoRecuperacion.RECOVER_PARENT), hash(PropositoRecuperacion.RETIRE))
    }
    // Las String de JVM no permiten prometer borrado físico de memoria.
    fun descartar() { valores.clear() }
    override fun toString() = "CodigosRecuperacion(ocultos)"
}

internal class RecoveryCodeGenerator(private val aleatorio: SecureRandom = SecureRandom()) {
    fun generar(): CodigosRecuperacion {
        val valores = mutableMapOf<PropositoRecuperacion, String>()
        for (proposito in PropositoRecuperacion.entries) {
            var codigo: String
            do {
                val bytes = ByteArray(20)
                aleatorio.nextBytes(bytes)
                codigo = base32(bytes)
                bytes.fill(0)
            } while (codigo in valores.values)
            valores[proposito] = codigo
        }
        return CodigosRecuperacion(valores)
    }

    companion object {
        private const val ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        internal fun base32(bytes: ByteArray): String {
            require(bytes.size == 20)
            var acumulado = 0
            var bits = 0
            return buildString {
                for (byte in bytes) {
                    acumulado = (acumulado shl 8) or (byte.toInt() and 255)
                    bits += 8
                    while (bits >= 5) { bits -= 5; append(ALFABETO[(acumulado ushr bits) and 31]) }
                }
            }
        }
        internal fun normalizar(codigo: String): String {
            require(codigo.length <= 96)
            require(codigo.all { it in 'A'..'Z' || it in 'a'..'z' || it in '2'..'7' || it == '-' || it == ' ' })
            return codigo.filter { it != '-' && it != ' ' }.uppercase(java.util.Locale.ROOT).also {
                require(it.length == 32 && it.all { caracter -> caracter in ALFABETO })
            }
        }
        internal fun hash(pairId: String, proposito: PropositoRecuperacion, codigo: String): String {
            val texto = "CPv1/RECOVERY\n$pairId\n${proposito.name}\n${normalizar(codigo)}"
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(texto.toByteArray(Charsets.UTF_8)))
        }
    }
}
