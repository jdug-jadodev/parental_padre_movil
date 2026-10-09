// Herramienta exclusiva de pruebas JVM/Node. Nunca guarda ni imprime privadas.
import { generateKeyPairSync, sign } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { CodecProtocolo } from '../../src/protocol.ts';
import { bytesFirma, ClaveVerificacion, CriptografiaProtocolo } from '../../src/crypto-protocol.ts';

let entrada = '';
for await (const bloque of process.stdin) {
  entrada += bloque.toString('utf8');
  if (entrada.length > 32768) throw new Error('Entrada de prueba demasiado grande');
}
const peticion = JSON.parse(entrada);
const codec = new CodecProtocolo(readFileSync(new URL('../../../compartido/protocolo.schema.json', import.meta.url)));
const crypto = new CriptografiaProtocolo(codec);
let respuesta;
if (peticion.accion === 'verificar') {
  const clave = ClaveVerificacion.desdeSpki(peticion.rol, peticion.publicaB64);
  const resultado = crypto.verificarSobre(Buffer.from(JSON.stringify(peticion.sobre)), clave, peticion.proposito);
  respuesta = { ok: true, hash: resultado.payloadSha256B64 };
} else if (peticion.accion === 'firmar') {
  const payload = Buffer.from(peticion.payloadB64, 'base64url');
  codec.leerPayloadSinVerificar(payload, peticion.proposito);
  const { publicKey, privateKey } = generateKeyPairSync('ec', { namedCurve: 'prime256v1' });
  const publicaB64 = publicKey.export({ type: 'spki', format: 'der' }).toString('base64url');
  const sigB64 = sign('sha256', bytesFirma(peticion.proposito, payload), { key: privateKey, dsaEncoding: 'der' }).toString('base64url');
  const sobre = { v: 1, kind: 'SIGNED', purpose: peticion.proposito, payloadB64: peticion.payloadB64, sigB64 };
  crypto.verificarSobre(codec.codificarFrame(sobre), ClaveVerificacion.desdeSpki(peticion.rol, publicaB64), peticion.proposito);
  respuesta = { publicaB64, sobre };
} else {
  throw new Error('Acción de interoperabilidad desconocida');
}
process.stdout.write(JSON.stringify(respuesta));
