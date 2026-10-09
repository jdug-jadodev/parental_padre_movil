# Estado de implementación

Última actualización: 2026-10-08
Commits C00: 4cc7a39, e25bff5, 00329bd. C01: f7e2044, dc50004, 6725515. C02: f120557, 68348c2. P00: 37c74a3, 943082c. Primer bloque P01: 0f143c4; cierre local en el commit posterior, consultar git log.
Tarea actual: P03 (dependencia necesaria antes de P02)
Estado: EN_CURSO — almacenamiento cifrado y escritor único implementados; pruebas de fallos/recuperación pendientes de cierre

## Entorno observado

Android Studio AI-253.32098.37.2534.15232325 / JDK 21.0.6 / Gradle 8.13 / AGP 8.11.1 / Kotlin 2.2.0. Detalles en BUILD_ENV.md.
Node 24.21.0 / npm 11.19.0 / Windows 11 amd64
SM / Android / One UI / parche del hijo: PENDIENTE DE INTEGRACION

## Terminado y demostrado

- P03 primer bloque: ParentStore con una coroutine escritora, metadatos locales versionados, contador/intención en una transacción, comandos originales firmados y evidencia separada. AtomicFile y AES-256-GCM con alias Keystore distinto de firma y nonce generado por proveedor. No se conecta todavía a la UI ni crea archivos automáticamente al abrir la app.
- P03: cuatro pruebas JVM AES-GCM y una transaccional aprobadas (23 tests app en total); debug y lint correctos. Se implementa P03 antes de P02 porque PLAN_PADRE P02 depende explícitamente de P03; no se inició vinculación QR.

- A56, prueba interactiva aceptar autorizada y ejecutada: autenticación oficial confirmada, firma CP/1 con clave protegida temporal y verificación correctas. Tras 61 segundos Keystore lanzó UserNotAuthenticatedException y la sesión rechazó otra firma. Un test aprobado, cero fallos, 76.324 segundos. El propietario confirmó que usó huella; credencial PIN/contraseña todavía no probada.
- Exclusión del A13 persistida como acuerdo obligatorio en AGENTS.md y padre/AGENTS.md. Ningún comando de dispositivo ni instalación dirigido a A13. Las futuras sesiones deben usar selector explícito de A56.

- Primer lote físico autorizado por el propietario en el padre A56 (SM-A566E, Android 16/API 36): APK debug y de pruebas instalados con éxito, 12 tests aprobados y 1 interactivo omitido expresamente. Se verificaron navegación/estados, parser/esquema, vectores, firma con clave temporal no exportable y parámetros de identidad protegida/reapertura del almacén. No acredita autenticación física ni persistencia tras reiniciar proceso.
- El A13 estaba conectado pero quedó excluido: ningún comando shell, instalación ni prueba dirigido a él. Se usó ADB con selector explícito del A56; no Gradle connected contra todos los teléfonos. Identificadores de dispositivo no se guardan en documentación.

- P01: ParentKeyStore con alias EC P-256 protegido, ventana de 60 segundos y ambos autenticadores fuertes/sistema. No reemplaza identidad incompatible ni crea clave durante firma. Alias de metadatos separado reservado, almacenamiento cifrado todavía pendiente.
- P01 sesión de interfaz con reloj monotónico, invalidación en segundo plano/cancelación y rechazo de callbacks viejos/duplicados. LocalAuthentication usa BiometricPrompt oficial API 30. Pantalla inicial permite crear/cargar explícitamente y autenticar; no habilita órdenes ni crea vínculo.
- P01: nueve tests JVM nuevos correctos (18 tests de app en total). Debug, instrumentado y release sin firma de distribución compilados; lint correcto. Dos pruebas nuevas de identidad y una interactiva real compiladas, no ejecutadas. No se accedió al teléfono ni se acredita persistencia tras muerte de proceso.

