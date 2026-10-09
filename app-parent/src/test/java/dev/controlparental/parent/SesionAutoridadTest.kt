package dev.controlparental.parent

import dev.controlparental.parent.security.*
import org.junit.Assert.*
import org.junit.Test

class SesionAutoridadTest {
    private var tiempo = 1000L
    private val sesion = SesionAutoridad { tiempo }
    private fun autenticar() {
        sesion.primerPlano()
        assertTrue(sesion.completar(sesion.solicitar()))
    }

    @Test fun nuncaAbreSinRespuestaDelPrompt() {
        assertThrows(AutenticacionRequerida::class.java) { sesion.solicitar() }
        sesion.primerPlano()
        sesion.solicitar()
        assertThrows(AutenticacionRequerida::class.java) { sesion.exigirVigencia() }
    }

    @Test fun ventanaEsMonotonicaYExpiraExactamenteALos60Segundos() {
        autenticar()
        tiempo += 59999
        sesion.exigirVigencia()
        tiempo++
        assertThrows(AutenticacionRequerida::class.java) { sesion.exigirVigencia() }
    }

    @Test fun cancelacionOBackgroundNoSeReabrePorCallbackTardio() {
        sesion.primerPlano()
        val cancelado = sesion.solicitar()
        sesion.invalidar()
        assertFalse(sesion.completar(cancelado))
        val detenido = sesion.solicitar()
        sesion.bloquear()
        sesion.primerPlano()
        assertFalse(sesion.completar(detenido))
        assertThrows(AutenticacionRequerida::class.java) { sesion.exigirVigencia() }
    }

    @Test fun solicitudNuevaDescartaExitoAnteriorYNoAceptaCallbacksDuplicados() {
        autenticar()
        val anterior = sesion.solicitar()
        val actual = sesion.solicitar()
        assertFalse(sesion.completar(anterior))
        assertTrue(sesion.completar(actual))
        tiempo += 59000
        assertFalse(sesion.completar(actual))
        tiempo += 1000
        assertThrows(AutenticacionRequerida::class.java) { sesion.exigirVigencia() }
    }

    @Test fun relojRegresivoYReinicioNoMantienenAutorizacion() {
        autenticar()
        tiempo--
        assertThrows(AutenticacionRequerida::class.java) { sesion.exigirVigencia() }
        val reiniciada = SesionAutoridad { tiempo }
        reiniciada.primerPlano()
        assertThrows(AutenticacionRequerida::class.java) { reiniciada.exigirVigencia() }
    }
}
