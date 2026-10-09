# Estado de implementación

Última actualización: 2026-10-08
Commits C00: 4cc7a39, e25bff5, 00329bd. C01: f7e2044, dc50004, 6725515. Primer bloque C02: f120557; cierre en el commit posterior, consultar git log.
Tarea actual: P00
Estado: EN_CURSO — navegación y estados separados implementados; falta cierre de pruebas y evidencia

## Entorno observado

Android Studio AI-253.32098.37.2534.15232325 / JDK 21.0.6 / Gradle 8.13 / AGP 8.11.1 / Kotlin 2.2.0. Detalles en BUILD_ENV.md.
Node 24.21.0 / npm 11.19.0 / Windows 11 amd64
SM / Android / One UI / parche del hijo: PENDIENTE DE INTEGRACION

## Terminado y demostrado

- P00 primer bloque: MainActivity, AppContainer, ParentViewModel/StateFlow, cuatro pantallas y estados separados. Producción usa fuente no operativa, no un simulador; repositorio falso exclusivo de tests. Botones de órdenes deshabilitados, recuperación informativa siempre accesible.
- P00: 9 tests JVM de la app correctos (8 nuevos). Compilación debug/instrumentada y lint correctos antes del último lote de tests; UI nueva instrumentada todavía pendiente de preparar/ejecutar. Se corrigió la limpieza de ViewModelStore antes de resetMain en las pruebas JVM, sin simular APIs Android.

- Acuerdos del propietario persistidos en AGENTS.md y protegidos por un commit.
- Base Android compilada: APK debug de la app y APK instrumentado de pruebas generados.
- Base del relay compilada; responde 503 en health y rechaza upgrades/producción. No implementa todavía R01–R03.
- Cliente hijo simulado exclusivamente en tests, limitado a loopback; lectura de fixture firmado congelado.
- Verificación local completa mediante scripts/verificar.ps1 aprobada. No se ha instalado la app ni demostrado control real de un dispositivo.
- C01: modelos de todos los tipos CP/1 en Kotlin/Node, parser estricto, serialización determinista y validación adicional de secuencias, base64, SPKI P-256, DER y origen HTTPS. Los diez vectores preservan sus bytes exactos en ambos runtimes.
- Kotlin incorpora el esquema directamente desde compartido/; Node recibe sus bytes desde inicialización confiable. Ambos verifican su SHA-256 congelado. No se cambia ni duplica manualmente el contrato.
- Ajv 8.20.0 fijado para JSON Schema 2020-12. Se descartó 8.17.1 al detectar un aviso moderado en npm audit; la versión finalmente fijada no presenta avisos en la auditoría ejecutada.
- C02: verificación estándar SHA256withECDSA/DER sobre bytes originales en JVM y crypto.verify en Node; comparación de propósito y rol. El firmante Kotlin se inyecta desde la futura capa Keystore P01, sin almacenar privadas ni crear alternativas.
- Firmas nuevas verificadas en ambas direcciones para los diez vectores: 20 intercambios JVM↔Node, ambos roles y todos los propósitos. Claves de prueba efímeras en memoria, sin guardarlas ni imprimirlas.
- Tests instrumentados de criptografía y clave temporal no exportable preparados y compilados; pendientes del teléfono. No se acredita Keystore real ni autenticación del adulto por inspección o tests JVM.
- No hay autorización contextual: leerPayloadSinVerificar no acredita firmas; DocumentoVerificado acredita firma/estructura, no pareja autorizada, sesión vigente ni aplicación de una orden.

## Archivos modificados en la última tarea

00_EMPIEZA_AQUI.md, README.md, BUILD_ENV.md, ESTADO.md, scripts/verificar.ps1.
core-protocol/build.gradle.kts, core-protocol/src/main/kotlin/dev/controlparental/protocol/CriptografiaProtocolo.kt.
core-protocol/src/test/kotlin/dev/controlparental/protocol/CriptografiaProtocoloTest.kt e InteroperabilidadTest.kt.
relay/src/crypto-protocol.ts, relay/test/crypto-protocol.test.ts, relay/test/soporte/interoperabilidad.mjs.
app-parent/build.gradle.kts, app-parent/src/androidTest/java/dev/controlparental/parent/CriptografiaAndroidTest.kt.
pruebas/MATRIZ_ACEPTACION.md, pruebas/EVIDENCIA_C02.md, pruebas/HANDOFF_C02.md.

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
- C02 cierre: scripts/verificar.ps1 correcto con 30 pruebas JVM del núcleo, 1 de la app, 30 Node y 1 de interoperabilidad, total 62 aprobadas. El caso de interoperabilidad ejecutó 20 intercambios con firmas nuevas. Vectores documentales: 78 comprobaciones correctas.
- :app-parent:assembleDebug, :app-parent:assembleDebugAndroidTest y :app-parent:lintDebug correctos; lint con 0 errores y 11 avisos de versiones más recientes. :app-parent:assembleRelease correcto; produce APK sin firma de distribución, no una entrega aprobada.
- Inspección de los tres APK como ZIP y búsqueda de identificadores de clases de prueba en DEX: fixture solo en APK instrumentado; ni fixture, ni clases criptográficas de prueba examinadas, ni alias cp.test.crypto en la app debug/release.
- Primer intento de empaquetado C02 detectó dependencia implícita de assets/lint. Se corrigió con la API oficial addGeneratedSourceDirectory y se repitió la verificación completa con éxito.
- Cinco pruebas instrumentadas disponibles (pantalla, dos de parser, dos de criptografía/Keystore); compiladas, no ejecutadas. No se usó ADB.

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

Próxima tarea de desarrollo: P00 — padre/PLAN_PADRE.md, SPEC_PADRE P2–P3; navegación y estados de presentación sin endpoints reales. Consultar ADR-P-001 respetando la separación independiente del paquete. No iniciar P01 antes de P00.
Comprobación física disponible: cuando el propietario habilite el teléfono del padre por ADB, ejecutar :app-parent:connectedDebugAndroidTest y registrar resultados de pantalla/parser/esquema/criptografía. No requiere Device Owner, vinculación, órdenes ni restablecimiento. La app requiere Android 11/API 30 o superior. La clave Keystore del test es temporal y se elimina; no reemplaza ninguna identidad.

## Seguridad

No almacenar claves, códigos, QR sensibles, contraseñas ni datos del menor aquí.
