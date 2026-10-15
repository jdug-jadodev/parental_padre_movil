# PADRE-INTEROP01 — vector Node verificado y vuelta JVM publicada

2026-10-15. Base35928e5. Resultado: **PUBLICAR_VECTOR_JVM_Y_VERIFICO_NODE**.
Significa: JVM propio verifica el vector Node recibido y publica una salida propia;
**Node/relay todavía no han verificado esa salida aquí**. Sin teléfonos o despliegue.

## Procedencia y hashes exactos

VectorCP2-I01 suministrado íntegro en conversación, generación Node24 en Render y
privadas soloRAM reportadas por emisor; no lectura/ejecución de otros repos ni
verificación independiente de aquel proceso. Copia exacta UTF8+LFfinal:
`core-protocol/src/test/resources/cp2_node_publico.json`, SHA256
`967ae05aecd28c6b7f90b8ae90e8421b408dd70acbf22fd30911085c69a964ec`.

Única salida nueva generada en proceso JVM local efímero, SHA256UTF8+LFfinal:
`pruebas/interop_cp2_jvm_publica.json`
`fb830c79349479eca0e7ee5392c05d5a32ca62157af7e00fd1d890dcf2c65d4b`.
No regeneration/rerun para obtener una firma legible/verde. Herramienta usa
CREATE_NEW y rechaza sobrescritura; ejecutada una vez. Tras generación se corrigió
warning de instanciaJson en código de test, **sin ejecutar otra vez el generador**.
Los tests pinan ambos hashes y verifican las mismas muestras guardadas.

Ambos documentos tienen exactamente8cadenas: fixture, parentPublicKeyB64,
childPublicKeyB64, childOfferUtf8, pairFrameUtf8, pairPayloadSha256B64,
authParentFrameUtf8 y authChildFrameUtf8. MarkerCP2_PUBLICO_EFIMERO_SIN_AUTORIDAD.
Públicas P256SPKI, UUIDv4, nonce32base64url, payloads/frame canónicos ASCII exactos;
origen https://relay.example:443. PAIR liga misma oferta/enrollId/enrollNonce y
ambosAUTH mismo pairId, con firma de la clave efímera de cada rol. Tres hashes de
recuperación aleatorios32bytes ficticios, no códigosadultos/credenciales reales.
Privadas JVM solo variablesRAM del proceso terminado, nunca serializadas/PEM/log;
sin identidadesKeystore ni promesa de borrado forense de memoria.

## Verificación con codecs reales y negativos

4tests nuevos `core-protocol/.../cp2/InteropCp2Test.kt` y helper test exclusivo:
- OfertaNode con VinculacionCp2.leerOferta y bytes exactos de recodificación canónica;
  PAIR con verificar y pública esperada del fixture, nunca autoridad desde payload.
- Copias de frame y payload iguales a bytes originales recibidos/base64decodificados;
  hashSHA256payload declarado igual al calculado. Identidades/enrollId/enrollNonce/
  origen/pareja correlacionados. AUTH PARENT/CHILD con AutenticacionCp2.verificar y
  cada pública/rol esperado, preservando bytes originales y pairId.
- PAIR_ACCEPT **10campos** y conjunto exacto coinciden con schema local required/
  properties; oferta6 y sobre5. Corrección: las menciones a11 en README/relevo/
  evidenciasPAIR01/PAIR02 eran error documental de conteo. Se corrigieron con nota,
  sin modificar evidencia histórica de comandos/resultados, schema ni codec.
- Clave/rol/versionv1, bytes de origen/rol alterados conservando firma, codecCP1
  cruzado rechazan. Firmantes efímeros negativos con CPv1/otropropósito/dobleescape
  son rechazados por los codecs propios; no bypassJCA como verificador alternativo.
- VueltaJVM guardada se carga por archivo/hash y verifica igualmente, sin recrearla.

GenerarVectorPublicoCp2 vive solo en src/test; firma PAIR y ambosAUTH mediante codecs
reales con prefijos LFreal. Autoverifica antes de escribir y solo imprime hash.
No altera producción/CP1/CP2schemas/fixturescongelados/contratos/ParentStore/MAIN.