- P00: MainActivity, AppContainer, ParentViewModel/StateFlow, cuatro pantallas y estados separados. Producción usa fuente no operativa, no un simulador; repositorio falso exclusivo de tests. Botones de órdenes deshabilitados, recuperación informativa siempre accesible. No se crea autoridad ni vinculación al explorar controles.
- P00: 9 tests JVM de la app correctos (8 nuevos). Compilación debug/instrumentada/release sin firma de distribución y lint correctos. Cinco nuevos tests UI instrumentados preparados y compilados, no ejecutados. Se corrigió la limpieza de ViewModelStore antes de resetMain en las pruebas JVM, sin simular APIs Android.
- Estado de presentación confirmado únicamente con vínculo, relay conectado, hijo presente y resumen marcado fresco; al desconectar/ausentarse pasa a última evidencia conocida. P05 deberá producir resumen desde evidencia autenticada/correlacionada y calcular antigüedad/frescura; P00 no valida ni aplica órdenes.

- Acuerdos del propietario persistidos en AGENTS.md y protegidos por un commit.
- Base Android compilada: APK debug de la app y APK instrumentado de pruebas generados.
- Base del relay compilada; responde 503 en health y rechaza upgrades/producción. No implementa todavía R01–R03.
- Cliente hijo simulado exclusivamente en tests, limitado a loopback; lectura de fixture firmado congelado.
- Verificación local completa mediante scripts/verificar.ps1 aprobada. No se ha instalado la app ni demostrado control real de un dispositivo.
- C01: modelos de todos los tipos CP/1 en Kotlin/Node, parser estricto, serialización determinista y validación adicional de secuencias, base64, SPKI P-256, DER y origen HTTPS. Los diez vectores preservan sus bytes exactos en ambos runtimes.
- Kotlin incorpora el esquema directamente desde compartido/; Node recibe sus bytes desde inicialización confiable. Ambos verifican su SHA-256 congelado. No se cambia ni duplica manualmente el contrato.
- Ajv 8.20.0 fijado para JSON Schema 2020-12. Se descartó 8.17.1 al detectar un aviso moderado en npm audit; la versión finalmente fijada no presenta avisos en la auditoría ejecutada.
- C02: verificación estándar SHA256withECDSA/DER sobre bytes originales en JVM y crypto.verify en Node; comparación de propósito y rol. El firmante Kotlin se inyecta desde la futura capa Keystore P01, sin almacenar privadas ni crear alternativas.
- Firmas nuevas verificadas en ambas direcciones para los diez vectores: 20 intercambios JVM↔Node, ambos roles y todos los propósitos. Claves de prueba efímeras en memoria, sin guardarlas ni imprimirlas.
- Tests instrumentados de criptografía y clave temporal no exportable preparados y compilados; pendientes del teléfono. No se acredita Keystore real ni autenticación del adulto por inspección o tests JVM.
- No hay autorización contextual: leerPayloadSinVerificar no acredita firmas; DocumentoVerificado acredita firma/estructura, no pareja autorizada, sesión vigente ni aplicación de una orden.

## Archivos modificados en la última tarea

README.md, BUILD_ENV.md, ESTADO.md, app-parent/src/main/AndroidManifest.xml.
app-parent/src/main/java/dev/controlparental/parent/MainActivity.kt.
app-parent/src/main/java/dev/controlparental/parent/security/SesionAutoridad.kt, AutoridadPadre.kt, ParentKeyStore.kt, LocalAuthentication.kt.
app-parent/src/main/java/dev/controlparental/parent/ui/AplicacionPadre.kt e IdentidadPadre.kt.
app-parent/src/test/java/dev/controlparental/parent/SesionAutoridadTest.kt y AutoridadPadreTest.kt.
app-parent/src/androidTest/java/dev/controlparental/parent/IdentidadKeystoreTest.kt y AutenticacionRealTest.kt.
app-parent/src/androidTest/java/dev/controlparental/parent/PersistenciaProcesoTest.kt (comprobación física adicional en dos fases).
pruebas/MATRIZ_ACEPTACION.md, pruebas/EVIDENCIA_P01.md, pruebas/HANDOFF_P01.md, pruebas/PRUEBA_FISICA_P01.md.

