# Estado de implementación

Última actualización: 2026-10-09
Commits C00: 4cc7a39, e25bff5, 00329bd. C01: f7e2044, dc50004, 6725515. C02: f120557, 68348c2. P00: 37c74a3, 943082c. Primer bloque P01: 0f143c4; cierre local en el commit posterior, consultar git log.
Tarea actual: P02 — escaneo directo con CameraX local
Estado: CAMARA_IMPLEMENTADA_LOCAL — 81 pruebas locales aprobadas; permiso/cámara/lifecycle físicos del APK nuevo e integración pendientes

## Entorno observado

### Cambio de alcance acordado

El propietario autorizó separar el relay. Se trasladó a
`C:\Users\Usuario\Documents\parental_render\relay`, incluidos sus tests y
dependencias/resultados locales. Dieciocho archivos propios comparados por
SHA-256 antes/después, sin cambios. Contrato/ejemplos/fixture y verificadores
copiados al servidor sin modificar CP/1. Documentación/evidencias antiguas del
padre conservadas como historia; tareas R01–R03 retiradas del plan activo y
conservadas en el plan original copiado al servidor. Proyecto externo tiene
instrucciones, estado, README y script propio; desarrollo de servidor posterior.

scripts/verificar.ps1 del padre ya no instala npm ni verifica un relay local.
pruebas/interoperabilidad.mjs es oráculo de tests Node estándar contra payloads
congelados, no servidor/codec de producción. Cinco negativos/positivos propios.
Test JVM/Node predeterminado independiente; cruce con codec del servidor solo
opcional con -PrutaRelay. No se oculta ni elimina la prueba con codec real.

Verificación padre: 38 app, 30 núcleo, 1 interop y 5 oráculo Node, 74 tests.
Gradle de tests repetido --rerun-tasks: correcto; script completo también correcto,
APK debug/instrumentado y lint correctos (tareas sin cambios reutilizan resultados).
Servidor: npm ci/check/build y 30 tests correctos; ambos verificadores documentales
10 vectores/78 comprobaciones. Interop opcional con codec externo también aprobado
con --rerun-tasks: 20 cruces de firmas nuevas. No hubo ADB/despliegue ni funciones
nuevas de servidor. Los 99 tests anteriores eran antes de separar los 30 Node.

Archivos de separación: relay/ trasladado fuera; scripts/verificar.ps1,
core-protocol/build.gradle.kts y src/test/kotlin/dev/controlparental/protocol/InteroperabilidadTest.kt;
pruebas/interoperabilidad.mjs e interoperabilidad.test.mjs; AGENTS.md,
00_EMPIEZA_AQUI.md, README.md, BUILD_ENV.md, INTEGRACION_FUTURA.md,
padre/PLAN_PADRE.md y este ESTADO.md. Evidencia en pruebas/SEPARACION_RENDER.md.
Próximo desarrollo del padre: continuar pendientes P02 según HANDOFF_P02.md.
Servidor: R01 en sesión/proyecto aparte cuando el propietario lo indique.
Primer commit del repositorio servidor independiente: 4b9309d. Sin push ni
remoto configurado por esta extracción; cierre del padre en commit posterior.

Android Studio AI-253.32098.37.2534.15232325 / JDK 21.0.6 / Gradle 8.13 / AGP 8.11.1 / Kotlin 2.2.0. Detalles en BUILD_ENV.md.
Node 24.21.0 / npm 11.19.0 / Windows 11 amd64
SM / Android / One UI / parche del hijo: PENDIENTE DE INTEGRACION

## Terminado y demostrado

- P02 cámara, preparación física autorizada: app padre y APK instrumentado actualizados con install -r correctamente, solo en A56. App abierta sin crear identidad. QR PNG público del fixture 11_child_offer generado fuera del repositorio y abierto en PC. No hay datos de pareja real ni secretos; no se preparó vínculo. Lectura óptica/permiso pendientes de respuesta manual del propietario; no se declaran aprobados. No se ejecutaron instrumentados ni operaciones A13.

