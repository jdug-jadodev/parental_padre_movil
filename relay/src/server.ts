import { createServer, type Server } from 'node:http';
import { pathToFileURL } from 'node:url';

// Base C00 exclusivamente local. No hay eco WSS ni modo público sin autenticar.
export function crearServidorLocal(): Server {
  const servidor = createServer((solicitud, respuesta) => {
    respuesta.setHeader('Content-Type', 'application/json; charset=utf-8');
    respuesta.setHeader('Cache-Control', 'no-store');
    if (solicitud.method === 'GET' && solicitud.url === '/healthz') {
      respuesta.writeHead(503);
      respuesta.end(JSON.stringify({ ok: false, protocol: 1 }));
      return;
    }
    respuesta.writeHead(404);
    respuesta.end(JSON.stringify({ error: 'Ruta no disponible' }));
  });
  servidor.on('upgrade', (_solicitud, socket) => {
    socket.end('HTTP/1.1 503 Service Unavailable\r\nConnection: close\r\n\r\n');
  });
  return servidor;
}

export function leerPuerto(valor: string | undefined): number {
  if (valor === undefined) return 10000;
  if (!/^[1-9][0-9]{0,4}$/.test(valor)) throw new Error('PORT debe ser un entero entre 1 y 65535');
  const puerto = Number(valor);
  if (puerto > 65535) throw new Error('PORT debe ser un entero entre 1 y 65535');
  return puerto;
}

function iniciar(): void {
  if (process.env.NODE_ENV === 'production') {
    throw new Error('C00 solo permite ejecución local. El relay público requiere R01–R03.');
  }
  const servidor = crearServidorLocal();
  servidor.on('error', () => {
    console.error('No se pudo iniciar el servidor local. Revisa el puerto.');
    process.exitCode = 1;
  });
  servidor.listen(leerPuerto(process.env.PORT), '127.0.0.1', () => {
    console.info('Base local iniciada; /healthz indica que CP/1 todavía no está operativo.');
  });
  const detener = (): void => {
    const limite = setTimeout(() => servidor.closeAllConnections(), 3000);
    limite.unref();
    servidor.close(() => clearTimeout(limite));
  };
  process.once('SIGTERM', detener);
  process.once('SIGINT', detener);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) iniciar();
