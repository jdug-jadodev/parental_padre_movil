import assert from 'node:assert/strict';
import { generateKeyPairSync, sign } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import { CodecProtocolo } from '../src/protocol.js';
import { bytesFirma, ClaveVerificacion, CriptografiaProtocolo, FirmaInvalida } from '../src/crypto-protocol.js';
import { MensajeInvalido } from '../src/json-estricto.js';
import type { Proposito, Rol, SobreFirmado } from '../src/modelos.js';

const codec = new CodecProtocolo(readFileSync(new URL('../../compartido/protocolo.schema.json', import.meta.url)));
const crypto = new CriptografiaProtocolo(codec);
const fixture = JSON.parse(readFileSync(new URL('../../pruebas/vectores_crypto.json', import.meta.url), 'utf8')) as {
  publicKeys: Record<Rol, string>;
  vectors: { signerRole: Rol; payloadSha256B64: string; envelope: SobreFirmado }[];
};
const clave = (rol: Rol): ClaveVerificacion => ClaveVerificacion.desdeSpki(rol, fixture.publicKeys[rol]);
const efimera = (): ReturnType<typeof generateKeyPairSync> => generateKeyPairSync('ec', { namedCurve: 'prime256v1' });

test('verifica diez vectores firmados y hashes del payload original', () => {
  for (const vector of fixture.vectors) {
    const bytes = codec.codificarFrame(vector.envelope);
    const resultado = crypto.verificarSobre(bytes, clave(vector.signerRole), vector.envelope.purpose);
    assert.equal(resultado.payloadSha256B64, vector.payloadSha256B64);
    assert.deepEqual(resultado.bytesPayload(), codec.bytesPayload(vector.envelope));
    assert.deepEqual(resultado.bytesFrame(), bytes);
  }
});

test('rechaza claves impostoras cambios de propósito y payload alterado antes de parsearlo', () => {
  for (const { signerRole, envelope } of fixture.vectors) {
    const otra = signerRole === 'PARENT' ? 'CHILD' : 'PARENT';
    assert.throws(() => crypto.verificarSobre(codec.codificarFrame(envelope), clave(otra), envelope.purpose), FirmaInvalida);
    const payload = codec.bytesPayload(envelope);
    payload[payload.length - 1] = payload[payload.length - 1]! ^ 1;
    const cambiado = { ...envelope, payloadB64: payload.toString('base64url') };
    assert.throws(() => crypto.verificarSobre(codec.codificarFrame(cambiado), clave(signerRole), envelope.purpose), FirmaInvalida);
    const otroProposito: Proposito = envelope.purpose === 'AUTH' ? 'MESSAGE' : 'AUTH';
    assert.throws(() => crypto.verificarSobre(codec.codificarFrame({ ...envelope, purpose: otroProposito }), clave(signerRole), otroProposito), FirmaInvalida);
  }
});

test('firma DER alterada no se acepta por conservar estructura válida', () => {
  const vector = fixture.vectors[0]!;
  const firma = Buffer.from(vector.envelope.sigB64, 'base64url');
  firma[firma.length - 1] = firma[firma.length - 1]! ^ 1;
  const cambiado = { ...vector.envelope, sigB64: firma.toString('base64url') };
  assert.throws(() => crypto.verificarSobre(codec.codificarFrame(cambiado), clave(vector.signerRole), vector.envelope.purpose), FirmaInvalida);
});

test('firma válida sobre bytes ambiguos no habilita un documento inválido', () => {
  const { publicKey, privateKey } = efimera();
  const publica = ClaveVerificacion.desdeSpki('PARENT', publicKey.export({ type: 'spki', format: 'der' }).toString('base64url'));
  const original = codec.bytesPayload(fixture.vectors[2]!.envelope).toString('utf8');
  for (const texto of [' ' + original, original.replace('"v":1', '"v":1,"v":1'), original.replace('"v":1', '"v":1e0'), original.replace('PARENT', 'CHILD')]) {
    const payload = Buffer.from(texto);
    const firma = sign('sha256', bytesFirma('MESSAGE', payload), { key: privateKey, dsaEncoding: 'der' });
    const sobre: SobreFirmado = { v: 1, kind: 'SIGNED', purpose: 'MESSAGE', payloadB64: payload.toString('base64url'), sigB64: firma.toString('base64url') };
    assert.throws(() => crypto.verificarSobre(codec.codificarFrame(sobre), publica, 'MESSAGE'), MensajeInvalido);
  }
});

test('AUTH con firma correcta no puede reclamar el otro rol', () => {
  const { publicKey, privateKey } = efimera();
  const publica = ClaveVerificacion.desdeSpki('PARENT', publicKey.export({ type: 'spki', format: 'der' }).toString('base64url'));
  const payload = codec.bytesPayload(fixture.vectors[1]!.envelope);
  const firma = sign('sha256', bytesFirma('AUTH', payload), { key: privateKey, dsaEncoding: 'der' });
  const sobre: SobreFirmado = { v: 1, kind: 'SIGNED', purpose: 'AUTH', payloadB64: payload.toString('base64url'), sigB64: firma.toString('base64url') };
  assert.throws(() => crypto.verificarSobre(codec.codificarFrame(sobre), publica, 'AUTH'), MensajeInvalido);
});

test('resultado no se modifica al alterar buffers del llamador ni su payload', () => {
  const vector = fixture.vectors[2]!;
  const bytes = codec.codificarFrame(vector.envelope);
  const resultado = crypto.verificarSobre(bytes, clave(vector.signerRole), vector.envelope.purpose);
  bytes.fill(0);
  resultado.bytesFrame().fill(0);
  resultado.bytesPayload().fill(0);
  assert.deepEqual(resultado.bytesFrame(), codec.codificarFrame(vector.envelope));
  assert.deepEqual(resultado.bytesPayload(), codec.bytesPayload(vector.envelope));
  assert.ok(Object.isFrozen(resultado));
  assert.ok(Object.isFrozen(resultado.mensaje));
  assert.throws(() => Object.assign(resultado.mensaje, { sender: 'CHILD' }), TypeError);
  assert.throws(() => crypto.verificarSobre(resultado.bytesFrame(), clave('PARENT'), 'OFFLINE'), MensajeInvalido);
});

test('el contenedor no puede introducir algoritmo o pública elegidos por el emisor', () => {
  const vector = fixture.vectors[0]!;
  for (const campo of [{ alg: 'none' }, { publicKeyB64: fixture.publicKeys.PARENT }]) {
    const frame = Buffer.from(JSON.stringify({ ...vector.envelope, ...campo }));
    assert.throws(() => crypto.verificarSobre(frame, clave('PARENT'), 'AUTH'), MensajeInvalido);
  }
});
