# SPEC-R — Relay WSS mínimo en Render

Propietario de implementación: desarrollador del paquete padre. Un solo backend compartido, no uno por app.

**Objetivo:** autenticar ambos teléfonos y reenviar mensajes en vivo sin guardar órdenes, políticas, historial ni secretos privados. No decide cuándo bloquear. Contrato: `compartido/00_CONTRATO_V1.md`. Fuentes [S11–S13, S23, S25, S26].

## R1. Stack y archivos

Node.js 24 LTS, TypeScript estricto, paquete `ws`, validación con `zod` o un validador equivalente que refleje exactamente el esquema compartido. Servidor HTTP nativo de Node: Express y NestJS no son necesarios. Pruebas con `node:test` ejecutadas mediante `tsx`. Sin Docker en v1.

```text
relay/
  src/config.ts           # valida env al arrancar
  src/server.ts           # HTTP, upgrade /ws, SIGTERM
  src/auth.ts             # retos, verificación de firma y rol
  src/protocol.ts         # codec, parser y límites
  src/registry.ts         # 2 sockets máximo por pareja, uno por rol
  src/heartbeat.ts        # ping/pong y limpieza
  test/auth.test.ts
  test/routing.test.ts
  test/limits.test.ts
  package.json
  package-lock.json
  tsconfig.json
  .node-version           # 24
  render.yaml
```

No código de negocio de temporizadores en el relay. No cron, persistencia, base de datos, archivos de familia ni llaves privadas en disco. La configuración se lee al arrancar; si es inválida, falla cerrado y no escucha tráfico operativo.

## R2. Configuración pública estable

| Variable | Validación | Secreto |
|---|---|---|
| `PORT` | Entero proporcionado por Render; 10000 solo como fallback local | No |
| `PUBLIC_ORIGIN` | Origen HTTPS exacto, sin path/query/fragment | No |
| `ACTIVE_PAIR_ID` | UUID v4 | No |
| `PARENT_PUBLIC_KEY_B64` | SPKI DER EC P-256 en base64url | No |
| `CHILD_PUBLIC_KEY_B64` | SPKI DER EC P-256 en base64url; distinta de la del padre | No |
| `NODE_ENV` | `production` en Render | No |
| `LOG_LEVEL` | `error` en producción | No |

La autorización del relay no se basa en un `role` arbitrario, un pairId secreto ni una ruta difícil de adivinar. El par de claves públicas se fija manualmente desde el exportador del padre. Permitir cualquier clave recibida convertiría el sistema en suplantable.

Reemplazar al padre exige actualizar pairId y claves del registro. Cerrar todas las conexiones al desplegar la nueva configuración. El hijo ya debe haber revocado localmente al padre anterior mediante recuperación; cambiar solo Render no cambia la autoridad que el hijo acepta.

## R3. HTTP y WSS

`GET /healthz` devuelve `200 {"ok":true,"protocol":1}` únicamente cuando configuración y bucle del servidor están listos. No informa pairId, claves ni presencia. Otras rutas HTTP: 404. Upgrade permitido únicamente en `/ws` con subprotocolo `cp.v1`. Rechazar frames binarios y compresión (`perMessageDeflate:false`), `maxPayload:16384`.

Los clientes Android no requieren `Origin`. Rechazar por defecto navegadores que envíen un Origin, salvo un allowlist explícito futuro; CORS no sustituye autenticación WSS. No transmitir credenciales ni datos sensibles en query strings. HTTPS/WSS públicos, sin `trustAll` en los clientes. `ws://127.0.0.1` solo puede habilitarse en variantes locales debug que nunca se distribuyan.

Secuencia de conexión: CHALLENGE -> firma AUTH -> AUTH_OK -> mensajes de aplicación. Reto de 32 bytes, ligado a connectionId/socket y reloj monotónico, máximo 10 segundos. Eliminar de RAM al usarlo, expirar o desconectar.

## R4. Enrutamiento y autorización

| Emisor autenticado | Puede enviar al otro cliente |
|---|---|
| Padre | `SYNC_REQUEST`, `SET_POLICY` firmados como padre |
| Hijo | `STATE`, `ACK` firmados como hijo |
| Ambos | PING al relay; no se reenvía a la otra parte |

El relay comprueba estructura, versión, pareja, rol, propósito, firma y límites. Puede reenviar el contenedor intacto tras validar; no reserializa ni firma mensajes como los móviles. La validación final y la decisión de aplicar pertenecen al receptor. Mensajes `PAIR` y `OFFLINE` no viajan por `/ws`.

