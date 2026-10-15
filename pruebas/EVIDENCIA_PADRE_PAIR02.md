# PADRE-PAIR02 — preparación/firma MAIN y área pública CP/2

2026-10-15. Base2d1d6e6. Código **caf4333**, **371b07d**, ajusteUI **2511353**.
Resultado: **MAIN_PAIR_CP2_IMPLEMENTADO_PROBADO_LOCAL / PREPARADO_FISICO**.
Sin ADB/instalación, ceremonia física, confirmación hijo o vínculo operativo.

## Rama productiva y autoridad

MAIN inyecta VincularCp2 con ParentStore real, áreaCP2 Android, pública existente,
SesionAutoridad y AutoridadPadre.firmarPairCp2. Selector explícito NUEVO PERFILCP2,
sin fakes/flags desde QR. En CP2 no presenta crear identidad ni llama crearSiFalta,
rota o exporta privadas. Autenticación oficial LocalAuthentication existente.
Preparar requiere confirmación concreta de nuevo enrolamiento y sesión vigente;
después valida identidad y legado reales. Solo SIN_VINCULAR **con metadatos
existentes coherentes**, o PREPARADO con aceptaciónCP1 verificable, contador0 y sin
evidencia/pendiente/intención. VINCULADO/REQUIERE_RECUPERACION, identidad distinta,
archivo ausente/corrupto/incierto o preparación incompleta bloquean con causa segura.
No se comprobó almacén actual A56: puede resultar legítimamente bloqueado al probar.

ParentStore añade inspeccionarSinMigrar: lectura/validación sin escribir migración
privadav1/v2→v3. leer() de llamadoresCP1 conserva comportamiento previo. Hash de
representación validada completa ancla snapshotCP2 y se recomprueba antes/después
de persistir. No sobrescribir archivo/aceptación/hashes/secuencias/evidencia legados,
convertir hashesv1 a v2 ni migrar consumos/datos hijo. SnapshotAltaCp2 deriva de esas
guardas+consentimiento+auth, pública real/hash legado/marca instanciaRAM (no bootremoto).

Fuentes: schema/READMEPAIR01/AUTH01 íntegros recibidos en conversación y draft75eb56e§10.
No otros repos. Schemas/codecsCP1/CP2 sin cambios. Fuente íntegra mantiene6oferta/
**10aceptación**/5sobre (corrección documental INTEROP01:11era error de conteo, no de codec/schema). Reportes
HijoPAIR02/Render133tests no ejecutados aquí ni evidencia de compatibilidad física.

## Ceremonia local, códigos y cancelación

UI: leeroferta→autenticar→autorizaralta→preparar→guardar3códigos→compararhuellas/
origenexacto→firmar. PrepararPairCp2 reutilizado; TTLmáximo5min desde primera lectura,
no renovado al generar códigos. El hijo mantiene ventana/nonce/sesión propios.
3códigos nuevos20bytes/Base32de32 y hashCPv2RECOVERY, soloRAM/pantalla; nunca logs,
chat, QRpayload, archivo o backup. No códigoscompuestos/pregunta ni promesa de borrar
forensemente Strings. Huella padre mostrada se actualiza desde ticket preparado.

AutoridadPadre.firmarPairCp2 exige prefijoCPv2/PAIR+LFreal, payloadcanónicoPAIR,
públicaesperada=Keystore y sesión antes/después; error invalida sesión. Preparador
verifica bytes/firma reales. QRLocal comprueba capacidad antes de persistir. QR solo
retorna tras escritura/readback/verificación y recomprobar identidad/contexto/TTL/auth.
Firma propia/QR/persistencia/AUTH_OK nunca confirman hijo/ACTIVO/APPLIED.

Cancelar, oferta/origen/consentimiento nuevos, salida/cambio perfil, fondo, fallo de
prompt/firma o vencimiento invalidan ticket/códigos/QR; no autogenerar al resume.
Cambio perfil invalida sesión/preparación. CP1 creación/preparación queda bloqueada
si áreaCP2ocupada/incierta, incluso fallo al consultarla; no se borra legado ni se
quita guarda de Recuperación. Códigos/QR no se restauran de archivo.

