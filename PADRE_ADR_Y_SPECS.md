# PADRE — ADR y especificaciones de control parental

**Versión 1.0 · 8 de octubre de 2026 · Especificación para implementar, no una aplicación terminada.**

## Objetivo de este documento

Controlar al hijo desde el móvil del padre, custodiar la autoridad criptográfica y operar un relay mínimo en Render, sin base de datos ni historial.

Este archivo reúne el material del rol y el contrato compartido. Las rutas citadas corresponden a la raíz del paquete ZIP; al copiarlo bajo `docs/`, ajustar solo la ruta de lectura, nunca nombres del protocolo. El esquema JSON, los ejemplos y sus verificadores están en el ZIP. Para una IA de contexto reducido, usar los archivos modulares y una tarea por conversación, en lugar de cargar esta lectura completa.

Se eligieron **dos APK Kotlin separados** con núcleo compartido y **un relay Node/TypeScript/WSS en Render**. No hay base de datos central, Firebase ni historial. El A13 es candidato inicial; su modelo exacto/Android y compatibilidad física están pendientes. Emergencias, suspensión, reinicio, recuperación y desinstalación se validan en el teléfono antes de uso diario.

## Índice

1. [Instrucciones para una IA pequeña](#parte-1)
2. [SPEC de la app padre](#parte-2)
3. [SPEC del relay Render](#parte-3)
4. [ADR-P-001](#parte-4)
5. [ADR-P-002](#parte-5)
6. [ADR-P-003](#parte-6)
7. [ADR-P-004](#parte-7)
8. [ADR-P-005](#parte-8)
9. [Plan paso a paso del padre y relay](#parte-9)
10. [Contrato compartido CP/1](#parte-10)
11. [Uso del esquema y validaciones](#parte-11)
12. [Modelo de amenazas](#parte-12)
13. [Fuentes y compatibilidad](#parte-13)
14. [Tareas comunes C00–C02](#parte-14)
15. [Trazabilidad de requisitos](#parte-15)
16. [Matriz de aceptación](#parte-16)
17. [Ficha del dispositivo](#parte-17)
18. [Plantilla de estado](#parte-18)
19. [Plantilla de traspaso de contexto](#parte-19)
20. [Resultados de validación del paquete](#parte-20)

---

<a id="parte-1"></a>

## Parte 1 · Instrucciones para una IA pequeña

Archivo modular: `padre/AGENTS.md`.

## Instrucciones para una IA de contexto reducido — padre

Tu responsabilidad es **app del padre y relay Render**. El repositorio es por crear; este paquete es documentación. No afirmes que el producto funciona hasta ejecutar pruebas apropiadas.

### Lectura mínima por tarea

1. Lee este archivo y `ESTADO.md` del repositorio.
2. Abre únicamente la tarea elegida en `PLAN_PADRE.md` y las secciones citadas de `SPEC_PADRE.md / SPEC_RELAY_RENDER.md`.
3. Para protocolos, lee las secciones correspondientes de `compartido/00_CONTRATO_V1.md` y el esquema JSON. Para Android específico, consulta las fuentes primarias de compatibilidad antes de inventar una API.
4. Si cambias arquitectura, consulta el ADR afectado; no cambies decisiones globales para resolver un error local.

### Invariantes

Dos APK Kotlin. Hijo Device Owner y Lock Task; padre sin permisos de administración. Render es relay WSS de una instancia, Node24/TypeScript/`ws`, sin DB, Firebase, Redis, historial ni secretos privados del padre. Configuración pública de claves en env sí está permitida y es obligatoria.

Claves P-256, firmas SHA-256 ECDSA DER y bytes deterministas CP/1. No aceptar claves suministradas por un comando. Un ACK del relay no prueba bloqueo. Duraciones con reloj monotónico, contador persistido, contexto de boot/sesión y desafío. Nunca ampliar permisos ante errores.

Emergencia del sistema siempre accesible. App y servicio visibles, sin espionaje ni accesibilidad/overlays como sustituto de políticas oficiales. Recuperación local antes de endurecer. Retirada presencial con aviso de borrado, nunca remota. A13 candidato no significa A13 certificado ni compatibilidad universal.

### Forma de trabajar

Implementa una tarea pequeña por turno. Propón división antes de modificar muchos módulos. Mantén las funciones puras separadas de Android. Usa interfaces/fakes en tests y jamás una clave maestra, bypass o simulación de APPLIED en release. No ejecutes restablecimientos ni restricciones que corten recuperación en un teléfono diario.

Prueba el cambio. Si no hay SDK/dispositivo/permisos, reporta exactamente qué no pudiste verificar y deja la tarea como pendiente de esa prueba. No marques pruebas manuales como aprobadas por inspección del código.

Entrega: breve resultado, rutas cambiadas, pruebas realmente ejecutadas, pendientes y siguiente tarea. Actualiza `ESTADO.md` sin guardar claves, códigos o información personal. Para continuar usa `plantillas/HANDOFF.md`.

### Prompt inicial sugerido

```text
Trabaja solo en la tarea [ID] de padre/PLAN_PADRE.md.
Lee padre/AGENTS.md, ESTADO.md y las secciones que esa tarea indica.
No implementes las tareas posteriores. Respeta CP/1 y los ADR aceptados.
Implementa, ejecuta los tests disponibles y actualiza ESTADO.md.
Indica de forma explícita cualquier prueba que requiera el A13 real.
```

---

<a id="parte-2"></a>

## Parte 2 · SPEC de la app padre

Archivo modular: `padre/SPEC_PADRE.md`.

## SPEC-P — Aplicación del padre

**Objetivo:** desde el teléfono del adulto, vincular un hijo Android administrado, bloquearlo o permitir su uso, establecer duraciones y comprobar el resultado real, sin base de datos central ni historial.

Estado: listo para implementar por tareas; no es una aplicación construida. Protocolo obligatorio: `compartido/00_CONTRATO_V1.md`. Modelo de amenazas y fuentes: los otros dos archivos de `compartido/`.

### P1. Resultado mínimo demostrable

Con padre e hijo activos y vinculados, pulsar «Bloquear» provoca una orden firmada y el padre ve «Bloqueado · confirmado por el dispositivo» únicamente después de un ACK verificable de aplicación. Si el hijo no está conectado, se ve «Pendiente local» o «Sin confirmar», nunca un éxito inventado. El padre también configura «Bloquear durante…», «Permitir» y «Permitir durante…».

La app sirve para controlar, no para leer información privada del niño. No solicita contactos, ubicación, acceso a SMS, contenido de otras apps ni accesibilidad. No configura una cuenta en la nube. Una app por rol evita que el hijo pueda habilitar privilegios de padre cambiando una opción.

### P2. Tecnología y estructura

Base elegida: Kotlin, Jetpack Compose, coroutines/StateFlow, kotlinx.serialization, OkHttp WebSocket y Android Keystore. `minSdk 30`, `compileSdk 36`, `targetSdk 36`. No usar Capacitor, Angular ni WebView en v1; el navegador queda fuera del alcance inicial.

Un solo repositorio facilita compartir el contrato:

```text
control-parental/
  android/
    core-protocol/       # DTO, codec determinista, firmas, validadores, fixtures
    app-parent/          # applicationId: dev.controlparental.parent
    app-child/           # applicationId: dev.controlparental.child
    gradle/libs.versions.toml
    gradlew / gradlew.bat
  relay/                 # Node/TypeScript, propiedad del paquete padre
  docs/                  # Este paquete de documentación
```

Dentro de `app-parent`:

```text
ui/                 MainActivity, PairingScreen, ControlScreen, RecoveryScreen
presentation/       ParentViewModel, ParentUiState
domain/             PairChild, RequestFreshState, SubmitPolicy, ReconcilePending
data/               ParentStore, RelayConnection
security/           ParentKeyStore, RecoveryCodeGenerator, LocalAuthentication
```

Inyección manual por constructor y una clase `AppContainer`; no añadir Hilt, microservicios, Room ni un framework de sincronización. Los ViewModel no firman ni abren sockets directamente. Las funciones puras del núcleo no importan Android.

### P3. Pantallas y requisitos verificables

| ID | Requisito | Comportamiento esperado |
|---|---|---|
| P-F01 | Primer inicio | Explica dos roles, solicita bloqueo seguro del teléfono del padre y crea identidad local |
| P-F02 | Autenticación | Exige credencial del dispositivo o biometría fuerte para usar la clave de autoridad |
| P-F03 | Vincular | Escanea CHILD_OFFER, compara huellas y muestra PAIR_ACCEPT firmado |
| P-F04 | Configurar relay | Acepta origen HTTPS; exporta solo el registro público requerido por Render |
| P-F05 | Control básico | Botones de LOCK, LOCK_FOR, ALLOW y ALLOW_FOR; validación local de duración |
| P-F06 | Estado verificable | Diferencia relay conectado, hijo presente y estado del hijo firmado/fresco |
| P-F07 | Orden pendiente | Mantiene una sola intención local, permite cancelar; no acumula historial |
| P-F08 | Reconectar | Obtiene STATE fresco y reconcilia; no reenvía desbloqueos antiguos automáticamente |
| P-F09 | Recuperación offline | Escanea desafío del hijo y genera respuesta firmada MAINTENANCE o RETIRE |
| P-F10 | Pérdida del padre | Muestra instrucciones de código físico RECOVER_PARENT; no inventa recuperación por correo |
| P-F11 | Privacidad | Ningún dato personal del niño ni código secreto se envía al relay |
| P-F12 | Seguridad visual | QR sensibles solo con adulto autenticado; no exportar claves privadas ni códigos al portapapeles automáticamente |

#### P3.1 Pantalla inicial

Texto claro: «Este teléfono será el administrador. El teléfono del hijo requiere configuración como dispositivo administrado». Botón «Configurar este teléfono como padre». Este texto no permite cambiar de rol en el APK del hijo.

Comprobar `KeyguardManager.isDeviceSecure`; si no hay credencial del sistema, explicar cómo configurarla y no crear autoridad operativa. El PIN de Android protege al padre; no inventar una contraseña propia con recuperación insegura.

#### P3.2 Pantalla principal

Debe mostrar cuatro datos separados: estado de conexión con Render; disponibilidad indicativa del hijo; último estado confirmado y su antigüedad; estado de la intención pendiente. Ejemplo:

```text
Hijo: A13 (alias local)
Relay: conectado
Dispositivo: conectado, estado verificado hace 4 s
Uso: bloqueado por el padre
Última orden actual: aplicada
[Bloquear] [Bloquear durante…]
[Permitir] [Permitir durante…]
```

Si se pierde conexión, reemplazar cualquier indicación de estado actual por «Último estado confirmado: …». No seguir descontando como si el hijo hubiera confirmado nuevos cambios. Un reloj de interfaz es aproximación, no evidencia remota.

`LOCK_FOR`: mostrar «Se permitirá el uso al terminar, si conserva conexión y no se reinicia». `ALLOW`: mostrar «Hasta que bloquees, pierda conexión o reinicie». `ALLOW_FOR`: mostrar «Volverá a bloquearse al terminar». El tiempo no empieza hasta la aceptación en el hijo.

#### P3.3 Sin conexión y cambios pendientes

Permitir preparar una intención y verla como pendiente local, sin afirmar envío. Si ya hay una intención, pedir reemplazarla o cancelarla. Guardar únicamente una. Cancelar no revoca una orden que el hijo ya recibió: antes de informar cancelación efectiva se sincroniza. Una orden aceptada se cambia enviando otra, no borrando un elemento visual.

Tras reabrir la app, autenticar, sincronizar y mostrar «Hay una intención pendiente. ¿Enviar ahora a este dispositivo?». Para `ALLOW`/`ALLOW_FOR` esta confirmación es obligatoria. Una orden cuyo ACK se perdió se consulta antes de crear una nueva para evitar reiniciar su duración.

### P4. Identidad, almacenamiento y permisos

Clave de firma: EC P-256, alias `cp.parent.sign.v1`; privada no exportable en Android Keystore. `setUserAuthenticationRequired(true)` y una ventana de 60 segundos con `AUTH_BIOMETRIC_STRONG | AUTH_DEVICE_CREDENTIAL` en la base API 30. Pedir autenticación nuevamente si la clave no puede utilizarse. La ventana es una decisión del proyecto; bloquear también la interfaz al ir a segundo plano. No convertir fallos de autenticación en firma con una clave alternativa sin protección [S10].

No exigir StrongBox: puede no existir en todos los modelos. Registrar capacidades localmente durante diagnóstico. No prometer hardware específico sin comprobarlo.

Metadatos privados: archivo atómico cifrado AES-256-GCM con otro alias de Keystore y nonce nuevo generado por el proveedor en cada escritura. Guardar `schemaVersion`, alias, pairId, ambas claves públicas, relayOrigin, contador local, estado de vinculación e intención pendiente. No guardar la clave privada dentro del JSON. Las operaciones de contador/intención deben ser transaccionales; una sola coroutine escritora.

Los códigos de recuperación se muestran al generarse para registro externo por el adulto; después no se guardan en texto claro ni se reconstruyen desde los hashes. Antes de terminar la vinculación se pide confirmar que se conservaron fuera del celular. Perder el celular y todos los códigos puede obligar a restablecer y reprovisionar; no hay un soporte central que recupere claves.

Permisos: `INTERNET`, `ACCESS_NETWORK_STATE`, `USE_BIOMETRIC` y `CAMERA` solo para QR mientras la pantalla está abierta. QR mediante CameraX + decodificador local; no usar una API remota para leerlo. No servicio en primer plano en el padre. No permisos de administrador ni Device Owner. Sin backups automáticos; revisar también reglas de transferencia de dispositivo, no solo `allowBackup`.

### P5. Algoritmo de una orden

```text
1. Validar acción y duración.
2. Exigir UI activa y autenticación del adulto.
3. Conectar y autenticar con Render.
4. Pedir STATE firmado con syncNonce nuevo.
5. Verificar firma, identidad, frescura y compatibilidad del hijo.
6. Mostrar confirmación de la intención que se va a enviar.
7. Reservar y persistir contadorLocal+1, usando max con la secuencia del hijo.
8. Construir payload con bootId, sesión y commandNonce actuales.
9. Firmar sus bytes exactos; persistir comando e intención antes de enviar.
10. Enviar y mostrar ENVIADA_SIN_CONFIRMAR.
11. Validar ACK por firma, commandId, seq, hash del payload e identidad.
12. Mostrar APPLIED, pendiente de aplicación o fallo según ACK y estado real.
13. Al cerrar, desconectar sin borrar la intención sin confirmar.
```

La validación de STATE es del dispositivo, no del nombre «A13» ni de un booleano `online` enviado por el relay. El alias local nunca selecciona una clave criptográfica.

El contador se puede adelantar pero no retroceder. Si alcanza el máximo, bloquear nuevas órdenes y exigir nuevo vínculo, no usar módulo ni volver a cero. Para CP/1 solo hay un padre firmante; no resolver concurrencia de varios administradores en esta versión.

### P6. Manejo de errores

| Situación | Respuesta de la app |
|---|---|
| TLS o certificado inválido | Detener conexión y mostrar error; no aceptar cualquier certificado |
| Keystore invalidado o app reinstalada | No regenerar silenciosamente autoridad; flujo de recuperación física |
| Hijo sin Device Owner | Mostrar «No configurado correctamente», no «Protegido» |
| ACK con firma incorrecta | Ignorar como confirmación; mantener sin confirmar y cerrar si hay abuso |
| Secuencia menor local que remota | Usar estado remoto verificado para avanzar la reserva |
| Último resultado coincide pero la pantalla sigue bloqueada por red | Mostrar «Orden aplicada; bloqueo por falta de conexión» |
| Relay reiniciado | Reconectar, autenticar y pedir estado nuevo |
| Hijo reinició | Advertir que se requiere autorización para el nuevo arranque |
| QR equivocado/expirado | Rechazar sin sustituir claves ni estado actual |
| Padre ya no está en primer plano | No intentar conexión/firmas indefinidas; conservar pendiente |

### P7. Pruebas mínimas del módulo

Las pruebas de UI usan un `FakeChild` local y claves ficticias. Deben cubrir las cuatro acciones, las duraciones límite, la denegación biométrica, muerte de proceso entre reserva y envío, ACK perdido, duplicado, fuera de orden, sesión vieja, firma falsa y contador remoto superior.

Los tests de persistencia simulan fallo antes y después de escribir: nunca quedan contador de una orden y payload de otra. Los tests criptográficos usan los fixtures comunes y verifican interoperabilidad Android/JVM/Node, no solo firmar y verificar con la misma función.

Se considera terminada la app padre cuando P-F01…P-F12 y todos los casos P-* de la matriz tienen evidencia. No se considera terminada por mostrar una maqueta de botones.

### P8. Fuera de alcance

No historial, multi-hijo, multi-padre, recuperación de cuentas, llamadas remotas, streaming, localización, webs públicas, notificaciones push ni backend con usuarios. La selección de aplicaciones permitidas se realiza presencialmente en la app del hijo durante preparación; cambiarla remotamente requiere una futura versión explícita del contrato. El control desde navegador necesitará delegación segura y un ADR posterior.

---

<a id="parte-3"></a>

## Parte 3 · SPEC del relay Render

Archivo modular: `padre/SPEC_RELAY_RENDER.md`.

## SPEC-R — Relay WSS mínimo en Render

Propietario de implementación: desarrollador del paquete padre. Un solo backend compartido, no uno por app.

**Objetivo:** autenticar ambos teléfonos y reenviar mensajes en vivo sin guardar órdenes, políticas, historial ni secretos privados. No decide cuándo bloquear. Contrato: `compartido/00_CONTRATO_V1.md`. Fuentes [S11–S13, S23, S25, S26].

### R1. Stack y archivos

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

### R2. Configuración pública estable

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

### R3. HTTP y WSS

`GET /healthz` devuelve `200 {"ok":true,"protocol":1}` únicamente cuando configuración y bucle del servidor están listos. No informa pairId, claves ni presencia. Otras rutas HTTP: 404. Upgrade permitido únicamente en `/ws` con subprotocolo `cp.v1`. Rechazar frames binarios y compresión (`perMessageDeflate:false`), `maxPayload:16384`.

Los clientes Android no requieren `Origin`. Rechazar por defecto navegadores que envíen un Origin, salvo un allowlist explícito futuro; CORS no sustituye autenticación WSS. No transmitir credenciales ni datos sensibles en query strings. HTTPS/WSS públicos, sin `trustAll` en los clientes. `ws://127.0.0.1` solo puede habilitarse en variantes locales debug que nunca se distribuyan.

Secuencia de conexión: CHALLENGE -> firma AUTH -> AUTH_OK -> mensajes de aplicación. Reto de 32 bytes, ligado a connectionId/socket y reloj monotónico, máximo 10 segundos. Eliminar de RAM al usarlo, expirar o desconectar.

### R4. Enrutamiento y autorización

| Emisor autenticado | Puede enviar al otro cliente |
|---|---|
| Padre | `SYNC_REQUEST`, `SET_POLICY` firmados como padre |
| Hijo | `STATE`, `ACK` firmados como hijo |
| Ambos | PING al relay; no se reenvía a la otra parte |

El relay comprueba estructura, versión, pareja, rol, propósito, firma y límites. Puede reenviar el contenedor intacto tras validar; no reserializa ni firma mensajes como los móviles. La validación final y la decisión de aplicar pertenecen al receptor. Mensajes `PAIR` y `OFFLINE` no viajan por `/ws`.

Si el receptor no existe, devuelve `PEER_UNAVAILABLE` y descarta ese mensaje; no lo pone en una cola. Si la salida del receptor tiene más de 64 KiB acumulados (`bufferedAmount`), cierra esa conexión por lentitud y notifica indisponibilidad. No acumular memoria ilimitada para sostener la ilusión de entrega.

Solo se reemplaza un socket de un rol cuando el nuevo pasó autenticación. Al cerrar un socket, borrarlo del mapa únicamente si sigue siendo el registrado para ese rol; evita que el cierre del antiguo elimine al nuevo.

### R5. Límites y tolerancia a fallos

Límites iniciales del proyecto: máximo 16 conexiones sin autenticar globales, máximo 2 operativas, 10 mensajes de aplicación/segundo por socket autenticado con ráfaga 20; rate limit conservador de upgrades por IP con máximo de entradas y TTL. No confiar en `X-Forwarded-For` arbitrario; validar la cadena de proxy según el entorno de despliegue. Limitar globalmente incluso si no hay una IP fiable.

Solo aceptar un PING pendiente por socket, devolver su nonce; ignorar paquetes duplicados excesivos. Un contador de `ws` puede limpiar sockets muertos con ping/pong de transporte; no confundirlo con prueba de aplicación de órdenes.

En SIGTERM: dejar de aceptar upgrades, cerrar sockets con código 1012 y terminar tras un margen breve. Las políticas permanecen en los teléfonos. No intentar «salvar» órdenes a un archivo antes de cerrar.

No registrar cuerpos, QR, payloads completos ni huellas familiares. Para depurar usar fixtures y contadores agregados de proceso. No habilitar captura de cuerpos en servicios externos. No prometer ausencia de logs de plataforma: Render puede conservar registros operativos.

### R6. Desarrollo y despliegue paso a paso

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

### R7. Definición de terminado

Arranca sin base de datos, falla con env inválido, sirve health, rechaza impostores, reenvía únicamente mensajes válidos y no altera bytes firmados. La caída y reconexión no pierden políticas de los móviles. Cada caso R-* de la matriz tiene prueba automatizada. Además se mide memoria sin crecimiento sostenido durante pruebas repetidas de conexión/desconexión.

---

<a id="parte-4"></a>

## Parte 4 · ADR-P-001

Archivo modular: `padre/adr/ADR-P-001.md`.

## ADR-P-001 — Dos APK y núcleo compartido

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

### Contexto

La misma app con un selector de rol simplifica instalación, pero facilita mezclar permisos y lógica. Se necesita una separación entendible para un principiante.

### Decisión

Crear `app-parent` y `app-child` con applicationId diferentes, en el mismo repositorio Kotlin, y una biblioteca `core-protocol`. El padre no incluye DeviceAdminReceiver. El hijo no incluye una opción para convertirse en padre.

### Consecuencias y alternativas descartadas

Se comparten DTO, firma y pruebas, pero no se comparten bases de datos ni claves privadas. Se generan dos APK. Usar una sola app, un framework web o dos repositorios independientes queda rechazado para v1.

### Validación y revisión

P-F01, P-F03, H-SEC-01. Revisar solo al incorporar otra plataforma.

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.

---

<a id="parte-5"></a>

## Parte 5 · ADR-P-002

Archivo modular: `padre/adr/ADR-P-002.md`.

## ADR-P-002 — Autoridad y datos locales

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

### Contexto

No se necesita una cuenta central ni conservar historial. Sí se necesita proteger la autoridad y recuperar la pérdida del padre.

### Decisión

Clave P-256 no exportable en Keystore y autenticación del adulto. Metadatos atómicos cifrados y una intención pendiente local. Sin base de datos ni backup automático. Los códigos de recuperación se preparan presencialmente.

### Consecuencias y alternativas descartadas

No hay inicio de sesión por correo ni restauración de una clave desde Render. Si se pierde la clave se crea un nuevo vínculo mediante recuperación física. El navegador futuro necesitará delegación, no exportación automática de la privada.

### Validación y revisión

P-F02, P-F07, P-KEY-01, H-REC-04. Fuente técnica [S10].

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.

---

<a id="parte-6"></a>

## Parte 6 · ADR-P-003

Archivo modular: `padre/adr/ADR-P-003.md`.

## ADR-P-003 — Relay sin estado de negocio en Render

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

### Contexto

El propietario prefiere Render, WebSockets y ausencia de base de datos e historial.

### Decisión

Un web service Node 24/TypeScript con `ws`, una instancia y registro público fijo por variables de entorno. Guardar solo sockets, desafíos y límites temporales en memoria.

### Consecuencias y alternativas descartadas

Un reinicio descarta conexiones y órdenes no entregadas. La configuración pública es necesaria para autenticar tras reinicio. No Redis, NestJS, Postgres, Firebase ni Docker obligatorio. Multi-instancia y buzón durable exigirían otro ADR.

### Validación y revisión

R-AUTH-01, R-ROUTE-01, R-FAIL-01. Fuentes [S11–S13, S23, S26].

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.

---

<a id="parte-7"></a>

## Parte 7 · ADR-P-004

Archivo modular: `padre/adr/ADR-P-004.md`.

## ADR-P-004 — Confirmación firmada y envío presencial del padre

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

### Contexto

Un mensaje enviado no significa bloqueo ejecutado. Mantener la app padre activa permanentemente añade complejidad y debilita el control de su clave.

### Decisión

La app padre conecta y firma con la UI activa y autenticación vigente. Antes de cada emisión solicita estado/desafío fresco. Solo ACK firmado o STATE correlacionado permiten confirmar resultado.

### Consecuencias y alternativas descartadas

Cerrar el padre interrumpe envíos pendientes, pero no las reglas del hijo. No se prometen notificaciones ni envíos en background. La pérdida de ACK no reinicia temporizadores. Se rechaza pintar éxito por confirmación del relay.

### Validación y revisión

P-ACK-01/02/03, C-SEQ-04. Contrato CP/1 secciones 7–8.

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.

---

<a id="parte-8"></a>

## Parte 8 · ADR-P-005

Archivo modular: `padre/adr/ADR-P-005.md`.

## ADR-P-005 — Recuperación fuera de la nube y retirada presencial

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

### Contexto

La administración no debe quedar irrecuperable si falla internet o se pierde el padre. Un PIN universal sería una debilidad.

### Decisión

Códigos aleatorios por propósito y un solo uso; alternativa de desafío QR firmado para mantenimiento/retirada. La retirada exige presencia física y confirmación explícita de borrado.

### Consecuencias y alternativas descartadas

No hay borrado remoto por WebSocket ni un endpoint maestro de soporte. Recuperación de red no habilita uso normal offline. Es obligatorio probar recuperación antes de endurecer el equipo.

### Validación y revisión

H-REC-01/02/03/04, H-RET-01. Referencia a las APIs de retirada/borrado [S03].

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.

---

<a id="parte-9"></a>

## Parte 9 · Plan paso a paso del padre y relay

Archivo modular: `padre/PLAN_PADRE.md`.

## Plan de implementación — padre y relay

Estado de todas las tareas: **PENDIENTE**. Ninguna aplicación ha sido compilada o probada por este paquete. Cada tarea termina con código, tests y actualización de `ESTADO.md`. Leer únicamente la sección indicada y las dependencias necesarias; no enviar todo el documento completo a una IA pequeña.

### Índice de ejecución

| Tarea | Entregable | Depende de |
|---|---|---|
| P00 | Esqueleto y navegación | C00 |
| P01 | Clave del padre y autenticación | C02, P00 |
| P02 | Vinculación QR y configuración pública | P01, P03; H08 solo para prueba conjunta |
| P03 | Archivo cifrado y pendiente única | P01 |
| R01 | HTTP, configuración y límites básicos | C00 |
| R02 | Autenticación por reto | R01, C02 |
| R03 | Relay, latidos y despliegue | R02 |
| P04 | Cliente WSS del padre | P01, R02 |
| P05 | Sincronización y envío idempotente | P03, P04; H09/H10 para integración |
| P06 | Interfaz de control final | P05 |
| P07 | Autorización local sin internet | P01, P02; H05/H11 solo para integración |
| P08 | Integración y entrega privada | P06, P07, R03, H10, H11 |

### P00 — Esqueleto y navegación

**Depende de:** C00.

**Leer:** SPEC-P P2–P3.

**Archivos/componentes previstos:** MainActivity, ParentUiState, ParentViewModel.

**Implementar:** Crear navegación Primer inicio → Vincular → Control → Recuperación. Usar un repositorio falso exclusivamente en tests/debug. Dibujar estados separados de relay/hijo/confirmación.

**Criterio de aceptación:** Las cuatro acciones se muestran; ningún botón llama aún a un endpoint real. Se puede representar hijo ausente sin pintarlo bloqueado.

**Comprobación:** UI test con estados no vinculado, desconectado y confirmado.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### P01 — Clave del padre y autenticación

**Depende de:** C02, P00.

**Leer:** SPEC-P P4; CP/1 §4.

**Archivos/componentes previstos:** ParentKeyStore, LocalAuthentication, tests de identidad.

**Implementar:** Comprobar bloqueo seguro, crear P-256 no exportable y ventana de autenticación. Implementar exportación solo de pública SPKI. Separar alias de cifrado de metadatos del de firma.

**Criterio de aceptación:** No se firma sin autenticación válida; una cancelación no crea clave alternativa. La clave persiste al reiniciar el proceso.

**Comprobación:** P-KEY-01 y pruebas instrumentadas de autenticación; no usar una biometría simulada como evidencia final.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### P02 — Vinculación QR y configuración pública

**Depende de:** P01, P03; H08 solo para prueba conjunta.

**Leer:** CP/1 §5 y §9; SPEC-P P3.2/P4.

**Archivos/componentes previstos:** PairChild, PairingScreen, RecoveryCodeGenerator.

**Implementar:** Leer CHILD_OFFER, crear pairId, generar códigos por propósito, firmar PAIR_ACCEPT y comparar huellas. Exportar únicamente env pública; probar ambos QR sin internet.

**Criterio de aceptación:** El hijo rechaza oferta vencida, clave que no coincide, propósito falso o nueva vinculación ya estando configurado. Ningún secreto aparece en exportación.

**Comprobación:** P-PAIR-01, C-PAIR-01 y revisión manual de datos exportados.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### P03 — Archivo cifrado y pendiente única

**Depende de:** P01.

**Leer:** SPEC-P P4–P5; CP/1 §10.

**Archivos/componentes previstos:** ParentStore, PendingIntentRecord, tests de recuperación.

**Implementar:** Implementar AtomicFile con AES-GCM y nonce nuevo por escritura. Reservar contador e intención transaccionalmente. Recuperar tras fallos y cancelar intención sin borrar evidencia remota.

**Criterio de aceptación:** Un fallo entre reserva y envío no reutiliza seq para otra orden. Reinstalar no restaura automáticamente una identidad.

**Comprobación:** P-STORE-01 y simulación de fallo de escritura/cierre.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### R01 — HTTP, configuración y límites básicos

**Depende de:** C00.

**Leer:** SPEC-R R1–R3.

**Archivos/componentes previstos:** config.ts, server.ts, config.test.ts.

**Implementar:** Crear relay Node24, scripts de compilación/test y health. Validar env y las públicas P-256. Configurar upgrade seguro, maxPayload y deshabilitar compresión.

**Criterio de aceptación:** Sin configuración válida no atiende sockets. Health no expone claves/pareja.

**Comprobación:** R-CONFIG-01 y R-LIMIT-01.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### R02 — Autenticación por reto

**Depende de:** R01, C02.

**Leer:** CP/1 §6; SPEC-R R4.

**Archivos/componentes previstos:** auth.ts, protocol.ts, auth.test.ts.

**Implementar:** Retos por socket con nonce32B y timeout10s; verificar firma del rol configurado; consumir reto antes de registrar. Implementar AUTH_OK y cierre por fallo.

**Criterio de aceptación:** Un cliente no puede enviar su propia clave para convertirse en padre ni reutilizar el reto de otra conexión.

**Comprobación:** R-AUTH-01/02/03; pruebas contra fixture de ambos roles.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### R03 — Relay, latidos y despliegue

**Depende de:** R02.

**Leer:** SPEC-R R4–R7; CP/1 §8.

**Archivos/componentes previstos:** registry.ts, heartbeat.ts, routing.test.ts, render.yaml.

**Implementar:** Mantener un socket por rol, dirigir mensajes válidos, responder PING y PEER_UNAVAILABLE. Añadir backpressure, limpieza, cierre SIGTERM y despliegue de una instancia.

**Criterio de aceptación:** El relay no acumula órdenes. Una conexión impostora no expulsa la auténtica. Un cierre de socket antiguo no borra el nuevo.

**Comprobación:** R-ROUTE-01/02, R-FAIL-01, R-LIMIT-02.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### P04 — Cliente WSS del padre

**Depende de:** P01, R02.

**Leer:** CP/1 §6/8; SPEC-P P5–P6.

**Archivos/componentes previstos:** RelayConnection, conexión a lifecycle, tests.

**Implementar:** Conectar solo con pantalla activa, comprobar TLS, subprotocolo y challenge, autenticar con Keystore. Reconectar con backoff sin sockets duplicados.

**Criterio de aceptación:** Al volver a foreground se solicita autenticación cuando la clave lo requiere; cerrar app no deja un servicio oculto.

**Comprobación:** P-NET-01, R-AUTH-01 y TLS inválido.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### P05 — Sincronización y envío idempotente

**Depende de:** P03, P04; H09/H10 para integración.

**Leer:** CP/1 §7; SPEC-P P5.

**Archivos/componentes previstos:** RequestFreshState, SubmitPolicy, ReconcilePending.

**Implementar:** Solicitar nonce de sincronización, validar STATE, reservar seq, construir y firmar SET_POLICY, correlacionar ACK y retransmitir solo exactamente el mismo comando.

**Criterio de aceptación:** Pérdida de ACK no reinicia duración. Reconexión exige estado/desafío nuevo y confirmación para una nueva intención.

**Comprobación:** P-ACK-01/02/03, C-SEQ-01/04.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### P06 — Interfaz de control final

**Depende de:** P05.

**Leer:** SPEC-P P3/P6.

**Archivos/componentes previstos:** ControlScreen, formularios de duración, UI tests.

**Implementar:** Conectar botones a casos de uso, validar 60–604800s, mostrar causa real, última evidencia y pendiente. Evitar doble pulsación concurrente.

**Criterio de aceptación:** LOCK_FOR explica apertura posterior condicionada; ALLOW enviado no se pinta como permitido cuando el hijo sigue bloqueado por otra causa.

**Comprobación:** P-UI-01/02 y pruebas de accesibilidad de texto/botones.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### P07 — Autorización local sin internet

**Depende de:** P01, P02; H05/H11 solo para integración.

**Leer:** CP/1 §9; SPEC-P P3/P4.

**Archivos/componentes previstos:** RecoveryScreen, OfflineChallengeSigner.

**Implementar:** Escanear desafío del hijo, autenticar al adulto y firmar OFFLINE con mismo propósito/boot/nonce. Presentar advertencia de borrado para RETIRE; no enviar por relay.

**Criterio de aceptación:** Token MAINTENANCE no sirve para RETIRE ni ALLOW. Repetir token o reiniciar hijo lo invalida.

**Comprobación:** H-REC-01/02, H-RET-01; no borrar dispositivo diario.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### P08 — Integración y entrega privada

**Depende de:** P06, P07, R03, H10, H11.

**Leer:** Matriz completa y README.

**Archivos/componentes previstos:** BUILD_ENV.md, ESTADO.md, evidencias de release.

**Implementar:** Compilar releases, verificar firmas de APK, integrar con A13, reiniciar Render, perder ACK, apagar padre y probar reconexión. Auditar logs/exportaciones.

**Criterio de aceptación:** No queda historial, DB, endpoint maestro, clave de prueba ni credencial en repositorio. Todas las pruebas P/R/C pertinentes tienen evidencia.

**Comprobación:** G1 y G3; no declarar G0/G4 sin evidencia del hijo.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

### Dependencias de integración, sin ciclos

Las referencias a otro rol para una prueba conjunta no bloquean desarrollar y cerrar sus tests unitarios con fakes locales. P02 y H08 se construyen por separado y se integran en P08; lo mismo ocurre con P07 y H11. P08 y H12 usan componentes implementados, no esperan que la otra tarea final ya esté cerrada. La entrega habitual requiere finalmente la evidencia de **ambas**. Dentro del padre, implementar P03 antes de finalizar P02 para persistir el vínculo de forma segura.

---

<a id="parte-10"></a>

## Parte 10 · Contrato compartido CP/1

Archivo modular: `compartido/00_CONTRATO_V1.md`.

## Contrato compartido de control parental — CP/1

Versión de protocolo: 1.0. Fecha: 2026-10-08. Estado: especificación normativa por implementar.

Este archivo es la única fuente de verdad del protocolo. Padre, hijo y relay deben usar los mismos nombres, unidades y validaciones. «DEBE» es obligatorio. Los intervalos y límites de este documento son decisiones del proyecto. Referencias externas: [S09–S13, S20, S23–S26] en `02_FUENTES_Y_COMPATIBILIDAD.md`.

### 1. Alcance y arquitectura mínima

Una familia, un padre y un hijo en v1. Dos APK Kotlin comparten una biblioteca `core-protocol`. El padre conserva su clave privada, configuración y una única intención pendiente. El hijo conserva su clave privada de identidad, la clave pública del padre, restricciones y estado actual. Un proceso Node.js + TypeScript + `ws` en Render conecta ambas partes mediante WSS.

No hay Firebase, base de datos, Redis, colas persistentes, historial, analítica, login con correo ni panel web en v1. Docker no es necesario. El relay conserva **configuración pública estable** en variables de entorno y conexiones temporales en RAM. Eso no es una base de datos ni contiene la clave privada del padre. Sin esta raíz de confianza no se podría autenticar a los clientes tras reiniciar.

El relay usa exactamente una instancia y un proceso; no hay clúster ni escalado horizontal. Puede desconectarse por despliegue o fallo. Los móviles reconectan. El padre necesita la aplicación abierta y autenticada para enviar una orden; el hijo no necesita que el padre esté conectado para ejecutar una política ya aceptada.

### 2. Semántica del producto

#### 2.1 Órdenes

| Acción | Estado solicitado al aceptar | Qué ocurre después |
|---|---|---|
| `LOCK` | Bloqueado | Permanece así hasta nueva autorización |
| `LOCK_FOR` | Bloqueado | Al vencer, permite uso solo con red/canal válidos y en el mismo arranque autorizado |
| `ALLOW` | Permitido | Uso permitido mientras las condiciones locales lo permitan |
| `ALLOW_FOR` | Permitido | Al vencer, vuelve a bloqueado |

`LOCK_FOR` y `ALLOW_FOR` requieren `durationSec` entero entre 60 y 604800 (1 minuto a 7 días). `LOCK` y `ALLOW` requieren `durationSec: null`. Una nueva orden sustituye la anterior: no se acumulan temporizadores. Las duraciones comienzan en la primera aceptación duradera por el hijo, no al pulsar el padre ni al recibir el relay. El ACK devuelve el tiempo restante real.

El bloqueo significa quiosco con pantalla del hijo y acceso de emergencia del sistema; no apagar telefonía, internet ni el servicio controlador. Permitir significa acceso a las aplicaciones expresamente aprobadas durante configuración; **no devolver acceso libre a Ajustes ni retirar Device Owner**. La selección local inicial de aplicaciones autorizadas es parte necesaria del quiosco, no un sistema de seguimiento de aplicaciones.

#### 2.2 Condiciones con prioridad

```text
Si no está aprovisionado o vinculado: CONFIGURACION, no apto para entrega.
Si falla integridad o aplicación de restricciones: ERROR_SEGURO, mantener bloqueo.
Si está antes del primer desbloqueo Android: ARRANQUE_BLOQUEADO.
Si hay mantenimiento presencial autorizado y credenciales Android disponibles: solo herramientas de mantenimiento.
Si no hay autorización aplicable a este arranque: ARRANQUE_BLOQUEADO.
Si falta red validada tras margen: BLOQUEADO_SIN_RED.
Si el heartbeat del relay venció: BLOQUEADO_SIN_CANAL.
Si la política temporal vigente pide bloquear: BLOQUEADO_POR_PADRE.
En los demás casos: PERMITIDO dentro del quiosco.
Emergencia: siempre accesible por el sistema, independientemente de la rama anterior.
```

Cada arranque nuevo queda bloqueado incluso si antes estaba permitido. **Volver a tener internet no elimina ese bloqueo de arranque**: el padre debe enviar una nueva autorización para ese `bootId`. `ALLOW`, `ALLOW_FOR` y `LOCK_FOR` autorizan explícitamente ese arranque; `LOCK` no lo libera. No se acepta una orden pendiente destinada al arranque anterior.

En una desconexión sin reinicio, la política y el temporizador siguen vigentes. Cuando vuelve la red y un heartbeat nuevo demuestra el canal, se reevalúa la misma política; solo vuelve a permitido si no queda ninguna otra causa de bloqueo. Esto permite que el teléfono del padre esté apagado después de emitir una autorización válida.

#### 2.3 Valores iniciales

| Parámetro | Valor v1 | Regla |
|---|---|---|
| Heartbeat de aplicación | Cada 15 segundos | Un solo reloj de reconexión; no bucle ocupado |
| Caducidad del canal | 45 segundos desde último PONG válido | No depende de presencia del padre |
| Margen al perder red validada | 3 segundos | Local; tolera cambio breve Wi-Fi/datos |
| Timeout de autenticación WSS | 10 segundos | El relay cierra conexiones sin autenticar |
| Reconexión | 1, 2, 4, 8, 16, 30 segundos con jitter | Reiniciar al recuperar red; nunca múltiples sockets |
| Espera inicial de ACK | 5 segundos | Después: «sin confirmar», no «falló» definitivo |
| Frescura de emisión de una orden | 30 segundos | Desafío emitido por hijo; reloj monotónico local |
| Ventana de enrolamiento QR | 5 minutos visibles | Medida localmente por el hijo |
| Mantenimiento local | 5 minutos | Sin acceso a apps ordinarias; reinicio lo cancela |
| Frame WSS | Máximo 16384 bytes | Sin compresión ni binarios |
| Payload firmado decodificado | Máximo 8192 bytes | Validar antes de procesar |

Objetivo de latencia del proyecto: desde recepción y validación de una orden en un hijo activo hasta aplicar el bloqueo, ≤1 segundo en el A13 de pruebas; objetivo extremo a extremo p95 ≤3 segundos con ambos clientes activos y red estable. Son métricas por medir, **no garantías con internet interrumpido, Doze o proceso suspendido**. Ante wake/resume se evalúa la política antes de habilitar interacción; demostrarlo con pruebas de procesos y otras apps.

### 3. Identidades y datos

- `pairId`, `bootId`, `childSessionId`, `connectionId`, `commandId`: UUID v4 en minúsculas.
- `nonce`, `syncNonce`, `commandNonce`, `enrollNonce`: 32 bytes aleatorios, base64url sin `=`.
- `seq`: entero positivo de 64 bits representado como **cadena decimal**. Rango 1..9223372036854775807; sin signo, espacios ni ceros iniciales. Estado sin órdenes usa `lastAcceptedSeq: "0"`.
- `stateSeq`: contador de estado de la sesión del hijo, también cadena decimal. Se reinicia solo al cambiar `childSessionId`.
- Tiempos de duración: segundos enteros. Reloj de medición: `elapsedRealtime()` en hijo, reloj monotónico en relay. No se usan fechas de pared para desbloquear.
- Nombres visibles y datos del menor son locales. El protocolo lleva identificadores aleatorios, modos y códigos, no nombre, edad, teléfono ni ubicación.

El hijo genera un `bootId` persistido por arranque, no por cada callback. `LOCKED_BOOT_COMPLETED` y `BOOT_COMPLETED` pertenecen al mismo arranque y no deben crear dos identidades. Se contrasta el contador de arranque del sistema y el estado guardado; si no se puede probar continuidad, se adopta un nuevo contexto bloqueado. Un simple reinicio del proceso no reinicia el contador de órdenes ni el temporizador de la política.

El hijo crea un `childSessionId` nuevo cada vez que autentica una conexión WSS. Las órdenes nuevas se dirigen a ese identificador. Reconectar no borra la política existente, pero invalida órdenes nuevas dirigidas a la sesión anterior.

### 4. Firma y serialización interoperable

Se utilizan APIs criptográficas estándar, no una implementación propia de curvas. Claves EC P-256 (`secp256r1`), SHA-256 y ECDSA; firmas **ASN.1 DER**, no el formato de 64 bytes `r || s` de JOSE. Clave pública: SPKI DER codificada en base64url. En Android se usa `SHA256withECDSA`; en Node `crypto.sign/verify('sha256', ..., {dsaEncoding:'der'})`. No llamar ES256/JWT a este contenedor.

Contenedor externo:

```json
{"v":1,"kind":"SIGNED","purpose":"MESSAGE","payloadB64":"BASE64URL_DE_PAYLOAD","sigB64":"BASE64URL_DE_FIRMA_DER"}
```

Las cadenas anteriores son marcadores explicativos, no un mensaje aceptable. Los vectores de `pruebas/vectores_crypto.json` sí incluyen firmas verificables.

Bytes que se firman:

```text
UTF8("CPv1/" + purpose + "\n") || payloadBytes
```

Propósitos válidos: `AUTH`, `MESSAGE`, `PAIR`, `OFFLINE`. Un mensaje firmado para un propósito no puede usarse para otro.

**Serialización determinista restringida:** objetos con claves ASCII ordenadas lexicográficamente; arrays conservan orden; sin espacios, saltos finales, claves duplicadas ni propiedades desconocidas. Strings de protocolo ASCII imprimible, sin controles. Números: solo enteros seguros, sin exponentes, negativos ni decimales; contadores grandes van como strings. `null` es explícito en los campos opcionales aquí definidos. Para todos los mensajes, el emisor genera estos bytes una sola vez. El receptor verifica la firma sobre los bytes recibidos, después valida UTF-8 estricto, esquema y que recanonizar produzca exactamente los mismos bytes; así rechaza también claves duplicadas y variantes ambiguas. No verifica una firma sobre un JSON reconstruido distinto.

El contenedor externo se valida estructuralmente; ningún campo externo decide autoridad por sí solo. Nunca se acepta `alg` elegible por el cliente ni una clave pública enviada en un comando como sustituto de la clave vinculada.

### 5. Vinculación física inicial, sin servidor de cuentas

1. El hijo, todavía en configuración presencial, genera su clave de identidad no exportable. Muestra un QR `CHILD_OFFER` con `childPublicKeyB64`, `enrollNonce` y `enrollId` aleatorios. El QR no contiene secretos.
2. El padre genera su clave de autoridad en Keystore, protegida por autenticación del dueño del teléfono. Escanea la oferta y crea un `pairId` nuevo. La aplicación solicita el origen del relay (`https://nombre.onrender.com`); nunca acepta `http` en release.
3. El padre genera tres códigos de recuperación diferentes de 160 bits y prepara sus hashes con propósito. Los muestra para guardarlos fuera del teléfono. No los transmite; solo se vinculan sus hashes.
4. El padre presenta un QR `PAIR_ACCEPT` firmado con propósito `PAIR`: `pairId`, `enrollId`, `enrollNonce`, ambas claves públicas, `relayOrigin` y los tres hashes. El hijo lo escanea, verifica coincidencia con su oferta vigente, firma y confirmación física del adulto en ambos equipos. Cada pantalla muestra el mismo resumen de huellas de claves para comparar. Una vez vinculado, esta pantalla no puede cambiar claves sin recuperación autorizada.
5. El padre exporta únicamente configuración **pública** para Render: `ACTIVE_PAIR_ID`, `PARENT_PUBLIC_KEY_B64`, `CHILD_PUBLIC_KEY_B64`, `PUBLIC_ORIGIN`. El desarrollador la coloca en el servicio. Las claves privadas y códigos no se exportan.
6. Padre e hijo prueban autenticación con el relay. La vinculación no se da por terminada hasta recibir estado firmado del hijo y comprobar una recuperación local en entorno de prueba.

Este paso manual evita un sistema de registro, usuarios y base de datos. No se entrega una credencial de administración de Render a ninguna app. El QR de aprovisionamiento Android para instalar un DPC es **otro QR distinto** del QR que vincula padre e hijo. Si se utiliza ese aprovisionamiento se implementan sus handlers oficiales [S18, S19].

### 6. Conexión y autenticación del relay

Endpoint único: `wss://<servicio>/ws`, subprotocolo `cp.v1`. Endpoint de salud: `GET /healthz`, sin datos de la familia. Render termina TLS; Node escucha HTTP en `0.0.0.0:$PORT` internamente [S11, S13].

Al conectar, el relay manda:

```json
{"v":1,"kind":"CHALLENGE","connectionId":"00000000-0000-4000-8000-000000000001","nonce":"NONCE_ALEATORIO","timeoutSec":10}
```

El cliente responde contenedor firmado `AUTH` cuyo payload es:

```json
{"connectionId":"00000000-0000-4000-8000-000000000001","kind":"AUTH_PROOF","nonce":"NONCE_ALEATORIO","pairId":"00000000-0000-4000-8000-000000000002","role":"PARENT","v":1}
```

El relay compara `pairId`, selecciona la clave configurada del rol (`PARENT` o `CHILD`), verifica firma y desafío no usado de esa conexión, y devuelve `AUTH_OK`. No acepta claves propuestas por el cliente. El desafío se consume en RAM. Solo después se incorpora el socket al mapa de la pareja.

Hay máximo un socket por rol. Una conexión nueva **ya autenticada** sustituye a la anterior; una conexión sin autenticar no puede expulsar a nadie. Un desafío antiguo no sirve después de reiniciar Render ni de abrir otro socket.

El padre requiere autenticación local para usar su clave. Su conexión solo se mantiene mientras su interfaz esté activa; si una reconexión requiere nueva autenticación, se pide de nuevo. No se debilita la protección de la clave para lograr envíos en segundo plano.

### 7. Sincronización, emisión y confirmación

#### 7.1 Obtener estado fresco

El padre crea `syncNonce` nuevo y envía `SYNC_REQUEST` firmado con `pairId`, `sender:"PARENT"`, `v:1`, `syncNonce`. No consume `seq`.

El hijo responde `STATE` firmado con la misma pareja y ese `syncNonce`, su `bootId`, `childSessionId`, `stateSeq`, `lastAcceptedSeq`, `lastCommandId`, `lastCommandPayloadSha256B64`, `lastResult`, `mode`, `reason`, `remainingSec`, `networkValidated`, `channelHealthy`, `isDeviceOwner`, `lockTaskActive` y un **nuevo `commandNonce` válido 30 segundos en su reloj monotónico**. El hijo conserva ese desafío en RAM para esa sesión. Cada sincronización reemplaza el anterior. Si no hubo orden previa, id/hash son null y `lastResult:"NONE"`.

El padre acepta el estado solo con firma de la clave del hijo y `syncNonce` de su sesión actual; en actualizaciones posteriores exige `stateSeq` creciente y la misma sesión del hijo. Al reconectar solicita nueva sincronización. La presencia indicada por el relay no reemplaza este proceso.

#### 7.2 Enviar una política

```json
{
  "action":"LOCK_FOR",
  "bootId":"00000000-0000-4000-8000-000000000003",
  "childSessionId":"00000000-0000-4000-8000-000000000004",
  "commandId":"00000000-0000-4000-8000-000000000005",
  "commandNonce":"NONCE_EMITIDO_POR_HIJO",
  "durationSec":1800,
  "kind":"SET_POLICY",
  "pairId":"00000000-0000-4000-8000-000000000002",
  "sender":"PARENT",
  "seq":"7",
  "v":1
}
```

El ejemplo está expandido para lectura. Para firmarlo debe serializarse según la sección 4. El padre reserva `seq = max(contadorLocal, lastAcceptedSeqVerificado) + 1` de forma duradera, guarda la intención y el comando firmado, y lo envía. Un contador reservado nunca se reutiliza para una orden distinta. Se permiten saltos para recuperar cierres inesperados.

El hijo valida firma, esquema, pareja, rol, arranque y sesión. Para una orden nueva exige `seq > lastAcceptedSeq`, `commandNonce` pendiente y no vencido. Consume el desafío y persiste aceptación, hash del **payload**, id y tiempo de inicio de forma atómica antes de ampliar permisos. La firma DER puede tener representaciones distintas; **no usar hash de la firma para detectar duplicados**.

El comando exactamente igual al último aceptado, con mismo id, seq y hash de payload, es idempotente: devuelve el resultado actual sin reiniciar duración ni exigir nuevamente el desafío ya consumido. Esta excepción solo se acepta si bootId y childSessionId del comando siguen siendo los actuales, y solo consulta el último registro. Tras una reconexión o un nuevo arranque se responde STALE_SESSION/STALE_BOOT y se reconcilia mediante STATE; nunca se reaplica la política antigua como una autorización nueva. El mismo seq con otro id o hash se rechaza. Un comando antiguo desconocido se rechaza.

#### 7.3 ACK real

`ACK` está firmado por el hijo y contiene `pairId`, `sender:"CHILD"`, `bootId`, `childSessionId`, `commandId`, `seq`, `commandPayloadSha256B64`, `result`, `mode`, `reason`, `remainingSec` y `v:1`.

Valores de `result`:

- `APPLIED`: la intención está persistida y el controlador comprobó las restricciones efectivas; `mode` puede seguir bloqueado por red o por el PIN inicial de Android. La interfaz explica la causa.
- `ACCEPTED_PENDING_ENFORCEMENT`: existe aceptación duradera pero todavía no prueba suficiente de aplicación. No mostrar «Aplicado».
- `ENFORCEMENT_FAILED`: no se pudo comprobar la aplicación. El hijo intenta mantener bloqueo y el padre muestra error.
- `REJECTED`: no se aceptó ni aplicó esa orden. El campo `reason` explica el código permitido.

`STATE.lastResult` describe solo la última orden aceptada: `APPLIED`, `ACCEPTED_PENDING_ENFORCEMENT`, `ENFORCEMENT_FAILED` o `NONE` si no existe aceptación. Un rechazo posterior no sobrescribe ese registro. `mode` es `LOCKED`, `ALLOWED`, `MAINTENANCE` o `SETUP`. `reason` usa los códigos del esquema. `remainingSec` es null si no existe temporizador, o entero ≥0. No se usa un mensaje de Render para marcar `APPLIED`.

Si el ACK se pierde, el padre retransmite exactamente el mismo comando como máximo dos veces mientras la sesión siga siendo la misma. Luego solicita estado fresco. Solo reconoce la aplicación previa si el estado firmado incluye id, seq y hash coincidentes. No crea una nueva orden con nueva duración por el simple hecho de perder el ACK.

#### 7.4 Órdenes pendientes y cierre del padre

La aplicación guarda una única **intención pendiente**, no una cola histórica. Si no hay hijo conectado no puede firmar un comando nuevo con sesión y desafío válidos. Muestra «Pendiente local; abre esta app cuando el hijo tenga conexión».

Después de reconectar o reiniciar el hijo, no reproduce un desbloqueo firmado antiguo. Sincroniza, reconcilia el último resultado y pide confirmación al padre para emitir otra intención con nuevos identificadores de sesión, desafío y secuencia. Esto también evita desbloqueos diferidos sorpresivos. Ninguna orden tiene garantía de entrega tras cerrar el padre.

### 8. Heartbeats y disponibilidad

Después de `AUTH_OK`, cada cliente envía `PING` de aplicación con nonce aleatorio; el relay devuelve `PONG` con el mismo nonce. El hijo usa solo respuesta a su desafío vigente, en la conexión autenticada actual, como latido. Las tramas de control ping/pong WebSocket pueden usarse para limpiar sockets, pero no reemplazan ese estado de aplicación.

Red validada y canal sano son condiciones independientes. `NetworkCallback` actualiza la primera [S09]. PONG actualiza la segunda. Una respuesta HTTP 200, DNS resuelto, socket abierto o icono Wi-Fi no demuestran un canal autenticado. Captive portal, DNS roto o TLS inválido no permiten uso normal. Si Android pausa el proceso, al reanudarse se calcula el tiempo transcurrido antes de permitir interacción; no se reinicia el margen desde cero.

El relay emite `PEER_UNAVAILABLE` cuando no hay receptor; no almacena el mensaje. Los errores del relay son informativos y nunca autorizan al hijo a liberar el teléfono.

### 9. Recuperación y retirada

No existe PIN maestro fijo en código ni endpoint de desbloqueo administrativo en Render. Durante la vinculación se crean tres códigos independientes, cada uno con 20 bytes aleatorios mostrados como Base32 de 32 caracteres, agrupados para lectura. Se admite quitar guiones/espacios y normalizar mayúsculas; se rechaza cualquier otra transformación.

Hash almacenado en hijo:

```text
SHA256(UTF8("CPv1/RECOVERY\n" + pairId + "\n" + purpose + "\n" + codigoBase32Normalizado))
```

Propósitos: `MAINTENANCE`, `RECOVER_PARENT`, `RETIRE`. Solo se almacenan hashes y bits de consumo, en el archivo transaccional del hijo. La comparación es de tiempo constante. Cada código se consume duraderamente antes de habilitar su función. No se envía al relay ni se registra.

- `MAINTENANCE`: habilita durante 5 minutos únicamente herramientas propias de red y actualización firmada del propio controlador (SPEC-H H11). No concede uso normal sin internet.
- `RECOVER_PARENT`: permite reemplazar presencialmente al padre mediante el flujo QR, crea otro `pairId`, nuevas claves del padre y nuevos códigos, conserva bloqueo y revoca localmente la clave vieja. Exige actualizar las claves públicas de Render antes de retomar conexión. El propietario conserva acceso a su cuenta de Render por una vía independiente.
- `RETIRE`: abre la retirada local, explica que se restablecerá el teléfono, exige confirmación adicional y el texto `BORRAR`. Solo después solicita el restablecimiento mediante la API aplicable. No se expone como comando remoto.

Alternativa habitual sin gastar un código impreso: el hijo muestra un desafío QR nuevo firmado con su clave de identidad y propósito `OFFLINE` y acción `MAINTENANCE` o `RETIRE`, ligado a pairId, bootId y nonce vigente 5 minutos. El padre lo escanea sin internet, verifica la firma con la pública del hijo vinculado, autentica al adulto y firma la respuesta con ese mismo propósito/acción/nonce. El hijo verifica con la clave pública del padre, consume el desafío y aplica exactamente la función autorizada. `RETIRE` sigue requiriendo confirmación destructiva en el hijo. No se admite `ALLOW` offline en v1: mantener la regla «sin internet, bloqueado».

Los desafíos offline se pierden al reiniciar. Los códigos impresos usados **no** vuelven a habilitarse. Cinco intentos erróneos producen espera de 60 segundos; diez, espera de 5 minutos. El número de intentos y la obligación de espera se persisten. Al reiniciar una espera pendiente se reimpone conservadoramente completa. Nunca se bloquea emergencia por fallar un código. La espera afecta solo la interfaz de códigos, no respuestas QR válidas firmadas por el padre.

### 10. Persistencia sin base de datos

Padre: archivo privado cifrado con AES-GCM y clave local de Keystore para metadatos, sin backups automáticos; alias de clave de firma, vínculo, contador y una intención pendiente. La clave privada de firma nunca se serializa. Guardar no equivale a sincronizar.

Hijo: un archivo `control-state.json` mediante `AtomicFile` en almacenamiento protegido del dispositivo y un único escritor serializado. Contiene vínculo público, hashes de recuperación, última aceptación, etapa de aplicación, temporizador y contexto de arranque. No contiene clave privada del padre ni códigos. Su clave de identidad está en Keystore; se usa después del primer desbloqueo. Si antes no puede accederse a Keystore, **se mantiene bloqueado sin intentar fingir conectividad** [S07, S10, S21].

Separar archivo de política y contador sin transacción produciría fallos de repetición; no hacerlo. No guardar duración solo en memoria. Al leer datos desconocidos o corruptos, usar `ERROR_SEGURO`, no reabrir el enrolamiento sin autorización.

### 11. Errores y evolución

Códigos mínimos: `BAD_SCHEMA`, `BAD_SIGNATURE`, `WRONG_PAIR`, `WRONG_ROLE`, `STALE_BOOT`, `STALE_SESSION`, `STALE_NONCE`, `STALE_SEQUENCE`, `SEQUENCE_CONFLICT`, `NOT_DEVICE_OWNER`, `NOT_PROVISIONED`, `BEFORE_USER_UNLOCK`, `BOOT_AUTH_REQUIRED`, `NETWORK_LOST`, `CHANNEL_EXPIRED`, `PARENT_LOCK`, `TIMER_EXPIRED`, `OK`, `MAINTENANCE_ACTIVE`, `STATE_CORRUPT`, `ENFORCEMENT_ERROR`, `KEY_UNAVAILABLE`, `PEER_OFFLINE`, `RATE_LIMITED`, `UNKNOWN_VERSION`, `SYSTEM_KEYGUARD`, `CALL_IN_PROGRESS`.

Un comando con versión desconocida se rechaza. No se agrega «compatibilidad» aceptando campos sin validación. Cualquier cambio incompatible exige CP/2, otro ADR, nuevos fixtures y cambio coordinado de ambas aplicaciones. El futuro navegador necesita su propia delegación de clave/autorización: **no puede usar automáticamente una clave privada no exportable del Android del padre**.

#### 9.A. Recuperación interrumpida y alcance del mantenimiento

El mantenimiento autorizado no concede ALLOWED ni elimina el bloqueo del arranque: puede accederse a sus herramientas limitadas aun con ese bloqueo, después de desbloquear el Android ordinario.

RECOVER_PARENT habilita una ventana presencial de 5 minutos ligada al proceso/arranque. El código se consume al abrirla. La clave vieja se revoca y el nuevo vínculo sustituye al anterior **solo en el commit atómico de la nueva vinculación**. Antes del commit, un fallo/cancelación conserva el vínculo anterior pero cierra la ventana; no permite retomarla sin nueva autorización. Si el código único ya fue consumido y se perdió el padre, se usa la vía RETIRE todavía disponible para restablecer y configurar de nuevo. Después del commit, nunca se restaura silenciosamente la clave anterior. No persistir un booleano que reabra enrolamiento libre después de reiniciar.

---

<a id="parte-11"></a>

## Parte 11 · Uso del esquema y validaciones

Archivo modular: `compartido/04_USO_ESQUEMA.md`.

## Cómo usar el esquema JSON

`protocolo.schema.json` valida un frame o un payload **ya decodificado**. No descodifica automáticamente `payloadB64`, no verifica firmas ni demuestra que una orden esté autorizada. Se valida primero el contenedor y después la definición correspondiente al `kind` de los bytes internos.

| Purpose | Payloads válidos | Clave confiable |
|---|---|---|
| AUTH | AUTH_PROOF | Pública del rol fijada en la configuración del relay |
| MESSAGE | SYNC_REQUEST, SET_POLICY | Pública del padre ya vinculada |
| MESSAGE | STATE, ACK | Pública del hijo ya vinculada |
| PAIR | PAIR_ACCEPT | Nueva pública del padre solo en enrolamiento físico vigente, ligada a la oferta del hijo |
| OFFLINE | OFFLINE_CHALLENGE | Pública del hijo vinculado |
| OFFLINE | OFFLINE_RESPONSE | Pública del padre vinculado |

CHILD_OFFER es un QR local sin firma de autoridad previa. CHALLENGE, AUTH_OK, PING, PONG, PEER_STATUS, PEER_UNAVAILABLE y ERROR son frames de transporte; jamás sustituyen una orden o confirmación firmada. CHALLENGE/AUTH_OK vienen del relay sobre WSS; los latidos solo se aceptan después de autenticar. PAIR/OFFLINE no se aceptan en el relay.

Validaciones adicionales obligatorias: tamaño en bytes, UTF-8 estricto, ASCII del subconjunto, base64url canónico sin padding, SPKI de curva P-256, DER de firma, prefijo de propósito, serialización determinista, máximo real de seq (64 bits), relación entre estado/campos, identidad, boot, sesión, nonce y secuencia. El esquema no aplica por sí mismo esas reglas.

`STATE` describe la última orden **aceptada**. Rechazar una orden posterior no cambia lastAcceptedSeq ni sobrescribe el resultado de esa aceptación. `ACK(REJECTED)` puede describir un comando nuevo inválido en contexto sin modificar ese registro. Un mensaje malformado que no permite construir un ACK seguro se descarta o produce ERROR; no se inventan campos del comando.

`OFFLINE_CHALLENGE` del hijo y `OFFLINE_RESPONSE` del padre están firmados con propósito OFFLINE; ambos contienen pairId, bootId, nonce, action y sender según el esquema. El padre valida primero la firma/pareja del desafío. El hijo conserva el desafío y su vencimiento monotónico en RAM, y lo consume antes de habilitar mantenimiento/retirada. Una foto antigua no sirve después de consumo, vencimiento o reinicio.

`AUTH_OK` tiene connectionId. `PEER_STATUS` lleva peerRole y online, solo informativos. `PEER_UNAVAILABLE` usa reason PEER_OFFLINE y refCommandId (null en una sincronización); no es un ACK del hijo. Los payloads firmados no pueden cambiar estas definiciones unilateralmente.

Los ejemplos bajo `compartido/ejemplos` son objetos válidos de prueba. Sus identidades, nonces y claves no se usan en producción. Los ejemplos se firman y verifican en `pruebas/vectores_crypto.json`.

---

<a id="parte-12"></a>

## Parte 12 · Modelo de amenazas

Archivo modular: `compartido/01_MODELO_AMENAZAS.md`.

## Modelo de amenazas y criterios de seguridad

Versión: 1.0. Aplicable al padre, al hijo y al relay. Consultar fuentes en `02_FUENTES_Y_COMPATIBILIDAD.md`.

### Activos y confianza

El activo principal es la autoridad del padre para cambiar la política. También importan la integridad del estado local, la disponibilidad de emergencias y la posibilidad de recuperar un equipo administrado sin dejarlo inutilizado.

Se confía en Android sin root, el bloqueo de pantalla del padre, Android Keystore, el aprovisionamiento físico autorizado y la cuenta de Render del propietario. El relay transporta metadatos de control y se confía en él para disponibilidad, **no para generar órdenes válidas**. El hijo acepta órdenes únicamente de la clave pública del padre vinculada físicamente.

### Amenazas incluidas

| Amenaza | Respuesta de diseño | Prueba requerida |
|---|---|---|
| Cambiar el teléfono al modo padre | Dos APK con identificadores distintos; ninguna elevación por selector | H-SEC-01 |
| Cerrar, desinstalar o borrar datos desde la interfaz normal | Device Owner, restricciones y políticas oficiales | H-SEC-02 |
| Reiniciar para recuperar acceso | Nuevo contexto de arranque y estado bloqueado; autorización fresca del padre | H-BOOT-01 |
| Quitar internet o bloquear el dominio del relay | Evaluación local de red y vencimiento del heartbeat | H-NET-01/03 |
| Alterar la hora | Duraciones con reloj monotónico; no usar el reloj de pared para liberar | H-TIME-02 |
| Reenviar un desbloqueo antiguo | Firma, pairId, sesión, bootId, secuencia y desafío de emisión | C-SEQ-01/04 |
| Suplantar un teléfono en el relay | Claves públicas fijadas en configuración; reto firmado por conexión | R-AUTH-01 |
| Inventar una confirmación | ACK firmado por el hijo, ligado al comando y al estado aplicado | P-ACK-01 |
| Modificar o truncar el archivo local | Escritura atómica, validación, estado cerrado ante corrupción | H-STATE-02 |
| Adivinar recuperación | Códigos aleatorios de 160 bits, separados por propósito, un solo uso y límite de intentos | H-REC-03 |
| Pérdida del padre o de su clave | Recuperación física previamente preparada y nuevo vínculo; no puerta trasera del servidor | H-REC-04 |
| API de administración no aplicada por el sistema | Comprobación del resultado; nunca informar éxito sin evidencia | H-ENF-03 |

### Amenazas y garantías excluidas

No se promete resistencia a reflasheo, extracción física avanzada, vulnerabilidades del sistema, APK de desarrollo manipulado, cuenta del padre comprometida, poseedor de códigos de recuperación o control administrativo de Render. No se promete tiempo real estricto cuando el sistema no ejecuta la app.

Un relay malicioso puede ocultar órdenes, interrumpir conexiones, mentir sobre presencia o responder latidos mientras descarta mensajes. Las firmas impiden inventar un desbloqueo, pero no solucionan esa denegación/selectividad. La interfaz distingue estado verificado, estado antiguo y disponibilidad del relay. WSS protege cada conexión, pero **no es cifrado de extremo a extremo**: Render puede ver modo, duración y mensajes mínimos de control. No se transmiten claves privadas, códigos de recuperación, contenido de aplicaciones, ubicación, contactos ni historial.

### Reglas de seguridad para implementar

La pantalla de emergencia no depende del servidor y no debe quedar detrás de un PIN parental. No se ocultan la administración ni la notificación del servicio. No se añaden grabación, captura de pantalla, lectura de chats, seguimiento, accesibilidad ni permisos «por si acaso».

Los APK de prueba nunca se entregan al niño como versión final. No se añade borrado remoto automático. La retirada del dispositivo exige presencia física, autorización explícita y dos confirmaciones de pérdida de datos.

Los logs de pruebas del desarrollador no son un historial de uso del niño: se generan con datos ficticios o códigos técnicos, y nunca incluyen firmas completas, QR, claves, códigos, políticas completas ni identificadores innecesarios. En producción solo se usan códigos de error no identificables; los logs de infraestructura de Render pueden existir aunque la app no guarde historial.

### Puertas de salida

**G0:** emergencias y quiosco en el A13 real. **G1:** firma, vinculación y recuperación. **G2:** reinicio/red/reloj y ejecución local. **G3:** padre/relay/hijo integrados. **G4:** batería, actualización, proceso muerto y pruebas de evasión de interfaz.

Si una puerta falla, no se certifica el modo estricto para ese dispositivo. Se documenta el fallo y se corrige la implementación o se declara incompatibilidad; no se sustituye por una pantalla que aparenta bloquear.

---

<a id="parte-13"></a>

## Parte 13 · Fuentes y compatibilidad

Archivo modular: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`.

## Fuentes, compatibilidad y límites de la especificación

Versión documental: 1.0 · Fecha de consulta: 8 de octubre de 2026.
Estado: diseño para implementar; no existe certificación del teléfono ni una aplicación probada en este paquete.

### 1. Qué sabemos y qué no

El propietario identificó el teléfono del hijo como **A13**. Se toma como candidato un Samsung Galaxy A13, pero **no conocemos su código SM-…, versión de Android, One UI, operador ni parche de seguridad**. No se debe completar ese inventario inventando datos. Samsung publica documentación Android 14 para variantes SM-A135M y SM-A135F [S14]; eso no demuestra que el teléfono concreto la tenga instalada.

«Cualquier celular» se traduce en una arquitectura portable y una matriz de pruebas, no en una garantía imposible de verificar. Device Owner y Lock Task pertenecen a Android [S01, S02]. Este diseño no implementa control del hijo en iPhone. El panel web futuro podría administrar un hijo Android, pero no convierte un iPhone en un dispositivo Android administrado.

Decisión del proyecto, no mínimo universal de las APIs:

| Componente | Base elegida | Estado inicial |
|---|---|---|
| Padre | Android 11 / API 30 o superior | Por implementar y probar |
| Hijo | Android 12 / API 31 o superior | Por implementar y probar |
| Hijo A13 Android 12–14 | Primera familia a validar | No certificado todavía |
| Otros fabricantes y Android 15–17 | Candidatos, con pruebas específicas | No prometer compatibilidad |
| iPhone del hijo | Fuera de alcance | No soportado por este diseño |
| Navegador del padre | Evolución posterior | No implementado en v1 |

Se fija `compileSdk = 36`, `targetSdk = 36` como base deliberada del proyecto, no como afirmación de que sea la última API disponible. No se bajará el target para eludir restricciones. Los cambios de servicios en primer plano y comportamiento se revisan por versión [S05, S06, S24]. Las versiones concretas de Gradle, Kotlin, Android Gradle Plugin y dependencias se fijan al crear el proyecto desde Android Studio estable y se conservan en el repositorio.

### 2. Límites que no deben ocultarse

- El quiosco no reemplaza el cargador de arranque, la pantalla del PIN de la SIM ni el primer desbloqueo de Android.
- No se promete sobrevivir a un borrado por recovery, reinstalación del sistema, root, cargador de arranque desbloqueado ni fallos del fabricante. Restringir el restablecimiento desde Ajustes no elimina todas las vías físicas de restablecimiento [S03, S04].
- Una notificación o un WebSocket no son una garantía de ejecución continua. Hay que comprobar suspensión, reinicio del proceso y pantalla apagada [S05–S08].
- Emergencias accesibles no significa cobertura garantizada: la disponibilidad depende también del sistema de telefonía, radio, operador y estado del equipo. Nunca se exige internet al botón de emergencia [S04, S15].
- Las APIs oficiales de Device Owner son la base; no se usarán accesibilidad, superposiciones engañosas, ocultación de la app ni APIs privadas para simular control total.
- Sin servidor persistente de mensajes no se entrega una orden pendiente cuando el padre ya cerró su aplicación. El padre debe volver a abrirla, autenticarse y sincronizar.
- Solo se declara un dispositivo compatible después de aprobar las puertas de aceptación de este paquete. **Los campos de evidencia se entregan vacíos a propósito.**

### 3. Fuentes primarias

Los identificadores [Sxx] usados en el paquete apuntan a estas referencias. Los valores de intervalos, estados, límites y decisiones arquitectónicas son propuestas de este proyecto, no garantías de los proveedores.

| ID | Fuente oficial | Aplicación en este diseño |
|---|---|---|
| S01 | https://developer.android.com/work/dpc/dedicated-devices | Dispositivos dedicados y DPC |
| S02 | https://developer.android.com/work/dpc/dedicated-devices/lock-task-mode | Lock Task, lista de paquetes y diferencia con fijar pantalla |
| S03 | https://developer.android.com/reference/android/app/admin/DevicePolicyManager | APIs de administración, propietario, quiosco, borrado y retirada |
| S04 | https://developer.android.com/reference/android/os/UserManager | Restricciones del usuario; excepción de llamadas de emergencia |
| S05 | https://developer.android.com/develop/background-work/services/fgs/service-types | Tipo `systemExempted` y sus condiciones, entre ellas Device Owner |
| S06 | https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start | Excepciones y restricciones de inicio desde segundo plano |
| S07 | https://developer.android.com/privacy-and-security/direct-boot | Almacenamiento y componentes disponibles antes del desbloqueo |
| S08 | https://developer.android.com/training/monitoring-device-state/doze-standby | Suspensión, red y pruebas de Doze |
| S09 | https://developer.android.com/develop/connectivity/network-ops/reading-network-state | Callbacks y capacidades de red validadas |
| S10 | https://developer.android.com/privacy-and-security/keystore | Claves no exportables y autenticación del usuario |
| S11 | https://render.com/docs/websocket | WSS, conexiones, reinicios y reconexión |
| S12 | https://render.com/docs/free | Suspensión y límites de los servicios gratuitos |
| S13 | https://render.com/docs/web-services | Puerto de escucha y despliegue de un web service |
| S14 | https://www.samsung.com/latin/support/model/SM-A135MZKNTPA/ ; https://www.samsung.com/levant/support/model/SM-A135FZKGMEB/ | Identificación de variantes y manuales disponibles |
| S15 | https://developer.android.com/reference/android/telecom/TelecomManager | Telefonía del sistema |
| S16 | https://android.googlesource.com/platform/frameworks/base.git/+/master/telecomm/java/android/telecom/TelecomManager.java | `createLaunchEmergencyDialerIntent` está marcado `@SystemApi` / `@hide`: no usarlo como API pública |
| S17 | https://developer.android.com/work/dpc/dedicated-devices/cookbook | Launcher persistente y aprovisionamiento de desarrollo con ADB |
| S18 | https://source.android.com/docs/devices/admin/provision | Aprovisionamiento administrado |
| S19 | https://developer.android.com/work/versions/android-12 | Handlers de aprovisionamiento requeridos desde Android 12 |
| S20 | https://developer.android.com/reference/android/os/SystemClock | Reloj monotónico para duraciones |
| S21 | https://developer.android.com/reference/android/util/AtomicFile | Escritura de archivos con sustitución atómica |
| S22 | https://developer.android.com/reference/android/net/wifi/WifiManager | Configuración de Wi-Fi; restricciones y excepciones para administración |
| S23 | https://nodejs.org/en/about/previous-releases | Selección de Node.js 24 LTS |
| S24 | https://developer.android.com/about/versions/16/behavior-changes-16 | Base target API 36 |
| S25 | https://nodejs.org/api/crypto.html | Firmas ECDSA, SPKI y codificación DER; verificar contra Node 24 al implementar |
| S26 | https://github.com/websockets/ws | Biblioteca WebSocket del relay y latidos |

### 4. Evidencia que debe completar el desarrollador

No completar «APROBADO» por haber leído documentación o por pasar pruebas unitarias. Registrar: código exacto del equipo, Android/API, One UI, parche, versión y firma del APK, fecha, caso ejecutado, resultado observado y limitación. Las pruebas de un emulador no certifican llamadas ni comportamiento de batería del A13.

---

<a id="parte-14"></a>

## Parte 14 · Tareas comunes C00–C02

Archivo modular: `compartido/03_INICIO_TECNICO.md`.

## Inicio técnico común — tareas C00 a C02

### C00 — Crear el repositorio y fijar herramientas

**Entrada:** README, SPEC-P P2 y SPEC-H H2. **Salida:** repo con `android/core-protocol`, `app-parent`, `app-child`, `relay` y `docs`.

Crear proyecto en Android Studio estable. Incorporar módulos y applicationId del contrato. Registrar en `docs/BUILD_ENV.md` versiones reales de Android Studio, JDK, Gradle, AGP y Kotlin generadas por el IDE; no inventar combinaciones de versiones. Instalar SDK 36 y fijar minSdk de cada app. Añadir `.gitignore` para `.env`, keystores, APK, `local.properties`, secretos y salidas de build. En relay fijar Node 24 y lockfile cuando se añadan dependencias.

**Prueba:** compilan ambos APK vacíos y el test del núcleo; no declarar que existe bloqueo por ello. **Criterio de cierre:** comandos de build registrados y repetibles desde checkout limpio. No configurar Device Owner sobre el teléfono diario aún.

### C01 — Modelos, validación y serialización

**Entrada:** CP/1 secciones 3–8, `protocolo.schema.json`. **Archivos principales:** DTO, CanonicalJsonCodec, ProtocolValidator y sus tests.

Crear modelos sellados por `kind`/acción. Validar enteros seguros, seq como cadena de 64 bits, UUID/nonce, unknown fields y límites. La serialización es determinista del subconjunto ASCII definido, ordena objetos recursivamente y preserva arrays. Separar el contenedor externo y el payload. Nunca tratar un JSON recibido como una clase válida sin comprobar tipos/campos.

**Pruebas:** reordenación de propiedades, `null`, campos extra, valores fuera de rango, cero inicial en seq, claves duplicadas, bytes UTF-8 inválidos, base64url inválido, payload y frame demasiado grandes. Dado un mismo modelo, Android/JVM/Node producen idénticos bytes. **Cierre:** un único contrato implementado, no dos versiones parecidas.

### C02 — Firma, verificación y fixtures

**Entrada:** CP/1 sección 4, vectores de `pruebas/`. **Archivos:** interfaces Signer/Verifier, implementación JCA y pruebas cruzadas.

Verificar SPKI P-256, SHA256withECDSA y DER. Las privadas de los teléfonos van en Keystore; el núcleo recibe una interfaz, no rutas de claves privadas. No escribir una curva criptográfica propia. Usar bytes exactos y prefijo de propósito. Hash de idempotencia sobre payload, nunca sobre firma.

**Pruebas:** ejecutar `node pruebas/verificar_vectores.mjs`; luego verificar esos mismos fixtures desde tests JVM/Android. Un bit cambiado, otra clave, otro propósito, otra pareja o esquema inválido se rechazan por la capa pertinente. Una firma válida **no** significa que la política esté autorizada: faltan sesión, nonce, boot y secuencia.

**Cierre:** interoperabilidad documentada. La verificación de fixtures incluida en este paquete valida ejemplos, no sustituye pruebas del APK.

### Convenciones de trabajo

Una tarea por conversación de IA. No generar toda la app de una vez. Crear un commit después de prueba verificable y actualizar `ESTADO.md`. Si el cambio necesita más de cuatro archivos complejos, dividirlo manteniendo el mismo contrato. El límite de contexto de la IA no justifica borrar tests ni sustituir seguridad por un mock de producción.

---

<a id="parte-15"></a>

## Parte 15 · Trazabilidad de requisitos

Archivo modular: `TRAZABILIDAD.md`.

## Trazabilidad del objetivo a implementación y evidencia

| Requisito del propietario | Especificación principal | Decisión | Evidencia mínima |
|---|---|---|---|
| Control remoto desde su móvil | SPEC-P P3/P5; SPEC-R R3/R4; CP/1 §6–7 | ADR-P-003/004 | P-ACK-01/02/03, R-AUTH-01, H-ENF-02 |
| Bloquear tras reiniciar | SPEC-H H6/H8; CP/1 §2.2 | ADR-H-002/005 | H-BOOT-01/02/03 |
| Bloquear sin internet | SPEC-H H7; CP/1 §8 | ADR-H-002/003 | H-NET-01/02/03/04 |
| Duración de bloqueo elegida por el padre | SPEC-P P3/P5; SPEC-H H6 | ADR-H-005 | H-TIME-01/02/03, C-SEQ-01 |
| Restringir desinstalación y salidas | SPEC-H H4/H5 | ADR-H-001 | H-SEC-01/02/03 |
| Retirada solo autorizada | SPEC-H H10/H11; CP/1 §9/9.A | ADR-P-005 | H-REC-01/02/03/04, H-RET-01 |
| Emergencias disponibles | SPEC-H H9; INSTALACION_A13 | ADR-H-004 | H-EMG-01/02/03/04, puerta G0 |
| Datos y clave en móviles | SPEC-P P4; SPEC-H H8 | ADR-P-002, ADR-H-005 | P-KEY-01, P-STORE-01, H-STATE-01/02 |
| Render sin base de datos ni historial | SPEC-R R1/R2/R4 | ADR-P-003 | R-CONFIG-01, R-ROUTE-01, R-FAIL-01 |
| Mínimo consumo de infraestructura | SPEC-R R1/R5; SPEC-H H7 | ADR-P-003, ADR-H-003 | R-LIMIT-01/02, H-POWER-02 |
| Resistencia a órdenes falsas/repetidas | CP/1 §3–7 | ADR-P-002/004, ADR-H-005 | C-CRYPTO-01, C-SCHEMA-01, C-SEQ-01/02/03/04 |
| Primer objetivo A13; portabilidad | Fuentes/compatibilidad; FICHA_DISPOSITIVO | ADR-H-006 | G0–G4 en el modelo exacto, sin certificación extrapolada |
| Navegador en el futuro | SPEC-P P8 y contrato versionado | ADR-P-001 | Fuera de v1; requiere ADR de delegación de autoridad |
| Fácil de dividir para IA pequeña | Inicio técnico, AGENTS y planes | Organización documental | Una tarea, tests reales y HANDOFF por sesión |

Los sufijos compactos `H-EMG-01/02/...` se refieren a los IDs completos que aparecen en `pruebas/MATRIZ_ACEPTACION.md`. No son pruebas ejecutadas.

---

<a id="parte-16"></a>

## Parte 16 · Matriz de aceptación

Archivo modular: `pruebas/MATRIZ_ACEPTACION.md`.

## Matriz de aceptación — CP/1

Todos los casos del producto se entregan **PENDIENTES**. Completar evidencia con versión, entorno, resultado observado y archivo/commit. La verificación de fixtures adjunta es documental/criptográfica; no aprueba estos casos de la aplicación.

### Puertas

G0 = administración/quiosco y emergencia en A13. G1 = identidad, protocolo y recuperación. G2 = persistencia, tiempo, arranque y red. G3 = integración padre/relay/hijo. G4 = proceso muerto, energía, escapes y actualización en release. No omitir un caso crítico porque el emulador no lo soporte. Una llamada real de emergencia no se realiza sin coordinación formal.

### Contrato y seguridad criptográfica

| ID | Caso y preparación | Resultado esperado | Entorno | Estado |
|---|---|---|---|---|
| C-CRYPTO-01 | **Firma válida y verificación cruzada.** Firmar/verificar fixtures P-256 DER en JVM, Android y Node. | Se aceptan bytes originales y se rechazan cambios/clave/propósito incorrectos. | Automática | PENDIENTE |
| C-SCHEMA-01 | **Parser estricto.** Probar campos extra, UTF-8/base64 inválidos, duplicados, números y tamaños fuera de límites. | Todos se rechazan antes de aplicar políticas. | Automática | PENDIENTE |
| C-PAIR-01 | **Vinculación física.** QR vencido, clave del hijo diferente, otra pareja, cancelación y segunda vinculación sin permiso. | No cambian claves; solo oferta física vigente y confirmada crea vínculo. | Automática + A13 | PENDIENTE |
| C-SEQ-01 | **Repetición exacta.** Reenviar último comando en la misma sesión tras perder ACK. | Devuelve resultado, conserva inicio y duración; no vuelve a liberar. | Automática | PENDIENTE |
| C-SEQ-02 | **Secuencia conflictiva.** Mismo seq con otro payload/id; después un seq menor. | SEQUENCE_CONFLICT o STALE_SEQUENCE, sin cambiar estado aceptado. | Automática | PENDIENTE |
| C-SEQ-03 | **Orden concurrente.** Procesar seq7 y seq8 con demoras de enforcement. | No termina aplicando seq7 después de seq8. Un solo escritor. | Automática | PENDIENTE |
| C-SEQ-04 | **Sesión/arranque/desafío viejos.** Entregar ALLOW de sesión anterior, otro boot o nonce vencido. | Rechazo; pedir estado actual y nueva confirmación al padre. | Automática + integración | PENDIENTE |

### Padre

| ID | Caso y preparación | Resultado esperado | Entorno | Estado |
|---|---|---|---|---|
| P-KEY-01 | **Autenticación y pérdida de clave.** Cancelar credencial, expirar ventana, invalidar clave de prueba. | No firma ni crea una autoridad de reemplazo silenciosa. | Android padre | PENDIENTE |
| P-PAIR-01 | **Exportación y datos mínimos.** Inspeccionar QR/archivo público/env y logs de pruebas. | No contienen privadas ni códigos en claro; solo públicas/hashes/configuración. | Automática + manual | PENDIENTE |
| P-STORE-01 | **Persistencia ante cierre.** Fallar después de reservar seq y antes de enviar. | No reutiliza secuencia para otra intención; archivo sigue consistente. | Automática | PENDIENTE |
| P-ACK-01 | **ACK impostor.** Enviar ACK falso/otro hijo/otro hash o solo PEER_STATUS. | No muestra aplicado. | Automática | PENDIENTE |
| P-ACK-02 | **ACK perdido.** Aceptar en hijo y descartar ACK; sincronizar nuevamente. | Reconciliar id/seq/hash sin reiniciar temporizador. | Integración | PENDIENTE |
| P-ACK-03 | **Enforcement pendiente/fallido.** Hijo devuelve pending o failed; ALLOW aceptado pero red no sana. | UI muestra causa/pendiente/error, no permiso efectivo falso. | Automática + UI | PENDIENTE |
| P-NET-01 | **Padre cerrado.** Preparar intención sin hijo; cerrar y abrir después. | No entrega en background; pide autenticación/sincronización/confirmación. | Android padre | PENDIENTE |
| P-UI-01 | **Acciones y duraciones.** Probar cuatro acciones, 59,60,604800,604801s y doble pulsación. | Límites correctos, una orden en curso, explicación de efectos. | UI automática | PENDIENTE |
| P-UI-02 | **Estado antiguo.** Cortar conexión después de STATE válido. | Muestra último estado y antigüedad, no estado actual inventado. | UI automática | PENDIENTE |

### Relay Render

| ID | Caso y preparación | Resultado esperado | Entorno | Estado |
|---|---|---|---|---|
| R-CONFIG-01 | **Config inválida.** Quitar claves/env, usar curva incorrecta o claves iguales. | Falla cerrado; health operativo solo con configuración válida. | Automática | PENDIENTE |
| R-AUTH-01 | **Suplantación de rol.** Autenticar como padre con privada del hijo o pública propuesta por atacante. | Rechaza; no incorpora socket al mapa. | Automática | PENDIENTE |
| R-AUTH-02 | **Reto viejo o reutilizado.** Reutilizar AUTH en otro socket o después de 10s. | Rechaza y consume/limpia correctamente. | Automática | PENDIENTE |
| R-AUTH-03 | **Expulsión de cliente.** Abrir impostor y luego segundo socket autenticado. | Impostor no expulsa; el nuevo auténtico reemplaza limpiamente. | Automática | PENDIENTE |
| R-ROUTE-01 | **Receptor ausente.** Enviar SET_POLICY cuando no existe hijo registrado. | PEER_UNAVAILABLE; no se guarda ni entrega más tarde. | Automática | PENDIENTE |
| R-ROUTE-02 | **Rol y bytes.** Intentar SET_POLICY desde hijo; reenviar uno permitido. | Rechaza rol incorrecto; bytes de contenedor válido no se alteran. | Automática | PENDIENTE |
| R-FAIL-01 | **Reinicio/despliegue.** Reiniciar servicio con móviles conectados. | Sockets reconectan, políticas locales se conservan, sin colas recuperadas. | Render + móviles | PENDIENTE |
| R-LIMIT-01 | **Abuso de frames.** Binarios, >16KiB, compresión, campos desconocidos, inundación. | Cierre/rate limit, sin caída del proceso ni memoria sin límite. | Automática | PENDIENTE |
| R-LIMIT-02 | **Socket lento y memoria.** Receptor deja de leer; muchas reconexiones. | Backpressure y limpieza; mapa no borra el socket nuevo por cierre del viejo. | Automática | PENDIENTE |

### Hijo: administración y emergencia

| ID | Caso y preparación | Resultado esperado | Entorno | Estado |
|---|---|---|---|---|
| H-OWNER-01 | **Sin privilegio de propietario.** Instalar como app normal y comparar con DPC aprovisionado. | Primera situación aparece NO CONFIGURADO; segunda verifica Device Owner. | A13 | PENDIENTE |
| H-ENF-01 | **Quiosco real.** Pulsar Home, Recientes, Atrás y gestos desde bloqueado. | Sigue bajo launcher propio y LOCK_TASK_MODE_LOCKED, no PINNED. | A13 | PENDIENTE |
| H-ENF-02 | **Cambio permitido/bloqueado.** Abrir una app aprobada y recibir LOCK. | Deja de ser utilizable; tiempo local recepción-aplicación medido, objetivo ≤1s activo. | A13 | PENDIENTE |
| H-ENF-03 | **Política no aplicada.** Inyectar error de API/resultado y recuperar. | No ACK APPLIED hasta evidencia; estado seguro y error visible. | Automática + A13 | PENDIENTE |
| H-SEC-01 | **Elevación de rol.** Buscar cambio a padre en UI, intents o actividad exportada. | No existe vía operativa para elevar rol. | A13 + revisión | PENDIENTE |
| H-SEC-02 | **Controles normales sobre app.** Intentar desinstalar, detener, borrar datos, modo seguro y reset desde Ajustes. | Restricciones impiden la vía normal; no se afirma protección contra recovery. | A13 release | PENDIENTE |
| H-SEC-03 | **Salidas laterales.** Probar notificaciones, PiP, compartir, enlaces, instalador, otro launcher y usuarios. | No abren apps/componentes no autorizados ni ajustes libres. | A13 release | PENDIENTE |
| H-EMG-01 | **Keyguard y arranque.** Arranque antes del PIN Android, con/sin PIN SIM. Abrir marcador sin llamar. | Ruta de emergencia reconocible sin clave parental; documentar cada variante. | A13 físico | PENDIENTE |
| H-EMG-02 | **Sin internet.** Desconectar datos/Wi-Fi con radio telefónica disponible; abrir marcador sin llamar. | Emergencia no depende del relay ni de credenciales parentales. | A13 físico | PENDIENTE |
| H-EMG-03 | **Quiosco estricto y regreso.** Abrir marcador desde bloqueo endurecido; salir sin llamar. | Acceso funciona y vuelve a estado restringido sin escape a apps. | A13 físico | PENDIENTE |
| H-EMG-04 | **Llamada en curso y cambios.** Simulación autorizada de llamada de emergencia y devolución; cambiar política/red. | No se finaliza llamada ni ocultan controles críticos. No llamadas reales no coordinadas. | Entorno telefónico de pruebas autorizado | PENDIENTE |

### Hijo: estado, red y ejecución

| ID | Caso y preparación | Resultado esperado | Entorno | Estado |
|---|---|---|---|---|
| H-BOOT-01 | **Reinicio desde permitido.** ALLOW, reiniciar y esperar que vuelva internet. | Permanece bloqueado hasta orden fresca para nuevo bootId. | A13 | PENDIENTE |
| H-BOOT-02 | **Dos broadcasts de un arranque.** Procesar LOCKED_BOOT_COMPLETED y BOOT_COMPLETED del mismo boot. | Un solo bootId, sin reset de seq ni acceso prematuro. | Automática + A13 | PENDIENTE |
| H-BOOT-03 | **Direct Boot.** Reiniciar sin introducir credencial; observar servicio/pantalla. | No lee Credential Encrypted ni exporta privadas; bloqueo y emergencia se conservan. | A13 | PENDIENTE |
| H-TIME-01 | **Cuatro políticas.** Recorrer LOCK, LOCK_FOR, ALLOW y ALLOW_FOR con reloj inyectado. | Prioridades/expiraciones exactas del contrato; política nueva sustituye anterior. | Automática | PENDIENTE |
| H-TIME-02 | **Cambio de hora y zona.** Cambiar reloj ±24h en laboratorio y repetir en release restringido. | No adelanta autorización ni evita vencimiento; pruebas no dependen de fecha. | Automática + A13 | PENDIENTE |
| H-TIME-03 | **Expiración sin red.** Vence LOCK_FOR con canal vencido; luego volver a conectar. | Sigue bloqueado offline; reevalúa al recuperar si no hay otro bloqueo. | Automática + A13 | PENDIENTE |
| H-NET-01 | **Red perdida.** Quitar Wi-Fi/datos desde un escenario autorizado de laboratorio. | Bloqueo local tras 3s, sin orden remota. Medir tiempo real. | A13 | PENDIENTE |
| H-NET-02 | **Handover de red.** Cambiar Wi-Fi/datos con un corte inferior al margen y después superior. | Evita falso positivo breve y bloquea si supera margen; un socket/callback. | A13 | PENDIENTE |
| H-NET-03 | **Solo falla Render.** Mantener internet pero cortar acceso al relay o sus PONG. | Bloqueo al superar 45s desde último latido válido. | Integración + A13 | PENDIENTE |
| H-NET-04 | **Portal, DNS, TLS y latido falso.** Wi-Fi sin salida, portal, TLS inválido, PONG de nonce viejo. | Ninguno autoriza canal; no deshabilita validación TLS. | Automática + A13 | PENDIENTE |
| H-STATE-01 | **Crash entre aceptar/aplicar.** Matar proceso en cada fase del último comando. | Estado se reconcilia; no duplica duración ni concede permiso por error. | Automática + Android | PENDIENTE |
| H-STATE-02 | **Archivo corrupto/truncado.** Corromper copia de laboratorio antes de cargar estado. | ERROR_SEGURO; no reabre enrolamiento ni pone ALLOWED. | Automática + Android | PENDIENTE |
| H-KEY-01 | **Keystore indisponible.** Simular error y probar antes de primer desbloqueo. | No autentica ni inventa ACK; conserva bloqueo y emergencia. | Android | PENDIENTE |
| H-PROC-01 | **Proceso muerto con otra app abierta.** Mientras se usa app aprobada, matar/simular muerte del controlador en laboratorio. | No hay ventana de uso que viole el requisito; si existe, falla la certificación hasta corregir. | A13 físico | PENDIENTE |
| H-POWER-01 | **Doze y despertar.** Doze forzado y reposo natural; expirar tiempo/red mientras duerme. | Al despertar no se habilita interacción fuera de política; observar otras apps, no solo launcher. | A13 físico | PENDIENTE |
| H-POWER-02 | **Consumo en reposo.** Comparar dos periodos equivalentes de 24h, con misma señal/apps/carga inicial. | Registrar energía, CPU, bytes, reconexiones; objetivo inicial delta ≤5 puntos de batería, ajustar con evidencia sin debilitar bloqueo. | A13 físico | PENDIENTE |

### Hijo: recuperación, actualización y retirada

| ID | Caso y preparación | Resultado esperado | Entorno | Estado |
|---|---|---|---|---|
| H-REC-01 | **Recuperación offline.** Sin red, usar código/token MAINTENANCE. | Solo red dentro de UI propia por 5min; no habilita apps normales. | A13 | PENDIENTE |
| H-REC-02 | **Replay de recuperación.** Repetir token, usarlo para otro propósito o reiniciar. | Rechaza; códigos consumidos no vuelven y tokens efímeros expiran. | Automática + A13 | PENDIENTE |
| H-REC-03 | **Adivinación y reinicio.** Fallar códigos 5/10 veces y reiniciar durante espera. | Mantiene/reimpone espera; emergencia sigue accesible. | Automática + A13 | PENDIENTE |
| H-REC-04 | **Padre perdido.** Usar RECOVER_PARENT, revincular y actualizar env pública. | Clave vieja ya no autoriza; nueva funciona después de configuración, siempre con bloqueo durante transición. | A13 + Render | PENDIENTE |
| H-UPDATE-01 | **Actualización firmada.** Actualizar APK propio durante mantenimiento. | Conserva vínculo, claves, seq y temporizador; no requiere ADB permanente. | A13 release | PENDIENTE |
| H-UPDATE-02 | **Firma/migración incorrecta.** APK de otra firma o migración incompatible de estado. | No instala/acepta como legítimo; no pierde restricciones ni permite por defecto. | Automática + A13 | PENDIENTE |
| H-RET-01 | **Retirada local.** Código/token RETIRE, cancelar una vez; luego doble confirmación en equipo de prueba. | Cancelar no borra. Confirmar usa API correcta; jamás se ejecuta desde SET_POLICY remoto. | Destructiva, solo equipo autorizado | PENDIENTE |

---

<a id="parte-17"></a>

## Parte 17 · Ficha del dispositivo

Archivo modular: `pruebas/FICHA_DISPOSITIVO.md`.

## Ficha de validación física

Equipo declarado por el propietario: A13.
Fabricante / código SM completo: NO COMPROBADO
Versión Android / API / One UI / parche: NO COMPROBADO
Operador / SIM / PIN de SIM: REGISTRAR LOCALMENTE SIN NÚMEROS PERSONALES
APK / versionCode / huella del certificado de firma: NO CONSTRUIDO
Estado de Device Owner / bloqueo de arranque: NO COMPROBADO
Fecha / responsable autorizado: POR COMPLETAR

### Puertas

G0: PENDIENTE
G1: PENDIENTE
G2: PENDIENTE
G3: PENDIENTE
G4: PENDIENTE

### Evidencias

| Caso | Procedimiento realmente ejecutado | Resultado observado | Evidencia y entorno |
|---|---|---|---|
| POR COMPLETAR | | | |

### Emergencia

Ruta exacta desde pantalla bloqueada y desde boot:
Prueba de abrir/salir del marcador sin llamada:
Prueba de llamada completa en entorno autorizado, si existe:
Aspectos NO verificados:

### Ejecución y consumo

Tiempo recepción → enforcement con pantalla activa:
Tiempo sin red → bloqueo:
Tiempo sin PONG → bloqueo:
Comportamiento con otra app abierta y proceso controlador muerto:
Comportamiento tras despertar:
Comparación de batería y condiciones medidas:

### Veredicto

NO CERTIFICADO. Cambiarlo solo con evidencia de las puertas aplicables y autorización del responsable. Un modelo/firmware distinto requiere una nueva ficha.

---

<a id="parte-18"></a>

## Parte 18 · Plantilla de estado

Archivo modular: `plantillas/ESTADO.md`.

## Estado de implementación

Última actualización: POR COMPLETAR
Commit actual: POR COMPLETAR
Tarea actual: C00
Estado: PENDIENTE

### Entorno observado

Android Studio/JDK/Gradle/AGP/Kotlin: NO REGISTRADO
Node / sistema operativo: NO REGISTRADO
SM / Android / One UI / parche del hijo: NO COMPROBADO

### Terminado y demostrado

Ninguna tarea del producto. La documentación y fixtures no son una implementación.

### Archivos modificados en la última tarea

POR COMPLETAR

### Pruebas ejecutadas y resultado

POR COMPLETAR con comando, entorno y resultado real. No incluir pruebas imaginadas.

### Decisiones locales sin cambiar ADR

POR COMPLETAR

### Bloqueos y riesgos abiertos

G0–G4 pendientes. No hay validación física del A13.

### Próxima tarea y lectura mínima

C00 — compartido/03_INICIO_TECNICO.md

### Seguridad

No almacenar claves, códigos, QR sensibles, contraseñas ni datos del menor aquí.

---

<a id="parte-19"></a>

## Parte 19 · Plantilla de traspaso de contexto

Archivo modular: `plantillas/HANDOFF.md`.

## Relevo entre conversaciones o desarrolladores

Tarea terminada:
Commit:
Resultado realmente comprobado:
Archivos creados/modificados:
Interfaces públicas añadidas:
Pruebas y comandos ejecutados:
Pruebas que NO se ejecutaron:
Fallo conocido y forma de reproducir:
Decisión pendiente que requiere ADR:
Próxima tarea concreta:
Secciones mínimas que debe leer la siguiente IA:

No repetir toda la conversación. Máximo orientativo: 300–500 palabras. No incluir secretos ni marcar una API como verificada si solo se leyó su nombre.

---

<a id="parte-20"></a>

## Parte 20 · Resultados de validación del paquete

Archivo modular: `pruebas/VALIDACION_DEL_PAQUETE.md`.

## Validación documental realizada

Fecha: 8 de octubre de 2026. Alcance: archivos de documentación, esquema y fixtures de este paquete.

### Comprobaciones ejecutadas

- JSON Schema 2020-12 estructuralmente válido.
- 28 archivos JSON de ejemplos aceptados por el esquema; 13 contraejemplos estructurales rechazados.
- 10 vectores ECDSA P-256/SHA-256/DER: 78 comprobaciones de firmas, bytes, propósito, clave incorrecta, modificación y variantes de JSON no canónicas.
- JSON del paquete legible; bloques de código Markdown cerrados; 59 identificadores de pruebas de aceptación sin duplicados.
- No se incluyeron claves privadas en archivos. Los fixtures contienen claves públicas y firmas ficticias, no material de una familia real.

Entorno efectivamente usado: **Node v22.16.0**, Python 3.13.5, jsonschema 4.26.0. El relay del proyecto fija Node 24 LTS: **este entorno no ejecutó Node 24**. Repetir el verificador y todos los tests del relay en ese runtime al implementar; no confundir una prueba en Node 22 con certificación en Node 24.

Salida del verificador:

```text
OK: 10 vectores firmados; 78 comprobaciones documentales. Runtime: v22.16.0.
Esto no prueba Device Owner, Android Keystore, temporizadores, red real, emergencias ni el A13.
OK: esquema válido; 28 ejemplos aceptados y 13 contraejemplos rechazados.
Es validación estructural: no comprueba claves, firmas, nonce fresco ni permisos reales.
```

### Repetir estas comprobaciones

Desde la raíz del paquete:

```powershell
node pruebas/verificar_vectores.mjs
# Opcional: verificación del esquema en Python.
python -m pip install jsonschema
python pruebas/verificar_esquema.py
```

El primer comando no tiene dependencias adicionales a Node. El segundo flujo requiere Python y jsonschema. Los scripts no tocan un teléfono ni hacen conexiones de red; únicamente la instalación de la dependencia requiere acceso a su repositorio.

### No probado y no entregado

No se compiló una app Android ni un relay real; no hay APK. No se ejecutaron Device Owner, Lock Task, Android Keystore, arranque, restricciones, batería, latencia, mantenimiento, actualización ni retirada en el A13. Tampoco se hizo una llamada de emergencia ni una simulación telefónica. No se desplegó ningún servicio en Render.

Todos los **59 casos de aceptación del producto siguen PENDIENTES**. Los resultados anteriores no cambian su estado. La revisión de documentación reduce ambigüedades, pero no sustituye implementar, probar, revisar seguridad y validar físicamente el dispositivo.