- P02 cámara: CameraX 1.4.2 (camera-core/camera2/lifecycle/view) y ZXing local. Botón explícito, permiso CAMERA solo a petición, hardware opcional; preview trasero y plano Y con KEEP_ONLY_LATEST en escritor/executor único. No fotos, archivos ni solicitudes remotas. Solo entrega un CHILD_OFFER estructuralmente válido, no confirma identidad/vigencia. Imagen QR sigue disponible como alternativa.
- CamaraOferta cierra UI al pausar, desasocia solo sus casos al salir, cierra cada ImageProxy en finally y descarta callbacks al salir/no RESUMED. No unbindAll ni servicio oculto; errores de permiso/cámara conservan alternativa/recuperación. LuminanciaQr respeta posición/límite/strides y acota planos/submuestreo. Pruebas sintéticas correctas no acreditan cámara óptica real ni liberación física.
- Rutas cámara: gradle/libs.versions.toml, app-parent/build.gradle.kts, app-parent/src/main/AndroidManifest.xml; app-parent/src/main/java/dev/controlparental/parent/{pairing/LuminanciaQr.kt,pairing/QrLocal.kt,ui/CamaraOferta.kt,ui/PairingScreen.kt}; app-parent/src/test/java/dev/controlparental/parent/LuminanciaQrTest.kt; README.md, BUILD_ENV.md, ESTADO.md, pruebas/EVIDENCIA_P02.md y pruebas/HANDOFF_P02.md. CP/1 y proyecto servidor sin cambios; no ADB.

- P02 lote físico nuevo autorizado: ambos APK actualizados con install -r solo en A56. PantallaInicialTest (4), PantallaVinculoTest (4) y VinculoPersistidoAndroidTest (2): diez aprobados, cero fallos/omisiones, 15.071 s. Recorrido/recuperación, flag FLAG_SECURE, casillas/firma fallida, descarte por entradas/salida/segundo plano, QR aislado persistente/env sin códigos y migración v1 real con reserva/nonce nuevo comprobados. Cleanup temporal completado, sin identidad/vínculo cotidiano ni cambios de credenciales. A13 conectado pero sin operaciones dirigidas a él.

- P02 nueva revisión: cambiar oferta (incluido selector de imagen)/origen o salir de pantalla invalida códigos, casillas y generación en curso. Guardia síncrona contra dobles operaciones. PairChild rechaza preparación descartada o cuyos códigos no corresponden a los hashes/pareja antes de firmar. Dos nuevos tests JVM aprobados, 40 app en total.
- Pruebas aisladas Android compiladas: EntornoVinculoAndroid usa archivo/alias AES temporal propio y firma JCA efímera exclusivamente instrumentada (no autentica al adulto ni reemplaza Keystore de firma real). VinculoPersistidoAndroidTest prepara/persiste/reabre QR, comprueba env sin códigos/no revinculación y migra v1 real conservando reserva/contador. PantallaVinculoTest comprueba ambas casillas/fallo de firma, segundo plano, cambio de entrada y salida de pantalla. PantallaInicialTest actualizado al lector actual y nuevo test de FLAG_SECURE sin configurar identidad. Ninguno ejecutado físicamente en este turno.
- Acuerdo nuevo registrado en AGENTS.md e INTEGRACION_FUTURA.md: avisar al propietario al completar parte padre preparada para integración; él orquestará IA en proyecto servidor y se probará comunicación local antes de desplegar. No se lanzó agente, modificó servidor ni usó ADB. El padre aún no está listo para integración completa: cliente WSS y casos de órdenes siguen pendientes.
- Rutas de este turno: AGENTS.md, INTEGRACION_FUTURA.md, ESTADO.md, pruebas/EVIDENCIA_P02.md, pruebas/HANDOFF_P02.md; app-parent/src/main/java/dev/controlparental/parent/ui/PairingScreen.kt y pairing/PairChild.kt; app-parent/src/test/java/dev/controlparental/parent/PairChildTest.kt; app-parent/src/androidTest/java/dev/controlparental/parent/{PantallaInicialTest,EntornoVinculoAndroid,VinculoPersistidoAndroidTest,PantallaVinculoTest}.kt.

