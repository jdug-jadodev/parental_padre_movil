# PADRE-ORD01 — codec, firmante y preparación aislada MESSAGE CP/2

Fecha2026-10-15. Base `8e13290`. Resultado:
**CODEC_FIRMANTE_MESSAGE_CP2_PROBADOS_LOCAL_SIN_EMISION**.

## Fuente y conteo

Implementado desde lista cerrada recibida del propietario, sin esperar schema
Node ni leer repos externos. Procedencia parcial en contrato-cp2/ORD01_FUENTE_PARCIAL.md;
no freeze CP/2 completo. SYNC_REQUEST6 y SIGNED5 con asserts de conjuntos exactos.
La lista SET_POLICY suma **12**, no13: v,kind,sender,pairId,bootId,childSessionId,
commandId,seq,expectedRevision,commandNonce,action,durationSec. Lista comunicada
al comenzar, assert12+conjunto exacto; no campo inventado ni protocol añadido.
No cambios al draft general, schema/fixturesCP1 ni PAIR/AUTH/schema/fixturesCP2.

## Código y límites de autoridad

- core-protocol/.../cp2/MensajesPadreCp2.kt: DTOs inmutables, codec manual cerrado,
  payload y sobre canónicos antes de firmar; dominioCPv2/MESSAGE+LFreal. UTF8
  estricto, sin duplicados/extras, UUIDv4lowercase, nonce32roundtrip, SPKIP256DER,
  firmaDERr,s, frame16384/payload8192/depth6 con primitivas existentes sin modificar.
  Contadores son cadenas decimales sin ceros iniciales; positivos para seq, cero
  permitido en expectedRevision, máximo9223372036854775807. durationSec entero,
  solo60..86400 múltiplo60 en ALLOW_FOR, null obligatorio para LOCK/ALLOW.
- AutoridadPadre.firmarMessageCp2: misma autoridad/AlmacenClavePadre/SesionAutoridad
  que usa AndroidKeystore; no crearSiFalta ni rotación. Pública esperada igual a
  identidad existente antes/durante/después de firma, sesión vigente y verificación
  criptográfica del resultado por codec. Error invalida sesión. Operación propia,
  separada de Pair/Auth/CP1. MAIN existente no la invoca.
- PrepararMensajeCp2: fuente inyectada de snapshot confirmado compatible y STATE
  YA verificado por puerto externo; **ninguna implementación productiva**. El puerto
  futuro debe acreditar firma/identidad/contexto y reto vivo, y devolver null al
  perder vigencia/conexión/autoridad. No parserSTATE parcial ni STATE hardcoded en main.
  SnapshotCP1/ausente/pendiente/no compatible/identidad distinta bloquea. No se toca
  ParentStore/PairStore ni se promueve pendiente a activo para conseguir una firma.
- Tickets RAM ligados a instancia y sync actual, tiempo monotónico no negativo;
  caducan a20s desde comienzoSYNC, incluyendo tránsito/preparación/firma. Relectura
  snapshot y STATE/contexto antes/después; pair/action/syncNonce exactos, boot/sesión/
  revisión/commandNonce exactos desde STATE externo. Confirmación explícita del
  payload completo; cambio/error/cancelación retira sync. Nuevo sync invalida anterior;
  no refirma automática tras conflicto ni renovación por recibir STATE/PONG.
  El futuro llamador de lifecycle debe cancelar tickets al salir/fondo; no hay UI
  conectada en esta unidad. Sesión/foreground se comprueban al firmar.
- Preparación valida seqPropuesta mayor a lastAcceptedSeq externo; **no** reserva,
  consulta ni resetea contador local. Firmar una propuesta en RAM no autoriza
  enviarla. Reserva max(local,lastAccepted)+1, persistencia intención/bytes/hash y
  reintento/reconciliación irán en unidad siguiente; no exactly-once demostrado.
- ALLOW_FOR exige Int minutos explícitos1..1440, sin valor preseleccionado;
  LOCK/ALLOW prohíben minutos. Sin LOCK_FOR/deferredAllowSec/WAITING_ALLOW.
  No implementar/copiar ALLOWoffline del draft§5: regla superada NORMAL10s/OFFLINE
  local explícita no cambia payloads ni autoriza offline remoto; sin enforcement aquí.

## Pruebas ejecutadas

15tests nuevos en app-parent/src/test/.../OrdenCp2Test.kt. ClavesP256 efímeras RAM,
snapshots y contextoSTATE ficticios marcados soloJVMtest. No nuevas muestras ni
privadas guardadas. Casos: campos/conjuntos/null obligatorio, copias inmutables/hash,
dominioLFreal, extremos/overflow/ceros/tipos, minutos/duración, desconocidos/extras,
canonicalidad/UTF8/duplicados/límites/depth, UUID/nonce, firma real y mutación de bytes,
claves/DER/r,s inválidos y cruces CP1/PAIR/AUTH/dobleescape. Fuente ausente/pendiente/
CP1/cambiada, contextoSTATE ausente/conflictivo, confirmación distinta, tickets
cancelados/ajenos/reemplazados/agotados, secuencia agotada sin reset, cambios durante
firma, caducidad de20s desde inicio sin rejuvenecer. Sourcegate recorre main y
comprueba ausencia de implementación/instanciación fuente/caso y llamadas de emisión
desde UI/network/MainActivity; no garantiza contra futuras modificaciones fuera del test.

