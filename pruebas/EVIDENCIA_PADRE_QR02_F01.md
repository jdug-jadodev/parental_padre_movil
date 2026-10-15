# PADRE-QR02-F01 — APK comprobado; cámara bloqueada por guarda existente

Fecha: 2026-10-14. Base documental94d1746; código QR02ff9aa4c intacto.
Resultado: **APK_DEBUG_INSTALADO_VERIFICADO / CAMARA_NO_EJECUTADA**.
La validación óptica sigue siendo solo sintética; vínculo sigue PREPARADO.

## Autorización y operaciones

El propietario transmitió autorización específica «sí» para actualizar APK app
debug en A56 y probar cámara con fixture público PC. Se avisó antes de operar.
No autorizaba testAPK/instrumentación, altas ficticias, borrar datos o permisos
especiales. A13 excluido, sin consultas de dispositivo contra él.

La primera secuencia ADB descubrió un único modelo A56 anunciado, comprobó modelo
solo con selector explícito, consultó `pm path` del paquete propio y midió SHA256
de su base.apk. Actualización única `install -r`: Success; sin -t, downgrade,
desinstalación ni reintentos. APK local sin atributo testOnly; versión1/0.0.1,
debuggable. Hash antes de aquella instalación:
`a65e8828711f352f45b1d5b23975907a49c7a83779e0d80dd50c63821ea7c41b`.
Hash local y medido después:
`7f3947394c5bb921a11539a66ed7162029818702d0fc1a3181c53a5b55d6f534`.

El propietario cuestionó esa instalación porque creía que el A56 no estaba
conectado ni tenía wifi. Se retiró la atribución a su teléfono y se detuvieron
las operaciones; comprobar modelo no acreditaba por sí solo posesión física.
Después autorizó continuar y confirmó cable conectado. Se resolvió la identidad
física del acceso actual mediante intervención manual, sin inventario guardado:

1. Descubrimiento host: un A56 anunciado, selector sin formato de red.
2. A petición, propietario retiró SOLO cable A56 y confirmó: cero A56 disponibles.
3. Propietario reconectó mismo A56 y desbloqueó: un A56 disponible; modelo propio
   comprobado con selector explícito constante para las consultas de ese bloque.
4. `pm path` propio + `sha256sum` de base.apk: mismo hash QR02 autorizado.
   **No se reinstaló**. Esto demuestra APK actual en el A56 del propietario;
   no demuestra retrospectivamente qué conexión estaba activa en el primer bloque.

Todos los comandos de dispositivo usaron `adb -s` con único A56 seleccionado.
Solo `adb devices -l` para descubrimiento host, capturado en memoria y resumido sin
seriales; no getprop global/A13/inventario de apps ni contenido privado. No lectura
ParentStore/identidad/claves, clear-data, force-stop, reset o cambio de permisos.
Actualización conservadora -r; preservación interna de datos no auditada leyendo
el store. No afirmar identidad/Keystore/vínculo acreditados por hash del APK.

## Fixture PC y observación manual

Original intacto `app-parent/src/test/resources/qr01_respuesta_publica.json`;
SHA256 histórico `4fc43b386a08c35333298371d3c0c33d99500c2161c83c918422f71f9c587c80`.
Se renderizó el frameB64 original con QrLocal.generar compilado de QR02, JVM local
con ZXing3.5.3/Kotlin existentes: PNG768×768 negro/blanco, margen/máscara originales,
sin regenerar firma ni contenido. Java auxiliar y PNG solo en temporal aprobado:
`C:/Users/Usuario/AppData/Local/Temp/opencode/MostrarQr02F01.java` y
`C:/Users/Usuario/AppData/Local/Temp/opencode/qr02_f01_publico.png`.
PNG SHA256 `8c6402524c5350265436600976ace1cd9be3e5436ae01cda383624ddf87acfac`.
Se abrió visor PC con Start-Process; no captura de pantalla ni imágenes de cámara.
Auxiliar no incorporado a repo/APK, sin privadas ni firmas nuevas.

Presupuesto acordado: una exposición hasta60s o lectura identificada, solo fixture
PARENT ficticio, sin ceremonia. **No se inició exposición/preview**: el propietario
eligió «Escáner deshabilitado» inicialmente y, tras confirmar USB/hash, volvió a
confirmar «Está deshabilitado» en Recuperación. No CAMERA nuevo concedido, permiso
existente no consultado/modificado; cámara no abierta por esta prueba.

Fuente propia revisada: RecoveryScreen.kt57–59 determina `conocida` por existencia
de pairId/childPublicKeyB64; línea79 habilita CamaraRetoOffline con `conocida`.
No se leyó store para determinar causa exacta en ejecución. La observación es
compatible con guarda sin pareja conocida, no prueba de corrupción ni defecto QR.
CamaraOferta no está conectada a la pantalla Vincular actual; no se modificó esa UI.
No crear pairId/identidad falsa ni autoParentConfirm para desbloquear cámara.

Además, CamaraOferta.kt89–104 decodifica y luego valida tipo/propósito, ocultando
excepciones de ambos pasos. El fixture PARENT no es reto CHILD; su rechazo sería
correcto. Ausencia de resultado o mensaje genérico no acreditaría bytes ópticos.
En esta sesión ni siquiera hubo preview: **sin lectura óptica, bytes exactos de
cámara, firma/freshness físicas, latencia o rechazo semántico observado**.

## Cierre y siguiente decisión separada

Solo documentación propia modificada: esta evidencia, ESTADO.md y
COORDINACION_PROYECTOS.md. Sin código/build/tests JVM nuevos: suites173fixture/
141normal son antecedentes QR02, no reejecutadas ni sumadas aquí. Sin APKtest,
instrumentación, logs/payloads de dispositivo, A13/hijo/relay/repos ajenos/CA/LAN/
WSS/grantsADB/aprovisionamiento/push/despliegue. No cámara abierta que cerrar;
posición final de navegación/app no comprobada por ADB. Sin cambio de CP1/CP2,
fixtures congelados, auth/guards ni promoción PREPARADO.

Impedimento cerrado, no otra tarea automática. Si se asigna diagnóstico posterior:
considerar permiso específico para APKtest/prueba propia aislada con fixture público
y comprobación exacta de bytes, o ruta productiva de vinculación en unidad separada.
Ninguna de esas opciones autorizada/ejecutada por F01. No pedir ceremonia real o
crear pareja ficticia para superar esta guarda. Esperar decisión del propietario/
coordinador; no prompts de vuelta ni acceso adicional a dispositivo por este cierre.