Si cancela/fondo durante escritura ya iniciada, esta termina NonCancellable bajo
mutex/readback: puede quedar pendiente público, **sin entregar QR**. Se conserva
para reconciliar, no se borra/reemplaza por error. Reabrir valida y muestra etapa,
no reconstruye códigos ni QR históricos; pendiente existente bloquea otra ceremonia
incluso con nueva auth. Falta reconciliación acordada, no inventar recibo para reabrir.

## Persistencia separada, pública y verificada

PairStoreCp2/AlmacenPairCp2Android: escritor por proceso/mutex en
`noBackupFilesDir/pair-cp2-public-v1.json`, ArchivoAtomicoMetadatos/AtomicFile existente
(finishWrite/failWrite/readback), sin criptografía nueva. Documento solo público:
schemaVersion1, protocolcp.v2, etapaPENDIENTE_CONFIRMACION_HIJO, legadoSha256B64,
frameB64 original firmado, payloadSha256B64. Snapshot de pareja/públicas/origen/hashes
CP2 está dentro del payload firmado íntegro, sin copias con autoridad independiente.
Sin secretos para restaurar visualización, sin tocar backupglobal (allowBackupfalse).

Carga≤32768bytes: JSON privado exacto/roundtrip sin extras/duplicados, versión/perfil/
etapa, base64/tamaño, firmaCPv2PAIR con pública real externa, hashpayload/ancla legado.
Corrupción/readback/desaparición vista en proceso latchan incierto, no alta nueva.
Ausencia en arranque nuevo no acredita que nunca existió: sin promesa antirollback/
antiborrado forense. Legadoausente bloquea siempre. Export público solo snapshotCP2
pendiente validado, con CP2_STAGE y perfil: no configuración ACTIVE ni activarWSS.
No transacción conjunta de dos archivos: CP1 nunca se escribe aquí; cambio concurrente
se detecta por ancla/relecturas y bloquea exposición/carga. Sin tolerancia a corte
físico acreditada ni coordinación multiproceso afirmada.

## Pruebas y comandos ejecutados

14tests JVM nuevos PairIntegradoCp2Test: PREPARADO exacto preservado/sin escribir,
SIN_VINCULAR existente; ACTIVO/recuperación/incompleto/contador/ausente/corrupto/
identidad/auth/consentimiento bloqueados; TTLoriginal/cancelación/confirmaciones/
staleticket; cambiolegado; escriturafallida/readback/reintento; versión/perfil/etapa/
hash/frame/JSON corruptos; reinicio sin nueva ceremonia; doblependiente/desaparición;
cancelación durantecommit sinQR; inspecciónv2 sin migrar; firmante real inyectado
con P256efímeraRAM sin crear identidad. PAIR01/AUTH01/QR02/P02/P07 originales incluidos.
No privadas/códigos reales persistidos o logs.

Nuevo PairStoreCp2AndroidTest **solo compilado**: archivo propio temporal noBackup,
AtomicFile/readback/reapertura/.newinterrumpido/corrupción, firma efímeraRAM sin nuevo
Keystore. No ejecutado en teléfono. UI/prompt/Keystore/AtomicFileAndroid sin prueba
runtime o física en esta unidad.

```powershell
# 1.12integración+9PAIR01=21tests; warningJson corregido después.
.\gradlew.bat :app-parent:testDebugUnitTest --tests '*PairIntegradoCp2Test' --tests '*PairCp2Test' --console=plain
# 2.14integración+9PAIR01=23tests, y compilar APKtest con AtomicFileaislado.
.\gradlew.bat :app-parent:testDebugUnitTest --tests '*PairIntegradoCp2Test' --tests '*PairCp2Test' :app-parent:assembleDebugAndroidTest --console=plain
# 3.Suitefixture, fuentes posteriormente comprometidas371b07d.
.\gradlew.bat :core-protocol:test :core-protocol:testInterop :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture --rerun-tasks --console=plain
# 4.Normal/builds/lint en371b07d.
.\gradlew.bat :app-parent:testDebugUnitTest :app-parent:assembleDebug :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease :app-parent:lintDebug :app-parent:lintRelease --rerun-tasks --console=plain
# 5.AjusteUI2511353: normal/builds/lint por cambio real, sin rerun forzado.
.\gradlew.bat :app-parent:testDebugUnitTest :app-parent:assembleDebug :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease :app-parent:lintDebug :app-parent:lintRelease --console=plain
```

