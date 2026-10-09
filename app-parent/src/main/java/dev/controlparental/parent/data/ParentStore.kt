package dev.controlparental.parent.data

import dev.controlparental.protocol.*
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface ArchivoMetadatos {
    fun existe(): Boolean
    fun leer(): ByteArray
    fun escribir(bytes: ByteArray)
}

interface CifradorMetadatos {
    fun prepararClave()
    fun cifrar(bytes: ByteArray): ByteArray
    fun descifrar(bytes: ByteArray): ByteArray
}

class AlmacenNoDisponible(causa: Throwable? = null) : IllegalStateException(
    "Almacén ausente, incierto o corrupto; requiere recuperación, no inicialización automática", causa)

// Una sola coroutine procesa lecturas y transacciones. Sin envío ni firma aquí.
class ParentStore(
    private val archivo: ArchivoMetadatos,
    private val cifrador: CifradorMetadatos,
    scope: CoroutineScope,
) {
    private data class Peticion(val ejecutar: () -> Any?, val respuesta: CompletableDeferred<Any?>)
    private val canal = Channel<Peticion>(16)
    private var incierto = false
    private val json = Json { encodeDefaults = true; explicitNulls = true }
    private val escritor = scope.launch {
        for (peticion in canal) {
            try { peticion.respuesta.complete(peticion.ejecutar()) }
            catch (error: Exception) { peticion.respuesta.completeExceptionally(error) }
        }
    }.also { job ->
        job.invokeOnCompletion {
            canal.close()
            while (true) {
                val pendiente = canal.tryReceive().getOrNull() ?: break
                pendiente.respuesta.completeExceptionally(AlmacenNoDisponible())
            }
        }
    }

    suspend fun cerrar() { canal.close(); escritor.join() }

    private suspend fun <T> transaccion(operacion: () -> T): T {
        val respuesta = CompletableDeferred<Any?>()
        canal.send(Peticion(operacion, respuesta))
        @Suppress("UNCHECKED_CAST")
        return select {
            respuesta.onAwait { it as T }
            escritor.onJoin { throw AlmacenNoDisponible() }
        }
    }

    suspend fun inicializar(datos: MetadatosPadre): MetadatosPadre = transaccion {
        check(!incierto && !archivo.existe()) { "No reemplazar un almacén existente o incierto" }
        require(datos.contadorLocal == "0" && datos.pendiente == null && datos.ultimaEvidenciaFrameB64 == null)
        validarMetadatos(datos)
        try { cifrador.prepararClave(); guardar(datos) }
        catch (error: Exception) { incierto = true; throw AlmacenNoDisponible(error) }
        datos
    }

    suspend fun leer(): MetadatosPadre = transaccion { cargar() }
    suspend fun existe(): Boolean = transaccion { archivo.existe() }

    // P05 debe aportar secuencia de STATE autenticado/correlacionado, nunca presencia del relay.
    suspend fun reservarIntencion(action: Accion, durationSec: Int?, secuenciaRemotaVerificada: String): PendingIntentRecord = transaccion {
        val datos = cargar()
        check(datos.vinculo == VinculoPersistido.VINCULADO && datos.pendiente == null)
        val anterior = maxOf(secuenciaLocal(datos.contadorLocal), secuenciaLocal(secuenciaRemotaVerificada))
        check(anterior < Long.MAX_VALUE) { "Secuencia agotada; requiere nuevo vínculo" }
        val pendiente = PendingIntentRecord(UUID.randomUUID().toString(), action, durationSec, (anterior + 1).toString())
        guardar(datos.copy(contadorLocal = pendiente.seq, pendiente = pendiente))
        pendiente
    }

    suspend fun cancelarIntencion(commandId: String) = transaccion {
        val datos = cargar()
        check(datos.pendiente?.commandId == commandId)
        // El contador consumido y la evidencia remota no se borran.
        guardar(datos.copy(pendiente = null))
    }

    suspend fun guardarComandoFirmado(commandId: String, frame: ByteArray) {
        val copia = frame.copyOf()
        transaccion {
            val datos = cargar()
            val pendiente = datos.pendiente ?: throw IllegalStateException("No hay reserva")
            check(pendiente.commandId == commandId && pendiente.etapa == EtapaIntencion.RESERVADA)
            val sobre = CodecProtocolo.leerFrame(copia) as? SobreFirmado ?: throw IllegalArgumentException("Falta firma")
            val documento = CriptografiaProtocolo.verificarSobre(copia,
                ClaveVerificacion.desdeSpki(Rol.PARENT, datos.parentPublicKeyB64), Proposito.MESSAGE)
            val orden = documento.mensaje as? OrdenPolitica ?: throw IllegalArgumentException("No es una orden")
            require(sobre.purpose == Proposito.MESSAGE && orden.pairId == datos.pairId &&
                orden.commandId == commandId && orden.seq == pendiente.seq && orden.action == pendiente.action &&
                orden.durationSec == pendiente.durationSec)
            guardar(datos.copy(pendiente = pendiente.copy(etapa = EtapaIntencion.FIRMADA,
                frameB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(copia))))
        }
    }

    suspend fun marcarEnviada(commandId: String) = transaccion {
        val datos = cargar()
        val pendiente = datos.pendiente ?: throw IllegalStateException("No hay intención")
        check(pendiente.commandId == commandId && pendiente.etapa == EtapaIntencion.FIRMADA)
        guardar(datos.copy(pendiente = pendiente.copy(etapa = EtapaIntencion.ENVIADA_SIN_CONFIRMAR)))
    }

    // Solo retiene evidencia firmada; su frescura y autorización son trabajo de P05.
    suspend fun retenerEvidencia(documento: DocumentoVerificado) = transaccion {
        val datos = cargar()
        val frame = documento.bytesFrame()
        val verificada = CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.CHILD, datos.childPublicKeyB64 ?: error("Sin vínculo")), Proposito.MESSAGE)
        val pareja = when (val mensaje = verificada.mensaje) {
            is EstadoDispositivo -> mensaje.pairId
            is Confirmacion -> mensaje.pairId
            else -> error("No es evidencia de estado/confirmación")
        }
        require(pareja == datos.pairId)
        guardar(datos.copy(ultimaEvidenciaFrameB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(frame)))
    }

    private fun cargar(): MetadatosPadre {
        if (incierto) throw AlmacenNoDisponible()
        return try {
            if (!archivo.existe()) throw AlmacenNoDisponible()
            val claro = cifrador.descifrar(archivo.leer())
            require(claro.size <= MAX_BYTES)
            val texto = claro.toString(Charsets.UTF_8)
            val datos = json.decodeFromString<MetadatosPadre>(texto)
            require(json.encodeToString(datos).toByteArray().contentEquals(claro)) { "Documento local no canónico" }
            validarMetadatos(datos)
            datos
        } catch (error: Exception) {
            incierto = true
            throw AlmacenNoDisponible(error)
        }
    }

    private fun guardar(datos: MetadatosPadre) {
        validarMetadatos(datos)
        val claro = json.encodeToString(datos).toByteArray(Charsets.UTF_8)
        require(claro.size <= MAX_BYTES)
        try { archivo.escribir(cifrador.cifrar(claro)) }
        catch (error: Exception) { incierto = true; throw AlmacenNoDisponible(error) }
    }

    companion object { const val MAX_BYTES = 131072 }
}
