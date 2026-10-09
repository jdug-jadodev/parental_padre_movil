# Evidencia C01 — modelos y validación local

## Alcance

Modelos y decodificación estricta en Kotlin/JVM y Node, sin autenticación ni
aplicación de políticas. Commits de bloques: `f7e2044` y `dc50004`; el último
bloque y este documento se registran en el commit de cierre (consultar git log).
Entorno y versiones: `BUILD_ENV.md`.

## Ejecución real

Comando final desde la raíz: `scripts/verificar.ps1`.

- Núcleo: 21 pruebas JUnit, sin fallos ni errores.
- App: 1 prueba JVM, sin fallos ni errores.
- Relay: 23 pruebas Node, sin fallos, omisiones ni cancelaciones.
- Compilación debug de app y APK instrumentado: correctas.
- Chequeo de TypeScript y compilación: correctos.
- Lint Android: cero errores; once avisos de versiones más nuevas.
- Verificador documental: diez vectores y 78 comprobaciones correctas.
- npm ci: instalación reproducible; auditoría automática sin vulnerabilidades.

Los tests cubren campos extra/ausentes, tipos falsos, propósitos incompatibles,
UUID, base64 no canónico, longitudes reales, enteros seguros, secuencias Long/BigInt,
duraciones y sus relaciones, estado inicial coherente, orden de claves, duplicados
con escapes equivalentes, sintaxis ambigua, ASCII, UTF-8 inválido y BOM, profundidad,
límites exactos y excedidos, SPKI P-256 y DER estructural. JVM y Node comparan
directamente los bytes de los diez vectores; no regeneran fixtures.

Inspección estática del APK debug: contiene `protocolo.schema.json` con SHA-256
`eccf964fbb41869c47d86ddb0bfda6585136430bab297568d3c20b56607372bb`.
Ambos codecs rechazan usar un esquema cuya huella no corresponde a CP/1.

## Pendientes y límites

- Dos tests de protocolo Android y el test de pantalla están compilados, no ejecutados.
- No se usó ADB, ningún teléfono, Render ni el repositorio del otro rol.
- La estructura DER no demuestra que una firma sea válida. La implementación
  criptográfica y pruebas de firmas alteradas pertenecen a C02.
- Validar un documento no acredita pareja, arranque, sesión, nonce vigente ni
  autorización contextual. Esas reglas corresponden a los casos de uso futuros.
- El verificador Python no se ejecuta porque falta jsonschema; es opcional.
- Los avisos de dependencias no se ocultaron; versiones fijadas y compatibles.

**C-SCHEMA-01 validada localmente en JVM/Node; Android pendiente.** Ninguna puerta
G0–G4 ni caso de aplicación física se considera aprobado por esta evidencia.