## Pruebas ejecutadas y resultado

- Revisión manual informada por el propietario en A56: abrió la app, recorrió Configurar este teléfono como padre → Ver controles → Recuperación sin internet y confirmó «todo bien» al preguntar si llegó a Recuperación y leyó bien la pantalla. Recorrido y legibilidad básica confirmados por el propietario, no por inspección remota. Rotación, texto grande, Atrás y bloqueo al volver de segundo plano no fueron confirmados en esta revisión.

- A56 persistencia entre procesos autorizada: nuevo PersistenciaProcesoTest opt-in, compilación APK instrumentado/lint correctos e instalación de actualización del APK de pruebas con -r. Fase preparar aprobada (0.103 s); force-stop únicamente dev.controlparental.parent en A56; fase verificar aprobada (0.036 s). La segunda fase comprobó PID distinto, alias aún existente, misma huella SHA-256 de pública y carga sin crearSiFalta. Nueva SesionAutoridad rechazó firma sin autenticar. Alias temporal y registro de prueba eliminados en finally; no se borraron datos de la app ni se reinició teléfono. MainActivity reabierta al terminar. Ninguna operación sobre A13.
- El registro temporal contiene solo alias cp.test.persist.*, hash de pública y PID, en noBackupFilesDir. No contiene privadas, credenciales ni datos de vínculo. Se elimina al verificar; no implementa ni reemplaza el almacenamiento cifrado de producción. No se usó cp.parent.sign.v1. La prueba demuestra persistencia del almacén de identidad protegido entre procesos; no firma autenticada después del reinicio ni persistencia de metadatos de vinculación.

- A56: segunda ejecución aceptar con PIN/contraseña, confirmado expresamente por el propietario (sin huella). Un test aprobado, status 0/OK (1 test), cero fallos, 72.282 s. Firma protegida y caducidad Keystore/sesión tras 61 segundos comprobadas de nuevo. A13 excluido, sin operaciones.

- A56 interactivo cancelar autorizado: runner ADB con selector explícito del A56, filtro AutenticacionRealTest y autenticacionReal=cancelar. Un test aprobado, status 0/OK (1 test), cero fallos, 21.311 s. Cancelación física recibida como no autenticada; AutoridadPadre rechazó firmar. Pública temporal conservada al reabrir almacén, alias eliminado en finally. Ninguna operación en A13 ni firma/orden enviada.

- A56 interactivo: runner ADB dirigido al A56, filtro AutenticacionRealTest y autenticacionReal=aceptar: status 0, OK (1 test), 76.324 s. Adulto respondió físicamente; sin éxito biométrico simulado. Firma temporal verificada y caducidad del proveedor/sesión comprobadas. Alias temporal eliminado por finally. No se enviaron mensajes al relay ni se creó identidad operativa.

- A56: adb install -r para app debug y APK instrumentado, ambos correctos. Runner AndroidJUnitRunner ejecutado directamente con adb -s dirigido al A56: 13 casos descubiertos, 12 status 0 y 1 status -4 (AssumptionViolatedException del test interactivo sin argumento). Resumen del runner OK (13 tests), interpretado correctamente como 12 aprobados/1 omitido; duración 8.571 s. Sin fallos.
- Dos tests de identidad protegida y los diez tests anteriores efectivamente aprobados en el A56. El test de identidad reabre el almacén dentro del mismo proceso; no prueba reinicio. No se cambiaron credenciales, biometría, timeout, restricciones ni configuración de administración. Solo se crean/eliminan alias temporales de tests.

