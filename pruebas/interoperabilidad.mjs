// Oráculo criptográfico de pruebas JVM/Node, no servidor ni codec de producción.
// Solo admite los payloads congelados. No guarda ni imprime privadas.
import { createHash, createPublicKey, generateKeyPairSync, sign, verify } from 'node:crypto';
import { readFileSync } from 'node:fs';
import assert from 'node:assert/strict';

const fixture = JSON.parse(readFileSync(new URL('./vectores_crypto.json', import.meta.url), 'utf8'));
let entrada = '';
for await (const bloque of process.stdin) {
  entrada += bloque.toString('utf8');
  assert.ok(entrada.length <= 32768, 'Entrada de prueba demasiado grande');
}
const peticion = JSON.parse(entrada);
assert.ok(['PARENT', 'CHILD'].includes(peticion.rol));
assert.ok(['AUTH', 'MESSAGE', 'PAIR', 'OFFLINE'].includes(peticion.proposito));
function base64(valor) {
  assert.equal(typeof valor, 'string');
  assert.match(valor, /^[A-Za-z0-9_-]+$/);
  const bytes = Buffer.from(valor, 'base64url');
  assert.equal(bytes.toString('base64url'), valor);
  return bytes;
}
const payloadB64 = peticion.accion === 'verificar' ? peticion.sobre?.payloadB64 : peticion.payloadB64;
assert.ok(fixture.vectors.some(vector => vector.signerRole === peticion.rol &&
  vector.envelope.purpose === peticion.proposito && vector.envelope.payloadB64 === payloadB64),
  'Solo payloads de los vectores congelados, con su rol y propósito');
const payload = base64(payloadB64);
const bytesFirma = Buffer.concat([Buffer.from(`CPv1/${peticion.proposito}\n`, 'utf8'), payload]);
let respuesta;
if (peticion.accion === 'verificar') {
  assert.deepEqual(Object.keys(peticion.sobre).sort(), ['kind', 'payloadB64', 'purpose', 'sigB64', 'v']);
  assert.equal(peticion.sobre.v, 1);
  assert.equal(peticion.sobre.kind, 'SIGNED');
  assert.equal(peticion.sobre.purpose, peticion.proposito);
  const publica = createPublicKey({ key: base64(peticion.publicaB64), format: 'der', type: 'spki' });
  assert.equal(publica.asymmetricKeyType, 'ec');
  assert.equal(publica.asymmetricKeyDetails?.namedCurve, 'prime256v1');
  assert.ok(verify('sha256', bytesFirma, { key: publica, dsaEncoding: 'der' }, base64(peticion.sobre.sigB64)));
  respuesta = { ok: true, hash: createHash('sha256').update(payload).digest('base64url') };
} else if (peticion.accion === 'firmar') {
  const { publicKey, privateKey } = generateKeyPairSync('ec', { namedCurve: 'prime256v1' });
  const publicaB64 = publicKey.export({ type: 'spki', format: 'der' }).toString('base64url');
  const sigB64 = sign('sha256', bytesFirma, { key: privateKey, dsaEncoding: 'der' }).toString('base64url');
  respuesta = { publicaB64, sobre: { v: 1, kind: 'SIGNED', purpose: peticion.proposito, payloadB64, sigB64 } };
} else {
  throw new Error('Acción de prueba desconocida');
}
process.stdout.write(JSON.stringify(respuesta));
