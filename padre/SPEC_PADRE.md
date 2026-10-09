# SPEC-P — Aplicación del padre

**Objetivo:** desde el teléfono del adulto, vincular un hijo Android administrado, bloquearlo o permitir su uso, establecer duraciones y comprobar el resultado real, sin base de datos central ni historial.

Estado: listo para implementar por tareas; no es una aplicación construida. Protocolo obligatorio: `compartido/00_CONTRATO_V1.md`. Modelo de amenazas y fuentes: los otros dos archivos de `compartido/`.

## P1. Resultado mínimo demostrable

Con padre e hijo activos y vinculados, pulsar «Bloquear» provoca una orden firmada y el padre ve «Bloqueado · confirmado por el dispositivo» únicamente después de un ACK verificable de aplicación. Si el hijo no está conectado, se ve «Pendiente local» o «Sin confirmar», nunca un éxito inventado. El padre también configura «Bloquear durante…», «Permitir» y «Permitir durante…».

La app sirve para controlar, no para leer información privada del niño. No solicita contactos, ubicación, acceso a SMS, contenido de otras apps ni accesibilidad. No configura una cuenta en la nube. Una app por rol evita que el hijo pueda habilitar privilegios de padre cambiando una opción.

## P2. Tecnología y estructura

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

## P3. Pantallas y requisitos verificables

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

### P3.1 Pantalla inicial

Texto claro: «Este teléfono será el administrador. El teléfono del hijo requiere configuración como dispositivo administrado». Botón «Configurar este teléfono como padre». Este texto no permite cambiar de rol en el APK del hijo.

Comprobar `KeyguardManager.isDeviceSecure`; si no hay credencial del sistema, explicar cómo configurarla y no crear autoridad operativa. El PIN de Android protege al padre; no inventar una contraseña propia con recuperación insegura.

### P3.2 Pantalla principal

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

### P3.3 Sin conexión y cambios pendientes

Permitir preparar una intención y verla como pendiente local, sin afirmar envío. Si ya hay una intención, pedir reemplazarla o cancelarla. Guardar únicamente una. Cancelar no revoca una orden que el hijo ya recibió: antes de informar cancelación efectiva se sincroniza. Una orden aceptada se cambia enviando otra, no borrando un elemento visual.

Tras reabrir la app, autenticar, sincronizar y mostrar «Hay una intención pendiente. ¿Enviar ahora a este dispositivo?». Para `ALLOW`/`ALLOW_FOR` esta confirmación es obligatoria. Una orden cuyo ACK se perdió se consulta antes de crear una nueva para evitar reiniciar su duración.

## P4. Identidad, almacenamiento y permisos

Clave de firma: EC P-256, alias `cp.parent.sign.v1`; privada no exportable en Android Keystore. `setUserAuthenticationRequired(true)` y una ventana de 60 segundos con `AUTH_BIOMETRIC_STRONG | AUTH_DEVICE_CREDENTIAL` en la base API 30. Pedir autenticación nuevamente si la clave no puede utilizarse. La ventana es una decisión del proyecto; bloquear también la interfaz al ir a segundo plano. No convertir fallos de autenticación en firma con una clave alternativa sin protección [S10].

No exigir StrongBox: puede no existir en todos los modelos. Registrar capacidades localmente durante diagnóstico. No prometer hardware específico sin comprobarlo.

Metadatos privados: archivo atómico cifrado AES-256-GCM con otro alias de Keystore y nonce nuevo generado por el proveedor en cada escritura. Guardar `schemaVersion`, alias, pairId, ambas claves públicas, relayOrigin, contador local, estado de vinculación e intención pendiente. No guardar la clave privada dentro del JSON. Las operaciones de contador/intención deben ser transaccionales; una sola coroutine escritora.

Los códigos de recuperación se muestran al generarse para registro externo por el adulto; después no se guardan en texto claro ni se reconstruyen desde los hashes. Antes de terminar la vinculación se pide confirmar que se conservaron fuera del celular. Perder el celular y todos los códigos puede obligar a restablecer y reprovisionar; no hay un soporte central que recupere claves.

Permisos: `INTERNET`, `ACCESS_NETWORK_STATE`, `USE_BIOMETRIC` y `CAMERA` solo para QR mientras la pantalla está abierta. QR mediante CameraX + decodificador local; no usar una API remota para leerlo. No servicio en primer plano en el padre. No permisos de administrador ni Device Owner. Sin backups automáticos; revisar también reglas de transferencia de dispositivo, no solo `allowBackup`.

## P5. Algoritmo de una orden

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

## P6. Manejo de errores

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

## P7. Pruebas mínimas del módulo

Las pruebas de UI usan un `FakeChild` local y claves ficticias. Deben cubrir las cuatro acciones, las duraciones límite, la denegación biométrica, muerte de proceso entre reserva y envío, ACK perdido, duplicado, fuera de orden, sesión vieja, firma falsa y contador remoto superior.

Los tests de persistencia simulan fallo antes y después de escribir: nunca quedan contador de una orden y payload de otra. Los tests criptográficos usan los fixtures comunes y verifican interoperabilidad Android/JVM/Node, no solo firmar y verificar con la misma función.

Se considera terminada la app padre cuando P-F01…P-F12 y todos los casos P-* de la matriz tienen evidencia. No se considera terminada por mostrar una maqueta de botones.

## P8. Fuera de alcance

No historial, multi-hijo, multi-padre, recuperación de cuentas, llamadas remotas, streaming, localización, webs públicas, notificaciones push ni backend con usuarios. La selección de aplicaciones permitidas se realiza presencialmente en la app del hijo durante preparación; cambiarla remotamente requiere una futura versión explícita del contrato. El control desde navegador necesitará delegación segura y un ADR posterior.