- `node pruebas/verificar_vectores.mjs`: 10 vectores y 78 comprobaciones documentales correctas en Node 24.21.0.
- `python -B pruebas/verificar_esquema.py`: no ejecutó validación; falta el paquete jsonschema.
- `scripts/verificar.ps1 -Modulo relay`: instalación reproducible con npm ci, chequeo TypeScript, compilación y vectores correctos. Al añadir el cliente local simulado se repitieron `npm test` y `npm run check`: 6 pruebas aprobadas.
- Ejecutable compilado del relay: arranque local y GET /healthz observado con 503; inicio en producción rechazado con código 1.
- SHA-256 de contrato y esquema: coinciden con INTEGRACION_FUTURA.md.
- `:core-protocol:test`: 1 prueba JVM aprobada (reporte XML, sin fallos ni errores).
- `:app-parent:testDebugUnitTest`: 1 prueba aprobada (reporte XML, sin fallos ni errores).
- `:app-parent:assembleDebug` y `:app-parent:assembleDebugAndroidTest`: compilaciones correctas. La segunda solo compila las pruebas; no las ejecuta en Android.
- `:app-parent:lintDebug`: 0 errores y 9 avisos de versiones más recientes. Se mantienen versiones fijadas y compatibles; no se deshabilitaron comprobaciones.
- Primer comando Android interrumpido a los 120 segundos. Segundo intento completó APK/tests, pero lint detectó un escape incorrecto en local.properties y ausencia de icono. Ambos corregidos. La ejecución final completa de `scripts/verificar.ps1` terminó correctamente: núcleo, app, lint, relay y vectores.
- Ninguna prueba por ADB, en teléfono, en Render o con el APK real del hijo.
- C01 cierre: `scripts/verificar.ps1` correcto: 21 pruebas del núcleo JVM, 1 de la app y 23 Node, total 45 aprobadas. Compilaciones de ambos APK, TypeScript, chequeo de tipos y vectores correctos. Lint: 0 errores y 11 avisos de versiones más nuevas; no se suprimieron comprobaciones.
- Inspección del APK como ZIP: contiene protocolo.schema.json con el SHA-256 congelado exacto. Eso comprueba empaquetado, no ejecución dentro del teléfono.
- C02 cierre: scripts/verificar.ps1 correcto con 30 pruebas JVM del núcleo, 1 de la app, 30 Node y 1 de interoperabilidad, total 62 aprobadas. El caso de interoperabilidad ejecutó 20 intercambios con firmas nuevas. Vectores documentales: 78 comprobaciones correctas.
- :app-parent:assembleDebug, :app-parent:assembleDebugAndroidTest y :app-parent:lintDebug correctos; lint con 0 errores y 11 avisos de versiones más recientes. :app-parent:assembleRelease correcto; produce APK sin firma de distribución, no una entrega aprobada.
- Inspección de los tres APK como ZIP y búsqueda de identificadores de clases de prueba en DEX: fixture solo en APK instrumentado; ni fixture, ni clases criptográficas de prueba examinadas, ni alias cp.test.crypto en la app debug/release.
- Primer intento de empaquetado C02 detectó dependencia implícita de assets/lint. Se corrigió con la API oficial addGeneratedSourceDirectory y se repitió la verificación completa con éxito.
- Cinco pruebas instrumentadas disponibles (pantalla, dos de parser, dos de criptografía/Keystore); compiladas, no ejecutadas. No se usó ADB.
- P00 cierre: scripts/verificar.ps1 correcto; 30 tests del núcleo, 9 de la app, 30 Node y 1 interoperabilidad, total 70 aprobados. Los tests Gradle del núcleo/interoperabilidad estaban actualizados en esa ejecución; se volvieron a ejecutar explícitamente con --rerun-tasks, junto con tests de app y compilaciones/debug/instrumentado/release y lint, con éxito (130 tareas ejecutadas).
- P00 lint: cero errores y 15 avisos de versiones más recientes; sin suprimir comprobaciones. npm ci/check/build/test correctos; 78 comprobaciones documentales correctas. npm informó cero vulnerabilidades y aviso de script postinstall de esbuild sin autorización individual; no se alteraron permisos de npm.
- P00: inspección ZIP/DEX de app debug y release no encuentra fixture ni identificadores RepositorioPadreFalso, EstadoPantallaTest o cp.test.crypto. No reemplaza análisis de seguridad completo.
- Ahora hay diez pruebas instrumentadas compiladas (tres de inicio/navegación, tres de estados, dos de parser, dos de criptografía/Keystore). Ninguna ejecutada. La validación visual, botón Atrás, rotación, texto grande y acceso físico a recuperación siguen pendientes de Android.
- P01 cierre local: scripts/verificar.ps1 correcto; Gradle tests del núcleo/interoperabilidad actualizados en esa ejecución. Se repitieron tests de app/núcleo/interoperabilidad y compilaciones debug/instrumentado/release/lint con --rerun-tasks: 130 tareas ejecutadas con éxito. Reportes: 18 app + 30 núcleo + 1 interoperabilidad, cero fallos/errores; 30 Node correctos, total 79 pruebas locales aprobadas. Vectores: 78 comprobaciones correctas.
- P01 lint: cero errores y 15 avisos de versiones. El último ajuste del test interactivo se volvió a compilar con assembleDebugAndroidTest y lintDebug correctos. Inspección limitada ZIP/DEX de debug/release no encuentra fixture ni fake/tests/alias temporales examinados.
- Ahora hay trece tests instrumentados compilados, ninguno ejecutado: los diez anteriores, dos de identidad y uno interactivo opt-in. Los de identidad requieren credencial segura; el interactivo requiere argumento aceptar/cancelar y actuación real del adulto. Omitidos no equivalen a aprobados. Persistencia tras reinicio del proceso y recuperación de clave invalidada siguen pendientes.

