package dev.controlparental.parent.pairing

import dev.controlparental.parent.data.*
import dev.controlparental.protocol.*
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID

internal class PreparacionVinculo(
    val aceptacion: AceptacionVinculo,
    val codigos: CodigosRecuperacion,
) {
    val huellaPadre = huella(aceptacion.parentPublicKeyB64)
    val huellaHijo = huella(aceptacion.childPublicKeyB64)
    override fun toString() = "PreparacionVinculo(códigos ocultos)"
    companion object {
        fun huella(publica: String): String = MessageDigest.getInstance("SHA-256")
            .digest(Base64.getUrlDecoder().decode(publica)).joinToString("") { "%02X".format(it.toInt() and 255) }
            .chunked(4).joinToString(" ")
    }
}

internal class PairChild(
    private val store: ParentStore,
    private val publicaPadre: () -> String,
    private val firmar: (AceptacionVinculo) -> SobreFirmado,
    private val generador: RecoveryCodeGenerator = RecoveryCodeGenerator(),
) {
    suspend fun preparar(ofertaBytes: ByteArray, relayOrigin: String): PreparacionVinculo {
        val oferta = CodecProtocolo.leerOferta(ofertaBytes)
        val publica = publicaPadre()
        val datos = if (store.existe()) store.leer() else store.inicializar(MetadatosPadre(parentPublicKeyB64 = publica))
        check(datos.vinculo == VinculoPersistido.SIN_VINCULAR && datos.parentPublicKeyB64 == publica) {
            "Vínculo existente o identidad distinta: requiere recuperación presencial"
        }
        val codigos = generador.generar()
        try {
            val pairId = UUID.randomUUID().toString()
            val aceptacion = AceptacionVinculo(pairId, oferta.enrollId, oferta.enrollNonce,
                publica, oferta.childPublicKeyB64, relayOrigin, codigos.hashes(pairId))
            CodecProtocolo.codificarPayload(aceptacion)
            return PreparacionVinculo(aceptacion, codigos)
        } catch (error: Exception) { codigos.descartar(); throw error }
    }

    suspend fun aceptar(preparacion: PreparacionVinculo, huellasComparadas: Boolean, codigosGuardados: Boolean): ByteArray {
        check(huellasComparadas && codigosGuardados) { "Compara huellas y guarda los tres códigos fuera del teléfono" }
        val aceptacion = preparacion.aceptacion
        check(preparacion.codigos.hashes(aceptacion.pairId) == aceptacion.recoveryHashes) {
            "Preparación descartada o códigos distintos de los confirmados"
        }
        check(publicaPadre() == aceptacion.parentPublicKeyB64)
        val sobre = firmar(aceptacion) // Keystore y sesión exigen autenticación vigente.
        val frame = CodecProtocolo.codificarFrame(sobre)
        val verificado = CriptografiaProtocolo.verificarSobre(frame,
            ClaveVerificacion.desdeSpki(Rol.PARENT, aceptacion.parentPublicKeyB64), Proposito.PAIR)
        require(verificado.mensaje == aceptacion)
        // Persistir ANTES de mostrar QR: no se permite crear otro vínculo por accidente.
        QrLocal.generar(frame.toString(Charsets.UTF_8)) // Comprueba capacidad antes del commit.
        store.prepararVinculo(frame)
        preparacion.codigos.descartar()
        return frame
    }

    suspend fun exportarEnvPublica(): String {
        val datos = store.leer()
        check(datos.vinculo == VinculoPersistido.PREPARADO || datos.vinculo == VinculoPersistido.VINCULADO)
        return "ACTIVE_PAIR_ID=${datos.pairId}\nPARENT_PUBLIC_KEY_B64=${datos.parentPublicKeyB64}\n" +
            "CHILD_PUBLIC_KEY_B64=${datos.childPublicKeyB64}\nPUBLIC_ORIGIN=${datos.relayOrigin}\n"
    }
}
