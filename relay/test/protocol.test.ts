import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { generateKeyPairSync } from 'node:crypto';
import { test } from 'node:test';
import { CodecProtocolo } from '../src/protocol.js';
import { canonico, leerJsonEstricto, MensajeInvalido } from '../src/json-estricto.js';
import { base64, firmaDer, origenHttps, publicaP256, secuencia } from '../src/formatos.js';
import type { Proposito, SobreFirmado } from '../src/modelos.js';

const esquema = readFileSync(new URL('../../compartido/protocolo.schema.json', import.meta.url));
const codec = new CodecProtocolo(esquema);
const recurso = (nombre: string): Buffer => readFileSync(new URL(`../../compartido/ejemplos/${nombre}.json`, import.meta.url));
const objeto = (nombre: string): Record<string, unknown> => JSON.parse(recurso(nombre).toString()) as Record<string, unknown>;
const rechazar = (valor: unknown, proposito: Proposito = 'MESSAGE'): void => {
  assert.throws(() => codec.leerPayloadSinVerificar(Buffer.from(canonico(valor)), proposito), MensajeInvalido);
};

test('los diez vectores conservan bytes exactos, sin afirmar autenticación', () => {
  const fixture = JSON.parse(readFileSync(new URL('../../pruebas/vectores_crypto.json', import.meta.url), 'utf8')) as {
    vectors: { envelope: SobreFirmado }[];
  };
  for (const { envelope } of fixture.vectors) {
    const sobre = codec.leerFrame(Buffer.from(JSON.stringify(envelope))) as SobreFirmado;
    const bytes = codec.bytesPayload(sobre);
    const payload = codec.leerPayloadSinVerificar(bytes, sobre.purpose);
    assert.deepEqual(codec.codificarPayload(payload), bytes);
  }
});

test('frames y ofertas locales usan sus canales propios', () => {
  for (const nombre of ['12_challenge', '13_auth_ok', '14_ping', '15_pong', '16_peer_status', '17_peer_unavailable', '18_error']) {
    const frame = codec.leerFrame(recurso(`${nombre}.frame`));
    assert.equal(codec.leerFrame(codec.codificarFrame(frame)).kind, frame.kind);
  }
  assert.equal(codec.leerOferta(recurso('11_child_offer.frame')).kind, 'CHILD_OFFER');
  assert.throws(() => codec.leerFrame(recurso('11_child_offer.frame')), MensajeInvalido);
  assert.throws(() => codec.leerFrame(recurso('04_lock_for.payload')), MensajeInvalido);
});

test('JSON rechaza duplicados aun cuando las claves usan escapes distintos', () => {
  for (const texto of ['{"v":1,"v":2}', '{"v":1,"\\u0076":1}']) {
    assert.throws(() => leerJsonEstricto(texto), MensajeInvalido);
  }
  assert.equal(canonico(leerJsonEstricto('{ "z":[2,1],"a":null,"b":true }')), '{"a":null,"b":true,"z":[2,1]}');
});

test('JSON no admite numeros ambiguos controles ni profundidad sin límite', () => {
  for (const texto of ['01', '-0', '-1', '1.0', '1e0', '9007199254740992', '[1,]', '{"a":1,}', 'true false', '"ñ"', '"\\n"', '"\\u0000"', '"\\q"', '"sin cierre']) {
    assert.throws(() => leerJsonEstricto(texto), MensajeInvalido);
  }
  assert.throws(() => leerJsonEstricto('['.repeat(34) + '0' + ']'.repeat(34)), MensajeInvalido);
  assert.equal(canonico(leerJsonEstricto('"\\u0041"')), '"A"');
});

test('schema no acepta campos extra ausentes ni coerción de tipos', () => {
  const orden = objeto('04_lock_for.payload');
  rechazar({ ...orden, extra: true });
  const incompleta = { ...orden };
  delete incompleta.durationSec;
  rechazar(incompleta);
  for (const [clave, valor] of [['v', 2], ['v', '1'], ['seq', 7], ['sender', 'CHILD'], ['bootId', 'no-uuid'], ['commandNonce', 'A'.repeat(42) + 'B'], ['durationSec', true]] as const) {
    rechazar({ ...orden, [clave]: valor });
  }
});

test('duraciones válidas y relaciones de acciones coinciden con CP1', () => {
  const orden = objeto('04_lock_for.payload');
  for (const durationSec of [60, 604800]) {
    assert.equal(codec.leerPayloadSinVerificar(Buffer.from(canonico({ ...orden, durationSec })), 'MESSAGE').kind, 'SET_POLICY');
  }
  for (const durationSec of [null, 59, 604801]) rechazar({ ...orden, durationSec });
  rechazar({ ...orden, action: 'LOCK' });
  assert.equal(codec.leerPayloadSinVerificar(Buffer.from(canonico({ ...orden, action: 'LOCK', durationSec: null })), 'MESSAGE').kind, 'SET_POLICY');
});

test('secuencias usan BigInt sin redondear ni aceptar desbordamientos', () => {
  assert.equal(secuencia('9223372036854775807'), 9223372036854775807n);
  assert.equal(secuencia('9007199254740993'), 9007199254740993n);
  assert.equal(secuencia('0', true), 0n);
  for (const valor of ['0', '01', '-1', '+1', '1.0', ' 1', '9223372036854775808']) {
    assert.throws(() => secuencia(valor), MensajeInvalido);
  }
  const orden = objeto('04_lock_for.payload');
  rechazar({ ...orden, seq: '9223372036854775808' });
});