## Decisiones locales sin cambiar ADR

- Módulos Android en la raíz, adaptados al alcance independiente de C00. No se crea app-child ni se modifica el otro repositorio.
- SPEC-P P2 y ADR-P-001 conservan una estructura monorepo anterior; se sigue la separación explícita de 00_EMPIEZA_AQUI.md, C00 y AGENTS.md. No se cambia CP/1.
- Servidor C00 ligado solo a loopback y no operativo; health 200 queda para R01 con configuración válida.
- Compilador con JDK 21 y destino JVM 17; dependencias fijadas, wrapper con checksum.

## Bloqueos y riesgos abiertos

G0–G4 pendientes. No hay validación física del A13 ni del teléfono del padre.
Falta jsonschema para el verificador estructural Python.
Antes de usar ADB, avisar y esperar habilitación expresa del propietario.
P01 requiere validar el comportamiento del prompt y onStop en el fabricante, especialmente la credencial del dispositivo sin biometría. Se cancela y bloquea conservadoramente en segundo plano; si el flujo oficial produce una parada, registrar el fallo y corregir sin eliminar la barrera. No se declara ese flujo aprobado por compilación.
La referencia plantillas/HANDOFF.md de padre/AGENTS.md no existe; usar pruebas/HANDOFF_PLANTILLA.md.

## Próxima tarea y lectura mínima

Próximo paso inmediato: revisión visual/ciclo de vida (texto grande, rotación y recuperación accesible). Reinicio de proceso ya comprobado con identidad temporal sin borrar datos. Huella, PIN/contraseña, firma/caducidad y cancelación aprobadas y confirmadas por el propietario. Invalidación permanente/pérdida de clave requiere banco de pruebas separado y autorización específica: no modificar credenciales del A56 cotidiano. Usar solo selección explícita del A56; no tocar A13.
Tras validar P01, siguiente tarea de desarrollo P02 — vinculación QR. No se inició P02 ni almacenamiento de metadatos en este turno. P00 y C02 conservan aceptación física pendiente.

## Seguridad

No almacenar claves, códigos, QR sensibles, contraseñas ni datos del menor aquí.