- P02 físico autorizado: app padre y APK instrumentado actualizados con install -r solo en A56. ImagenQrAndroidTest y AlmacenAndroidTest: cuatro aprobados, cero fallos/omisiones, 1.226 s. PNG QR leído localmente, texto no oferta e imagen ilegible rechazados; regresión AES/Keystore/AtomicFile del formato actual correcta. Cleanup de archivos/alias temporales completado, sin cambiar identidad/vínculo operativo ni credenciales. A13 conectado pero sin operaciones dirigidas a él.

- P02 cierre local: pantalla en Vincular con selector oficial de imagen QR (no cámara directa), códigos solo en memoria y descartados al salir/pasar a segundo plano, huellas completas, casillas y autenticación antes de firmar. FLAG_SECURE en actividad. No se garantiza borrado físico de String JVM. QR/env pública recuperables tras cerrar la app; vínculo permanece PREPARADO, sin órdenes ni revinculación.
- Metadatos privados v2: aceptación firmada persistida y validada; migración explícita v1 mantiene contador/intención/evidencia sin inventar aceptación. CP/1/esquema/fixtures intactos. Cinco tests P02 correctos (38 app total), incluida migración y casos negativos en simulador de hijo. No prueban APK real del hijo ni vencimiento: CHILD_OFFER no tiene timestamp y su vigencia la verifica el hijo localmente. Primer bloque 65b9184; cierre en commit posterior.
- Rutas P02: gradle/libs.versions.toml, app-parent/build.gradle.kts; MainActivity.kt; pairing/{RecoveryCodeGenerator,PairChild,QrLocal,ImagenQrAndroid}.kt; ui/{PairingScreen,AplicacionPadre}.kt; data/{MetadatosPadre,ParentStore}.kt; res/values/strings.xml; test/{PairChildTest,ParentStoreTest}.kt; androidTest/ImagenQrAndroidTest.kt (todos bajo app-parent/src y paquete dev/controlparental/parent cuando corresponde). Documentación: ESTADO.md, README.md, BUILD_ENV.md y pruebas/{MATRIZ_ACEPTACION,EVIDENCIA_P02,HANDOFF_P02}.md.

- P02 primer bloque: PairChild lee CHILD_OFFER estricto, prepara pairId/códigos, huellas SHA-256 completas y PAIR_ACCEPT firmado con propósito PAIR; persiste PREPARADO antes de mostrar QR y no permite nueva vinculación. Generador de tres códigos independientes de 160 bits Base32/hash por propósito; ningún código se serializa. Exportación con solo cuatro campos públicos. ZXing core 3.5.3 fijado para QR local, sin servicios remotos.
- P02 primer bloque (histórico): tres nuevos tests JVM aprobados, 36 app entonces. Roundtrip offline de ambos QR, firma, confirmaciones y exportación probados. UI se incorporó después; cámara directa sigue sin implementar. No se usó ADB.

- P03: ParentStore con una coroutine escritora, metadatos locales versionados, contador/intención en una transacción, comandos originales firmados y evidencia separada. AtomicFile y AES-256-GCM con alias Keystore distinto de firma y nonce generado por proveedor. No se conecta todavía a la UI ni crea archivos automáticamente al abrir la app. Primer bloque a20f025; cierre en el commit posterior.
- P03: 15 tests nuevos aprobados (4 AES-GCM y 11 ParentStore), 33 tests app en total. Cierre antes de envío conserva reserva; cancelación no reutiliza contador ni borra evidencia; fallos antes/después de commit, concurrencia, corrupción y pérdida simulada de clave comprobados. Se implementó antes de P02 por su dependencia explícita; no se inició vinculación QR.
- P03 físico: actualización de app/APK instrumentado autorizada y ejecutada solo en A56. AlmacenAndroidTest: tres aprobados, cero omisiones/fallos, 1.286 s. AES-GCM/Keystore/AtomicFile comprobados, nonce nuevo, reserva conservada al reabrir, .new interrumpido conserva documento anterior y corrupción rechazada sin sobrescribir. Cleanup de archivos/alias temporales completado; no se modificaron identidad operativa ni credenciales. A13 sin operaciones.

