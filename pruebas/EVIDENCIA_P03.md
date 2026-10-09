# Evidencia P03 — archivo cifrado e intención única

Primer bloque `a20f025`; cierre en el commit posterior. **Completada localmente**,
pendiente de pruebas AtomicFile/Keystore Android. Se eligió P03 antes de P02 por
su dependencia explícita en PLAN_PADRE. CP/1/esquema/fixtures intactos.

## Implementación y límites

ParentStore serializa lectura/operaciones en una única coroutine con cola acotada.
AlmacenPadreAndroid ofrece una instancia por proceso, archivo en noBackupFilesDir.
AtomicFile usa startWrite/finishWrite/failWrite y comprueba lectura posterior;
no aporta exclusión entre escritores por sí mismo. No usar otro proceso/instancia
escribiendo el mismo archivo. No hay sockets, firma de nuevas órdenes ni reenvío.

Cifrado AES-256-GCM estándar, alias Keystore cp.parent.metadata.v1 distinto de
cp.parent.sign.v1, nonce de 12 bytes generado por el proveedor en cada cifrado y
cabecera CPSTORE1 autenticada con AAD. La clave no se exporta. Leer/descifrar no
genera claves ausentes ni restaura identidad tras reinstalación. Datos corruptos,
versiones desconocidas y escrituras inciertas bloquean la instancia; inicializar
es explícito y no sobrescribe datos existentes. No se crea archivo al abrir la UI.

Formato privado versionado: alias, públicas, pareja/origen/estado de vínculo,
contador, única intención y último frame de evidencia. No contiene privada,
códigos de recuperación ni historial. JSON desconocido/duplicado/no canónico se
rechaza; tamaños y formatos acotados. Cambios futuros requieren migración local
explícita, no borrar/reinicializar para eludir datos incompatibles.

Reserva persiste max(contador local, secuencia remota verificada)+1 y la intención
en el mismo documento. Secuencia agotada bloquea sin volver a cero. P05 debe
aportar secuencia desde STATE autenticado/correlacionado, nunca presencia relay.
Solo una intención; cancelar consume la secuencia y preserva evidencia. Reservada,
firmada y enviada-sin-confirmar son etapas distintas, ninguna acredita APPLIED.
Se verifica firma/contexto de pareja y coincidencia con reserva al guardar frame,
y se retienen sus bytes originales. Boot/sesión/nonce fresco y autorización siguen
siendo responsabilidad del caso de uso P05. Retener evidencia no confirma ni
elimina una intención; el almacén no interpreta frescura/aplicación remotas.

## Pruebas ejecutadas

`scripts/verificar.ps1` correcto. Gradle tests app/núcleo/interoperabilidad, APK
debug/instrumentado/release y lint repetidos con --rerun-tasks: 130 tareas correctas.
XML: 33 app, 30 núcleo y 1 interoperabilidad, sin fallos/errores. Node: 30 correctos.
**94 pruebas locales aprobadas**; diez vectores y 78 comprobaciones documentales.
Lint cero errores/15 avisos de versiones. Release sin firma de distribución
compilada, no entrega operativa aprobada. npm reportó cero vulnerabilidades y
aviso de postinstall de esbuild; no se alteraron permisos npm.

15 tests nuevos: cuatro AES-GCM y once ParentStore. Comprueban nonces distintos,
alteraciones/tag, clave distinta/ausente, reserva/cancelación/cierre, fallos antes
y después de commit, doce solicitudes concurrentes (solo una reserva), cancelación
del solicitante tras commit, agotamiento/entrada inválida, corrupción/versiones/
duplicados, comando firmado exacto y conservación de evidencia al cancelar.
Los fallos de escritura y reinstalación son simulaciones JVM, no cortes físicos.
Si falla antes de commit no se devolvió reserva para enviar; si reserva tuvo éxito
y ocurre cierre antes de envío, permanece consumida y no admite otra intención.

Inspección limitada ZIP/DEX app debug/release no encuentra fixture ni ArchivoFalso,
ParentStoreTest, CifradoMetadatosTest, AlmacenAndroidTest o alias temporales de tests.
No reemplaza análisis de seguridad completo. Dobles/privadas efímeras solo en tests.

## Pendientes

AlmacenAndroidTest: tres casos compilados, no ejecutados. Usan archivos y alias
temporales exclusivos: AES/Keystore/AtomicFile, .new sin finishWrite, corrupción
cifrada. .new interrumpido no simula un corte eléctrico del teléfono. Preparados
para A56 tras aviso/permiso nuevo; no requieren biometría ni cambian credenciales.
Hay 17 tests instrumentados compilados; no reinterpretar ejecuciones antiguas
de P01 como ejecución de este APK nuevo.

No se usó ADB, teléfono, Render ni otro repositorio en P03. No tocar A13.
P-STORE-01 queda validada localmente, Android pendiente. UI no integrada aún;
P02 y P05 deben conectar almacenamiento, inicialización/vínculo y casos de uso.
No se implementó QR ni se habilitaron órdenes. G0–G4 e integración reales pendientes.
