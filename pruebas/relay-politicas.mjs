// Harness opcional del padre. Usa el relay compilado externo sin modificarlo.
// Hijo ficticio solo en tests; certificados/privadas únicamente en RAM/pipe.
import assert from 'node:assert/strict';
import { createHash, generateKeyPairSync, randomBytes, randomUUID, sign } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { createRequire } from 'node:module';
import { createInterface } from 'node:readline';
import { once } from 'node:events';

assert.equal(process.argv.slice(2).length, 2, 'Se requieren rutas explícitas de relay y padre');
const [rutaRelay, rutaPadre] = process.argv.slice(2).map(ruta => resolve(ruta));
assert.equal(process.versions.node.split('.')[0], '24', 'El harness requiere Node 24');
const hash = bytes => createHash('sha256').update(bytes).digest('hex');
for (const archivo of ['compartido/00_CONTRATO_V1.md', 'compartido/protocolo.schema.json', 'pruebas/vectores_crypto.json']) {
  assert.equal(hash(readFileSync(join(rutaRelay, archivo))), hash(readFileSync(join(rutaPadre, archivo))), `Contrato distinto: ${archivo}`);
}
const cargar = ruta => import(pathToFileURL(join(rutaRelay, ruta)).href);
const { crearServidorLocal } = await cargar('dist/src/infrastructure/config/dependencias.js');
const { cerrarServidorHttp } = await cargar('dist/src/infrastructure/config/servidor.config.js');
const { CodecProtocolo } = await cargar('dist/src/infrastructure/adapter/protocol/codec-protocolo.js');
const { bytesFirma, ClaveVerificacion, CriptografiaProtocolo } = await cargar('dist/src/infrastructure/adapter/protocol/crypto-protocol.js');
const WebSocket = createRequire(join(rutaRelay, 'package.json'))('ws');
const entrada = createInterface({ input: process.stdin });
const lineas = [], esperas = [];
entrada.on('line', linea => { const espera = esperas.shift(); if (espera) espera(linea); else lineas.push(linea); });
const leer = () => lineas.length ? Promise.resolve(lineas.shift()) : new Promise(resolver => esperas.push(resolver));
let servidor, hijo;
const limite = setTimeout(() => { console.error('Timeout del harness de políticas'); process.exit(1); }, 60000);
try {
  const config = JSON.parse(await leer());
  const identidad = generateKeyPairSync('ec', { namedCurve: 'prime256v1' });
  const publicaHijo = identidad.publicKey.export({ type: 'spki', format: 'der' }).toString('base64url');
  const pairId = randomUUID(), bootId = randomUUID();
  let childSessionId = randomUUID(), commandNonce = randomBytes(32).toString('base64url');
  const codec = new CodecProtocolo(readFileSync(join(rutaRelay, 'compartido/protocolo.schema.json')));
  const cripto = new CriptografiaProtocolo(codec);
  const clavePadre = ClaveVerificacion.desdeSpki('PARENT', config.publicaPadre);
  const firmar = (mensaje, purpose = 'MESSAGE') => {
    const bytes = codec.codificarPayload(mensaje);
    const sigB64 = sign('sha256', bytesFirma(purpose, bytes), { key: identidad.privateKey, dsaEncoding: 'der' }).toString('base64url');
    return codec.codificarFrame({ v: 1, kind: 'SIGNED', purpose, payloadB64: bytes.toString('base64url'), sigB64 });
  };
  servidor = crearServidorLocal({ NODE_ENV: 'test', PUBLIC_ORIGIN: 'https://127.0.0.1', ACTIVE_PAIR_ID: pairId,
    PARENT_PUBLIC_KEY_B64: config.publicaPadre, CHILD_PUBLIC_KEY_B64: publicaHijo },
    { cert: config.certificado, key: config.claveTls });
  servidor.listen(0, '127.0.0.1'); await once(servidor, 'listening');
  const origen = `https://127.0.0.1:${servidor.address().port}`;
  hijo = new WebSocket(origen.replace('https:', 'wss:') + '/ws', 'cp.v1', { ca: config.certificado });
  let sincronizaciones = 0, ordenes = 0, original, ultima, ultimoHash;
  let resolverAuth, rechazarAuth;
  const autenticado = new Promise((resolver, rechazar) => { resolverAuth = resolver; rechazarAuth = rechazar; });
  hijo.on('error', rechazarAuth);
  hijo.on('message', bytes => {
    try {
      const frame = codec.leerFrame(bytes);
      if (frame.kind === 'CHALLENGE') {
        hijo.send(firmar({ v: 1, kind: 'AUTH_PROOF', pairId, role: 'CHILD', connectionId: frame.connectionId, nonce: frame.nonce }, 'AUTH'), { binary: false });
      } else if (frame.kind === 'AUTH_OK') resolverAuth();
      else if (frame.kind === 'SIGNED') {
        const doc = cripto.verificarSobre(bytes, clavePadre, 'MESSAGE'), mensaje = doc.mensaje;
        assert.equal(mensaje.pairId, pairId);
        if (mensaje.kind === 'SYNC_REQUEST') {
          sincronizaciones++;
          if (sincronizaciones === 4) childSessionId = randomUUID();
          commandNonce = randomBytes(32).toString('base64url');
          hijo.send(firmar({ v: 1, kind: 'STATE', pairId, sender: 'CHILD', syncNonce: mensaje.syncNonce,
            bootId, childSessionId, stateSeq: String(sincronizaciones), lastAcceptedSeq: ultima?.seq ?? '0',
            lastCommandId: ultima?.commandId ?? null, lastCommandPayloadSha256B64: ultimoHash ?? null,
            lastResult: ultima ? 'ENFORCEMENT_FAILED' : 'NONE', mode: 'LOCKED', reason: 'PARENT_LOCK',
            remainingSec: null, networkValidated: true, channelHealthy: true,
            isDeviceOwner: sincronizaciones !== 1, lockTaskActive: sincronizaciones !== 1, commandNonce }), { binary: false });
        } else {
          assert.equal(mensaje.kind, 'SET_POLICY');
          ordenes++;
          if (!original) {
            assert.equal(mensaje.bootId, bootId); assert.equal(mensaje.childSessionId, childSessionId);
            assert.equal(mensaje.commandNonce, commandNonce); assert.equal(mensaje.seq, '1');
            assert.equal(mensaje.action, 'LOCK_FOR'); assert.equal(mensaje.durationSec, 60);
            original = Buffer.from(bytes); ultima = mensaje; ultimoHash = doc.payloadSha256B64;
          } else assert.ok(original.equals(bytes), 'Retransmisión distinta');
          assert.ok(ordenes <= 3, 'Reproducción antigua o reintentos extra');
          if (ordenes === 3) {
            const ack = { v: 1, kind: 'ACK', pairId, sender: 'CHILD', bootId, childSessionId,
              commandId: ultima.commandId, seq: ultima.seq, commandPayloadSha256B64: ultimoHash,
              result: 'ACCEPTED_PENDING_ENFORCEMENT', mode: 'LOCKED', reason: 'PARENT_LOCK', remainingSec: 60 };
            hijo.send(firmar({ ...ack, commandPayloadSha256B64: 'E'.repeat(43) }), { binary: false });
            hijo.send(firmar(ack), { binary: false });
          }
        }
      }
    } catch { console.error('Falló validación del hijo simulado'); process.exit(1); }
  });
  await autenticado;
  console.log(JSON.stringify({ origen, pairId, publicaHijo }));
  assert.equal(await leer(), 'FIN');
  assert.equal(sincronizaciones, 4); assert.equal(ordenes, 3);
  console.log('OK_POLITICAS');
} finally {
  clearTimeout(limite); entrada.close();
  hijo?.terminate();
  if (servidor?.listening) { const cerrado = once(servidor, 'close'); cerrarServidorHttp(servidor); await cerrado; }
}
