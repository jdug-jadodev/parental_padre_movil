package dev.controlparental.parent.security

import dev.controlparental.protocol.Rol
import dev.controlparental.protocol.cp2.AuthVerificadaCp2
import dev.controlparental.protocol.cp2.AutenticacionCp2

// Adaptador independiente sobre el mismo almacén Android; no crearSiFalta ni rotación.
// No instancia transporte, ni obtiene pareja/pública de un CHALLENGE o de CP/1.
internal class FirmanteAuthCp2(private val almacen: AlmacenClavePadre, private val sesion: SesionAutoridad) {
    fun firmar(payload: ByteArray, publicaEsperada: String): AuthVerificadaCp2 {
        try {
            sesion.exigirVigencia()
            check(almacen.publicaSpki() == publicaEsperada)
            return AutenticacionCp2.firmar(payload, publicaEsperada, Rol.PARENT) { bytes ->
                sesion.exigirVigencia(); check(almacen.publicaSpki() == publicaEsperada)
                almacen.firmar(bytes)
            }.also { sesion.exigirVigencia(); check(almacen.publicaSpki() == publicaEsperada) }
        } catch (e: Exception) { sesion.invalidar(); throw e }
    }
}
