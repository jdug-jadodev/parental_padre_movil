package dev.controlparental.parent

import dev.controlparental.parent.data.VinculoPersistido
import dev.controlparental.parent.pairing.*
import dev.controlparental.protocol.*
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import org.junit.Assert.*
import org.junit.Test

class RevisionVinculoTest {
    // Identidades efímeras y declaraciones ficticias. Sin identidad operativa/archivos/dispositivos.
    private class Entorno {
        val padre = par(); val hijo = par()
        val snapshot = SnapshotRevision(ID, publica(Rol.PARENT), publica(Rol.CHILD), "https://prueba.invalid")
        val pair = AceptacionVinculo(ID, SESION, NONCE, snapshot.publicaPadre, snapshot.publicaHijo,
            snapshot.relayOrigin, HashesRecuperacion(NONCE, "E".repeat(43), "I".repeat(43)))
        val estado = EstadoDispositivo(ID, NONCE, BOOT, SESION, "2", "0", null, null, ResultadoEstado.NONE,
            Modo.LOCKED, Motivo.PARENT_LOCK, null, true, true, false, false, NONCE)
        val reto = RetoOffline(ID, BOOT, AccionOffline.MAINTENANCE, NONCE)
        fun publica(rol: Rol) = Base64.getUrlEncoder().withoutPadding().encodeToString((if (rol == Rol.PARENT) padre else hijo).public.encoded)
        fun bytes(m: Mensaje, rol: Rol, purpose: Proposito): ByteArray = CodecProtocolo.codificarFrame(
            CriptografiaProtocolo.firmarSobre(m, purpose, ClaveVerificacion.desdeSpki(rol, publica(rol))) { bytes ->
                Signature.getInstance("SHA256withECDSA").run { initSign((if (rol == Rol.PARENT) padre else hijo).private); update(bytes); sign() }
            })
        fun frame(m: Mensaje, rol: Rol, purpose: Proposito) = FrameRevision(bytes(m, rol, purpose))
        fun entrada(): EntradaRevision {
            val aceptacion = frame(pair, Rol.PARENT, Proposito.PAIR)
            val hash = CriptografiaProtocolo.verificarSobre(aceptacion.copia(), ClaveVerificacion.desdeSpki(Rol.PARENT,
                snapshot.publicaPadre), Proposito.PAIR).payloadSha256B64
            return EntradaRevision(snapshot, ContextoRevision(snapshot, hash, 1, BOOT, 0),
                SenalesRevision(snapshot.publicaPadre, 1, 7, 5_000, true, true, false, true, true), 6_000,
                aceptacion, ResumenRevision(ID, PreparacionVinculo.huella(snapshot.publicaPadre),
                    PreparacionVinculo.huella(snapshot.publicaHijo), snapshot.relayOrigin, EstatusResumen.POST_COMMIT_READBACK),
                frame(estado, Rol.CHILD, Proposito.MESSAGE), SolicitudRevision(NONCE, 7, BOOT, SESION, 5_000),
                OfflineRevision(frame(reto, Rol.CHILD, Proposito.OFFLINE),
                    frame(RespuestaOffline(ID, BOOT, AccionOffline.MAINTENANCE, NONCE), Rol.PARENT, Proposito.OFFLINE), 1_000, 2_000),
                ObservacionesRevision(hash, 1, BOOT, 3_000, true, true, true, true, true))
        }
    }
    private fun rechazo(e: EntradaRevision, causa: CausaRevision) =
        assertEquals(DiagnosticoRevision(ClaseDiagnostico.RECHAZADA, causa), ValidadorRevisionVinculo.evaluar(e))
    private fun incompleta(e: EntradaRevision) =
        assertEquals(ClaseDiagnostico.INCOMPLETA, ValidadorRevisionVinculo.evaluar(e).clase)

