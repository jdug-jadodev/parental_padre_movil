# Estado de implementación

Última actualización: 2026-10-08
Commit de acuerdos: 4cc7a39; commit de base C00: e25bff5. Correcciones y cierre registrados en el commit posterior; consultar git log.
Tarea actual: C01
Estado: EN_CURSO — codec Kotlin validado; modelos Node listos, falta su codec y pruebas cruzadas

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
- C01 parcial: DTO Kotlin para todos los tipos CP/1, esquema tomado directamente de la copia congelada, parser estricto, codec determinista y validación adicional de secuencias, base64, SPKI P-256, DER y origen HTTPS. No hay verificación de firmas ni autorización contextual todavía.
- DTO Node añadidos y Ajv 8.20.0 fijado para JSON Schema 2020-12. Se descartó 8.17.1 porque npm audit detectó un aviso moderado; la versión finalmente fijada no presenta avisos en la auditoría ejecutada.

## Archivos modificados en la última tarea

AGENTS.md, .gitignore, .gitattributes, README.md, BUILD_ENV.md, ESTADO.md.
settings.gradle.kts, build.gradle.kts, gradle.properties, gradle/, gradlew, gradlew.bat.
app-parent/, core-protocol/, relay/, scripts/verificar.ps1, pruebas/HANDOFF_C00.md.
local.properties creado solo para este equipo y excluido de Git.

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
- C01 primer bloque: `:core-protocol:test` correcto, 8 pruebas aprobadas: cinco de JSON estricto, dos de base64/secuencias y el test base.
- C01 segundo bloque: `:core-protocol:test` correcto con 19 pruebas; diez vectores preservan sus bytes exactos. `npm run check` correcto con los nuevos DTO. Las firmas criptográficas alteradas se comprobarán en C02, no se declara autenticación por validación de DER.

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

Continuar C01: codec Node y pruebas con los mismos vectores/contraejemplos; después, verificación local completa. Aún no iniciar C02.
Prueba física disponible: cuando el propietario habilite el teléfono del padre por ADB, ejecutar :app-parent:connectedDebugAndroidTest y registrar resultado. No requiere Device Owner ni restablecimiento. La app requiere Android 11/API 30 o superior.

## Seguridad

No almacenar claves, códigos, QR sensibles, contraseñas ni datos del menor aquí.
