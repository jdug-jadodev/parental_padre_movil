import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { test } from 'node:test';

test('C00 no puede arrancar como relay público de producción', () => {
  const ruta = fileURLToPath(new URL('../src/server.ts', import.meta.url));
  const resultado = spawnSync(process.execPath, ['--import', 'tsx', ruta], {
    env: { ...process.env, NODE_ENV: 'production' },
    encoding: 'utf8',
    timeout: 5000,
  });
  assert.equal(resultado.error, undefined);
  assert.equal(resultado.status, 1);
  assert.match(resultado.stderr, /solo permite ejecución local/);
});
