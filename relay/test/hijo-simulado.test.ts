import assert from 'node:assert/strict';
import { test } from 'node:test';
import { once } from 'node:events';
import type { AddressInfo } from 'node:net';
import { crearServidorLocal } from '../src/server.js';
import { conectarHijoSimulado, leerEstadoSimulado } from './soporte/hijo-simulado.js';

test('el doble de hijo utiliza un fixture firmado, no una confirmación inventada', async () => {
  const estado = await leerEstadoSimulado() as { purpose: string; payloadB64: string };
  assert.equal(estado.purpose, 'MESSAGE');
  const datos = JSON.parse(Buffer.from(estado.payloadB64, 'base64url').toString('utf8'));
  assert.equal(datos.kind, 'STATE');
  assert.equal(datos.sender, 'CHILD');
  assert.equal(datos.lastResult, 'NONE');
});

test('el cliente hijo de pruebas recibe rechazo hasta implementar autenticación', async (contexto) => {
  const servidor = crearServidorLocal();
  servidor.listen(0, '127.0.0.1');
  await once(servidor, 'listening');
  contexto.after(() => new Promise<void>((resolver) => servidor.close(() => resolver())));
  const { port } = servidor.address() as AddressInfo;
  const hijo = conectarHijoSimulado(`ws://127.0.0.1:${port}/ws`);
  const [error] = await once(hijo, 'error');
  assert.match(String(error), /503/);
  assert.throws(() => conectarHijoSimulado('wss://example.com/ws'), /loopback/);
});
