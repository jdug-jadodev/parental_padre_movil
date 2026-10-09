import assert from 'node:assert/strict';
import { connect } from 'node:net';
import type { AddressInfo } from 'node:net';
import { once } from 'node:events';
import { test } from 'node:test';
import { crearServidorLocal, leerPuerto } from '../src/server.js';

test('la base local no afirma estar lista para operar CP/1', async (contexto) => {
  const servidor = crearServidorLocal();
  servidor.listen(0, '127.0.0.1');
  await once(servidor, 'listening');
  contexto.after(() => new Promise<void>((resolver, rechazar) => {
    servidor.close((error) => error ? rechazar(error) : resolver());
    servidor.closeAllConnections();
  }));
  const { port } = servidor.address() as AddressInfo;
  const respuesta = await fetch(`http://127.0.0.1:${port}/healthz`);
  assert.equal(respuesta.status, 503);
  assert.deepEqual(await respuesta.json(), { ok: false, protocol: 1 });
  const otraRuta = await fetch(`http://127.0.0.1:${port}/`);
  assert.equal(otraRuta.status, 404);
  const otroMetodo = await fetch(`http://127.0.0.1:${port}/healthz`, { method: 'POST' });
  assert.equal(otroMetodo.status, 404);

  await contexto.test('rechaza el upgrade en lugar de abrir un eco sin autenticación', async () => {
    const socket = connect(port, '127.0.0.1');
    contexto.after(() => socket.destroy());
    socket.setTimeout(3000, () => socket.destroy(new Error('El servidor no respondió')));
    socket.setEncoding('utf8');
    await once(socket, 'connect');
    socket.write('GET /ws HTTP/1.1\r\nHost: localhost\r\nConnection: Upgrade\r\nUpgrade: websocket\r\n\r\n');
    const [datos] = await once(socket, 'data');
    assert.match(String(datos), /^HTTP\/1\.1 503/);
    socket.destroy();
  });
});

test('valida el puerto sin conversiones ambiguas', () => {
  assert.equal(leerPuerto(undefined), 10000);
  assert.equal(leerPuerto('65535'), 65535);
  for (const valor of ['', '0', '-1', '01', '1.5', '1e3', ' 10000', '65536']) {
    assert.throws(() => leerPuerto(valor), /PORT/);
  }
});
