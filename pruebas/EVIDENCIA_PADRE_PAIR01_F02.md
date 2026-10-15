# PADRE-PAIR01-F02 — lectura física de oferta CP/2 observada

Fecha: 2026-10-15. Base documental d92a349; APK código0b010d7.
Resultado: **LECTURA_CP2_UI_CONFIRMADA_POR_PROPIETARIO / SIN_VINCULAR**.
No verificación de bytes ópticos exactos ni ceremonia de vinculación completada.

## Autorización y preparación

Autorización específica nueva transmitida por propietario: «sí» a actualizar APK
debug en A56 conservando datos y leer oferta nueva generada manualmente por él
en sesión adulta A13; solo lectura, sin APKtest/nuevas identidades/guardar vínculo/
órdenes. Aviso previo enviado; no heredar autorización física de F01.

Repositorio limpio en d92a349; último código0b010d7. APK existente sin reconstruir:
`app-parent/build/outputs/apk/debug/app-parent-debug.apk`, SHA256 local
`33ef534732cf2ce0e3a3e1acc1ca2afc2b9d98ac5266373d9ba11cac54d4dd7a`.
Se comprobaron git status/log y Get-FileHash; no Gradle/build/tests repetidos.

Primer descubrimiento ADB abortó antes de seleccionar/consultar dispositivo o
instalar: aviso de inicio del daemon local tcp5037 tratado por PowerShell como
NativeCommandError con ErrorActionPreference=Stop. Se informó al propietario.
Una comprobación posterior de descubrimiento confirmó un único A56; no se repitió
instalación por reinicio del servicio: la primera aún no había ocurrido.

Luego, con selector explícito A56 en cada operación, modelo propio confirmado;
`pm path` y SHA256 de base.apk propio antes/después. Una sola actualización
`install -r` devolvió Success, sin -t, uninstall/clear-data/downgrade/resign/reintentos.
Hash previo: `7f3947394c5bb921a11539a66ed7162029818702d0fc1a3181c53a5b55d6f534`.
Hash instalado: mismo33ef5347…d4dd7a del artefacto autorizado.
No lectura de ParentStore/identidad/privadas ni cambio deliberado de sus datos;
preservación mediante -r, no auditoría del contenido interno del almacén.

## Ensayo guiado y observaciones humanas

Ruta manual A56: app padre → Configurar padre → Vinculación →
Abrir NUEVO PERFIL CP/2 (sin migrar) → Escanear oferta con cámara.
No Recuperación ni retirada de su guarda de pareja conocida. Se indicó no generar
QR en A13 hasta que el lector estuviera listo; propietario eligió «Sí, cámara lista».
Entonces se entregó explícitamente **LECTOR_LISTO** y se pidió generar oferta nueva
en sesión adulta A13, identidad existente, códigos solo localmente y sin enviarlos.

Presupuesto indicado: un intento de hasta60s, sin reutilizar QR/sesión caducados.
El propietario preguntó «cual oferta?»; se aclaró QR de vinculación CP/2, no wifi
ni recuperación de contraseña. Después informó «ya lo estoy haciendo». No hubo
oferta anterior leída ni solicitud de renovar ventana/replay para obtener éxito.
No se midió duración real del escaneo ni vigencia del reloj hijo; este detalle es
instrucción del ensayo, no prueba instrumental de que se cumpliera un deadline.

Al preguntar si aparecían «Oferta CP/2 leída» y huella del hijo, respondió **«si aparece»**.
Esto es confirmación manual de UI tras la ruta de cámara/schema CP/2: no captura,
payload, pública, nonce, huella ni código recogidos. Sin lectura/verificación de
firma de oferta (CHILD_OFFER no está firmada). No cotejo de bytes ópticos exactos
contra emisor ni comparación humana de ambas identidades acreditados.

REQUIERE_MIGRACION se explicó como esperado del estado legado y no fallo óptico;
no se pidió ni recibió confirmación independiente de ese mensaje o del estado del
botón de firma. Firma MAIN permanece bloqueada por implementación previa intacta,
no se ejecutó preparación/códigos/firma/commit. No promoción PREPARADO o declaración
de hijo operativo, recuperación, consumo, enforcement, WSS o APPLIED.

## Cierre observado y comprobación final

Se solicitó retirar QR/cerrar sesión adulta A13 y cerrar cámara/perfil CP2 A56,
sin firmar/guardar/enviar. Propietario eligió **«Cerrado, sin permiso nuevo»**:
cierre manual confirmado, sin nuevo CAMERA concedido. No se consultó permiso por
ADB ni se otorgaron grants/appops. El permiso que ya existía no se modificó.

Solo comprobación final propia A56: único modelo anunciado, getprop modelo solo
seleccionado, pm path/hash del paquete. SHA256 final instalado completo:
`33ef534732cf2ce0e3a3e1acc1ca2afc2b9d98ac5266373d9ba11cac54d4dd7a`.
Sin segunda instalación, cierre forzado, navegación ADB o lectura de almacenes.

## Comandos realmente usados y límites

Se ejecutaron bloques PowerShell con estas operaciones (selector/ruta de instalación
no se guardan; placeholders aquí, no comandos contra todos los teléfonos):

```powershell
git status --short
git log -2 --format="%h %s"
Get-FileHash app-parent/build/outputs/apk/debug/app-parent-debug.apk -Algorithm SHA256
# Descubrimiento HOST capturado en memoria y resumido sin seriales; primer bloque abortó.
adb devices -l
# Todos los comandos de DISPOSITIVO con el único A56 y selector explícito:
adb -s <A56> shell getprop ro.product.model
adb -s <A56> shell pm path dev.controlparental.parent
adb -s <A56> shell sha256sum <base.apk-propio>
adb -s <A56> install -r <APK-debug-autorizado>
# pm path/hash después de instalar, y modelo/pm path/hash en comprobación final.
git diff --check
```

Seriales/rutas base.apk y descubrimiento no publicados/guardados; ningún comando de
dispositivo A13 u otro teléfono. A13 solo acciones manuales del propietario según
autorización, sin consultar repo/app/store hijo. Sin logcat/screenshots/UI dumps/
frames/payloads/privadas/códigos, testAPK/instrumentación/connectedtests, nuevas
identidades/perfiles persistidos, órdenes/red/router/radios/TLS/CA/Owner/keyguard/
enforcement/boot/protecciones/reset. Sin otros repos/subagentes/sesiones/push/deploy.

Solo documentación propia modificada: esta evidencia, ESTADO.md y relevo. No código,
compilaciones o tests nuevos: las suites AUTH01 siguen antecedentes, no reejecutadas.
PAIR01 snapshotMAIN/persistencia/ceremonia y AUTH01 conexión real siguen pendientes.
Detener tras resultado y commit propio; ninguna siguiente tarea automática ni
permiso adicional para repetir instalación/prueba por este cierre.
