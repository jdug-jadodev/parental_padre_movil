# Evidencia P02 — vinculación QR parcial local

## Actualización física autorizada — A56

El propietario autorizó actualizar ambos APK y ejecutar pruebas automáticas.
App padre/APK instrumentado instalados con -r correctamente, solo en A56.
Runner filtrado a ImagenQrAndroidTest y AlmacenAndroidTest: **cuatro aprobados,
cero fallos/omisiones**, status 0 cada caso, OK (4 tests), 1.226 s.
Los otros 14 instrumentados no se ejecutaron. A13 conectado, sin operaciones.

Lector Android: PNG QR generado/leído localmente, texto que no es CHILD_OFFER
rechazado por codec, imagen ilegible rechazada. Regresión almacenamiento del
APK actual: nonce nuevo, AES no exportable, reserva recuperada al reabrir,
.new interrumpido conserva datos anteriores y corrupción no sobrescribe.
Cleanup de archivos/alias temporales completado; identidad/vínculo operativos,
datos de app y credenciales sin cambios de prueba. Ambos APK quedan actualizados.

No se desactivó la red ni se abrió selector en modo avión. El test usa archivo
local y lector sin solicitudes remotas; no valida disponibilidad offline de un
proveedor cloud. Tampoco prueba migración v1 real, flujo completo de aceptación,
pantalla/FLAG_SECURE/prompt ni comunicación con otro teléfono/Render.
Esos pendientes siguen vigentes. Los apartados siguientes son el cierre local
anterior; las referencias a imagen/regresión pendientes se resuelven aquí.

Primer bloque `65b9184`; cierre en commit posterior. CP/1 intacto; no ADB,
push/despliegue Render ni cambios del otro repositorio. A13 excluido.

## Implementado

PairChild lee oferta estricta, usa pública de autoridad existente y prepara UUID
de pareja nuevo. No crea identidad durante firma. RecoveryCodeGenerator usa
SecureRandom y tres códigos distintos de 20 bytes Base32 ASCII agrupados;
hash SHA256 con dominio/pareja/propósito exactos CP/1. Solo normaliza guiones,
espacios ASCII y mayúsculas. Códigos solo memoria/pantalla, nunca JSON/env/QR/logs.
Descartar elimina referencias; no se promete borrado físico de String JVM.

Huellas SHA256 completas sobre SPKI decodificado, hex agrupado. Confirmación de
huellas/códigos exigida antes de firmar; el adulto debe comparar también en el hijo.
Firma PAIR mediante AutoridadPadre/Keystore/sesión vigentes. Frame verificado y
capacidad QR comprobados antes de persistir PREPARADO y bytes originales. No
admite segundo vínculo ni órdenes desde este estado. No acredita aceptación real.

Metadatos privados v2 añaden pairAcceptanceFrameB64 (solo firma/públicas/hashes).
Migración v1 explícita: validación JSON canónica/sin duplicados/extraños, conserva
contador/intención/evidencia. Al leer v1 válido escribe v2 con nonce nuevo;
fallo cierra instancia, no borra datos. No cambia protocolo compartido. QR firmado
recuperable al reabrir sin recrear códigos/vínculo. Datos corruptos no autoenrolan.

Pantalla Vincular: selector PickVisualMedia/ZXing local, sin permisos cámara ni
galería completa. Imagen elegida hasta 8 MiB, dimensiones acotadas/submuestreo
a 2048; URI/copia no persistidos. Imagen del teléfono disponible offline, no se
garantiza descarga de proveedores cloud. No hay scanner de cámara directa.
Alternativa de prueba: pegar CHILD_OFFER. QR firmado 768 píxeles con margen.
FLAG_SECURE en actividad; códigos desaparecen al salir/segundo plano/rotación.
Si sucede antes de firmar, preparar/guardar códigos nuevos. Control de generación
impide publicar códigos tras cambio de ciclo durante preparación. Recuperación
permanece visible/accesible fuera del flujo, sin conexión.

Env pública copiable: ACTIVE_PAIR_ID, PARENT_PUBLIC_KEY_B64,
CHILD_PUBLIC_KEY_B64 y PUBLIC_ORIGIN. Origen HTTPS validado por codec, sin ruta,
credenciales/inyección de líneas. No privadas/códigos/credenciales Render,
archivo público ni despliegue. No se marca VINCULADO automáticamente.

## Ejecutado

scripts/verificar.ps1 correcto. Gradle --rerun-tasks de tests app/núcleo/interop,
APK debug/instrumentado/release sin firma de distribución y lint: 130 tareas
correctas. **99 tests locales**: 38 app (cinco P02 nuevos), 30 núcleo,
1 interoperabilidad y 30 Node, sin fallos/errores. Documental: 78 comprobaciones,
diez vectores. Lint cero errores/16 avisos de versiones. APK nuevos no instalados.

Cinco PairChildTest: Base32/normalización/hash por propósito; dos QR offline,
firma/confirmaciones/env sin códigos/reapertura sin revincular; orígenes inseguros,
oferta errónea/autenticación bloqueada; migración v1 conserva reserva/contador;
simulador hijo rechaza no vigente/configurado/clave/enrollId diferentes/propósito
falso/firma de otra clave. El simulador solo expresa criterios de CP/1, no prueba
APK real del otro rol. CHILD_OFFER no tiene timestamp: vigencia debe comprobarla
el hijo con su contexto local, no inventando campos/reloj remoto en el padre.

## Pendiente

ImagenQrAndroidTest compilado/no ejecutado: PNG local y rechazo de imagen ilegible
y texto no oferta, solo archivo temporal propio. 18 tests instrumentados compilados.
Evidencia física P03 anterior no valida migración v2/este APK. Falta regresión Android
y revisión UI/prompt/rotación/texto grande/segundo plano/FLAG_SECURE/imagen en A56
con permiso nuevo. No crear vínculo cotidiano ficticio solo para pruebas.

P-PAIR-01 parcial local; revisión manual pendiente. C-PAIR-01 simulador parcial;
hijo real pendiente. No terminar vínculo sin estado firmado y recuperación local
en banco autorizado. No hay red/órdenes/control reales ni transición a VINCULADO;
no interpretar ACK de relay como aplicación.
