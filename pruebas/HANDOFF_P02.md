# Relevo — P02 parcial local

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