Si el receptor no existe, devuelve `PEER_UNAVAILABLE` y descarta ese mensaje; no lo pone en una cola. Si la salida del receptor tiene más de 64 KiB acumulados (`bufferedAmount`), cierra esa conexión por lentitud y notifica indisponibilidad. No acumular memoria ilimitada para sostener la ilusión de entrega.

Solo se reemplaza un socket de un rol cuando el nuevo pasó autenticación. Al cerrar un socket, borrarlo del mapa únicamente si sigue siendo el registrado para ese rol; evita que el cierre del antiguo elimine al nuevo.

## R5. Límites y tolerancia a fallos

Límites iniciales del proyecto: máximo 16 conexiones sin autenticar globales, máximo 2 operativas, 10 mensajes de aplicación/segundo por socket autenticado con ráfaga 20; rate limit conservador de upgrades por IP con máximo de entradas y TTL. No confiar en `X-Forwarded-For` arbitrario; validar la cadena de proxy según el entorno de despliegue. Limitar globalmente incluso si no hay una IP fiable.

Solo aceptar un PING pendiente por socket, devolver su nonce; ignorar paquetes duplicados excesivos. Un contador de `ws` puede limpiar sockets muertos con ping/pong de transporte; no confundirlo con prueba de aplicación de órdenes.

En SIGTERM: dejar de aceptar upgrades, cerrar sockets con código 1012 y terminar tras un margen breve. Las políticas permanecen en los teléfonos. No intentar «salvar» órdenes a un archivo antes de cerrar.

No registrar cuerpos, QR, payloads completos ni huellas familiares. Para depurar usar fixtures y contadores agregados de proceso. No habilitar captura de cuerpos en servicios externos. No prometer ausencia de logs de plataforma: Render puede conservar registros operativos.

## R6. Desarrollo y despliegue paso a paso

1. Instalar Node 24 LTS, Git y editor. Crear `relay/` y ejecutar `npm init -y`.
2. Instalar `ws` y validador con versiones exactas; instalar TypeScript, `tsx`, `@types/node@24` y `@types/ws` como dependencias de desarrollo, también fijadas. Guardar `package-lock.json` y usar `npm ci` en CI/despliegue. Nunca dejar dependencias `*`.
3. Activar TypeScript `strict`, módulo NodeNext y tipos correctos para Node 24. Scripts esperados: `dev` con `tsx watch`, `build` con `tsc`, `start` con `node dist/server.js`, `test` con `tsx --test test/*.test.ts` y `check` sin emitir.
4. Implementar primero `/healthz` y cierre controlado. Luego parser/firma, autenticación, mapa y reenvío. No probar seguridad sobre un eco abierto a internet.
5. Generar la configuración pública de pruebas; las privadas de fixtures nunca sirven para producción. Ejecutar tests de límites y suplantación localmente.
6. Subir código a un repositorio del propietario. Crear un Web Service de Render con root directory `relay`, runtime Node, build `npm ci --include=dev && npm run build`, start `npm start`, health `/healthz` y una sola instancia.
7. Colocar las variables públicas reales generadas por el padre y reiniciar despliegue. El nombre del servicio y origen deben coincidir con el vínculo QR de los teléfonos.
8. Usar una instancia que no se suspenda por inactividad para uso habitual. El plan gratuito queda para prototipos y puede introducir bloqueos al suspenderse o por sus otros límites [S12]. No se especifica precio porque debe consultarse al contratar.
9. Validar con dos clientes de prueba autenticados: mensaje, receptor ausente, restart, hash inválido, dos sockets por rol y reconexión. Luego integrar móviles.

Antes del QR definitivo, conocer el origen real asignado al servicio de Render. Si aún no hay vínculo, el servicio se mantiene no operativo por falta de configuración válida: no abrir temporalmente un modo sin autenticación. Vincular presencialmente con ese origen y después cargar la exportación pública y desplegar. No hace falta que el relay funcione para intercambiar los QR locales.

`plantillas/render.yaml` es una plantilla de configuración, no un backend listo. No desplegarla sin implementar el código y crear sus scripts.

## R7. Definición de terminado

Arranca sin base de datos, falla con env inválido, sirve health, rechaza impostores, reenvía únicamente mensajes válidos y no altera bytes firmados. La caída y reconexión no pierden políticas de los móviles. Cada caso R-* de la matriz tiene prueba automatizada. Además se mide memoria sin crecimiento sostenido durante pruebas repetidas de conexión/desconexión.
