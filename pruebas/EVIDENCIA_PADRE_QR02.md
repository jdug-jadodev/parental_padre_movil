# PADRE-QR02 — lector corregido y regresión sintética fija

Fecha: 2026-10-14. Base75eb56e, implementación **ff9aa4c**.
Estado: **FIX_VALIDADO_SINTETICO / PREPARADO_FISICO**. No prueba de cámara A56,
vínculo, identidad, APPLIED, recuperación real ni CP/2. PREPARADO no se promociona.

## 1. Reproducción antes de cambiar producción

Fixture original intacto:
`app-parent/src/test/resources/qr01_respuesta_publica.json`, SHA-256
`4fc43b386a08c35333298371d3c0c33d99500c2161c83c918422f71f9c587c80`.
Solo pública/sobre ficticios; ninguna privada/nueva firma/código/dispositivo.
EVIDENCIA_PADRE_QR01.md permanece histórica sin cambios (3/128 fallos de aquella muestra).

Ejecutado test fijo anterior, una vez, antes de modificar lector: tres lecturas
QrLocal dieron NotFoundException como esperaba la caracterización; lector QR directo
ChecksumException y detector eligió (122.5,676.0) en vez de (68.5,698.5). Control
PURE de test recuperó bytes/firma. BUILD SUCCESSFUL significa caracterización del
fallo conocido, NO que entonces estuviera corregido.

Después, presupuesto fijo **8** detecciones oficiales del mismo frame/imagen,
sin nuevas firmas: 0/90/180/270 grados × TRY_HARDER ausente/presente. Ambas a0°
dieron ChecksumException; las seis orientadas dieron bytes exactos. No repetir
muestreo aleatorio ni probar hasta verde. Test conservado como EstrategiaQrTest:
en versión final verifica las seis firmas con SPKI real del fixture y exige6 éxitos.

## 2. Corrección mínima y límites operativos

QrLocal conserva generador, máscara/margen, contenido, CP/1 y ZXing **3.5.3**.
Ambas entradas RGB/Y pasan por plano gris y QRCodeReader oficial + HybridBinarizer,
UTF-8/TRY_HARDER. Detectar a0°; solo si ReaderException, rotar plano90° y repetir,
máximo4 orientaciones totales. Cambia orden de barrido/geometría del finder, no
módulos/bytes del símbolo. Geometría siempre se detecta, no PURE_BARCODE/crop cuadrado
presupuesto/otra firma/máscara fija. No inversion fallback o SDK/proveedor nuevos.

Se preserva checksum/formato si alguno falló; si ninguna orientación decodifica,
lanza ReaderException, nunca texto reparado/inventado. Nueva imagen de cámara es
otro frame del analizador existente, no retry de tests para aprobarlos.

Presupuestos: ancho/alto1..2048, buffer exacto, hasta4194304 píxeles; cuatro scans y
solo tres planos rotados nuevos como máximo (≤12582912bytes acumulados para rotaciones
a tamaño máximo, aparte de plano original/RGB/estructuras ZXing). Sin cache de imágenes
o resultados. No iniciar otro scan si pasaron400ms desde entrada de decodificación;
**presupuesto cooperativo, no deadline duro**: ZXing síncrono no permite interrumpir
una detección en curso; conversión RGB/GC/latencia de dispositivo no acreditadas.
CameraX ya usa executor único y KEEP_ONLY_LATEST; no se cambió ciclo de cámara/UI.

No se instrumentó/corrigió FinderPatternFinder ni se acredita que su selección sea
siempre correcta. Para el fixture falla el barrido inicial y orientación distinta
recupera la geometría válida mediante ZXing. Escenas ambiguas pueden devolver un
candidato o error: lector single-QR no detecta/rechaza universalmente varios QR.
Bordes de codec/firma/pareja/nonce y confirmación adulta siguen siendo obligatorios.

## 3. Texto frente a bytes y controles de seguridad

QRCodeReader.Result.rawBytes contiene codewords/datos QR, no el sobre CP/1. Los
BYTE_SEGMENTS tampoco se concatenan ciegamente: QR puede mezclar segmentos.
Usar contenido lógico `.text`, CHARACTER_SET UTF-8, sin trim, normalización Unicode,
espacios eliminados o recanonización. Re-encoding UTF-8 estricto con REPORT para
surrogates inválidos y rechazo U+FFFD; límite CodecProtocolo.MAX_FRAME=16384 intacto.
CP/1 lleva ASCII determinista: test exige igualdad de **todos los bytes frame** y
verificación real de firma OFFLINE con pública vinculada ficticia al terminar.
UTF-8 general válido conserva también espacios/salto/acentos/CJK sin normalizar;
eso no lo convierte en mensaje CP válido. No aceptar checksum fallido como contenido.

CamaraOferta sigue codec oferta/sobre OFFLINE estructural y callback; PairChild/
OfflineChallengeSigner verifican después bytes/firma/pareja/contexto. Lectura óptica
sola no acredita identidad/vínculo/consumo ni APPLIED. No se cambió ninguna guarda,
CP1codec/schema/fixtures compartidos, ParentStore, auth o validadorC02.

