// Tipos CP/1. Los campos conservan el nombre congelado del contrato.
export type Rol = 'PARENT' | 'CHILD';
export type Proposito = 'AUTH' | 'MESSAGE' | 'PAIR' | 'OFFLINE';
export type Accion = 'LOCK' | 'LOCK_FOR' | 'ALLOW' | 'ALLOW_FOR';
export type AccionOffline = 'MAINTENANCE' | 'RETIRE';
export type Modo = 'LOCKED' | 'ALLOWED' | 'MAINTENANCE' | 'SETUP';
export type ResultadoAck = 'APPLIED' | 'ACCEPTED_PENDING_ENFORCEMENT' | 'ENFORCEMENT_FAILED' | 'REJECTED';
export type ResultadoEstado = Exclude<ResultadoAck, 'REJECTED'> | 'NONE';
export type Motivo =
  | 'BAD_SCHEMA' | 'BAD_SIGNATURE' | 'WRONG_PAIR' | 'WRONG_ROLE' | 'STALE_BOOT'
  | 'STALE_SESSION' | 'STALE_NONCE' | 'STALE_SEQUENCE' | 'SEQUENCE_CONFLICT'
  | 'NOT_DEVICE_OWNER' | 'NOT_PROVISIONED' | 'BEFORE_USER_UNLOCK' | 'BOOT_AUTH_REQUIRED'
  | 'NETWORK_LOST' | 'CHANNEL_EXPIRED' | 'PARENT_LOCK' | 'TIMER_EXPIRED' | 'OK'
  | 'MAINTENANCE_ACTIVE' | 'STATE_CORRUPT' | 'ENFORCEMENT_ERROR' | 'KEY_UNAVAILABLE'
  | 'PEER_OFFLINE' | 'RATE_LIMITED' | 'UNKNOWN_VERSION' | 'SYSTEM_KEYGUARD' | 'CALL_IN_PROGRESS';

interface Version<K extends string> { v: 1; kind: K }
interface Identidad { pairId: string }
interface Contexto { bootId: string; childSessionId: string }

export interface SobreFirmado extends Version<'SIGNED'> {
  purpose: Proposito;
  payloadB64: string;
  sigB64: string;
}
export interface RetoConexion extends Version<'CHALLENGE'> {
  connectionId: string;
  nonce: string;
  timeoutSec: 10;
}
export interface PruebaAutenticacion extends Version<'AUTH_PROOF'>, Identidad {
  role: Rol;
  connectionId: string;
  nonce: string;
}
export interface AutenticacionAceptada extends Version<'AUTH_OK'> { connectionId: string }
export interface Latido extends Version<'PING'> { nonce: string }
export interface RespuestaLatido extends Version<'PONG'> { nonce: string }
export interface Presencia extends Version<'PEER_STATUS'> { peerRole: Rol; online: boolean }
export interface ReceptorAusente extends Version<'PEER_UNAVAILABLE'> {
  reason: 'PEER_OFFLINE';
  refCommandId: string | null;
}
export interface ErrorProtocolo extends Version<'ERROR'> { code: Motivo }
export interface SolicitudSincronizacion extends Version<'SYNC_REQUEST'>, Identidad {
  sender: 'PARENT';
  syncNonce: string;
}
export interface OrdenPolitica extends Version<'SET_POLICY'>, Identidad, Contexto {
  sender: 'PARENT';
  commandId: string;
  commandNonce: string;
  seq: string;
  action: Accion;
  durationSec: number | null;
}
export interface Confirmacion extends Version<'ACK'>, Identidad, Contexto {
  sender: 'CHILD';
  commandId: string;
  seq: string;
  commandPayloadSha256B64: string;
  result: ResultadoAck;
  mode: Modo;
  reason: Motivo;
  remainingSec: number | null;
}
export interface EstadoDispositivo extends Version<'STATE'>, Identidad, Contexto {
  sender: 'CHILD';
  syncNonce: string;
  stateSeq: string;
  lastAcceptedSeq: string;
  lastCommandId: string | null;
  lastCommandPayloadSha256B64: string | null;
  lastResult: ResultadoEstado;
  mode: Modo;
  reason: Motivo;
  remainingSec: number | null;
  networkValidated: boolean;
  channelHealthy: boolean;
  isDeviceOwner: boolean;
  lockTaskActive: boolean;
  commandNonce: string;
}
export interface OfertaHijo extends Version<'CHILD_OFFER'> {
  childPublicKeyB64: string;
  enrollId: string;
  enrollNonce: string;
}
export interface AceptacionVinculo extends Version<'PAIR_ACCEPT'>, Identidad {
  enrollId: string;
  enrollNonce: string;
  parentPublicKeyB64: string;
  childPublicKeyB64: string;
  relayOrigin: string;
  recoveryHashes: { MAINTENANCE: string; RECOVER_PARENT: string; RETIRE: string };
}
export interface RetoOffline extends Version<'OFFLINE_CHALLENGE'>, Identidad {
  sender: 'CHILD';
  bootId: string;
  action: AccionOffline;
  nonce: string;
}
export interface RespuestaOffline extends Version<'OFFLINE_RESPONSE'>, Identidad {
  sender: 'PARENT';
  bootId: string;
  action: AccionOffline;
  nonce: string;
}

export type Frame = SobreFirmado | RetoConexion | AutenticacionAceptada | Latido
  | RespuestaLatido | Presencia | ReceptorAusente | ErrorProtocolo;
export type Payload = PruebaAutenticacion | SolicitudSincronizacion | OrdenPolitica
  | Confirmacion | EstadoDispositivo | AceptacionVinculo | RetoOffline | RespuestaOffline;
export type Mensaje = Frame | Payload | OfertaHijo;