- A56, P01: huella y PIN/contraseña confirmados por el propietario y aprobados, firma protegida y caducidad Keystore/sesión tras 61 segundos comprobadas, cancelación física aprobada. Persistencia entre procesos aprobada con identidad temporal. Detalles y límites en EVIDENCIA_P01.md; invalidación permanente segura y revisión visual ampliada siguen pendientes.
- Exclusión del A13 persistida como acuerdo obligatorio en AGENTS.md y padre/AGENTS.md. Ningún comando de dispositivo ni instalación dirigido a A13. Las futuras sesiones deben usar selector explícito de A56.

- Primer lote físico autorizado por el propietario en el padre A56 (SM-A566E, Android 16/API 36): APK debug y de pruebas instalados con éxito, 12 tests aprobados y 1 interactivo omitido expresamente. Se verificaron navegación/estados, parser/esquema, vectores, firma con clave temporal no exportable y parámetros de identidad protegida/reapertura del almacén. No acredita autenticación física ni persistencia tras reiniciar proceso.
- El A13 estaba conectado pero quedó excluido: ningún comando shell, instalación ni prueba dirigido a él. Se usó ADB con selector explícito del A56; no Gradle connected contra todos los teléfonos. Identificadores de dispositivo no se guardan en documentación.

- P01: ParentKeyStore con alias EC P-256 protegido, ventana de 60 segundos y ambos autenticadores fuertes/sistema. No reemplaza identidad incompatible ni crea clave durante firma. El alias de metadatos separado se implementó después en P03.
- P01 sesión de interfaz con reloj monotónico, invalidación en segundo plano/cancelación y rechazo de callbacks viejos/duplicados. LocalAuthentication usa BiometricPrompt oficial API 30. Pantalla inicial permite crear/cargar explícitamente y autenticar; no habilita órdenes ni crea vínculo.
- P01 cierre local: nueve tests JVM nuevos correctos (18 tests app en ese momento), APK compilados y lint correcto. Posteriormente identidad, autenticación y persistencia se comprobaron en A56; resultados físicos en EVIDENCIA_P01.md.

- P00: MainActivity, AppContainer, ParentViewModel/StateFlow, cuatro pantallas y estados separados. Producción usa fuente no operativa, no un simulador; repositorio falso exclusivo de tests. Botones de órdenes deshabilitados, recuperación informativa siempre accesible. No se crea autoridad ni vinculación al explorar controles.
- P00 cierre local: 9 tests JVM app correctos (8 nuevos), APK/lint correctos y cinco tests UI preparados. Posteriormente aprobaron en el lote físico A56. Se corrigió limpieza de ViewModelStore antes de resetMain en tests JVM, sin simular APIs Android.
- Estado de presentación confirmado únicamente con vínculo, relay conectado, hijo presente y resumen marcado fresco; al desconectar/ausentarse pasa a última evidencia conocida. P05 deberá producir resumen desde evidencia autenticada/correlacionada y calcular antigüedad/frescura; P00 no valida ni aplica órdenes.