Fuentes primarias consultadas (tag exacto de dependencia):
- https://raw.githubusercontent.com/zxing/zxing/zxing-3.5.3/core/src/main/java/com/google/zxing/qrcode/QRCodeReader.java
- https://raw.githubusercontent.com/zxing/zxing/zxing-3.5.3/core/src/main/java/com/google/zxing/qrcode/decoder/DecodedBitStreamParser.java

## 4. Presupuesto de regresión y todos los resultados

QrLocalRegresionTest: **9** tests nuevos normales, sin exclusión opt-in:
- Fixture original/firma y4 rotaciones, RGB/Y (8 lecturas), plano recibido inmutable.
- Matriz fija48 casos: tamaños/margen (768/4,752/4,510/4,680/4,768/8,744/8) ×8 máscaras;
  dos entradas=96 lecturas, **cero fallos**, todos bytes exactos/firma real. No firmar
  de nuevo para cambiar contenido. Primer focal registró897ms total para96 lecturas
  en JVM de PC; no métrica de cámara/A56 ni cota general de latencia.
- Dos cizallas±0.06, traslación sobre plano900×850 (4 lecturas RGB/Y); no simulación
  completa de perspectiva/lente/iluminación/distancia/movimiento de cámara.
- Blanco/ruido sin QR; dimensiones/buffers inválidos y borde2048; error explícito.
- Símbolo con datos destruidos y texto U+FFFD rechazados; no checksum→payload.
- No protocolo y firma alterada solo en imagen sintética: lectura no elimina rechazo
  posterior del codec/crypto reales. Sin privada ni fixture compartido cambiado.
- Dos candidatos: contenido exacto de uno o ReaderException, nunca concatenación ni
  firma inventada. No garantiza elección del esperado en escena ambigua.
- UTF-8 sin normalizar y generación>2500 rechazada. QR válido no admite16384bytes de
  texto: no fabricar QR imposible para «probar» rama límite de frame; guarda revisada.

EstrategiaQrTest: **1** test nuevo de ocho scans deterministas; seis positivos con
bytes/firma y dos Checksum esperados. DiagnosticoQrTest fijo existente actualizado
para exigir éxito del lector corregido (ya no assertFalse); conserva control directo
fallido/geometría falsa y PURE solo como baseline de laboratorio. Muestreo opt-in
`diagnosticoQr=true` no ejecutado ni prueba de estabilidad; no excluye originalP07.

Resultados Gradle, en orden (ningún rerun hasta verde):

```powershell
# 1. Baseline ANTES del fix: test viejo aún existente; caracteriza tres NotFound.
.\gradlew.bat :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture --tests '*DiagnosticoQrTest.casoPublicoFijoCaracterizaFalloNoCorregidoSinFirmarDeNuevo' --rerun-tasks --console=plain
# 2. Ocho scans de estrategia fija, dos checksum, seis lecturas.
.\gradlew.bat :app-parent:testDebugUnitTest --tests '*EstrategiaQrTest' --rerun-tasks --console=plain
# 3. Corrección + focal P02/P07:29 tests; regresión aún8 antes de agregar negativo dañado.
.\gradlew.bat :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture --tests '*QrLocalRegresionTest' --tests '*DiagnosticoQrTest.casoPublicoFijoExigeLecturaCorregidaSinFirmarDeNuevo' --tests '*OfflineChallengeSignerTest' --tests '*PairChildTest' --tests '*LuminanciaQrTest' --rerun-tasks --console=plain
# 4. Suite final fixture, ya9 regresiones y verificación crypto de estrategia.
.\gradlew.bat :core-protocol:test :core-protocol:testInterop :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture --rerun-tasks --console=plain
# 5. Suite normal + builds/lint, mismo código final.
.\gradlew.bat :app-parent:testDebugUnitTest :app-parent:assembleDebug :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease :app-parent:lintDebug :app-parent:lintRelease --rerun-tasks --console=plain
# 6. Commit implementación ff9aa4c; rebuild de APK con ese HEAD y sin cambios fuente.
.\gradlew.bat :app-parent:assembleDebug :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease --rerun-tasks --console=plain
```

Las seis órdenes BUILD SUCCESSFUL, respectivamente25/25/25/29/131/113 tareas
ejecutadas. Suite final fixture: **142app+30núcleo+1interop=173** ejecutados distintos,
cero fallos/errores/omitidos; normal **141app**, cero fallos/errores/omitidos. No sumar
otra vez ejecuciones repetidas ni core del build como tests: test/testInterop sí
ejecutados en orden4, no UP-TO-DATE. Node/relay/CP2/instrumentación física no ejecutados.
OfflineChallengeSignerTest8, PairChildTest7 y LuminanciaQrTest5 pasan en suites;
original `ambosQrSeLeenLocalmenteYLaRespuestaConservaFirmaOffline` intacto/incluido.

