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