- Acuerdos del propietario persistidos en AGENTS.md y protegidos por un commit.
- Base Android compilada: APK debug de la app y APK instrumentado de pruebas generados.
- Base del relay compilada; responde 503 en health y rechaza upgrades/producción. No implementa todavía R01–R03.
- Cliente hijo simulado exclusivamente en tests, limitado a loopback; lectura de fixture firmado congelado.
- Verificación local completa mediante scripts/verificar.ps1 aprobada. App de desarrollo instalada en A56 para pruebas; todavía no se ha demostrado control real de otro dispositivo.
- C01: modelos de todos los tipos CP/1 en Kotlin/Node, parser estricto, serialización determinista y validación adicional de secuencias, base64, SPKI P-256, DER y origen HTTPS. Los diez vectores preservan sus bytes exactos en ambos runtimes.
- Kotlin incorpora el esquema directamente desde compartido/; Node recibe sus bytes desde inicialización confiable. Ambos verifican su SHA-256 congelado. No se cambia ni duplica manualmente el contrato.
- Ajv 8.20.0 fijado para JSON Schema 2020-12. Se descartó 8.17.1 al detectar un aviso moderado en npm audit; la versión finalmente fijada no presenta avisos en la auditoría ejecutada.
- C02: verificación estándar SHA256withECDSA/DER sobre bytes originales en JVM y crypto.verify en Node; comparación de propósito y rol. El firmante Kotlin se inyecta desde la futura capa Keystore P01, sin almacenar privadas ni crear alternativas.
- Firmas nuevas verificadas en ambas direcciones para los diez vectores: 20 intercambios JVM↔Node, ambos roles y todos los propósitos. Claves de prueba efímeras en memoria, sin guardarlas ni imprimirlas.
- Tests instrumentados de criptografía y clave temporal no exportable aprobaron en el lote físico A56. Keystore y autenticación reales se acreditan solo por las pruebas efectivamente ejecutadas, no por inspección/JVM; ver límites en EVIDENCIA_P01.md.
- No hay autorización contextual: leerPayloadSinVerificar no acredita firmas; DocumentoVerificado acredita firma/estructura, no pareja autorizada, sesión vigente ni aplicación de una orden.

## Archivos modificados en la última tarea

README.md, BUILD_ENV.md, ESTADO.md, app-parent/build.gradle.kts.
app-parent/src/main/java/dev/controlparental/parent/data/MetadatosPadre.kt, ParentStore.kt, ArchivoAtomicoMetadatos.kt, AlmacenPadreAndroid.kt.
app-parent/src/main/java/dev/controlparental/parent/security/CifradoMetadatos.kt y MetadataKeyStore.kt.
app-parent/src/test/java/dev/controlparental/parent/CifradoMetadatosTest.kt y ParentStoreTest.kt.
app-parent/src/androidTest/java/dev/controlparental/parent/AlmacenAndroidTest.kt.
pruebas/MATRIZ_ACEPTACION.md, pruebas/EVIDENCIA_P03.md y pruebas/HANDOFF_P03.md.

## Pruebas ejecutadas y resultado

- P02 cámara: scripts/verificar.ps1 correcto tras corregir ruta de fixture inexistente en test nuevo (ahora pública JCA efímera en test). 45 app + 30 núcleo + 1 interop + 5 oráculo Node = 81 pruebas locales correctas; 78 comprobaciones documentales. Cinco tests LuminanciaQr nuevos: strides/padding/posición, límites/truncado, submuestreo, oferta en cuatro giros y rechazo de QR no oferta. Debug/instrumentado/release sin firma de distribución compilados; lint cero errores/20 avisos de versiones. No se instalaron APK nuevos ni se ejecutaron tests físicos; los diez A56 anteriores son evidencia del APK anterior.

- P02 A56: instalaciones app/APK instrumentado correctas; runner AndroidJUnitRunner filtrado a tres clases y selector explícito A56. Diez status 0, OK (10 tests), 15.071 s. No se ejecutaron los otros 15 instrumentados. Firma de aceptación fue JCA efímera exclusivamente de test; no acredita firma Keystore autenticada de PAIR_ACCEPT. Migración/cifrado/archivo usaron Android reales y alias/directorio temporal propio. FLAG_SECURE verificado como flag, no captura real bloqueada.

- P02 seguimiento: scripts/verificar.ps1 correcto, 40 app + 30 núcleo + 1 interoperabilidad + 5 oráculo Node = 76 locales; diez vectores/78 comprobaciones documentales. APK debug/instrumentado/release sin firma de distribución compilados y lint sin errores. Hay 25 tests Android disponibles, no se ejecutaron en este turno. Los diez de PantallaInicialTest/PantallaVinculoTest/VinculoPersistidoAndroidTest requieren aviso/permiso nuevo antes de instalar APK/ejecutarlos en A56. Firma protegida/autenticación no simuladas como resultado real.

