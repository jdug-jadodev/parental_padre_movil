# PADRE-PAIR01 — lectura MAIN CP/2 y preparación RAM separada

Fecha: 2026-10-14. Basebeecf75; implementación **82734c7**.
Estado: **LECTOR_CP2_MAIN_PREPARADO_FISICO / PREPARACION_FIRMADA_LOCAL_PROBADA**.
Entrega parcial: firma/códigos CP/2 deshabilitados en MAIN, sin snapshot compatible
autorizado. No migración, commit, vínculo operativo, WSS CP/2 ni ceremonia física.

## Fuentes y alcance

Schema/README CP2-V01 Render4ac4962 recibidos íntegros en conversación. No lectura
externa, ejecución relay o comprobación de aquel commit. Copia local nueva exacta
del schema: contrato-cp2/vinculacion.schema.json, SHA256
`7290e6cb419e910756d3848586be24358f063455b1c65c47f24e75b5cef22239`.
Corrección documental PADRE-INTEROP01: fuente íntegra tiene6 campos oferta/**10 aceptación**/5 sobre;
la mención previa a11 era error de conteo, no de schema/codec. Schema no se carga en producción; codec
valida explícitamente campos/tipos/formatos equivalentes. Test contrasta conjuntos
required/properties de los tres objetos y ausencia de cada campo.

Leídos instrucciones/estado/P02, componentes propios de vinculación/cámara/MAIN,
borrador75eb56e§§2–3/10.1 y primitivas CP/1. GateC08/hijoHIJO-PAIR01/RenderAUTHCP2/
LOCAL01–08 son contexto reportado, no código externo leído/ejecutado ni evidencia
de interoperabilidad actual. No esperar hijo para desarrollar bordes independientes.

## Codec Kotlin separado

`core-protocol/.../protocol/cp2/VinculacionCp2.kt`: DTOs inmutables oferta,
aceptación, recuperación y documento verificado con copias defensivas de bytes.
Campos cerrados, v2/cp.v2/kind/PAIR exactos, UUIDv4 minúsculo, nonce/hash32bytes,
base64url roundtrip sin padding, pública SPKI canónica P256 con curva y punto en
campo, origen HTTPS exacto min9/max200 con gramática/puerto/URI sin normalización.

UTF8 estricto, parser sin duplicados, cadenas ASCII imprimible, claves canónicas
ordenadas en todos los objetos. Barrera profundidad6 antes de parse anidado,
payload8192/frame16384 antes de crypto/parse según borde. Se mantienen primitivas
internas sin editar CP1. DER8..72bytes/r y s positivos mínimos y menores que n;
firma SHA256withECDSA sobre prefijo exacto `CPv2/PAIR\n` + payload original.
Pública padre esperada aportada por llamador y comparada con declaración del payload;
no derivación de autoridad desde QR. Hash SHA256 del payload original, no del sobre.
Verificar firma no aporta replay/freshness/commit; no PAIR por relay o fallbackv1.

## Caso de uso y guardas

`PrepararPairCp2`: sin dependencia ParentStore ni método commit. Snapshot de alta
explícito incluye pública existente, generación, boot, ausencia coherente y alta
autorizada. Si falta autorización/coherencia, rechaza antes de códigos/firma.
Snapshot es autoridad del llamador, no producto del mensaje. **MAIN no fabrica ni
provee ese snapshot a partir de datos CP1**: la preparación está probada con fakes,
pero todavía no conectada a firma/Keystore/persistencia productivos CP/2.

Nueva preparación descarta anterior. Crea pairId UUIDv4 y3 códigos únicos20bytes
Base32 de32 en RAM;8 intentos máximos de unicidad por código. Hash separado
`SHA256(UTF8("CPv2/RECOVERY\n"+pairId+"\n"+purpose+"\n"+codigoNormalizado))`;
normalización técnica existente sin reutilizar hashCP1. QR solo hashes, nunca códigos.
Ticket conserva payload original mediante copias; recuperación y aceptación con
campos inmutables, códigos privados al flujo mutable para descartarlos. Guardas
recomprueban hashes/códigos, identidad/snapshot/boot/generación/ventana5min/reloj no
retrocedido antes/durante/después de firmar. Huellas/origen/códigos guardados son
confirmaciones obligatorias del llamador, más autenticación vigente antes/durante/
después. Cualquier error de firma/prompt/caducidad descarta ticket/códigos. Resultado
criptográfico no se persiste ni cambia estado; no se reusa ticket firmado.

