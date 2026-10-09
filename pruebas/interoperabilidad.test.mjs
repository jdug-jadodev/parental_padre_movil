// Pruebas del oráculo local; sin npm, servidor, red ni claves persistidas.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { createPublicKey, verify } from 'node:crypto';

const fixtures = JSON.parse(readFileSync(new URL('./vectores_crypto.json', import.meta.url), 'utf8'));
const vector = fixtures.vectors[0];
const herramienta = fileURLToPath(new URL('./interoperabilidad.mjs', import.meta.url));
const peticion = () => ({ accion: 'verificar', rol: vector.signerRole, proposito: vector.envelope.purpose,
  publicaB64: fixtures.publicKeys[vector.signerRole], sobre: { ...vector.envelope } });
function ejecutar(entrada) {
  const proceso = spawnSync(process.execPath, [herramienta], {
    input: JSON.stringify(entrada), encoding: 'utf8', timeout: 10000, maxBuffer: 65536,
  });
  assert.ifError(proceso.error);
  return proceso;
}
test('oráculo local verifica fixture y hash exacto', () => {
  const resultado = ejecutar(peticion());
  assert.equal(resultado.status, 0);
  assert.deepEqual(JSON.parse(resultado.stdout), { ok: true, hash: vector.payloadSha256B64 });
});
test('oráculo rechaza propósito o rol distintos al fixture', () => {
  for (const cambio of [{ proposito: 'OFFLINE' }, { rol: vector.signerRole === 'PARENT' ? 'CHILD' : 'PARENT' }]) {
    assert.notEqual(ejecutar({ ...peticion(), ...cambio }).status, 0);
  }
});
test('oráculo rechaza clave impostora y firma alterada', () => {
  const otraClave = fixtures.publicKeys[vector.signerRole === 'PARENT' ? 'CHILD' : 'PARENT'];
  assert.notEqual(ejecutar({ ...peticion(), publicaB64: otraClave }).status, 0);
  const entrada = peticion();
  const firma = Buffer.from(entrada.sobre.sigB64, 'base64url');
  firma[firma.length - 1] ^= 1;
  entrada.sobre.sigB64 = firma.toString('base64url');
  assert.notEqual(ejecutar(entrada).status, 0);
});
test('oráculo no procesa payload arbitrario ni campos externos extra', () => {
  const entrada = peticion();
  entrada.sobre.payloadB64 = Buffer.from('{}').toString('base64url');
  assert.notEqual(ejecutar(entrada).status, 0);
  const extra = peticion();
  extra.sobre.alg = 'ninguno';
  assert.notEqual(ejecutar(extra).status, 0);
});
test('firma efímera Node válida sin exportar privada', () => {
  const resultado = ejecutar({ accion: 'firmar', rol: vector.signerRole,
    proposito: vector.envelope.purpose, payloadB64: vector.envelope.payloadB64 });
  assert.equal(resultado.status, 0);
  const respuesta = JSON.parse(resultado.stdout);
  assert.deepEqual(Object.keys(respuesta).sort(), ['publicaB64', 'sobre']);
  const publica = createPublicKey({ key: Buffer.from(respuesta.publicaB64, 'base64url'), format: 'der', type: 'spki' });
  const bytes = Buffer.concat([Buffer.from(`CPv1/${vector.envelope.purpose}\n`), Buffer.from(vector.envelope.payloadB64, 'base64url')]);
  assert.ok(verify('sha256', bytes, { key: publica, dsaEncoding: 'der' }, Buffer.from(respuesta.sobre.sigB64, 'base64url')));
});
