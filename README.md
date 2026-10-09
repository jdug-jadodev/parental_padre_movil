# Control parental — aplicación del padre y relay

Proyecto Android nativo en Kotlin y servicio Node/TypeScript. Este repositorio
solo implementa el rol del padre y el relay. El APK del hijo pertenece a otro
proyecto. La copia del protocolo CP/1 en `compartido/` permanece congelada.

## Qué hay implementado (C00–C02, P00 y P01 local)

- `app-parent/`: navegación en español e identidad/autenticación Keystore implementadas, pendientes de validación física; sin vinculación real ni envío de órdenes.
- `core-protocol/`: modelos, parser estricto, esquema congelado y serialización determinista CP/1, sin dependencias de Android.
- `relay/`: servidor **local** ligado a `127.0.0.1`, sin WebSocket operativo.
- `relay/test/soporte/`: cliente hijo simulado limitado a loopback y lectura de un fixture congelado; no prueba políticas reales.
- `scripts/verificar.ps1`: compilación y pruebas locales, sin acceso a dispositivos.

C01 añade los mismos modelos y validaciones en `relay/src/protocol.ts`, mediante
Ajv 2020-12. Ambos lados conservan los bytes de los diez vectores comunes.
Se controlan UTF-8, ASCII, duplicados, límites, secuencias, base64url, formatos
SPKI/DER, propósito y coherencia de los campos del estado.

C02 añade firma SHA-256/ECDSA DER en Kotlin mediante un firmante inyectado y
verificación estándar en JVM/Node sobre los bytes originales. La privada del
padre y su autenticación usan en P01 Android Keystore. El relay
no firma ni recibe privadas.

`leerPayloadSinVerificar` sigue siendo solo estructural. Para verificar firmas
se usa `CriptografiaProtocolo.verificarSobre` con propósito esperado y pública
de configuración/vínculo confiable, nunca una pública elegida por el comando.
`DocumentoVerificado` no acredita frescura, autorización contextual ni `APPLIED`:
esas comprobaciones corresponden a los casos de uso futuros. Ninguno de estos
datos se usa todavía para confirmar ni aplicar órdenes.

P00 separa conexión al relay, presencia indicativa del hijo, última evidencia e
intención pendiente. Las cuatro acciones están visibles y deshabilitadas. La
recuperación informativa es accesible desde todas las pantallas, sin conexión ni
vínculo. Explorar el recorrido no crea autoridad. El repositorio de producción es
no operativo y muestra datos desconocidos; el falso se compila solo en tests JVM
e instrumentados. P05 deberá alimentar evidencia autenticada/correlacionada y
calcular su antigüedad/frescura; P00 no implementa ese cálculo ni un reloj remoto.

P01 añade creación/carga explícita de EC P-256 no exportable, autenticación del
sistema (credencial o biometría fuerte), ventana de 60 segundos y bloqueo de
interfaz al ir a segundo plano. Un error no sustituye claves ni habilita firma
alternativa. Se muestra exclusivamente pública SPKI, sin copiarla al portapapeles.
El alias de cifrado de metadatos está reservado por separado; su almacenamiento
cifrado/transaccional todavía no se implementó. El botón del recorrido no crea
identidad: hay un botón explícito separado. Las órdenes siguen deshabilitadas.
La persistencia tras reiniciar proceso y la autenticación real están pendientes.

El relay devuelve **503** en `/healthz` porque todavía no está listo para CP/1.
Rechaza upgrades y ejecución en `NODE_ENV=production`. No es desplegable en
Render hasta implementar R01–R03. No hay modo público de prueba sin autenticación.

## Compilar y probar en Windows

Instalar JDK 21, SDK Android 36 y Node 24. Abrir la raíz del proyecto con Android
Studio o ejecutar desde PowerShell:

```powershell
.\scripts\verificar.ps1
```

Para un módulo: `-Modulo nucleo`, `-Modulo app` o `-Modulo relay`.
El script detiene la verificación si falla cualquier comando.
La verificación completa también ejecuta `:core-protocol:testInterop`: firmas
nuevas cruzadas en ambos sentidos JVM/Node para los diez vectores. Requiere Node
y las dependencias instaladas en relay; el script las prepara antes. El test
JVM puro no exige Node y no incluye ese caso de interoperabilidad.

Comandos Android individuales:

```powershell
.\gradlew.bat :core-protocol:test
.\gradlew.bat :app-parent:testDebugUnitTest :app-parent:assembleDebug
.\gradlew.bat :app-parent:assembleDebugAndroidTest :app-parent:lintDebug
```

El APK debug se genera en `app-parent/build/outputs/apk/debug/`. Se excluye de
Git, igual que claves, archivos `.env`, resultados y configuración local.

## Iniciar la base del relay

Desde la carpeta `relay/`, ejecutar cada comando por separado:

```powershell
npm ci
npm run check
npm test
npm run build
npm start
```

Consultar `http://127.0.0.1:10000/healthz`. Detener con Ctrl+C.
`npm run dev` permite desarrollo con recarga. `PORT` admite valores 1–65535.

## Pruebas físicas

Se avisará antes de necesitar ADB. Cuando el propietario habilite el **teléfono
del padre**, se podrá instalar el APK debug y ejecutar
`:app-parent:connectedDebugAndroidTest` con selección de dispositivo verificada.
El primer lote ya se ejecutó en el A56: 12 aprobados y 1 interactivo omitido.
El A13 queda excluido; con ambos conectados se usó runner ADB dirigido
explícitamente al A56, no Gradle contra todos los teléfonos.
Además de la pantalla, el APK instrumentado verifica el parser y el acceso al
esquema dentro de Android. Estas pruebas aprobaron en el A56.
Incluye además pruebas de navegación y presentación con hijo ausente, estado
confirmado, desconexión e intención sin confirmar. Son datos ficticios exclusivos
del APK instrumentado, no evidencias de un dispositivo hijo.
P01 incluye dos pruebas de identidad con alias temporales y una prueba interactiva
de autenticación real, desactivada salvo argumento explícito. No habilitarla sin
acordar la interacción física. Un test omitido por ausencia de bloqueo seguro o
de argumento no cuenta como aprobado. Ver `pruebas/PRUEBA_FISICA_P01.md`.
También comprueba vectores criptográficos y una clave temporal no exportable en
Keystore. Esa clave existe solo en el test instrumentado y se elimina en su
`finally`; no es identidad de producción ni prueba autenticación del adulto.
Esta app no administra ni restablece teléfonos. Las pruebas del hijo real y
la integración se planifican por separado.

Estado real y próxima tarea: `ESTADO.md`. Herramientas: `BUILD_ENV.md`.
Acuerdos del propietario, idioma y frecuencia de commits: `AGENTS.md`.
Evidencia criptográfica local: `pruebas/EVIDENCIA_C02.md`.
Evidencia de navegación y estados: `pruebas/EVIDENCIA_P00.md`.
Evidencia de identidad/autenticación: `pruebas/EVIDENCIA_P01.md`.
