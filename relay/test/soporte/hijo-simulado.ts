import { readFile } from 'node:fs/promises';
import WebSocket from 'ws';

// Cliente exclusivo de pruebas locales: nunca usa claves ni identidades reales.
export function conectarHijoSimulado(origen: string): WebSocket {
  const direccion = new URL(origen);
  if (direccion.protocol !== 'ws:' || direccion.hostname !== '127.0.0.1') {
    throw new Error('El simulador solo puede conectarse a loopback local');
  }
  return new WebSocket(direccion, 'cp.v1', {
    perMessageDeflate: false,
    handshakeTimeout: 3000,
  });
}

// Doble de prueba: reproduce un fixture, no ejecuta políticas ni acredita Android.
export async function leerEstadoSimulado(): Promise<unknown> {
  const ruta = new URL('../../../compartido/ejemplos/10_state_inicial.signed.json', import.meta.url);
  return JSON.parse(await readFile(ruta, 'utf8')) as unknown;
}
