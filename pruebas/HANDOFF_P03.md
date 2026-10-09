# Relevo — P03

P03 completada localmente, Android pendiente. Primer bloque `a20f025`; cierre en
commit posterior. Se ejecutó antes de P02 por PLAN_PADRE: P02 depende de P03.
No se inició QR ni cambió CP/1. No hubo ADB en este turno; no tocar A13.

## Interfaces

data/ParentStore: actor/coroutine única, inicialización explícita, leer/existe,
reservar/cancelar intención, guardarComandoFirmado, marcarEnviada y retenerEvidencia.
MetadatosPadre/PendingIntentRecord: formato local schemaVersion 1 y tres etapas.
AlmacenPadreAndroid: singleton por proceso sobre parent-state.enc en noBackupFilesDir.
ArchivoAtomicoMetadatos: AtomicFile oficial y lectura posterior; no abrir un segundo
escritor del mismo archivo ni usar segundo proceso. Metadatos no conectados a UI.

security/MetadataKeyStore y CifradoMetadatos: AES-256-GCM, alias distinto del de
firma, nonce generado por Cipher y cabecera autenticada. No claves exportables,
privadas/códigos en JSON ni generación de clave al descifrar. Archivo/clave dañados
fallan cerrados; no arreglar borrando/reinicializando. No restore automático de
identidad. Migraciones de formato local deben ser explícitas antes de cambiar DTO.

Reserva max(local,remotaVerificada)+1 y única intención juntas. P05 debe validar
STATE/frescura/contexto antes de aportar secuencia remota. Cancelar no retrocede
contador ni borra evidencia. Frames conservan bytes y se verifican contra claves
almacenadas y reserva. Almacenar evidencia no acredita frescura ni APPLIED. No
hay reenvío automático tras leer una intención enviada-sin-confirmar.

## Evidencia y siguiente paso

scripts/verificar.ps1 y repetición Gradle --rerun-tasks correctos: 33 app, 30 núcleo,
1 interoperabilidad y 30 Node, total 94 locales. APK debug/instrumentado/release
sin firma de distribución compilados; lint cero errores/15 avisos. Inspección
limitada ZIP/DEX no encuentra los fixtures/dobles/tests examinados en app.
Detalles y límites en pruebas/EVIDENCIA_P03.md.

Tres tests AlmacenAndroidTest compilados, pendientes de A56: cifrado real/reserva,
.new interrumpido y corrupción. Solo alias/directorio temporal propio; cleanup
limitado, no borrado de datos ni claves operativas. Avisar/esperar permiso antes
de instalar nuevos APK y ejecutarlos. Usar selector explícito A56, nunca connected
contra todos los dispositivos. No requiere actuación de huella/PIN.

Próxima tarea de desarrollo: P02, leyendo PLAN_PADRE P02, CP/1 §5/§9, SPEC-P
P3.2/P4. Integrar almacén explícitamente con vínculo, sin autoenrolamiento al
faltar datos. P01 tiene huella/PIN/caducidad/cancelación/persistencia comprobadas
en A56; invalidación permanente y revisión visual ampliada siguen pendientes.