La clase pura exige que su propietario invoque descartar al salir/fondo/destrucción;
no observa Android por sí sola ni almacena un QR firmado. No está conectada en MAIN,
por lo que MAIN no genera/muestra códigos o aceptación CP/2. Strings JVM no ofrecen
borrado forense; descartar elimina referencias del mapa, no promesa de zeroization.

## Ruta MAIN y límite productivo elegido

En Vinculación, botón **Abrir NUEVO PERFIL CP/2 (sin migrar)** alterna con CP/1 sin
invocar preparación/confirmación legada. OfertaCp2Screen lee el almacén existente
solo para mostrar pública/huella del padre; no inicializa, guarda, crea/rota claves,
limpia pairId o cambia PREPARADO. Cualquier almacén legado o incierto muestra
REQUIERE_MIGRACION; aun ausencia de archivo NO autoriza alta automáticamente.
Firma CP/2 siempre deshabilitada hasta snapshot/alta compatible acordados. Lectura
permitida sin pareja, no se modifica la guarda de Recuperación (`conocida`).

Oferta solo RAM, huella SHA256 del hijo y del padre si pública existente legible.
No contiene ni muestra nonce/códigos/privadas como registro diagnóstico. Advertencia
de oferta no firmada, comparar presencialmente y comprobar ventana/nonce en hijo.
Ventana local de visualización5min no se hace pasar por vigencia del reloj hijo.
Salir/cambio perfil/fondo/destrucción borran oferta mediante lifecycle/disposal;
caducidad borra y nueva lectura requiere nuevo contexto. No rememberSaveable/backup
de preparación. Sin prueba UI runtime de esas transiciones en esta unidad.

CamaraOfertaCp2 reutiliza planoY/QR02/QrLocal existente y CameraX propio. ReaderException
muestra «Sin lectura óptica válida todavía»; tras decodificación con schema/tipo
incorrectos, «QR decodificado, pero no es una oferta CP/2 válida», sin afirmar bytes
exactos ópticos y sin registrar body. Se detiene procesamiento del intento inválido
hasta cerrar/reabrir; no bypassPURE/máscara/inversión ni suprimir checksum. Oferta
válida entrega texto sin normalizar al codec y cierra cámara por ruta existente.
No garantías de lectura universal/latencia/varios candidatos de cámara.

## Pruebas y comandos efectivamente ejecutados

9tests nuevos `PairCp2Test` en test JVM, privadas P256 efímeras RAM/no persistidas:
bytes/hash/copia defensiva; firma CPv2 y pública esperada; dominiosCPv1/otro propósito
rechazados; CP1 rechaza objetosCP2 y CP2 rechaza v1; campos/extras/duplicados/tipos/
canonicidad/UTF8/profundidad/base64 inválidos; tamaños fuera de límite; SPKI curva/
DER extra; origen inválido; DERcero/rango; guardas legacy/incierto/no autorizado,
boot/generación/reloj/TTL/descartar-fondo/cambio oferta/stale-ticket/autenticación;
confirmaciones humanas; dominios de recuperación diferentes; QR bytes de oferta y
aceptación con firmas reales ficticias; oferta pública fija usando SPKI pública
originalQR01, codec canónico y schema copiado. No claves de test estáticas privadas
ni fixture dentro del APK. Fakes no se usan como snapshot productivo.

```powershell
.\gradlew.bat :app-parent:testDebugUnitTest --tests '*PairCp2Test' --console=plain
.\gradlew.bat :core-protocol:test :core-protocol:testInterop :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture --rerun-tasks --console=plain
.\gradlew.bat :app-parent:testDebugUnitTest :app-parent:assembleDebug :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease :app-parent:lintDebug :app-parent:lintRelease --rerun-tasks --console=plain
```

