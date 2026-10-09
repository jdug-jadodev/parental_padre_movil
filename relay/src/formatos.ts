import { createPublicKey } from 'node:crypto';
import { exigir, MensajeInvalido } from './json-estricto.js';

export function base64(valor: string, maximoBytes: number, longitud?: number): Buffer {
  exigir(valor.length <= Math.ceil(maximoBytes * 4 / 3) && /^[A-Za-z0-9_-]+$/.test(valor), 'Base64url inválido o demasiado largo');
  const bytes = Buffer.from(valor, 'base64url');
  exigir(bytes.length <= maximoBytes && (longitud === undefined || bytes.length === longitud), 'Longitud decodificada incorrecta');
  exigir(bytes.toString('base64url') === valor, 'Base64url no canónico');
  return bytes;
}

export function secuencia(valor: string, permiteCero = false): bigint {
  exigir(/^(0|[1-9][0-9]{0,18})$/.test(valor), 'Secuencia decimal inválida');
  const numero = BigInt(valor);
  exigir(numero <= 9223372036854775807n && (permiteCero || numero > 0n), 'Secuencia fuera de 64 bits');
  return numero;
}

export function publicaP256(bytes: Buffer): void {
  try {
    const clave = createPublicKey({ key: bytes, format: 'der', type: 'spki' });
    exigir(clave.asymmetricKeyType === 'ec' && clave.asymmetricKeyDetails?.namedCurve === 'prime256v1', 'La pública no es P-256');
    exigir(bytes.equals(clave.export({ format: 'der', type: 'spki' })), 'SPKI no canónico');
  } catch (error) {
    if (error instanceof MensajeInvalido) throw error;
    throw new MensajeInvalido('Pública SPKI inválida');
  }
}

// Solo estructura ASN.1 DER. La verificación criptográfica pertenece a C02.
export function firmaDer(bytes: Buffer): void {
  exigir(bytes.length >= 8 && bytes.length <= 72 && bytes[0] === 0x30, 'Firma DER inválida');
  exigir(bytes[1] === bytes.length - 2, 'Longitud DER inválida');
  let posicion = 2;
  for (let entero = 0; entero < 2; entero++) {
    exigir(posicion + 2 < bytes.length && bytes[posicion++] === 2, 'Entero DER inválido');
    const longitud = bytes[posicion++]!;
    exigir(longitud >= 1 && longitud <= 33 && posicion + longitud <= bytes.length, 'Longitud de entero DER inválida');
    const primero = bytes[posicion]!;
    exigir(primero < 128, 'Entero DER negativo');
    exigir(longitud === 1 || primero !== 0 || (bytes[posicion + 1]! & 128) !== 0, 'Padding DER innecesario');
    exigir(longitud < 33 || primero === 0, 'Entero DER mayor de 256 bits');
    exigir(bytes.subarray(posicion, posicion + longitud).some((it) => it !== 0), 'Entero DER cero');
    posicion += longitud;
  }
  exigir(posicion === bytes.length, 'Bytes extra en firma DER');
}

export function origenHttps(valor: string): void {
  let url: URL;
  try { url = new URL(valor); } catch { throw new MensajeInvalido('Origen HTTPS inválido'); }
  exigir(url.protocol === 'https:' && url.username === '' && url.password === '' &&
    url.pathname === '/' && url.search === '' && url.hash === '' &&
    /^https:\/\/[a-z0-9][a-z0-9.-]*(?::[0-9]{1,5})?$/.test(valor), 'Se requiere un origen HTTPS exacto');
  const puerto = /:(\d+)$/.exec(valor)?.[1];
  exigir(puerto === undefined || Number(puerto) >= 1 && Number(puerto) <= 65535, 'Puerto HTTPS inválido');
  exigir(/^[a-z0-9](?:[a-z0-9-]*[a-z0-9])?(?:\.[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)*\.?$/.test(url.hostname), 'Host HTTPS inválido');
}
