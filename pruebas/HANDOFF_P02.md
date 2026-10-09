# Relevo — P02 parcial local

## Seguimiento CameraX (prioritario)

Cámara directa implementada localmente, 81 tests locales correctos. CamaraOferta
pide CAMERA tras botón explícito, usa preview trasero/analizador plano Y + ZXing
offline y solo entrega CHILD_OFFER estricto. No fotos/archivos/red. Al pausar
cierra UI y al desmontar limpia/cierra proxies/desasocia casos propios. Hardware
opcional; permiso/cámara fallida mantiene imagen/recuperación. No autoabre al
regresar ni unbindAll. Callback único y descartado fuera de RESUMED/efecto activo.

LuminanciaQr acota plano/strides/posición, submuestrea si >2048; cinco tests
sintéticos correctos, incluidos cuatro giros QR. Debug/instrumentado/release sin
firma compilados, lint cero errores/20 avisos de versiones. No ADB este turno;
los 25 instrumentados están recompilados, no se ejecutaron en APK nuevo.

Próximo: avisar/esperar permiso para actualizar ambos APK y probar cámara real
en A56, con QR público de prueba en pantalla PC, sin preparar vínculo cotidiano.
Aceptar/denegar permiso, lectura, cancelar/cerrar, giro/salida/fondo y no reapertura
automática. No cambiar permisos por ADB sin acuerdo expreso. Si se autoriza,
regresión de diez tests aislados. A13 excluido. No afirmar cámara aprobada por
lectura sintética de luminancia. PAIR con autoridad real, manual texto grande/
FLAG_SECURE y aceptación APK hijo pendientes. Cliente WSS/órdenes también faltan.
No modificar proyecto servidor ni lanzar IA: propietario coordinará después,
con conexiones locales probadas antes de desplegar.

## Último resultado físico (prioritario)

Con permiso nuevo se actualizaron ambos APK y ejecutaron solo las tres clases
PantallaInicialTest/PantallaVinculoTest/VinculoPersistidoAndroidTest en A56:
diez aprobados, cero fallos/omisiones, 15.071 s. Cleanup temporal completado,
sin identidad/vínculo operativo ni cambios de credenciales. A13 sin operaciones.
No repetir por pendientes históricos siguientes; no se ejecutaron otros 15 tests.

Migración v1/persistencia QR/guardas UI/segundo plano/recuperación/FLAG_SECURE
(flag solamente) comprobados. Firma JCA de test no acredita PAIR_ACCEPT
autenticado con Keystore de autoridad. Próximo desarrollo P02: cámara directa
CameraX/local de SPEC-P P4. Pendientes manuales: selector offline/rotación/texto
grande/captura real bloqueada, aceptación con autoridad e integración hijo real.
Nuevo acceso físico necesita aviso/permiso. App no lista para integración final.

## Seguimiento más reciente (prioritario)

Se retoma P02 después de separar servidor. 76 tests locales correctos, APK
debug/instrumentado compilados y lint correcto. No ADB en este turno. UI descarta
preparación/códigos/casillas al cambiar oferta/origen y entrar/salir de selector
según ciclo; guardia síncrona contra operaciones dobles. PairChild no firma si
preparación fue descartada o códigos no corresponden a hashes/pareja.

Próximo físico: avisar/esperar permiso, instalar APK nuevos y ejecutar únicamente
PantallaInicialTest, PantallaVinculoTest y VinculoPersistidoAndroidTest (diez
casos). 25 tests instrumentados disponibles; no probar todos ni AutenticacionRealTest
sin acuerdo adicional. A56 explícito, A13 excluido. No requiere huella/PIN.
EntornoVinculoAndroid usa almacén AES/archivo temporal real y firmante JCA efímero
solo en tests; no acredita firma autenticada/Keystore de autoridad. No crea
identidad/vínculo cotidiano ni cambia credenciales. Cleanup exacto del alias y
directorio temporal; no usar borrado general ni modificar cp.parent.*.

