# Evidencia P04 — cliente WSS local parcial

## Alcance y límites

Solo app padre. No cambios en parental_render, CP/1, esquema ni fixtures.
No ADB, instalaciones ni operaciones en A56/A13 en este turno. No servidor
externo, APK hijo ni despliegue. Cierre local parcial, no aceptación integrada.

## Implementación

- RelayConnection: origen HTTPS válido → WSS /ws; cp.v1 obligatorio; reto
  estructuralmente CP/1 válido, único y dentro de plazo monotónico. Firma AUTH
  exacta con autoridad vigente; verificación local contra pública guardada.
  AUTH_OK correlacionado con connectionId; socket abierto no significa autenticado.
- TransporteOkHttp: TLS moderno, confianza y hostname del sistema, sin
  redirecciones, ws://, credenciales ni bypass. Rechaza frames binarios y mensajes
  por encima de MAX_FRAME tras recepción. OkHttp reensambla frames antes del callback:
  ese límite no es una garantía de memoria previa a la recepción.
- Latidos: nonce aleatorio de 32 bytes, PONG único del desafío vigente; cada
  10 s tras respuesta, timeout 15 s. Backoff 1–30 s más jitter 0–250 ms;
  se reinicia solo con PONG. TLS/protocolo inválidos detienen reintento automático.
- Control explícito con PREPARADO/VINCULADO persistido; PREPARADO sigue sin
  órdenes. Errores de almacén/clave no crean ni sustituyen identidad/vínculo.
- onStop cierra socket/bloquea autoridad, onDestroy libera cliente. Salir de
  Controles desconecta. Al volver se exige autoridad vigente antes de abrir;
  el adulto usa el prompt y vuelve a pulsar Conectar si se requiere autenticación.
- RedAndroid observa INTERNET+VALIDATED de la red predeterminada, registra y
  retira callback con lifecycle; descarta callbacks de observador retirado.
  Red, AUTH_OK, PONG y presencia se presentan por separado. No hay STATE/ACK
  verificados ni órdenes en esta tarea: corresponden a P05.

## Pruebas ejecutadas

`scripts/verificar.ps1`: correcto; Node oráculo 5 pruebas y verificador documental
10 vectores/78 comprobaciones correctas.

Gradle con `--rerun-tasks`: `:core-protocol:test`, `:core-protocol:testInterop`,
`:app-parent:testDebugUnitTest`, `:app-parent:assembleDebug`,
`:app-parent:assembleDebugAndroidTest`, `:app-parent:assembleRelease`,
`:app-parent:lintDebug`. Correcto, 130 tareas ejecutadas. XML: 61 app, 30 núcleo,
1 interop; cero fallos/errores/omisiones. Total con oráculo Node: 97 locales.
Lint: 0 errores/23 avisos. APK release sin firma de distribución; no entrega final.

Diez RelayConnectionTest: autoridad/foreground, no duplicación, firma AUTH exacta,
AUTH_OK/IDs/subprotocolo/reto duplicado, caducidad durante firma, PONG correlacionado,
backoff/callbacks viejos, fondo, origen seguro, fallo síncrono sin socket huérfano
y fondo durante firma sin envío.

Seis TransporteOkHttpTest usan TLS/WebSocket real en loopback JVM: texto y cp.v1,
certificado no confiable, hostname ajeno aunque certificado confiable, origen/ruta
insegura, frame binario y redirección real no seguida. Solo certificados efímeros
en memoria y cliente de confianza inyectado exclusivamente en tests.

## Pendientes y relevo

No acredita P-NET-01 físico (fondo/retorno/cambio de red/portal cautivo), ni
R-AUTH-01 contra servidor R02. La firma JVM efímera no prueba AUTH protegido por
Keystore/prompt real. UI y NetworkCallback Android compilados, no ejecutados.
Para esas pruebas avisar y esperar permiso de A56, con selector explícito; A13
excluido. No crear vínculo cotidiano ficticio ni cambiar credenciales.

Próxima tarea local P05: CP/1 §7 y SPEC-P P5. Mantener evidencia/intención y
órdenes deshabilitadas hasta implementar sus guardas. El propietario coordinará
la IA del servidor cuando el padre esté preparado; comunicación local antes
del despliegue. No lanzar agentes ni modificar el repositorio externo.
