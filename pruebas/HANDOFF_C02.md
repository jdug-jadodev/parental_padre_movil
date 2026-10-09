# Relevo — C02

## Resultado comprobado

C02 completada localmente. Primer bloque: f120557; pruebas cruzadas y cierre en
el commit posterior. `scripts/verificar.ps1` correcto: 30 tests del núcleo, uno
de la app, 30 Node y uno de interoperabilidad, total 62 aprobados. El caso cruzado
realiza 20 intercambios nuevos para los diez vectores JVM↔Node. Los 78 chequeos
documentales siguen pasando. APK debug/instrumentado compilados, TypeScript
correcto y lint con cero errores y once avisos de actualización.

Release sin firma de distribución compilada; no es entrega aprobada. Inspección
ZIP/DEX confirma separación de fixture y código criptográfico de prueba examinado
respecto a los APK de la app. No se usó ADB, teléfono, Render ni otro repositorio.

## Interfaces y archivos

CriptografiaProtocolo, ClaveVerificacion, FirmanteMensaje, DocumentoVerificado
en core-protocol; verificador Node en relay/src/crypto-protocol.ts. Firmante
Kotlin inyectado, preparado para P01/Keystore; no hay privadas en producción.
Node solo verifica. DocumentoVerificado no equivale a autorizado o APPLIED.
El llamador debe aportar clave/rol confiables y propósito esperado. Se comprueba
firma original antes de interpretar el payload. Se conservan copias de bytes
originales y hash de payload, nunca hash de firma como identidad de comando.

Tests JVM/Node e interoperabilidad en sus directorios de pruebas. La herramienta
mjs genera claves efímeras y solo intercambia públicas/sobres; nunca guarda
privadas. El testInterop es explícito y separado del test JVM puro. El script
completo prepara npm antes de ejecutarlo. Assets del test Android se generan
desde el fixture congelado mediante addGeneratedSourceDirectory del AGP.

## Incidencias y pendientes

Se corrigió una dependencia implícita assets/lint y se repitió la verificación.
Cinco pruebas físicas disponibles pero pendientes de habilitación del teléfono
del padre por ADB, Android 11+. La clave temporal del test no autentica al adulto,
no sustituye identidad y se elimina en finally. No administrar ni restablecer.
Python/jsonschema sigue opcional y pendiente. Ninguna puerta física aprobada.

## Próxima tarea

P00, leyendo AGENTS.md, ESTADO.md, PLAN_PADRE P00 y SPEC_PADRE P2–P3.
Navegación y estados separados, sin endpoints reales ni falsas confirmaciones.
P01 viene después de P00; allí se implementa autoridad Keystore y autenticación.
Para pruebas físicas, avisar y esperar habilitación explícita de ADB antes de
connectedDebugAndroidTest. Detalles y resultados: pruebas/EVIDENCIA_C02.md.