Probar diez casos compilados no significa que hayan pasado: UI casillas/firma
fallida/fondo/cambio de entrada/salida, QR persistido, migración v1 con reserva
y nonce nuevo, recorrido actualizado y FLAG_SECURE como flag de ventana.
Cámara directa CameraX sigue pendiente (SPEC-P P4); imagen no la reemplaza en
aceptación final. Revisión manual selector/rotación/texto grande/ciclo y aceptación
APK real hijo pendientes. Resultados físicos antiguos siguientes no aprueban
este APK nuevo ni migración.

Acuerdo: avisar al propietario al completar padre preparado para integración,
para que orqueste IA en parental_render; comunicación local antes de desplegar.
No iniciar agentes ni modificar servidor en sesiones padre. Cliente WSS/órdenes
siguen pendientes, no avisar como si app estuviese terminada.

## Actualización A56

App padre y APK instrumentado actualizados con -r con permiso expreso. Solo
ImagenQrAndroidTest y AlmacenAndroidTest: cuatro aprobados, cero fallos/omisiones,
1.226 s. PNG/lector Android y regresión AES/Keystore/AtomicFile actuales correctos.
Cleanup temporal completado. No operaciones A13, cambio de credenciales ni
vínculo operativo. No repetir por pendientes históricos siguientes.

Pendientes: revisión manual selector/pantalla/prompt/FLAG_SECURE/ciclo de vida,
migración v1 física y aceptación aislada con aliases de test. No se desactivó red
ni se probó selector offline. No se ejecutaron otros 14 tests ni comunicación
real con hijo/Render. Nuevo acceso dispositivo requiere aviso/permiso.

Leer ESTADO.md/EVIDENCIA_P02.md. Primer bloque `65b9184`; cierre en commit
posterior. Sin ADB/push/despliegue ni cambios CP/1; A13 excluido. Solo P02.

## Componentes

pairing/PairChild, RecoveryCodeGenerator, QrLocal (ZXing 3.5.3), ImagenQrAndroid;
ui/PairingScreen en Vincular. MainActivity inyecta almacén/autoridad y FLAG_SECURE.
Selector oficial de imagen, no cámara directa. Códigos solo memoria, descartados
al salir/segundo plano/rotación, nunca savedState. Huellas completas, casillas de
comparación/guardado y firma PAIR protegida. QR/env pública recuperables tras cierre.

ParentStore.prepararVinculo(frame) verifica con pública almacenada y persiste
PREPARADO/frame original antes de mostrar. Solo desde SIN_VINCULAR; no finaliza
vínculo ni permite cambio de claves. Formato local v2 añade pairAcceptanceFrameB64;
migración v1 explícita conserva contador/intención/evidencia. Corruptos/extraños
fallan cerrados, sin borrar datos/reinicializar. CP/1 congelado intacto.

Autoridad requiere identidad cargada/creada explícitamente y auth vigente. No
inventar plazo CHILD_OFFER: hijo verifica su contexto local. Exportación copiable
solo cuatro campos públicos CP/1, no instala Render ni guarda códigos/privadas.

## Próximo paso

99 tests locales correctos: cinco PairChildTest nuevos (QR offline/hash/env/firma,
migración y negativos en simulador). No demuestran APK hijo. Gradle repetido
--rerun-tasks correcto, debug/instrumentado/release sin firma compilados; lint
cero errores/16 avisos. ImagenQrAndroidTest nuevo compilado/no ejecutado,
18 instrumentados disponibles. Pruebas físicas P03 anteriores eran formato v1.

Avisar/esperar permiso nuevo para instalar en A56 y ejecutar ImagenQrAndroidTest
y regresión AlmacenAndroidTest con selector explícito. Solo archivos/alias
temporales. No crear vínculo operativo ficticio ni tocar A13. Preparar pruebas
aisladas de pantalla/firma con aliases de test antes de aceptación completa.
Revisión manual de selector/prompt/huellas/códigos/rotación/texto grande/segundo
plano/FLAG_SECURE/env pública/recuperación accesible pendiente.

C-PAIR-01 hijo real pendiente para integración futura autorizada; no abrir repo
del otro rol ni cambiar CP/1. PREPARADO debe permanecer así hasta estado firmado
y recuperación local probada. No habilitar órdenes para aparentar éxito.
