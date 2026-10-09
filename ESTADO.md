# Estado de implementación

Última actualización: 2026-10-08
Commits C00: 4cc7a39, e25bff5, 00329bd. Bloques C01: f7e2044, dc50004; cierre en el commit posterior, consultar git log.
Tarea actual: C02
Estado: EN_CURSO — verificación y firma local implementadas; falta prueba cruzada de firmas nuevas y comprobación Android

## Entorno observado

Android Studio AI-253.32098.37.2534.15232325 / JDK 21.0.6 / Gradle 8.13 / AGP 8.11.1 / Kotlin 2.2.0. Detalles en BUILD_ENV.md.
Node 24.21.0 / npm 11.19.0 / Windows 11 amd64
SM / Android / One UI / parche del hijo: PENDIENTE DE INTEGRACION

## Terminado y demostrado

- Acuerdos del propietario persistidos en AGENTS.md y protegidos por un commit.
- Base Android compilada: APK debug de la app y APK instrumentado de pruebas generados.
- Base del relay compilada; responde 503 en health y rechaza upgrades/producción. No implementa todavía R01–R03.
- Cliente hijo simulado exclusivamente en tests, limitado a loopback; lectura de fixture firmado congelado.
- Verificación local completa mediante scripts/verificar.ps1 aprobada. No se ha instalado la app ni demostrado control real de un dispositivo.
- C01: modelos de todos los tipos CP/1 en Kotlin/Node, parser estricto, serialización determinista y validación adicional de secuencias, base64, SPKI P-256, DER y origen HTTPS. Los diez vectores preservan sus bytes exactos en ambos runtimes.
- Kotlin incorpora el esquema directamente desde compartido/; Node recibe sus bytes desde inicialización confiable. Ambos verifican su SHA-256 congelado. No se cambia ni duplica manualmente el contrato.
- Ajv 8.20.0 fijado para JSON Schema 2020-12. Se descartó 8.17.1 al detectar un aviso moderado en npm audit; la versión finalmente fijada no presenta avisos en la auditoría ejecutada.
- C02 parcial: verificación estándar SHA256withECDSA/DER sobre bytes originales en JVM y crypto.verify en Node; comparación de propósito y rol. El firmante Kotlin se inyecta desde la futura capa Keystore P01, sin almacenar privadas ni crear alternativas.
- No hay autorización contextual: leerPayloadSinVerificar no acredita firmas; DocumentoVerificado acredita firma/estructura, no pareja autorizada, sesión vigente ni aplicación de una orden.

## Archivos modificados en la última tarea

AGENTS.md, README.md, BUILD_ENV.md, ESTADO.md, build.gradle.kts, gradle/libs.versions.toml.
core-protocol/build.gradle.kts, core-protocol/src/main/kotlin/dev/controlparental/protocol/ y sus tests JVM.
relay/package.json, relay/package-lock.json, relay/src/modelos.ts, json-estricto.ts, formatos.ts, protocol.ts y relay/test/protocol.test.ts.
app-parent/src/androidTest/java/dev/controlparental/parent/ProtocoloAndroidTest.kt.
pruebas/MATRIZ_ACEPTACION.md, pruebas/EVIDENCIA_C01.md, pruebas/HANDOFF_C01.md.

## Pruebas ejecutadas y resultado

- `node pruebas/verificar_vectores.mjs`: 10 vectores y 78 comprobaciones documentales correctas en Node 24.21.0.
- `python -B pruebas/verificar_esquema.py`: no ejecutó validación; falta el paquete jsonschema.
- `scripts/verificar.ps1 -Modulo relay`: instalación reproducible con npm ci, chequeo TypeScript, compilación y vectores correctos. Al añadir el cliente local simulado se repitieron `npm test` y `npm run check`: 6 pruebas aprobadas.
- Ejecutable compilado del relay: arranque local y GET /healthz observado con 503; inicio en producción rechazado con código 1.
- SHA-256 de contrato y esquema: coinciden con INTEGRACION_FUTURA.md.
- `:core-protocol:test`: 1 prueba JVM aprobada (reporte XML, sin fallos ni errores).
- `:app-parent:testDebugUnitTest`: 1 prueba aprobada (reporte XML, sin fallos ni errores).
- `:app-parent:assembleDebug` y `:app-parent:assembleDebugAndroidTest`: compilaciones correctas. La segunda solo compila las pruebas; no las ejecuta en Android.
- `:app-parent:lintDebug`: 0 errores y 9 avisos de versiones más recientes. Se mantienen versiones fijadas y compatibles; no se deshabilitaron comprobaciones.
- Primer comando Android interrumpido a los 120 segundos. Segundo intento completó APK/tests, pero lint detectó un escape incorrecto en local.properties y ausencia de icono. Ambos corregidos. La ejecución final completa de `scripts/verificar.ps1` terminó correctamente: núcleo, app, lint, relay y vectores.
- Ninguna prueba por ADB, en teléfono, en Render o con el APK real del hijo.
- C01 cierre: `scripts/verificar.ps1` correcto: 21 pruebas del núcleo JVM, 1 de la app y 23 Node, total 45 aprobadas. Compilaciones de ambos APK, TypeScript, chequeo de tipos y vectores correctos. Lint: 0 errores y 11 avisos de versiones más nuevas; no se suprimieron comprobaciones.
- Inspección del APK como ZIP: contiene protocolo.schema.json con el SHA-256 congelado exacto. Eso comprueba empaquetado, no ejecución dentro del teléfono.
- Dos nuevas pruebas instrumentadas del parser/esquema Android compiladas; no ejecutadas, al igual que la prueba de pantalla de C00. Las firmas alteradas criptográficamente se comprobarán en C02: no se declara autenticación por validación de DER.
- C02 parcial: :core-protocol:test correcto con 30 pruebas (9 criptográficas nuevas); npm test correcto con 30 pruebas (7 nuevas). Firmas y payloads alterados, claves equivocadas, propósitos cambiados y roles impostores se rechazan. npm run check y npm run build correctos.

## Decisiones locales sin cambiar ADR

- Módulos Android en la raíz, adaptados al alcance independiente de C00. No se crea app-child ni se modifica el otro repositorio.
- SPEC-P P2 y ADR-P-001 conservan una estructura monorepo anterior; se sigue la separación explícita de 00_EMPIEZA_AQUI.md, C00 y AGENTS.md. No se cambia CP/1.
- Servidor C00 ligado solo a loopback y no operativo; health 200 queda para R01 con configuración válida.
- Compilador con JDK 21 y destino JVM 17; dependencias fijadas, wrapper con checksum.

## Bloqueos y riesgos abiertos

G0–G4 pendientes. No hay validación física del A13 ni del teléfono del padre.
Falta jsonschema para el verificador estructural Python.
Antes de usar ADB, avisar y esperar habilitación expresa del propietario.
La referencia plantillas/HANDOFF.md de padre/AGENTS.md no existe; usar pruebas/HANDOFF_PLANTILLA.md.

## Próxima tarea y lectura mínima

Cerrar C02: ejecutar testInterop con firmas nuevas JVM↔Node y preparar tests criptográficos instrumentados. No iniciar P00/P01 en esta tarea.
Primera comprobación física lista: cuando el propietario habilite el teléfono del padre por ADB, ejecutar :app-parent:connectedDebugAndroidTest y registrar resultado de pantalla/parser/esquema. No requiere Device Owner, vinculación, órdenes ni restablecimiento. La app requiere Android 11/API 30 o superior.

## Seguridad

No almacenar claves, códigos, QR sensibles, contraseñas ni datos del menor aquí.