    @Test fun positivoFicticioDeterministaSinMutacionNiDeviceOwnerFingido() {
        val e = Entorno().entrada(); val original = e.aceptacion!!.copia()
        val esperado = DiagnosticoRevision(ClaseDiagnostico.COHERENTE, CausaRevision.CONSISTENCIA_FICTICIA)
        repeat(3) { assertEquals(esperado, ValidadorRevisionVinculo.evaluar(e)) }
        assertArrayEquals(original, e.aceptacion.copia())
        assertEquals(VinculoPersistido.PREPARADO, e.snapshot.vinculo)
        assertEquals(OrigenObservaciones.FICTICIAS_JVM, e.observaciones!!.origen)
        assertFalse(esperado.toString().contains(e.snapshot.pairId))
    }
    @Test fun sobresDefensivosNoExponenArrayOriginalNiCopiaModificable() {
        val entorno = Entorno(); val bytes = entorno.bytes(entorno.pair, Rol.PARENT, Proposito.PAIR)
        val frame = FrameRevision(bytes); val original = frame.copia()
        bytes.fill(0); frame.copia().fill(0)
        assertArrayEquals(original, frame.copia())
        assertEquals("FrameRevision(contenido oculto)", frame.toString())
        assertThrows(IllegalArgumentException::class.java) { FrameRevision(ByteArray(CodecProtocolo.MAX_FRAME + 1)) }
    }
    @Test fun snapshotDebeSerPreparadoIntegroVersionConocidaEIdentidadLocalIgual() {
        val e = Entorno().entrada()
        for (s in listOf(e.snapshot.copy(vinculo = VinculoPersistido.VINCULADO), e.snapshot.copy(version = 2),
            e.snapshot.copy(procedencia = ProcedenciaRevision.LEGADO), e.snapshot.copy(procedencia = ProcedenciaRevision.NO_VERIFICABLE)))
            rechazo(e.copy(snapshot = s), CausaRevision.SNAPSHOT_INVALIDO)
        rechazo(e.copy(senales = e.senales.copy(publicaIdentidadLocal = null)), CausaRevision.IDENTIDAD_DISTINTA)
    }
    @Test fun generacionOSnapshotCambiadosNoReutilizanRevision() {
        val e = Entorno().entrada()
        rechazo(e.copy(snapshot = e.snapshot.copy(relayOrigin = "https://otro.invalid")), CausaRevision.CONTEXTO_CAMBIADO)
        rechazo(e.copy(senales = e.senales.copy(generacion = 2)), CausaRevision.CONTEXTO_CAMBIADO)
        rechazo(e.copy(revision = e.revision.copy(hashPayloadPair = NONCE)), CausaRevision.CONTEXTO_CAMBIADO)
    }
    @Test fun ausenciaDeCualquierEvidenciaEsIncompletaYStateSoloNoBasta() {
        val e = Entorno().entrada()
        listOf(e.copy(aceptacion = null), e.copy(resumen = null), e.copy(estado = null), e.copy(solicitud = null),
            e.copy(offline = null), e.copy(observaciones = null), e.copy(aceptacion = null, resumen = null, offline = null,
                observaciones = null)).forEach(::incompleta)
    }
    @Test fun aceptacionAjenaOFirmaImpostoraNoEsEvidencia() {
        val entorno = Entorno(); val e = entorno.entrada(); val intruso = Entorno()
        rechazo(e.copy(aceptacion = intruso.frame(entorno.pair, Rol.PARENT, Proposito.PAIR)), CausaRevision.CRIPTOGRAFIA_INVALIDA)
        rechazo(e.copy(aceptacion = entorno.frame(entorno.pair.copy(pairId = BOOT), Rol.PARENT, Proposito.PAIR)), CausaRevision.ACEPTACION_AJENA)
        rechazo(e.copy(aceptacion = FrameRevision("{}".toByteArray())), CausaRevision.CRIPTOGRAFIA_INVALIDA)
    }
    @Test fun resumenCompletoDebeCoincidirExactamenteSinNormalizacion() {
        val e = Entorno().entrada(); val r = e.resumen!!
        for (rAjeno in listOf(r.copy(pairId = BOOT), r.copy(relayOrigin = "${r.relayOrigin}/ws"),
            r.copy(huellaPadre = r.huellaHijo), r.copy(huellaHijo = r.huellaPadre)))
            rechazo(e.copy(resumen = rAjeno), CausaRevision.RESUMEN_DISTINTO)
    }
    @Test fun formatoDeHuellasYEstatusProvisionalONoVerificableSeRechazan() {
        val e = Entorno().entrada(); val r = e.resumen!!
        for (rMalo in listOf(r.copy(estatus = EstatusResumen.PROVISIONAL), r.copy(estatus = EstatusResumen.NO_VERIFICABLE),
            r.copy(huellaPadre = r.huellaPadre.lowercase()), r.copy(huellaHijo = r.huellaHijo.take(10)),
            r.copy(huellaPadre = r.huellaPadre.replace(" ", "")), r.copy(huellaHijo = " ${r.huellaHijo}")))
            rechazo(e.copy(resumen = rMalo), CausaRevision.RESUMEN_INVALIDO)
    }
    @Test fun stateFirmaTipoPropositoYPayloadEstructuralNoSuplenVerificacion() {
        val entorno = Entorno(); val e = entorno.entrada(); val intruso = Entorno()
        val frames = listOf(intruso.frame(entorno.estado, Rol.CHILD, Proposito.MESSAGE),
            FrameRevision(CodecProtocolo.codificarPayload(entorno.estado)),
            entorno.frame(entorno.reto, Rol.CHILD, Proposito.OFFLINE),
            entorno.frame(SolicitudSincronizacion(ID, NONCE), Rol.PARENT, Proposito.MESSAGE),
            FrameRevision(CodecProtocolo.codificarFrame(Presencia(Rol.CHILD, true))))
        frames.forEach { rechazo(e.copy(estado = it), CausaRevision.CRIPTOGRAFIA_INVALIDA) }
    }
    @Test fun stateDebeCorrelacionarParejaNonceTicketBootYSesion() {
        val entorno = Entorno(); val e = entorno.entrada()
        for (s in listOf(entorno.estado.copy(pairId = BOOT), entorno.estado.copy(syncNonce = "E".repeat(43)),
            entorno.estado.copy(bootId = ID), entorno.estado.copy(childSessionId = BOOT)))
            rechazo(e.copy(estado = entorno.frame(s, Rol.CHILD, Proposito.MESSAGE)), CausaRevision.ESTADO_AJENO)
        rechazo(e.copy(solicitud = e.solicitud!!.copy(ticket = 8)), CausaRevision.ESTADO_AJENO)
    }
    @Test fun secuenciasNoSeRepitenNiRetrocedenYContextoNumericoInvalidoFalla() {
        val e = Entorno().entrada(); val s = e.solicitud!!
        for (sMala in listOf(s.copy(ultimoStateSeq = "2"), s.copy(ultimoStateSeq = "3"), s.copy(ultimoAcceptedSeq = "1"),
            s.copy(ultimoStateSeq = "01"), s.copy(ultimoStateSeq = "9223372036854775808")))
            rechazo(e.copy(solicitud = sMala), CausaRevision.SECUENCIA_INVALIDA)
    }
    @Test fun ventanaDeEstadoDesdeSolicitudYFronteraExactaSinTimestampInventado() {
        val e = Entorno().entrada()
        assertEquals(ClaseDiagnostico.COHERENTE, ValidadorRevisionVinculo.evaluar(e.copy(evaluadaEn = 24_999)).clase)
        rechazo(e.copy(evaluadaEn = 25_000), CausaRevision.EVIDENCIA_VENCIDA)
        rechazo(e.copy(solicitud = e.solicitud!!.copy(solicitadaEn = 7_000)), CausaRevision.RELOJ_INVALIDO)
    }
    @Test fun offlineExigeFirmasVinculadasYMismoContextoSinUsoCruzado() {
        val entorno = Entorno(); val e = entorno.entrada(); val o = e.offline!!
        for (r in listOf(entorno.reto.copy(action = AccionOffline.RETIRE), entorno.reto.copy(pairId = BOOT), entorno.reto.copy(bootId = ID)))
            rechazo(e.copy(offline = o.copy(reto = entorno.frame(r, Rol.CHILD, Proposito.OFFLINE))), CausaRevision.RECUPERACION_AJENA)
        rechazo(e.copy(offline = o.copy(respuesta = entorno.frame(RespuestaOffline(ID, BOOT, AccionOffline.MAINTENANCE,
            "E".repeat(43)), Rol.PARENT, Proposito.OFFLINE))), CausaRevision.RECUPERACION_AJENA)
        rechazo(e.copy(offline = o.copy(reto = Entorno().frame(entorno.reto, Rol.CHILD, Proposito.OFFLINE))), CausaRevision.CRIPTOGRAFIA_INVALIDA)
        rechazo(e.copy(offline = o.copy(respuesta = e.aceptacion!!)), CausaRevision.CRIPTOGRAFIA_INVALIDA)
    }
    @Test fun observacionesFaltantesOAjenasNoSeInfierenDeState() {
        val e = Entorno().entrada(); val o = e.observaciones!!
        for (incompletas in listOf(o.copy(commitObservado = false), o.copy(mantenimientoObservado = false),
            o.copy(replayRechazado = false), o.copy(emergenciaAccesible = false), o.copy(codigosGuardados = false)))
            incompleta(e.copy(observaciones = incompletas))
        for (ajenas in listOf(o.copy(hashPayloadPair = NONCE), o.copy(generacion = 2), o.copy(bootId = ID)))
            rechazo(e.copy(observaciones = ajenas), CausaRevision.OBSERVACIONES_AJENAS)
    }
    @Test fun autoridadFondoCancelacionRedYCanalNoSeSuplenConBooleanosDelHijo() {
        val e = Entorno().entrada(); val s = e.senales
        rechazo(e.copy(senales = s.copy(autoridadVigente = false)), CausaRevision.AUTORIDAD_AUSENTE)
        for (inactiva in listOf(s.copy(visible = false), s.copy(cancelada = true))) rechazo(e.copy(senales = inactiva), CausaRevision.UI_INACTIVA)
        for (noSana in listOf(s.copy(redValidada = false), s.copy(canalSano = false), s.copy(ticket = null), s.copy(ultimoPongEn = null)))
            rechazo(e.copy(senales = noSana), CausaRevision.CANAL_NO_VIGENTE)
        rechazo(e.copy(senales = s.copy(ultimoPongEn = 0), evaluadaEn = 45_000), CausaRevision.CANAL_NO_VIGENTE)
    }
    @Test fun relojDiscontinuoYOrdenTemporalDeCeremoniaInvalidanRevision() {
        val e = Entorno().entrada()
        rechazo(e.copy(evaluadaEn = -1), CausaRevision.RELOJ_INVALIDO)
        rechazo(e.copy(senales = e.senales.copy(ultimoPongEn = 7_000)), CausaRevision.RELOJ_INVALIDO)
        rechazo(e.copy(offline = e.offline!!.copy(leidoEn = -1)), CausaRevision.RELOJ_INVALIDO)
        rechazo(e.copy(offline = e.offline.copy(respondidoEn = 500)), CausaRevision.RELOJ_INVALIDO)
        rechazo(e.copy(observaciones = e.observaciones!!.copy(observadasEn = 1_999)), CausaRevision.RELOJ_INVALIDO)
        rechazo(e.copy(observaciones = e.observaciones.copy(observadasEn = 5_001)), CausaRevision.RELOJ_INVALIDO)
    }
    @Test fun ventanaOfflineSeMideAlResponderNoExigeFrescuraDeRetoAlStateFinal() {
        val e = Entorno().entrada()
        fun tardia(respondido: Long) = e.copy(evaluadaEn = 301_000, senales = e.senales.copy(ultimoPongEn = 300_000),
            solicitud = e.solicitud!!.copy(solicitadaEn = 300_000), offline = e.offline!!.copy(respondidoEn = respondido),
            observaciones = e.observaciones!!.copy(observadasEn = 299_000))
        assertEquals(ClaseDiagnostico.COHERENTE, ValidadorRevisionVinculo.evaluar(tardia(240_999)).clase)
        rechazo(tardia(241_000), CausaRevision.EVIDENCIA_VENCIDA)
    }
    @Test fun hashPrivadoSeLigaAlPayloadNoAFirmaYNoEsDatoDelResumenHijo() {
        val entorno = Entorno(); val e = entorno.entrada()
        val nuevaFirma = entorno.frame(entorno.pair, Rol.PARENT, Proposito.PAIR)
        assertEquals(ValidadorRevisionVinculo.evaluar(e), ValidadorRevisionVinculo.evaluar(e.copy(aceptacion = nuevaFirma)))
        rechazo(e.copy(aceptacion = entorno.frame(entorno.pair.copy(enrollNonce = "E".repeat(43)),
            Rol.PARENT, Proposito.PAIR)), CausaRevision.CONTEXTO_CAMBIADO)
    }
    @Test fun stateAptoOMaintenanceNoSustituyeDeclaracionesDelAdulto() {
        val entorno = Entorno(); val e = entorno.entrada()
        val declarado = entorno.estado.copy(mode = Modo.MAINTENANCE, reason = Motivo.MAINTENANCE_ACTIVE,
            isDeviceOwner = true, lockTaskActive = true)
        incompleta(e.copy(estado = entorno.frame(declarado, Rol.CHILD, Proposito.MESSAGE), observaciones = null))
    }
    @Test fun politicaAllowNoEsRespuestaOfflineNiSuTipoPuedeAutorizarRevision() {
        val entorno = Entorno(); val e = entorno.entrada()
        val orden = OrdenPolitica(ID, BOOT, SESION, ID, NONCE, "1", Accion.ALLOW, null)
        rechazo(e.copy(offline = e.offline!!.copy(respuesta = entorno.frame(orden, Rol.PARENT, Proposito.MESSAGE))),
            CausaRevision.CRIPTOGRAFIA_INVALIDA)
    }
    @Test fun clavesNoValidasYOrigenOClaveAjenaEnAceptacionNoSeNormalizan() {
        val entorno = Entorno(); val e = entorno.entrada()
        val s = e.snapshot.copy(publicaHijo = "no-es-spki")
        rechazo(e.copy(snapshot = s, revision = e.revision.copy(snapshot = s)), CausaRevision.SNAPSHOT_INVALIDO)
        for (ajena in listOf(entorno.pair.copy(relayOrigin = "https://otro.invalid"),
            entorno.pair.copy(childPublicKeyB64 = Entorno().publica(Rol.CHILD))))
            rechazo(e.copy(aceptacion = entorno.frame(ajena, Rol.PARENT, Proposito.PAIR)), CausaRevision.ACEPTACION_AJENA)
    }
    companion object {
        const val ID = "00000000-0000-4000-8000-000000000002"
        const val BOOT = "00000000-0000-4000-8000-000000000003"
        const val SESION = "00000000-0000-4000-8000-000000000004"
        val NONCE = "A".repeat(43)
        fun par() = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }.generateKeyPair()
    }
}
