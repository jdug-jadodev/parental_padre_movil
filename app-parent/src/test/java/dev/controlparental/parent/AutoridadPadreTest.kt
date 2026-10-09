package dev.controlparental.parent

import dev.controlparental.parent.security.*
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import org.junit.Assert.*
import org.junit.Test

class AutoridadPadreTest {
    // Solo prueba de coordinación; no es evidencia de Keystore ni biometría.
    private class AlmacenFalso : AlmacenClavePadre {
        private val par = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        var creaciones = 0
        var firmas = 0
        var fallar = false
        override fun crearSiFalta(): String { creaciones++; return publicaSpki() }
        override fun publicaSpki() = Base64.getUrlEncoder().withoutPadding().encodeToString(par.public.encoded)
        override fun firmar(bytes: ByteArray): ByteArray {
            firmas++
            if (fallar) throw IllegalStateException("Rechazo ficticio de autenticación")
            return Signature.getInstance("SHA256withECDSA").run { initSign(par.private); update(bytes); sign() }
        }
    }

    private val sesion = SesionAutoridad { 1000L }
    private val almacen = AlmacenFalso()
    private val autoridad = AutoridadPadre(almacen, sesion)
    private val mensaje = PruebaAutenticacion(
        "00000000-0000-4000-8000-000000000002", Rol.PARENT,
        "00000000-0000-4000-8000-000000000010", "A".repeat(43),
    )
    private fun autenticar() {
        sesion.primerPlano()
        sesion.completar(sesion.solicitar())
    }

    @Test fun noFirmaSinAutenticacionNiCreaClaveComoEfectoDelIntento() {
        assertThrows(AutenticacionRequerida::class.java) { autoridad.firmar(mensaje, Proposito.AUTH) }
        assertEquals(0, almacen.firmas)
        assertEquals(0, almacen.creaciones)
    }

    @Test fun adaptadorProduceSobreCP1VerificableConPublicaOriginal() {
        autenticar()
        val sobre = autoridad.firmar(mensaje, Proposito.AUTH)
        val publica = ClaveVerificacion.desdeSpki(Rol.PARENT, autoridad.exportarPublica())
        val verificado = CriptografiaProtocolo.verificarSobre(CodecProtocolo.codificarFrame(sobre), publica, Proposito.AUTH)
        assertEquals(mensaje, verificado.mensaje)
        assertEquals(1, almacen.firmas)
        assertEquals(0, almacen.creaciones)
    }

    @Test fun rechazoCriptograficoBloqueaSesionSinGenerarAlternativa() {
        autenticar()
        almacen.fallar = true
        assertThrows(IllegalStateException::class.java) { autoridad.firmar(mensaje, Proposito.AUTH) }
        almacen.fallar = false
        assertThrows(AutenticacionRequerida::class.java) { autoridad.firmar(mensaje, Proposito.AUTH) }
        assertEquals(1, almacen.firmas)
        assertEquals(0, almacen.creaciones)
    }

    @Test fun creacionExplicitaInvalidaVentanaSinCambiarPublica() {
        autenticar()
        val original = autoridad.exportarPublica()
        assertEquals(original, autoridad.crearIdentidad())
        assertThrows(AutenticacionRequerida::class.java) { autoridad.firmar(mensaje, Proposito.AUTH) }
        assertEquals(1, almacen.creaciones)
        assertEquals(0, almacen.firmas)
    }
}