- P02 A56: runner AndroidJUnitRunner filtrado a ImagenQrAndroidTest y AlmacenAndroidTest, selector explícito de A56. Cuatro status 0, OK (4 tests), 1.226 s; ambos install -r correctos. No se ejecutaron los otros 14 tests. No se apagó la red: lectura/decodificación local no hace solicitudes remotas, pero no se probó selector en modo avión. No se ejecutó migración v1 física ni flujo completo de aceptación/UI.

- P02: scripts/verificar.ps1 correcto; repetición Gradle --rerun-tasks de tests app/núcleo/interoperabilidad, APK debug/instrumentado/release sin firma de distribución y lint: 130 tareas correctas. 38 app, 30 núcleo, 1 interoperabilidad y 30 Node, total 99 locales; 78 comprobaciones documentales. Lint cero errores/16 avisos de versiones. Un test Android nuevo de imagen QR compilado/no ejecutado (18 instrumentados disponibles). No se instalaron APK nuevos, no hubo ADB ni despliegue público.

- P03 cierre: scripts/verificar.ps1 correcto. Gradle tests app/núcleo/interoperabilidad, APK debug/instrumentado/release y lint repetidos con --rerun-tasks: 130 tareas ejecutadas correctamente. Reportes: 33 app, 30 núcleo y 1 interoperabilidad, sin fallos/errores; 30 Node correctos, total 94 pruebas locales aprobadas. Vectores documentales: 78 comprobaciones correctas. Lint: cero errores y 15 avisos de versiones nuevas.
- P03 cierre local: tres nuevos tests Android compilados (17 instrumentados disponibles). En ese turno no se usó ADB; posteriormente se ejecutaron con autorización expresa: tres aprobados/ninguno omitido en A56, status 0 cada uno, OK (3 tests), 1.286 s. Ambas instalaciones con -r correctas. Runner dirigido solo a AlmacenAndroidTest y solo al A56; no se ejecutaron los otros 14 tests en este lote. Inspección local ZIP/DEX previa sin fixtures/dobles/clases/alias de tests examinados en app debug/release.

### Historial de comprobaciones anteriores (ver evidencias de cada tarea)

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

G0–G4 pendientes. A13 excluido, sin validación física ni permiso para operarlo. El padre A56 tiene validación parcial P00/C02/P01 registrada; no certifica el producto completo ni compatibilidad universal.
Falta jsonschema para el verificador estructural Python.
Antes de usar ADB, avisar y esperar habilitación expresa del propietario.
P01 aceptar con huella y PIN/contraseña pasó físicamente en A56, incluida caducidad; cancelación y persistencia entre procesos pasaron. Rotación/texto grande/ciclo de vida manual ampliado e invalidación permanente siguen pendientes; no quitar barreras para aparentar éxito.
La referencia plantillas/HANDOFF.md de padre/AGENTS.md no existe; usar pruebas/HANDOFF_PLANTILLA.md.

## Próxima tarea y lectura mínima

Próximo paso físico P03 completado: tres pruebas autorizadas/aprobadas en A56. No repetir por referencias históricas a pendientes. Antes de nuevas instalaciones/pruebas, avisar y esperar permiso; no tocar A13 ni ejecutar connected contra todos los dispositivos.
Próximo paso P02: pedir autorización para actualizar APK y probar cámara/permiso reales en A56 con QR público de pruebas mostrado en PC, sin crear identidad ni vínculo cotidiano. Comprobar aceptar/denegar permiso, cancelar, lectura, giros, salir/fondo y ausencia de cámara al regresar hasta apertura explícita; regresión de diez casos aislados si se autoriza. No cambiar permisos por ADB sin acuerdo ni operar A13. Cámara local implementada pero pendiente físicamente; revisión selector/texto grande/FLAG_SECURE real y PAIR autenticado/hijo real siguen pendientes. Ver HANDOFF_P02.md. Aún falta cliente WSS/órdenes para integrar; avisar al propietario cuando esté preparada parte padre, con integración local antes de despliegue.

## Seguridad

No almacenar claves, códigos, QR sensibles, contraseñas ni datos del menor aquí.
