import { Ajv2020, type ValidateFunction } from 'ajv/dist/2020.js';
import { createHash } from 'node:crypto';
import type { Frame, Mensaje, OfertaHijo, Payload, Proposito, SobreFirmado } from './modelos.js';
import { canonico, exigir, leerJsonEstricto, utf8Estricto, type ValorJson } from './json-estricto.js';
import { base64, firmaDer, origenHttps, publicaP256, secuencia } from './formatos.js';

export const MAX_FRAME = 16384;
export const MAX_PAYLOAD = 8192;
const frames = new Set(['SIGNED', 'CHALLENGE', 'AUTH_OK', 'PING', 'PONG', 'PEER_STATUS', 'PEER_UNAVAILABLE', 'ERROR']);
const propositos: Readonly<Record<string, Proposito>> = {
  AUTH_PROOF: 'AUTH', SYNC_REQUEST: 'MESSAGE', SET_POLICY: 'MESSAGE', ACK: 'MESSAGE',
  STATE: 'MESSAGE', PAIR_ACCEPT: 'PAIR', OFFLINE_CHALLENGE: 'OFFLINE', OFFLINE_RESPONSE: 'OFFLINE',
};

export class CodecProtocolo {
  private readonly validar: ValidateFunction;

  // El esquema se inyecta desde configuración confiable, nunca desde un mensaje.
  constructor(esquemaCongelado: Uint8Array) {
    exigir(createHash('sha256').update(esquemaCongelado).digest('hex') ===
      'eccf964fbb41869c47d86ddb0bfda6585136430bab297568d3c20b56607372bb', 'La huella del esquema no coincide con CP/1');
    const ajv = new Ajv2020({ strict: true, allErrors: false, coerceTypes: false, removeAdditional: false });
    this.validar = ajv.compile(JSON.parse(utf8Estricto(esquemaCongelado)) as object);
  }

  leerFrame(bytes: Uint8Array): Frame {
    const objeto = this.documento(bytes, MAX_FRAME);
    exigir(frames.has(String(objeto.kind)), 'Payload suelto no permitido en WSS');
    return objeto as unknown as Frame;
  }

  leerOferta(bytes: Uint8Array): OfertaHijo {
    const objeto = this.documento(bytes, MAX_FRAME);
    exigir(objeto.kind === 'CHILD_OFFER', 'QR no es una oferta del hijo');
    return objeto as unknown as OfertaHijo;
  }

  // No acredita identidad: C02 debe verificar la firma sobre los bytes recibidos primero.
  leerPayloadSinVerificar(bytes: Uint8Array, proposito: Proposito): Payload {
    const objeto = this.documento(bytes, MAX_PAYLOAD);
    exigir(Buffer.from(canonico(objeto)).equals(Buffer.from(bytes)), 'Payload no canónico');
    exigir(Object.hasOwn(propositos, String(objeto.kind)) && propositos[String(objeto.kind)] === proposito, 'Propósito incompatible con payload');
    return objeto as unknown as Payload;
  }

  codificarPayload(mensaje: Payload): Buffer {
    exigir(Object.hasOwn(propositos, mensaje.kind), 'Tipo no apto para firma');
    return this.codificar(mensaje, MAX_PAYLOAD);
  }

  codificarFrame(mensaje: Frame): Buffer {
    exigir(frames.has(mensaje.kind), 'Tipo no apto para frame WSS');
    return this.codificar(mensaje, MAX_FRAME);
  }

  bytesPayload(sobre: SobreFirmado): Buffer { return base64(sobre.payloadB64, MAX_PAYLOAD); }

  private codificar(mensaje: Mensaje, limite: number): Buffer {
    const bytes = Buffer.from(canonico(mensaje));
    this.documento(bytes, limite);
    return bytes;
  }

  private documento(bytes: Uint8Array, limite: number): Record<string, ValorJson> {
    exigir(bytes.length > 0 && bytes.length <= limite, 'Tamaño de mensaje no permitido');
    const valor = leerJsonEstricto(utf8Estricto(bytes));
    exigir(valor !== null && typeof valor === 'object' && !Array.isArray(valor), 'Se esperaba un objeto de protocolo');
    exigir(this.validar(valor), 'El mensaje no cumple el esquema CP/1');
    for (const [clave, contenido] of Object.entries(valor)) {
      if (typeof contenido !== 'string') continue;
      if (['seq', 'stateSeq', 'lastAcceptedSeq'].includes(clave)) secuencia(contenido, clave === 'lastAcceptedSeq');
      if (['nonce', 'syncNonce', 'commandNonce', 'enrollNonce', 'commandPayloadSha256B64', 'lastCommandPayloadSha256B64'].includes(clave)) base64(contenido, 32, 32);
      if (clave === 'payloadB64') base64(contenido, MAX_PAYLOAD);
      if (clave === 'sigB64') firmaDer(base64(contenido, 75));
      if (['parentPublicKeyB64', 'childPublicKeyB64'].includes(clave)) publicaP256(base64(contenido, 150));
      if (clave === 'relayOrigin') origenHttps(contenido);
    }
    if (valor.recoveryHashes) {
      for (const hash of Object.values(valor.recoveryHashes as Record<string, string>)) base64(hash, 32, 32);
    }
    return valor;
  }
}
