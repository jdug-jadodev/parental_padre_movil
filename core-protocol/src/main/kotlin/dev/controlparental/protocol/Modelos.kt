package dev.controlparental.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Los nombres de campos y valores son los del contrato CP/1, no traducciones.
@Serializable
sealed interface Mensaje

@Serializable
enum class Proposito { AUTH, MESSAGE, PAIR, OFFLINE }

@Serializable
enum class Rol { PARENT, CHILD }

@Serializable
enum class Accion { LOCK, LOCK_FOR, ALLOW, ALLOW_FOR }

@Serializable
enum class AccionOffline { MAINTENANCE, RETIRE }

@Serializable
enum class Modo { LOCKED, ALLOWED, MAINTENANCE, SETUP }

@Serializable
enum class ResultadoAck { APPLIED, ACCEPTED_PENDING_ENFORCEMENT, ENFORCEMENT_FAILED, REJECTED }

@Serializable
enum class ResultadoEstado { NONE, APPLIED, ACCEPTED_PENDING_ENFORCEMENT, ENFORCEMENT_FAILED }

@Serializable
enum class Motivo {
    BAD_SCHEMA, BAD_SIGNATURE, WRONG_PAIR, WRONG_ROLE, STALE_BOOT, STALE_SESSION,
    STALE_NONCE, STALE_SEQUENCE, SEQUENCE_CONFLICT, NOT_DEVICE_OWNER, NOT_PROVISIONED,
    BEFORE_USER_UNLOCK, BOOT_AUTH_REQUIRED, NETWORK_LOST, CHANNEL_EXPIRED, PARENT_LOCK,
    TIMER_EXPIRED, OK, MAINTENANCE_ACTIVE, STATE_CORRUPT, ENFORCEMENT_ERROR,
    KEY_UNAVAILABLE, PEER_OFFLINE, RATE_LIMITED, UNKNOWN_VERSION, SYSTEM_KEYGUARD,
    CALL_IN_PROGRESS,
}

@Serializable
@SerialName("SIGNED")
data class SobreFirmado(
    val purpose: Proposito,
    val payloadB64: String,
    val sigB64: String,
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("CHALLENGE")
data class RetoConexion(
    val connectionId: String,
    val nonce: String,
    val timeoutSec: Int = 10,
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("AUTH_PROOF")
data class PruebaAutenticacion(
    val pairId: String,
    val role: Rol,
    val connectionId: String,
    val nonce: String,
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("AUTH_OK")
data class AutenticacionAceptada(val connectionId: String, val v: Int = 1) : Mensaje

@Serializable
@SerialName("PING")
data class Latido(val nonce: String, val v: Int = 1) : Mensaje

@Serializable
@SerialName("PONG")
data class RespuestaLatido(val nonce: String, val v: Int = 1) : Mensaje

@Serializable
@SerialName("PEER_STATUS")
data class Presencia(val peerRole: Rol, val online: Boolean, val v: Int = 1) : Mensaje

@Serializable
@SerialName("PEER_UNAVAILABLE")
data class ReceptorAusente(
    val refCommandId: String?,
    val reason: String = "PEER_OFFLINE",
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("ERROR")
data class ErrorProtocolo(val code: Motivo, val v: Int = 1) : Mensaje

@Serializable
@SerialName("SYNC_REQUEST")
data class SolicitudSincronizacion(
    val pairId: String,
    val syncNonce: String,
    val sender: String = "PARENT",
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("SET_POLICY")
data class OrdenPolitica(
    val pairId: String,
    val bootId: String,
    val childSessionId: String,
    val commandId: String,
    val commandNonce: String,
    val seq: String,
    val action: Accion,
    val durationSec: Int?,
    val sender: String = "PARENT",
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("ACK")
data class Confirmacion(
    val pairId: String,
    val bootId: String,
    val childSessionId: String,
    val commandId: String,
    val seq: String,
    val commandPayloadSha256B64: String,
    val result: ResultadoAck,
    val mode: Modo,
    val reason: Motivo,
    val remainingSec: Int?,
    val sender: String = "CHILD",
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("STATE")
data class EstadoDispositivo(
    val pairId: String,
    val syncNonce: String,
    val bootId: String,
    val childSessionId: String,
    val stateSeq: String,
    val lastAcceptedSeq: String,
    val lastCommandId: String?,
    val lastCommandPayloadSha256B64: String?,
    val lastResult: ResultadoEstado,
    val mode: Modo,
    val reason: Motivo,
    val remainingSec: Int?,
    val networkValidated: Boolean,
    val channelHealthy: Boolean,
    val isDeviceOwner: Boolean,
    val lockTaskActive: Boolean,
    val commandNonce: String,
    val sender: String = "CHILD",
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("CHILD_OFFER")
data class OfertaHijo(
    val childPublicKeyB64: String,
    val enrollId: String,
    val enrollNonce: String,
    val v: Int = 1,
) : Mensaje

@Serializable
data class HashesRecuperacion(
    val MAINTENANCE: String,
    val RECOVER_PARENT: String,
    val RETIRE: String,
)

@Serializable
@SerialName("PAIR_ACCEPT")
data class AceptacionVinculo(
    val pairId: String,
    val enrollId: String,
    val enrollNonce: String,
    val parentPublicKeyB64: String,
    val childPublicKeyB64: String,
    val relayOrigin: String,
    val recoveryHashes: HashesRecuperacion,
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("OFFLINE_CHALLENGE")
data class RetoOffline(
    val pairId: String,
    val bootId: String,
    val action: AccionOffline,
    val nonce: String,
    val sender: String = "CHILD",
    val v: Int = 1,
) : Mensaje

@Serializable
@SerialName("OFFLINE_RESPONSE")
data class RespuestaOffline(
    val pairId: String,
    val bootId: String,
    val action: AccionOffline,
    val nonce: String,
    val sender: String = "PARENT",
    val v: Int = 1,
) : Mensaje