TodosBUILD SUCCESSFUL; ejecutadas/UP-TO-DATE:1)5/20;2)9/45;3)29/0;4)131/0;5)30/101.
Fixture **177app+30core+1interop=208distintos** ejecutados en3; ajusteUI posterior
actualiza huellaticket/fallo cerrado consultaárea, sin codec/casouso/tests cambiados.
Normalfinal **176app** en5 ejecutado,0fallos/errores/omitidos. No sumar repeticiones/
UP-TO-DATE; núcleo/interop sí ejecutados en3. APKAndroidTest5 UP-TO-DATE, compilado/
empaquetado4, no «prueba física pasada». LintAnalyze5 ejecutado; reportes idénticos
UP-TO-DATE:0errores/23warningsversiones por variante; .so sinstrip advertidas en4.
Sin nuevas deps, muestreoQRtrue, relay o testsAndroid ejecutados.

## APK reales finales e inspección

Appdebug/release reconstruidos HEAD **2511353**, cierre posterior solo docs;
APKtest reutilizado de build4/371b07d sin cambio de clases. Hashes finales medidos
bajo app-parent/build/outputs/apk/:

| Ruta | SHA256 |
|---|---|
| debug/app-parent-debug.apk | 30780c682665832da6337ef62075fc9499b5de4d7c2aceb17a98d2ddc19e8af8 |
| release/app-parent-release-unsigned.apk | d4c19fa055d2cd01db4d449489d8b662e4f00a8196abd64943475773746e0cf3 |
| androidTest/debug/app-parent-debug-androidTest.apk | 1fdddce7fbc492fc416f38489d030baa8035b653f50444bbdd9f710a2eecf3ed |

Release sin firma distribución. NO instalados; A56 conserva AUTH01verificadoF02,
no esta preparaciónMAIN. No atribuir hash a APK previo no medido. InspecciónPython/
ZIP/tablasDEX de APKapp:568clasespropias,4controller/store/adaptador/autoridad presentes.
Contra235clases JVM+AndroidTest y nombresfalso/C02/diagnósticos:0coincidencias.
MarcadoresfixtureQR01(nombre/SPKI/frame) y vectorescrypto:0. Inspección dirigida,
no auditoría universal de secretos/DEX/hardware/certificados. APKtest separado.

## Rutas y cierre

Código data/{ParentStore.kt,PairStoreCp2.kt,AlmacenPairCp2Android.kt}; pairing/
{PrepararPairCp2.kt,VincularCp2.kt}; security/AutoridadPadre.kt; ui/OfertaCp2Screen.kt;
MainActivity.kt; test/PairIntegradoCp2Test.kt; androidTest/PairStoreCp2AndroidTest.kt.
Docs: esta evidencia/ESTADO.md/relevo/contrato-cp2/README.md. Bloques comprometidos
321líneas+/- primero,281segundo,6ajusteUI (<500manuales cada uno), sin relleno.
Revisión de diffs/preparado; schemas/compartido/fixtures/planes/ADR/AUTHnetwork intactos.

Sin ADB/install/A56/A13/QRfísico/Owner/radios/PIN/reset/otrosrepos/subagentes/sesiones/
push/despliegue/órdenes. F02cerrada; no heredar permisos. Falta ramaA56 con permiso
nuevo y snapshotreal compatible, aceptación local hijo/recuperación/reconciliación
de pendiente antes de remoto. No iniciar automáticamente esa prueba ni nueva unidad,
promover ACTIVO/operativo o inventar recibo. Detenido tras entrega.