Todos los errores ópticos observados de baseline/estrategia están arriba; casos
negativos exigen errores, sin fallos inesperados de compilación/tests/variantes.
No nuevo muestreo aleatorio diagnóstico ni bucles hasta éxito. Rotaciones internas
no se cuentan como casos aprobados independientes ni se ocultan como corrección de
ZXing upstream. El primer intento fallido del caso fijo sigue visible en baseline.

Lint debug/release: **0errores,23warnings** cada uno (AndroidGradlePluginVersion,
GradleDependency, NewerVersionAvailable). No cambios de dependencia por recomendaciones.
Build avisó librerías .so sin strip; empaquetadas como estaban, no fallo de compilación.

## 5. APK reales finales, procedencia y aislamiento inspeccionado

Artefactos finales reconstruidos en HEAD **ff9aa4c**, después del commit de código;
cierre posterior solo documentación, no cambia fuentes/APK. NO instalados.

| Ruta bajo app-parent/build/outputs/apk/ | SHA-256 final |
|---|---|
| debug/app-parent-debug.apk | 7f3947394c5bb921a11539a66ed7162029818702d0fc1a3181c53a5b55d6f534 |
| release/app-parent-release-unsigned.apk | 3025fedfea1634c843ce89842e85654ceda9d8955c95e92b4b6274bf78951857 |
| androidTest/debug/app-parent-debug-androidTest.apk | 2074540e73558d22ea5241c108ff432ad125d77a0839a16caa29a0f53931be25 |

Antes del commit (build5, fuentes finales sobre75eb56e), debug tenía mismo hash;
release era `b4fb8643a8f5ef762c7bb20e01d06dbd8956da956278a02f491e3b88c569385f`.
**Ese release previo no es el final**; se reconstruyó tras commit/extracción metadata
VCS. No atribuir hash de un archivo anterior a otro binario. Release sin firma de
distribución; debug/test con firma de desarrollo existente, sin entrega/despliegue.

Inspección local Python por stdin (`python -`, zipfile/struct/pathlib/hashlib/json):
leer tabla class_defs/type_ids/string_ids de cada classes*.dex de los dos APK app,
contrastar descriptores con141 clases compiladas de debugUnitTest, buscar nombres
Test/diagnósticos/RevisionVinculo/RepositorioPadreFalso y marcadores públicos del
fixture (nombre,SPKI,frameB64,frame bytes) en entradas ZIP descomprimidas. Resultado
en **cada APK app**:477 clases propias,1 QrLocal, **0 coincidencias JVMtest/prohibidas,
0 marcadores fixture**. SHA del fixture calculado con Get-FileHash (intacto).
APK androidTest es artefacto de pruebas separado, no ejecutado en dispositivo;
fixtures Android anteriores pertenecen a ese APK, no se confunden con release.
Comprobación dirigida de empaquetado, **no auditoría universal** de secretos o DEX,
no firma/certificado de distribución ni evidencia Keystore/hardware/privadas reales.

## 6. Cierre, física y contexto del coordinador

Cambios código/pruebas: QrLocal.kt, DiagnosticoQrTest.kt, EstrategiaQrTest.kt,
QrLocalRegresionTest.kt (240 líneas añadidas/11 retiradas según diff, <500 manuales).
Sin config/deps nuevas, sin editar fixture original ni evidenciaQR01. Documentos:
esta evidencia, COORDINACION_PROYECTOS.md y ESTADO.md. Git diff --check/preparado/estado
revisados; commit códigoff9aa4c, cierre documental hash devuelto al finalizar.

Sin autorización física nueva: **PREPARADO_FISICO**. No ADB/inventario/deviceId/
instalación/cámara/fotos/UI dumps/certificados/CA/LAN/aprovisionamiento/reset/PIN/
grants/hijo/A13/connected tests/push/despliegue/subagentes/sesiones/prompts de vuelta.
Antes de instalar/probar A56: nuevo aviso+confirmación específica en sesión padre,
selector explícito A56; primera cámara solo fixture público ficticio en pantalla PC,
sin usar hijo para mostrar QR o ceremonia de pareja sin autorización aparte.
Este fixture es respuesta PARENT ficticia: prueba óptica no significa que UI espere
aceptarlo como CHILD_OFFER/reto CHILD (debe conservar rechazo de rol/tipo/pareja).

LOCAL01–08/45f7a44 y deltas POSTC08 del hijo son contexto **reportado por Render**,
no reejecutado/aceptado físicamente aquí. NORMAL cae tras10s, excepción OFFLINE
explícita no automática, boot invalida ambas/DNSVPN no autoridad: no incorporados
a CP1/CP2 por este fix. No reabrir historia/push/cupos/retención ni certificar H05/
H06/enforcement/cero evasión. Borrador CP2 sigue sin schema/versionado acordados.
Siguiente paso solo si se asigna: autorización/prueba física A56 acotada o revisión
del coordinador. Sin nueva tarea automática. Corrección sintética no promete leer
cualquier cámara/QR, ni resuelve la ceremonia o comunicación remota por sí sola.
