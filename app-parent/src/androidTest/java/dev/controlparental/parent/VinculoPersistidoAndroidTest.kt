package dev.controlparental.parent

import android.util.AtomicFile
import dev.controlparental.parent.data.*
import dev.controlparental.protocol.*
import java.util.Base64
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class VinculoPersistidoAndroidTest {
    @Test fun aceptacionAisladaPersisteQrSinCodigosNiRevinculacion() = runBlocking {
        EntornoVinculoAndroid().use { e ->
            val store = e.abrir()
            val flujo = e.flujo(store)
            val preparado = flujo.preparar(e.oferta.toByteArray(), "https://prueba.invalid")
            val codigos = preparado.codigos.mostrar().values.map { it.replace("-", "") }
            val frame = flujo.aceptar(preparado, true, true)
            assertEquals(VinculoPersistido.PREPARADO, store.leer().vinculo)
            store.cerrar()
            val reabierto = e.abrir()
            assertArrayEquals(frame, Base64.getUrlDecoder().decode(reabierto.leer().pairAcceptanceFrameB64))
            val env = e.flujo(reabierto).exportarEnvPublica()
            val claro = e.cifrador.descifrar(e.ruta.readBytes()).toString(Charsets.UTF_8)
            for (codigo in codigos) {
                assertFalse(env.contains(codigo)); assertFalse(claro.contains(codigo))
                assertFalse(frame.toString(Charsets.UTF_8).contains(codigo))
            }
            assertTrue(preparado.codigos.mostrar().isEmpty())
            assertEquals(4, env.lines().count { it.isNotEmpty() })
            assertTrue(runCatching { e.flujo(reabierto).preparar(e.oferta.toByteArray(), "https://prueba.invalid") }.isFailure)
            assertTrue(runCatching { reabierto.reservarIntencion(Accion.LOCK, null, "0") }.isFailure)
        }
    }

    @Test fun migracionV1RealConservaReservaYEscribeV2ConNonceNuevo() = runBlocking {
        EntornoVinculoAndroid().use { e ->
            val store = e.abrir()
            store.inicializar(MetadatosPadre(parentPublicKeyB64 = e.publica,
                pairId = "00000000-0000-4000-8000-000000000001", childPublicKeyB64 = e.publica,
                relayOrigin = "https://prueba.invalid", vinculo = VinculoPersistido.VINCULADO))
            val reserva = store.reservarIntencion(Accion.LOCK, null, "7")
            store.cerrar()
            val v2 = e.cifrador.descifrar(e.ruta.readBytes()).toString(Charsets.UTF_8)
            val v1 = v2.replace("\"schemaVersion\":2", "\"schemaVersion\":1")
                .replace(",\"pairAcceptanceFrameB64\":null", "")
            val cifradoV1 = e.cifrador.cifrar(v1.toByteArray())
            val atomico = AtomicFile(e.ruta)
            val salida = atomico.startWrite()
            try { salida.write(cifradoV1); atomico.finishWrite(salida) }
            catch (error: Exception) { atomico.failWrite(salida); throw error }
            val reabierto = e.abrir()
            val migrados = reabierto.leer()
            assertEquals(2, migrados.schemaVersion)
            assertEquals(reserva, migrados.pendiente)
            assertEquals("8", migrados.contadorLocal)
            assertNull(migrados.pairAcceptanceFrameB64)
            assertFalse(cifradoV1.copyOfRange(8, 20).contentEquals(e.ruta.readBytes().copyOfRange(8, 20)))
            reabierto.cerrar()
            assertEquals(reserva, e.abrir().leer().pendiente)
        }
    }
}
