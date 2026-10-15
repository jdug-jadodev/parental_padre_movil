package dev.controlparental.parent.security

import dev.controlparental.protocol.*

internal interface AlmacenClavePadre {
    fun crearSiFalta(): String
    fun publicaSpki(): String
    fun firmar(bytes: ByteArray): ByteArray
}

// No crea identidad durante la firma ni reintenta con otra clave tras un error.
internal class AutoridadPadre(
    private val almacen: AlmacenClavePadre,
    private val sesion: SesionAutoridad,
) {
    fun crearIdentidad(): String {
        sesion.invalidar()
        return almacen.crearSiFalta()
    }

    fun exportarPublica(): String = almacen.publicaSpki()

    fun firmarPairCp2(bytes: ByteArray, publicaEsperada: String): ByteArray {
        try {
            sesion.exigirVigencia()
            val copia = bytes.copyOf(); val prefijo = "CPv2/PAIR\n".toByteArray(Charsets.UTF_8)
            check(copia.size > prefijo.size && copia.copyOfRange(0, prefijo.size).contentEquals(prefijo))
            val a = dev.controlparental.protocol.cp2.VinculacionCp2.leerAceptacion(copia.copyOfRange(prefijo.size, copia.size))
            check(a.parentPublicKeyB64 == publicaEsperada && almacen.publicaSpki() == publicaEsperada)
            return almacen.firmar(copia).also {
                sesion.exigirVigencia(); check(almacen.publicaSpki() == publicaEsperada)
            }
        } catch (e: Exception) { sesion.invalidar(); throw e }
    }

    fun firmar(mensaje: Mensaje, proposito: Proposito): SobreFirmado {
        sesion.exigirVigencia()
        return try {
            val publica = ClaveVerificacion.desdeSpki(Rol.PARENT, almacen.publicaSpki())
            CriptografiaProtocolo.firmarSobre(mensaje, proposito, publica) { bytes ->
                sesion.exigirVigencia()
                almacen.firmar(bytes)
            }.also { sesion.exigirVigencia() }
        } catch (error: Exception) {
            sesion.invalidar()
            throw error
        }
    }
}