Primero8tests, BUILD SUCCESSFUL (8tareas ejecutadas/17UP-TO-DATE); después9tests
nuevos incluidos en suitefixture: **151app+30núcleo+1interop=182** distintos,29tareas
ejecutadas. Se corrigió warning Kotlin de property Java nullable en test con
requireNotNull antes del commit82734c7; no error ni repetición para obtener verde.
Suite normal final **150app**,131tareas ejecutadas junto builds/lint. Todos sin
fallos/errores/omitidos; no sumar repeticiones ni UP-TO-DATE como nuevas pruebas.
Núcleo/interop se ejecutaron con rerun en segundo comando; no relay/Node externo.
QR02 fixture y P02/P07 originales incluidos; muestreo diagnóstico true no ejecutado.
Lint debug/release0errores/23warnings cada uno, solo avisos de versiones/deps;
advertencia .so sin strip empaquetadas como existentes. Sin deps nuevas.

## APK reales finales y empaquetado dirigido

Tercer comando tras commit código **82734c7**, sin diferencias fuente; cierre
posterior solo documentos. No rebuild previo confundido con hash final:

| Ruta bajo app-parent/build/outputs/apk/ | SHA256 |
|---|---|
| debug/app-parent-debug.apk | 8c919e1ec052f017e47668d4ae07fbe9087cdecc952fe5100f72d7cee247970b |
| release/app-parent-release-unsigned.apk | c39b7c1991a252c62e5fd4bc778a9977a5d8a1997dc6284b1cf1e0af13b645e9 |
| androidTest/debug/app-parent-debug-androidTest.apk | 2074540e73558d22ea5241c108ff432ad125d77a0839a16caa29a0f53931be25 |

No instalados. A56 conserva QR02 anterior confirmado en F01, no este lector CP2.
Release sin firma de distribución. APKtest es separado, compilado/no ejecutado.
Python por stdin, ZIP+tablas DEXclass_defs/type_ids/string_ids: cada APKapp503clases
propias, codec/preparadorCP2 presentes; contraste contra143clases JVMtest compiladas
y nombres C02/diagnóstico/falso:0coincidencias. Marcadores nombre/SPKI/frameB64/frame
originalQR01 y schema en ZIP descomprimido:0. Inspección dirigida, no auditoría
universal de secretos/DEX, firma de distribución o evidencia Keystore/hardware.

## Rutas y cierre

Código/pruebas/config: core-protocol/.../cp2/VinculacionCp2.kt;
app-parent/.../pairing/PrepararPairCp2.kt; ui/OfertaCp2Screen.kt; ui/CamaraOferta.kt;
MainActivity.kt; test/.../PairCp2Test.kt; app-parent/build.gradle.kts. Nuevo schema
separado y README en contrato-cp2/. Documentación: esta evidencia, ESTADO.md y relevo.
Commit82734c7:505altas/3bajas incluyendo53líneas schema congelado copiado; código/
tests/config452altas/3bajas (<500manuales), sin relleno; cierre documental posterior.

No editar compartido/CP1codec/fixtures/planes/ADR/QR02, servidor/hijo/repos externos.
Sin ADB/dispositivos/instalaciones/pruebas físicas/permisosCAMERA/grants/instrumentación/
CA/LAN/TLS/Push/pushgit/despliegue/subagentes/sesiones. NORMALred10s/excepciónOFFLINE/
boot/DNSVPNcancelado quedan contexto sin tocar acciones/STATE/retención/historia/red.

Siguiente decisión solo por asignación: snapshot/persistencia CP2 compatible y
ceremonia coordinada, o autorización física específica de instalación debug y
lectura oferta pública CP2 en A56. F01 no hereda permiso de ceremonia/QR hijo.
Hijo/relay no verificados listos; no usar A13 u otro teléfono para facilitar prueba.
No promover PREPARADO por oferta/huellas/firma propia/ACK. Detenido tras esta entrega.