Comandos exactos ejecutados, en este orden (línea full ejecutada dos veces):

```powershell
.\gradlew.bat :app-parent:testDebugUnitTest --tests '*OrdenCp2Test' --console=plain
.\gradlew.bat :app-parent:testDebugUnitTest --tests '*OrdenCp2Test' --console=plain
.\gradlew.bat :core-protocol:test :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture :app-parent:assembleDebug :app-parent:assembleRelease :app-parent:lintDebug :app-parent:lintRelease --console=plain
.\gradlew.bat :core-protocol:test :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture :app-parent:assembleDebug :app-parent:assembleRelease :app-parent:lintDebug :app-parent:lintRelease --console=plain
python pruebas/verificar_aislamiento_ord01.py
.\gradlew.bat :app-parent:testDebugUnitTest --console=plain
```

Primera focal falló compilación: helpercanonico interno de core no accesible desde
app test y paréntesis de test; corregidos usando helper canónico plano solo test.
Segunda focal11tests aprobados (antes de ampliar a15). Primera full34core+192app
fixture=**226distintos**,0fallos/errores/omitidos;108tareas31ejecutadas77UP-TO-DATE.
Después se corrigió warningnullability del test sourcegate y se aclaró comentario
de contrato del puerto; segunda full192app sí ejecutados, core34UP-TO-DATE (ya
ejecutado antes sin cambios),108tareas28ejecutadas80UP-TO-DATE. No sumar repeticiones
o UP-TO-DATE como nuevos tests. Normal final191app,0fallos/errores/omitidos,
25tareas1ejecutada24UP-TO-DATE. No se invocó Node/testInterop histórico ni otros repos.

Debug y releaseunsigned compilados con código final. LintAnalyze de variantes/tests
ejecutado; reportes idénticos reutilizados,23warnings versiones/0errores por variante.
No nueva instrumentación/Keystore real/ADB/prueba física ni build de APKtest solicitado.

## APK finales y aislamiento dirigido

SHA256debug: `dcb52612188d707c454a6bc8d673f1eb86bafadf2211e5f5639b2263ab119df6`.
SHA256releaseunsigned: `ba5f075870ea259d6560e3fc2c21613a230c890f55141b34d6f707b7aa693de7`.
Script pruebas/verificar_aislamiento_ord01.py inspeccionó ZIP descomprimido y DEX
contra210descriptores de clasesJVMtest (core+app), markerfake-solo-test, fixtures
CP2 públicos y sus públicas/sobres:0coincidencias en ambos APKapp. Comprueba3/3
descriptores productivos (codec/preparación/autoridad) presentes. No es auditoría
universal de secretos, verificación hardware ni afirmación de ejecución en Android.
No se generó nueva identidad/fixture ni se modificaron muestras congeladas.

## Entrega y pendientes

Archivos de código/test/script: MensajesPadreCp2.kt, PrepararMensajeCp2.kt,
AutoridadPadre.kt, OrdenCp2Test.kt, pruebas/verificar_aislamiento_ord01.py.
Docs: esta evidencia, contrato-cp2/ORD01_FUENTE_PARCIAL.md, README, ESTADO.md,
COORDINACION_PROYECTOS.md. Menos500líneas manuales de código/tests/script;
commit de cierre sin push. No documentación añadida para alcanzar cuota.

Propietario reporta Render verificó vueltaJVM hashfb830c79…65d4b y oferta/PAIR/ambosAUTH;
también interoperabilidad hijo hashfa04fc86…b36bf6. Información recibida, no ejecutada
aquí; no vínculo real. HIJO-AUTH01 y schema/codecNodeORD en curso solo reportados.
Falta contrastar fuenteNodeORD validada cuando llegue; integraciónSTATEfirmado,
reto vivo y snapshot compatible reales, reserva/persistencia/reintentos y UI/red
siguen pendientes por asignación. AUTH_OK/PONG/QR/PAIRpendiente nunca habilitan órdenes.
No APKinstalado ni órdenes reales, sin teléfonos/A56/A13/ceremonia/seqreal/almacenes/
otrosrepos/subagentes/sesiones/promptsdevuelta/push/deploy. F02cerrada. Detener al
cierre, sin otra tarea automática. Interop/firmas locales no prueban enrolamiento,
confirmación hijo, IOAndroid, cámara, ACK/APPLIED, frescura remota ni enforcement.
