import { createHash, createPublicKey, verify, type KeyObject } from 'node:crypto';
import type { Payload, Proposito, Rol } from './modelos.js';
import { CodecProtocolo } from './protocol.js';
import { base64, publicaP256 } from './formatos.js';
import { exigir, MensajeInvalido } from './json-estricto.js';

export class FirmaInvalida extends Error {
  constructor() { super('La firma no corresponde a los bytes y clave esperados'); }
}

export class ClaveVerificacion {
  private constructor(readonly rol: Rol, private readonly publica: KeyObject) { Object.freeze(this); }

  // Solo públicas configuradas/vinculadas. No se buscan claves dentro del payload.
  static desdeSpki(rol: Rol, publicaB64: string): ClaveVerificacion {
    exigir(rol === 'PARENT' || rol === 'CHILD', 'Rol de verificación inválido');
    const bytes = base64(publicaB64, 150);
    publicaP256(bytes);
    return new ClaveVerificacion(rol, createPublicKey({ key: bytes, format: 'der', type: 'spki' }));
  }

  comprobar(bytes: Buffer, firma: Buffer): void {
    let valida = false;
    try { valida = verify('sha256', bytes, { key: this.publica, dsaEncoding: 'der' }, firma); }
    catch { /* Un error criptográfico no abre una vía alternativa. */ }
    if (!valida) throw new FirmaInvalida();
  }
}

export interface DocumentoVerificado {
  readonly mensaje: Readonly<Payload>;
  readonly proposito: Proposito;
  readonly rol: Rol;
  readonly payloadSha256B64: string;
  bytesPayload(): Buffer;
  bytesFrame(): Buffer;
}

function congelar(valor: object): void {
  for (const campo of Object.values(valor) as unknown[]) {
    if (campo !== null && typeof campo === 'object') congelar(campo);
  }
  Object.freeze(valor);
}

export function bytesFirma(proposito: Proposito, payload: Uint8Array): Buffer {
  return Buffer.concat([Buffer.from(`CPv1/${proposito}\n`, 'utf8'), Buffer.from(payload)]);
}

// El relay solo verifica; aquí no hay firma, privadas ni generación de autoridades.
export class CriptografiaProtocolo {
  constructor(private readonly codec: CodecProtocolo) {}

  verificarSobre(bytesFrame: Uint8Array, clave: ClaveVerificacion, propositoEsperado: Proposito): DocumentoVerificado {
    const frame = Buffer.from(bytesFrame);
    const sobre = this.codec.leerFrame(frame);
    exigir(sobre.kind === 'SIGNED', 'Se esperaba un contenedor firmado');
    exigir(sobre.purpose === propositoEsperado, 'Propósito distinto del esperado');
    const payload = this.codec.bytesPayload(sobre);
    clave.comprobar(bytesFirma(sobre.purpose, payload), base64(sobre.sigB64, 75));
    // Nunca recanonizar antes de verificar ni verificar un JSON reconstruido.
    const mensaje = this.codec.leerPayloadSinVerificar(payload, sobre.purpose);
    const rol = mensaje.kind === 'AUTH_PROOF' ? mensaje.role :
      ['SYNC_REQUEST', 'SET_POLICY', 'PAIR_ACCEPT', 'OFFLINE_RESPONSE'].includes(mensaje.kind) ? 'PARENT' : 'CHILD';
    exigir(clave.rol === rol, 'La clave configurada no corresponde al rol del payload');
    congelar(mensaje);
    return Object.freeze({
      mensaje, proposito: sobre.purpose, rol: clave.rol,
      payloadSha256B64: createHash('sha256').update(payload).digest('base64url'),
      bytesPayload: () => Buffer.from(payload), bytesFrame: () => Buffer.from(frame),
    });
  }
}