## Comandos y resultados ejecutados

```powershell
Get-FileHash core-protocol/src/test/resources/cp2_node_publico.json -Algorithm SHA256
.\gradlew.bat :core-protocol:generarVectorCp2Jvm --console=plain
.\gradlew.bat :core-protocol:test --tests '*InteropCp2Test' --console=plain
.\gradlew.bat :core-protocol:test :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture --rerun-tasks --console=plain
```

GeneradorBUILD SUCCESSFUL3tareas ejecutadas/3UP-TO-DATE (autoverificación antes de
escritura, no contar como suiteJUnit). Focal4tests/2ejecutadas/4UP-TO-DATE. Suite
final **34núcleo+177app=211distintos**,28tareas ejecutadas;0fallos/errores/omitidos.
No sumar focal/repeticiones/UP-TO-DATE. P02/P07/QR02/PAIR/AUTH guardas existentes
incluidas. No suite normal separada, APKbuild/lint o instrumentación nuevos.
No `testInterop` históricoCP1 que ejecuta Node: esta unidad se mantuvo JVM sin
Nodeexterno/local invocado. Node→JVM aprobado con fixture; JVM→Node pendiente emisor.

## Exclusión dirigida y artefactos existentes

Inspección Python por stdin (json/hashlib/zipfile), sin log de cuerpos: confirma
8claves/cadenas/marker y LFfinal, ausencia de textoPRIVATE KEY, hashes de ambos
vectores. Es comprobación estructural dirigida, no auditoría universal de secretos.
FuenteNode solo src/test/resources; generador/tests solo src/test/kotlin; vuelta
en pruebas/, que core-protocol expone **solo a test**. Main resources continúa
whitelistprotocolo.schema.json; no nuevo sourceSetAndroidmain/assets.

ZIP/JARmain actual recompuesto por tareasJVM: core-protocol.jar SHA256
`ab471dfe6bd54c8cf8402ef199ee6f88053d264480f98bced2f7d5aa242ae366`.
Sin nombres de vectores/generador/test/helpers/marker ni públicas/sobres de las
dos muestras en entradas descomprimidas:0coincidencias. Inspección de APKapp
**existentes** de PAIR02, no reconstruidos: mismos0marcadores nuevos en ambos.
DebugSHA30780c682665832da6337ef62075fc9499b5de4d7c2aceb17a98d2ddc19e8af8;
releaseunsignedSHA d4c19fa055d2cd01db4d449489d8b662e4f00a8196abd64943475773746e0cf3.
Su falta de marcadores no es un nuevo build con fixtures; exclusión futura se
fundamenta además en sourceSets/whitelist/JARmain comprobados. Sin nueva instalación,
firma de distribución o promesa de auditoría completa APK.

## Cierre

Rutas nuevas: core-protocol/src/test/resources/cp2_node_publico.json;
core-protocol/src/test/kotlin/dev/controlparental/protocol/cp2/{InteropCp2Test.kt,
GenerarVectorPublicoCp2.kt}; pruebas/interop_cp2_jvm_publica.json y esta evidencia.
core-protocol/build.gradle.kts añade tareaJavaExec solo test y ruta a tests.
Docs actualizados: contrato-cp2/README.md, evidenciasPAIR01/PAIR02 (solo conteo),
ESTADO.md y COORDINACION_PROYECTOS.md. Código/tests manuales <500, muestras públicas
generadas/recibidas no cuentan como código manual; commit propio de cierre sin push.

Interop criptográfica no prueba UI/IOAndroid/cámara/consumos/frescura/enrolamiento/
confirmaciones humanas/ACK/APPLIED/identidadreal o autoridad de las muestras.
Sin ADB/A56/A13/APKinstall/QRreal/keysreales/migración/recibos/SDK/servidor/otrosrepos/
subagentes/sesiones/push/despliegue. Contexto hijoPAIR03/RenderCP2T01/testscitados
solo reportado, no ejecutado. F02cerrada/PAIR02sininstalar y pendientes funcionales
siguen igual. Detener tras publicar; comprobación Node de vuelta será posterior,
no atribuida aquí ni iniciada como siguiente tarea automática.
