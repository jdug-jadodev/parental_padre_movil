# PADRE-C02 — validador puro aislado de revisión

Fecha de sesión: 2026-10-09. Asignación única del coordinador tras PADRE-C01-R1
95dfd02 y acuerdo HIJO-C02 cb62a73. No aprobación de puertas/orden del hijo.

## Implementación y aislamiento

- `app-parent/src/revisionAislada/kotlin/dev/controlparental/parent/pairing/RevisionVinculo.kt`:
  modelos inmutables de entrada y ValidadorRevisionVinculo.evaluar.
- `app-parent/build.gradle.kts`: esa carpeta participa **solo en sourceSet test
  JVM**, no main ni androidTest. No es un componente del APK o callback productivo.
- `app-parent/src/test/java/dev/controlparental/parent/RevisionVinculoTest.kt`:
  21 tests, identidades JCA efímeras y observaciones explícitas FICTICIAS_JVM.

Sin Android, IO, red, reloj oculto, timers, UI, ParentStore, migración, escritura,
token de autorización, promoción VINCULADO ni envío de políticas. Únicamente
diagnóstico COHERENTE/INCOMPLETA/RECHAZADA con enum de causa sin datos de entrada.
COHERENTE lleva causa CONSISTENCIA_FICTICIA; no indica hechos físicos comprobados.
La misma entrada produce mismo diagnóstico y no consume/modifica contexto alguno.

Los sobres copian defensivamente ByteArray al construir y al leer; acotados a
MAX_FRAME. Modelos restantes usan val, String, enums y valores inmutables.
No acepta DocumentoVerificado elegido por otra clave ni payload desnudo: vuelve
a verificar sobres originales PAIR/MESSAGE/OFFLINE con codec/crypto CP/1 existentes
y claves vinculadas del snapshot. Solo MAINTENANCE con misma pareja/boot/nonce y
respuesta exacta. Hash PAIR privado liga snapshot/revisión, no se exige al hijo.

Señales/contexto explícitos: identidad pública local, snapshot PREPARADO íntegro
v3, revisión/generación, resumen de cuatro datos públicos, ticket/nonce/boot/sesión,
últimas secuencias, instantes monotónicos y señales locales. STATE conserva
frescura de 20 s **desde solicitud**, PONG límite de 45 s. Margen OFFLINE local
240 s **al responder**, no al STATE posterior al mantenimiento; el hijo decide
su vigencia/consumo real. No se inventa timestamp ni recibo CP/1.

## Obligaciones del llamador y límites

El llamador hipotético debe aportar señales locales fiables al instante de
evaluación y validar autoridad/Keystore en vivo. El diagnóstico no comprueba
autenticación física, identidad disponible en Android, integridad/readback real
del archivo, procedencia de una pantalla, veracidad de declaraciones, consumo
OFFLINE o restricciones. No se habilita un llamador productivo en esta unidad.
Solo existe etiqueta FICTICIAS_JVM para observaciones en este conjunto aislado.
No hay conversión de datos de test a observaciones reales ni registro persistido.

El resumen y STATE no prueban el PAIR_ACCEPT exacto o aplicación de recuperación;
flags Device Owner/Lock Task/MAINTENANCE tampoco suplen observaciones. Secuencias
previas pertenecen al contexto suministrado; al cambiar conexión/sesión el llamador
debe pedir sync nuevo y establecer baseline correcto, nunca reciclar un ticket.
El validador no tiene memoria de replay ni consume confirmaciones: reevaluación
coherente repetida no es permiso repetible. Persistencia/ceremonia siguen fuera.

## Pruebas ejecutadas y resultado

```powershell
.\gradlew.bat :app-parent:testDebugUnitTest --tests '*RevisionVinculoTest' --rerun-tasks --console=plain
.\gradlew.bat :core-protocol:test :core-protocol:testInterop :app-parent:testDebugUnitTest --rerun-tasks --console=plain
node --test pruebas/interoperabilidad.test.mjs
node pruebas/verificar_vectores.mjs
```

Primera prueba focalizada: 17 tests nuevos correctos. Se añadieron cuatro tests
de hash de payload/firma, STATE MAINTENANCE sin observaciones, ALLOW impropio y
clave/origen ajenos. Suite final: **21 nuevos correctos**, 131 app + 30 núcleo +
1 interop, cero fallos/errores/omisiones; cinco oráculo Node: **167 independientes**.
Gradle final: BUILD SUCCESSFUL, 29 tareas ejecutadas. Diez vectores/78 comprobaciones
documentales correctas. Integración externa opcional excluida, no reejecutada.

Negativos: diferencias pairId/huellas/origen/formato/estatus, aceptación ausente/
ajena/impostora, payload estructural/presencia/rol/propósito indebidos, solo STATE,
observaciones ausentes/ajenas, ticket/nonce/boot/sesión/secuencia viejos, snapshot/
generación cambiados, RETIRE/ALLOW impropios, autoridad/fondo/cancelación/red/canal,
tiempo discontinuo y fronteras exactas. Positivo ficticio, determinismo, copias
defensivas y PREPARADO intacto; hash de payload independiente de nueva firma DER.

Inspección local de clases compiladas: cero `*Revision*.class` en main/debug,
16 en debugUnitTest. No inspección de APK nuevos: no assemble, lint, release,
instrumentación ni script completo verificar.ps1 en esta unidad. Gradle compiló
solo las clases necesarias para pruebas JVM. Sin ADB/dispositivos, certificados,
LAN, push/despliegue, subagentes o cambios en repos externos/CP/1.

### Incidencia anterior fuera de C02

Primer intento de suite completa: 131 app, un fallo ZXing NotFoundException en
`OfflineChallengeSignerTest.ambosQrSeLeenLocalmenteYLaRespuestaConservaFirmaOffline`.
Los 21 tests de revisión pasaron. Se repitió ese test anterior aislado con
--rerun-tasks: correcto; luego suite completa --rerun-tasks: correcta. No se
cambió QR/cámara ni su test, no se identificó causa ni se declara corregido.
Posible inestabilidad sintética a diagnosticar en otra unidad; no prueba física.

## Devolución y siguiente unidad propuesta

PADRE-C02 implementada/validada solo como diagnóstico de laboratorio. Sin bloqueos
que exigieran nuevas semánticas; no resuelve H08/H10/H11, recuperación física,
puertas/orden ni acreditación operativa. Datos reales y promoción permanecen fuera.

Coordinador debe revisar código/resultados antes de asignar más. Siguiente unidad
propuesta: diagnóstico acotado de la incidencia QR sintética anterior, sin ADB,
cámara productiva, cambios CP/1 o integración relay; no iniciada. Persistencia/
migración/ceremonia posterior requiere asignación y decisiones separadas.
A56 disponible no autoriza operaciones; A13 sigue excluido en esta sesión.
