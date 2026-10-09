# Control parental — aplicación del padre y relay

Proyecto Android nativo en Kotlin y servicio Node/TypeScript. Este repositorio
solo implementa el rol del padre y el relay. El APK del hijo pertenece a otro
proyecto. La copia del protocolo CP/1 en `compartido/` permanece congelada.

## Qué hay implementado (C00 y C01)

- `app-parent/`: pantalla inicial en español, sin identidad, vinculación ni órdenes.
- `core-protocol/`: modelos, parser estricto, esquema congelado y serialización determinista CP/1, sin dependencias de Android.
- `relay/`: servidor **local** ligado a `127.0.0.1`, sin WebSocket operativo.
- `relay/test/soporte/`: cliente hijo simulado limitado a loopback y lectura de un fixture congelado; no prueba políticas reales.
- `scripts/verificar.ps1`: compilación y pruebas locales, sin acceso a dispositivos.

C01 añade los mismos modelos y validaciones en `relay/src/protocol.ts`, mediante
Ajv 2020-12. Ambos lados conservan los bytes de los diez vectores comunes.
Se controlan UTF-8, ASCII, duplicados, límites, secuencias, base64url, formatos
SPKI/DER, propósito y coherencia de los campos del estado.

**Todavía no hay autenticación criptográfica implementada.**
`leerPayloadSinVerificar` solo devuelve datos estructuralmente válidos; no
acredita firmas, identidad, frescura ni autorización. C02 añadirá la verificación
sobre los bytes recibidos, antes de validar el payload. Ninguno de estos datos
se usa todavía para confirmar ni aplicar órdenes.

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
`:app-parent:connectedDebugAndroidTest`. No ejecutar ese comando todavía.
Además de la pantalla, el APK instrumentado verifica el parser y el acceso al
esquema dentro de Android. Está compilado, pero esas pruebas no se han ejecutado.
Esta app no administra ni restablece teléfonos. Las pruebas del hijo real y
la integración se planifican por separado.

Estado real y próxima tarea: `ESTADO.md`. Herramientas: `BUILD_ENV.md`.
Acuerdos del propietario, idioma y frecuencia de commits: `AGENTS.md`.