test('estado inicial no permite inventar un APPLIED ni aceptación anterior', () => {
  const estado = objeto('10_state_inicial.payload');
  rechazar({ ...estado, lastResult: 'APPLIED' });
  rechazar({ ...estado, lastAcceptedSeq: '7' });
  rechazar({ ...estado, lastCommandId: estado.bootId });
  rechazar({ ...estado, stateSeq: '0' });
});

test('payload firmado exige bytes canónicos y vinculación de propósito', () => {
  const orden = canonico(objeto('04_lock_for.payload'));
  for (const texto of [' ' + orden, orden + '\n', orden.replace('"v":1', '"v":1,"v":1'), orden.replace('"v":1', '"v":1e0')]) {
    assert.throws(() => codec.leerPayloadSinVerificar(Buffer.from(texto), 'MESSAGE'), MensajeInvalido);
  }
  rechazar(objeto('04_lock_for.payload'), 'OFFLINE');
});

test('UTF8 y limites de bytes rechazan antes de procesar el contenido', () => {
  assert.throws(() => codec.leerFrame(Buffer.alloc(16385, 32)), MensajeInvalido);
  assert.throws(() => codec.leerPayloadSinVerificar(Buffer.alloc(8193, 32), 'MESSAGE'), MensajeInvalido);
  assert.throws(() => codec.leerFrame(Buffer.from([0xc3, 0x28])), MensajeInvalido);
  assert.throws(() => codec.leerFrame(Buffer.concat([Buffer.from([0xef, 0xbb, 0xbf]), recurso('14_ping.frame')])), MensajeInvalido);
});

test('base64 no permite padding bits sobrantes ni longitudes falsas', () => {
  assert.equal(base64('YQ', 1).toString(), 'a');
  for (const valor of ['YQ==', 'YR', 'YQ+', '', 'A', 'YWE']) assert.throws(() => base64(valor, 1), MensajeInvalido);
  assert.throws(() => base64('YQ', 32, 32), MensajeInvalido);
});

test('DER no es JOSE y exige enteros positivos sin padding innecesario', () => {
  const valida = Buffer.from([0x30, 6, 2, 1, 1, 2, 1, 1]);
  firmaDer(valida);
  for (const alterada of [Buffer.concat([valida, Buffer.from([0])]), Buffer.from([0x30, 6, 2, 1, 0, 2, 1, 1]), Buffer.alloc(64)]) {
    assert.throws(() => firmaDer(alterada), MensajeInvalido);
  }
  const sobre = objeto('04_lock_for.signed');
  assert.throws(() => codec.leerFrame(Buffer.from(JSON.stringify({ ...sobre, sigB64: Buffer.alloc(64).toString('base64url') }))), MensajeInvalido);
});

test('solo públicas P256, sin reinterpretar curvas distintas', () => {
  for (const namedCurve of ['prime256v1', 'secp384r1']) {
    const { publicKey } = generateKeyPairSync('ec', { namedCurve });
    const bytes = publicKey.export({ type: 'spki', format: 'der' });
    if (namedCurve === 'prime256v1') publicaP256(bytes);
    else assert.throws(() => publicaP256(bytes), MensajeInvalido);
  }
  assert.throws(() => publicaP256(Buffer.from([1, 2, 3])), MensajeInvalido);
});

test('origen HTTPS exacto sin ruta credenciales ni puerto imposible', () => {
  origenHttps('https://relay.example:443');
  for (const origen of ['http://relay.example', 'https://u@relay.example', 'https://relay.example/', 'https://relay.example:65536', 'https://relay.example?x=1', 'https://a-.example', 'https://a..example']) {
    assert.throws(() => origenHttps(origen), MensajeInvalido);
  }
});

test('frame externo admite espacios pero no claves duplicadas', () => {
  const frame = `{ "nonce":"${'A'.repeat(43)}", "kind":"PING", "v":1 }`;
  assert.equal(codec.leerFrame(Buffer.from(frame)).kind, 'PING');
  assert.throws(() => codec.leerFrame(Buffer.from(frame.replace('"v":1', '"v":1,"v":1'))), MensajeInvalido);
});

test('un esquema alterado nunca reemplaza la copia congelada', () => {
  assert.throws(() => new CodecProtocolo(Buffer.from('{"type":"object"}')), MensajeInvalido);
});

test('acepta limites exactos de frame y de bytes decodificados', () => {
  const ping = recurso('14_ping.frame');
  assert.equal(codec.leerFrame(Buffer.concat([Buffer.alloc(16384 - ping.length, 32), ping])).kind, 'PING');
  const sobre = codec.leerFrame(recurso('04_lock_for.signed')) as SobreFirmado;
  const maximo = { ...sobre, payloadB64: Buffer.alloc(8192, 65).toString('base64url') };
  assert.equal(codec.bytesPayload(codec.leerFrame(codec.codificarFrame(maximo)) as SobreFirmado).length, 8192);
  assert.throws(() => codec.codificarFrame({ ...sobre, payloadB64: Buffer.alloc(8193, 65).toString('base64url') }), MensajeInvalido);
});
