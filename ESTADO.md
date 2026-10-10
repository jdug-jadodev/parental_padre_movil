# Estado de implementación

Última actualización: 2026-10-09
Commits C00: 4cc7a39, e25bff5, 00329bd. C01: f7e2044, dc50004, 6725515. C02: f120557, 68348c2. P00: 37c74a3, 943082c. Primer bloque P01: 0f143c4; cierre local en el commit posterior, consultar git log.
Tarea actual: PADRE-C01-R1 — revisión documental del resumen público del hijo
Estado: PADRE_C01_R1_COMPATIBLE_PROPUESTO — sin aprobar puertas/orden ni implementar C02; vínculo y aceptación física pendientes

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

- PADRE-C01-R1: HIJO-C02 cb62a73 revisado solo en lectura, compatible como propuesta de resumen post-commit (pairId completo, huellas SHA-256 SPKI DER P-256 en 64 hex mayúsculos/16 grupos de cuatro y origen HTTPS exacto). Acordados snapshot íntegro/readback, estatus de commit local con acreditación pendiente y errores sin confirmación/reemplazo. Hash privado PAIR no se exige al hijo; resumen/STATE no son recibos exactos ni prueba de consumo OFFLINE. Declaraciones del adulto separadas de firmas. Matriz A/B/C1/C2 no activa H06 ni promoción padre; decisión propietario pendiente. PADRE-C02 acotado en §7 del diseño, solo diagnóstico puro futuro, no implementado. Hijo reporta 17 tests nuevos/346 JVM H05-L01 c7fb842: no reejecutados ni evidencia física nueva. Sin tests/builds/código/ADB/CP1/plan cambiado o repos externos editados.

- PADRE-C01: diseño acotado en padre/DISENO_ACREDITACION_VINCULO.md; aceptación propia, commit del hijo observado, STATE correlacionado, recuperación MAINTENANCE presencial y confirmación del adulto separados. Propone revisión no reutilizable y persistencia privada atómica de etiqueta/evidencia, sin política ni promoción automática. CP/1 no contiene recibo de aceptación exacta ni de recuperación por nonce: comprobación física no se sustituye por firmas; prueba remota automática requeriría CP/2 acordado y detener esa vía. Negativos/aceptación propuestos, no ejecutados. Coordinación del relay 8385410/e26aebb y 104 tests/seis variantes leída, no reejecutada. Sin ADB, código productivo, CP/1 cambiado, repos ajenos, APIs de sesiones o pruebas nuevas.

- P08-B: ControlPoliticas + ParentStore + CanalPoliticasRelay + RelayConnection + TransporteOkHttp contra relay externo compilado (ce59ef6), WSS loopback/puerto efímero y hijo Node ficticio. Hashes CP/1/esquema/vectores iguales antes de escuchar. Hijo no apto y vínculo PREPARADO bloquean órdenes; señales de red/autoridad ficticias negativas bloquean sync/envío. Se comprueba comando persistido antes de cada envío, reserva seq=1 y dos retransmisiones de bytes exactos; ACK firmado con hash ajeno rechazado, ACK pendiente no es APPLIED. Reapertura de ParentStore cifrado en RAM y reconexión no reproducen orden; STATE nuevo correlacionado id/seq/hash resuelve como ENFORCEMENT_FAILED simulado. No hay mutaciones de producción, bypass ni promoción de vínculo operativo. Harness opcional -PrutaRelayIntegracion; prueba independiente no necesita servidor/npm. Evidencia: pruebas/EVIDENCIA_P08_B.md. Sin ADB ni cambios/despliegue en parental_render.

- P08-A: atendida observación del handoff documentacion/INTEGRACION_LOCAL_PADRE.md del servidor (leído sin modificar ese repositorio). RelayConnection emite PING inicial tras AUTH_OK y siguientes cada 15 s desde envío, no 10 s desde PONG. Se comprueba caducidad de 45 s desde último PONG en tick y puerta de envío, incluso sin tick tras pausa; PONG tardío no resucita canal. Se conserva timeout conservador de respuesta de 15 s y un único desafío pendiente. Seis tests nuevos aprobados: cadencia con demora, caducidad sin tick, ausencia de ráfaga, renovación solo por PONG, contexto nuevo sin herencia y rechazo tardío. Evidencia: pruebas/EVIDENCIA_P08_A.md. Sin CP/1 cambiado, ADB, servidor modificado ni despliegue; no acredita todavía ControlPoliticas contra relay real.

- P07 cierre local: ocho tests JVM propios aprobados, incluidos ambos QR sintéticos y firma conservada. Dos PantallaRecuperacionTest aislados RAM/JCA compilados/no ejecutados (advertencia RETIRE y cambio de entrada). No modifican identidad cotidiana ni retiran dispositivos. El simulador comprueba rechazo de repetición, arranque distinto y acción cruzada, no acredita consumo duradero en el hijo. Evidencia: pruebas/EVIDENCIA_P07.md; primer commit 90d3eff, cierre en commit posterior. Base del padre preparada para coordinar integración local con servidor separado, no para entrega/despliegue ni para habilitar órdenes con PREPARADO. Propietario avisado; él organizará trabajo externo, sin agentes ni cambios de servidor aquí.

- P07 primer bloque: OfflineChallengeSigner verifica OFFLINE_CHALLENGE con pública del hijo guardada y pairId, autentica antes/después de firmar OFFLINE_RESPONSE exacta (acción/boot/nonce). Referencia de preparación única, revisión contra salida/cambio de entrada, margen local 4 min sin timestamp inventado; hijo decide sus 5 min/consumo/boot. RecoveryScreen muestra advertencia de borrado para RETIRE, QR solo en memoria/con autoridad vigente, descarte al salir/fondo, cámara e imagen locales sin relay. PREPARADO permite recuperación firmada necesaria para integración, pero no se promociona ni habilita uso normal. Siete tests JVM iniciales correctos (103 app total); no ADB, restablecimientos ni servidor externo. Retiro/aplicación reales y QR/autenticación físicos pendientes.

- P06 cierre local parcial: ControlScreen sustituye controles de recorrido en MainActivity con acciones reales inyectadas desde AccionesControlReal. Formularios de 60–604800 s, advertencias de las cuatro acciones, guardado local sin envío, reemplazo explícito y confirmación separada (también ALLOW/ALLOW_FOR). Guardia síncrona de operación; salida/fondo descartan diálogos y cancelan operación UI, conservando intención duradera. Vínculo PREPARADO no habilita acciones ni se promociona por STATE; recuperación siempre en barra inferior.
- P06 evidencia: modo/causa y última respuesta firmada se presentan separados de intención, relay y presencia. APPLIED con NETWORK_LOST/LOCKED informa bloqueo por red, nunca pinta permitido por pulsar ALLOW. Caída del canal/red degrada presentación inmediatamente; sincronizar no rejuvenece evidencia previa. Antigüedad conocida solo en instancia que recibió evidencia, desconocida tras reabrir; remainingSec no se descuenta como confirmación remota. Descartar un comando exige sincronización fresca y advierte que no revoca órdenes recibidas; no se afirma cancelación remota.
- P06 privado: v3 añade una única intención offline sin seq/frame y excluyente de pendiente reservado. Reemplazo por id; reserva consume selección local atómicamente y rechaza confirmación de id anterior. Migración v1/v2 conserva bytes de comandos/aceptación/evidencia y contador, sin inventar selección local. Solo formato privado Android, CP/1/fixtures intactos. Trece tests JVM nuevos correctos (96 app total); cinco PantallaControlTest nuevos compilados/no ejecutados, incluida escala de texto 2x. No ADB, instalaciones, pruebas físicas ni servidor externo. Primer bloque 2c262b3; cierre en commit posterior.

- P06 primer bloque: intención local offline sin firma/seq, única y excluyente de orden pendiente; reemplazo/descarte por id explícito. Formato privado v3 migra v1/v2 conservando contador, comando, evidencia y aceptación (sin cambios CP/1). Confirmación vincula id de intención local y rechaza reemplazos posteriores incluso con misma acción. Vista revalida frescura/canal; textos distinguen modo real, causa, ACK aplicado/pendiente/fallo/rechazo, antigüedad desconocida y tiempo reportado sin descontarlo. Once tests JVM nuevos correctos; 94 app total. UI final pendiente, sin ADB ni servidor externo.

- P05 cierre local parcial: ControlPoliticas serializa sincronización, preparación/confirmación, reserva/firma/envío, ACK y reconciliación. STATE exige firma de pública vinculada, pareja/nonce actuales, stateSeq creciente y lastAcceptedSeq no decreciente en misma sesión; nueva sesión requiere nueva sincronización. Ventana local conservadora de 20 s desde petición (no recepción), sin inventar timestamp CP/1; desafío del hijo sigue regido por sus 30 s. Confirmación por referencia no reutilizable, sin órdenes nuevas con PREPARADO/hijo no apto/canal no sano.
- P05 durabilidad: ParentStore.resolverIntencion valida nuevamente firma/id/seq/hash y guarda evidencia+resolución juntas. Comando y etapa ENVIADA_SIN_CONFIRMAR se persisten antes de encolar; el estado es conservador, no prueba entrega. Reintentos leen el comando duradero y transmiten exactamente los mismos bytes, máximo dos a intervalos de 5 s; después SYNC_REQUEST nuevo. Un ACK tardío correlacionado en la misma conexión puede confirmar el envío existente, pero no rearma el desafío para otra orden. Al reconectar/reabrir no se reproduce el comando anterior; resultado por STATE solo si id/seq/hash coinciden. Descartar pendiente es local, no cancela una orden ya recibida por el hijo, no reutiliza contador ni borra evidencia.
- P05 ruta Android: RelayConnection entrega sobres MESSAGE a una cola acotada etiquetada con generación; no acredita firma del hijo. CanalPoliticasRelay exige red validada y canal autenticado con PONG vigente. MainActivity solicita sincronización al disponer de nueva conexión sana/red validada, consume STATE/ACK y ejecuta reloj solo con lifecycle STARTED; alternativa manual para sincronización, sin servicio oculto. Controles finales siguen deshabilitados (P06); no se promociona vínculo PREPARADO ni certifica recuperación.
- P05 evidencia JVM: veinte ControlPoliticasTest nuevos y dos RelayConnectionTest adicionales. Recorrido relay simulado → STATE → orden → ACK, firma impostora, respuestas ajenas/fuera de orden, máximo contador/duración, caducidad, fondo/cambio de canal durante reserva/firma/persistencia, cancelación de confirmación, dos reintentos, reconexión/reapertura, fallos atómicos de resolución, doble confirmación concurrente y temporizador de hijo simulado que no reinicia duración. No acreditan aplicación de políticas ni temporizadores en dispositivo real. Ver pruebas/EVIDENCIA_P05.md. Primer bloque 0531f94; cierre en commit posterior.

- P05 primer bloque local: ControlPoliticas valida STATE firmado/nonce/pareja/secuencia/sesión, exige confirmación explícita por ticket no reutilizable, reserva max(local, remoto)+1 y persiste comando/etapa conservadora antes de encolar. ACK correlacionado por id/seq/hash del payload/boot/sesión; dos retransmisiones de bytes exactos y luego nueva sincronización, nunca duración nueva por ACK perdido. ParentStore.resolverIntencion guarda evidencia y resolución juntas. No promociona PREPARADO ni acredita recuperación. Cuatro tests iniciales aprobados (65 app total); integración al transporte/negativos ampliados pendientes. Sin ADB ni cambios de servidor/CP/1.

- P04 cierre local parcial: transporte OkHttp 4.12.0 con WSS exclusivo, /ws, cp.v1, confianza/hostname del sistema, sin redirecciones ni bypass TLS. AUTH usa AutoridadPadre/Keystore; el cliente verifica pública y reto exactos antes de encolar. Canal sano solo tras PONG vigente, separado de presencia indicativa y red validada por Android. Backoff se reinicia al recibir PONG, no solo por AUTH_OK. Se descartan callbacks antiguos y sockets devueltos después de un fallo síncrono.
- P04 Android: apertura explícita desde Controles con vínculo PREPARADO/VINCULADO guardado; nunca crea identidad/pareja para conectar. Al salir de Controles se desconecta; al pasar a fondo cierra socket, bloquea autoridad y retira NetworkCallback. Al volver requiere autoridad vigente para reconectar, sin servicio oculto. RedAndroid solo observa la red predeterminada; su indicador no prueba acceso al relay ni autoriza órdenes. No se cambió CP/1 ni el proyecto servidor.
- P04 pruebas: diez casos RelayConnectionTest y seis TransporteOkHttpTest (16 nuevos respecto de P02). WSS/TLS real exclusivamente loopback JVM con certificados efímeros de test; certificado no confiable/hostname ajeno rechazados, redirección real no seguida y frame binario rechazado. Firma de tests JCA efímera, no evidencia de AUTH con Keystore físico ni R-AUTH-01 contra servidor externo. Ver pruebas/EVIDENCIA_P04.md.

- P04 primer bloque: RelayConnection separa conexión/reto/AUTH_OK/latidos/presencia indicativa; origen HTTPS/subprotocolo cp.v1, firma AUTH sobre reto exacto y pública configurada. Autoridad vigente antes de abrir y después de firmar; timeout monotónico, PONG correlacionado, backoff acotado/jitter, cierre al fondo y descarte de callbacks antiguos. No envía órdenes ni acepta STATE/ACK como evidencia (P05 pendiente). Ocho tests JVM nuevos aprobados, 53 app total. Transporte real y UI pendientes; servidor R02 es dependencia externa para integración, no impide tests aislados según plan.

- P02 cámara, resultado manual: tras actualizar APK y mostrar QR público en PC, el propietario respondió «sip, apareció un json con muchos datos» a la comprobación de escaneo. Lectura óptica y publicación de oferta en campo confirmadas por observación del propietario. No acredita prueba ampliada de denegación de permiso, cierre/reapertura/fondo/giros, enfoque en condiciones diversas ni integración con APK hijo. No se preparó vínculo de prueba; no usar ese QR para vinculación operativa.

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

PADRE-C01-R1 (documentación únicamente): padre/DISENO_ACREDITACION_VINCULO.md
(precisión de estatus + §7), COORDINACION_PROYECTOS.md y ESTADO.md.

PADRE-C01 (solo documentación): padre/DISENO_ACREDITACION_VINCULO.md,
COORDINACION_PROYECTOS.md y ESTADO.md. Commit de cierre por mensaje PADRE-C01.

P08-B: app-parent/build.gradle.kts;
app-parent/src/test/java/dev/controlparental/parent/IntegracionPoliticasRelayTest.kt;
pruebas/relay-politicas.mjs y pruebas/EVIDENCIA_P08_B.md;
README.md, ESTADO.md y pruebas/MATRIZ_ACEPTACION.md. Solo harness/pruebas/configuración opcional, sin fuentes de producción ni CP/1 cambiados.

P08-A: app-parent/src/main/java/dev/controlparental/parent/network/RelayConnection.kt;
app-parent/src/test/java/dev/controlparental/parent/RelayConnectionTest.kt;
README.md, ESTADO.md y pruebas/EVIDENCIA_P08_A.md. Sin cambios de dependencias, permisos o CP/1.

P07: app-parent/src/main/java/dev/controlparental/parent/recovery/OfflineChallengeSigner.kt;
app-parent/src/main/java/dev/controlparental/parent/ui/{RecoveryScreen,CamaraOferta,AplicacionPadre}.kt;
app-parent/src/main/java/dev/controlparental/parent/MainActivity.kt;
app-parent/src/test/java/dev/controlparental/parent/OfflineChallengeSignerTest.kt;
app-parent/src/androidTest/java/dev/controlparental/parent/PantallaRecuperacionTest.kt;
README.md, ESTADO.md y pruebas/EVIDENCIA_P07.md. Sin nuevos permisos/dependencias ni cambios CP/1/servidor.

P06 (prioritario): app-parent/src/main/java/dev/controlparental/parent/ui/{ControlScreen,AplicacionPadre,ConexionRelayScreen}.kt;
app-parent/src/main/java/dev/controlparental/parent/presentation/{AccionesControl,PresentacionControl}.kt;
app-parent/src/main/java/dev/controlparental/parent/{MainActivity,policy/ControlPoliticas,data/MetadatosPadre,data/ParentStore}.kt;
app-parent/src/test/java/dev/controlparental/parent/{PresentacionControlTest,ParentStoreTest,ControlPoliticasTest,PairChildTest}.kt;
app-parent/src/androidTest/java/dev/controlparental/parent/{PantallaControlTest,VinculoPersistidoAndroidTest}.kt;
README.md, ESTADO.md y pruebas/EVIDENCIA_P06.md. Sin permisos/dependencias nuevos ni cambios externos.

P05 (prioritario): app-parent/src/main/java/dev/controlparental/parent/policy/ControlPoliticas.kt;
app-parent/src/main/java/dev/controlparental/parent/data/ParentStore.kt;
app-parent/src/main/java/dev/controlparental/parent/network/{RelayConnection,CanalPoliticasRelay}.kt;
app-parent/src/main/java/dev/controlparental/parent/MainActivity.kt y ui/ConexionRelayScreen.kt;
app-parent/src/test/java/dev/controlparental/parent/{ControlPoliticasTest,RelayConnectionTest}.kt;
README.md, ESTADO.md y pruebas/EVIDENCIA_P05.md. Sin nuevas dependencias ni cambios de formato privado/CP/1.

P04 (prioritario): app-parent/src/main/java/dev/controlparental/parent/network/{RelayConnection,TransporteOkHttp,RedAndroid}.kt;
app-parent/src/main/java/dev/controlparental/parent/ui/{ConexionRelayScreen,AplicacionPadre}.kt y MainActivity.kt;
app-parent/src/test/java/dev/controlparental/parent/{RelayConnectionTest,TransporteOkHttpTest}.kt;
app-parent/src/main/AndroidManifest.xml, app-parent/build.gradle.kts, gradle/libs.versions.toml;
README.md, BUILD_ENV.md, ESTADO.md y pruebas/EVIDENCIA_P04.md.
Las rutas siguientes son del cierre histórico P03.

README.md, BUILD_ENV.md, ESTADO.md, app-parent/build.gradle.kts.
app-parent/src/main/java/dev/controlparental/parent/data/MetadatosPadre.kt, ParentStore.kt, ArchivoAtomicoMetadatos.kt, AlmacenPadreAndroid.kt.
app-parent/src/main/java/dev/controlparental/parent/security/CifradoMetadatos.kt y MetadataKeyStore.kt.
app-parent/src/test/java/dev/controlparental/parent/CifradoMetadatosTest.kt y ParentStoreTest.kt.
app-parent/src/androidTest/java/dev/controlparental/parent/AlmacenAndroidTest.kt.
pruebas/MATRIZ_ACEPTACION.md, pruebas/EVIDENCIA_P03.md y pruebas/HANDOFF_P03.md.

## Pruebas ejecutadas y resultado

- P08-B: prueba opcional focalizada repetida y suite completa con -PrutaRelayIntegracion/--rerun-tasks correctas; 130 tareas ejecutadas, XML 111 app (una integración real) + 30 núcleo + 1 interop, sin fallos/errores/omisiones. Después scripts/verificar.ps1 sin ruta externo: 110 app + 30 núcleo + 1 interop y cinco oráculo Node, 146 independientes; integración excluida, no omitida ni dependencia obligatoria. Total demostrado: 147 incluyendo la opcional. APK debug/instrumentado/release sin firma de distribución compilados; lint cero errores/23 avisos. Diez vectores/78 comprobaciones documentales. Repositorio externo limpio antes/después; no build/instalación/npm allí. Sin instrumentados ejecutados.

- P08-A: tests app repetidos con --rerun-tasks correctos; Gradle completo repetido (app/núcleo/interoperabilidad, debug/instrumentado/release sin firma de distribución y lint): 130 tareas ejecutadas correctamente. scripts/verificar.ps1 correcto. XML: 110 app + 30 núcleo + 1 interop, cero fallos/errores/omisiones; cinco oráculo Node, total 146 locales. Diez vectores/78 comprobaciones documentales; lint cero errores/23 avisos. No instrumentados ejecutados ni harness externo en este turno.

- P07: scripts/verificar.ps1 correcto; Gradle completo --rerun-tasks, 130 tareas ejecutadas correctamente (tests app/núcleo/interoperabilidad, debug/instrumentado/release sin firma de distribución y lint). XML: 104 app + 30 núcleo + 1 interop, cero fallos/errores/omisiones; cinco oráculo Node, total 140 locales. Diez vectores/78 comprobaciones documentales; lint cero errores/23 avisos. Dos tests instrumentados nuevos compilados/no ejecutados. Sin ADB, instalaciones, restablecimientos, servidor externo ni despliegue.

- P06: scripts/verificar.ps1 correcto y Gradle completo con --rerun-tasks (tests app/núcleo/interoperabilidad, debug/instrumentado/release sin firma de distribución y lint): 130 tareas ejecutadas correctamente. XML: 96 app + 30 núcleo + 1 interop, cero fallos/errores/omisiones; oráculo Node cinco aprobados, total 132 locales. Diez vectores/78 comprobaciones documentales; lint cero errores/23 avisos. Primer intento de recompilación instrumentada falló por lambda en posición del nuevo parámetro Boolean de ControlScreen; se corrigió la llamada de test con autenticar nombrado, sin relajar controles. Cinco tests UI compilados, no ejecutados; modificación del test Android de migración compilada/no repetida en dispositivo. No instalaciones/ADB ni integración real.

- P05: scripts/verificar.ps1 correcto. Repetición Gradle --rerun-tasks de tests app/núcleo/interoperabilidad, debug/instrumentado/release sin firma de distribución y lint: 130 tareas ejecutadas correctamente. Tras ajustes finales de conexión automática/guardas y un test de concurrencia, script y release repetidos correctos. XML final: 83 app + 30 núcleo + 1 interop, cero fallos/errores/omisiones; cinco oráculo Node, total 119 locales. Diez vectores/78 comprobaciones documentales; lint cero errores/23 avisos. Un test de reapertura falló inicialmente porque el simulador retrocedía lastAcceptedSeq; se corrigió el simulador, conservando la guarda de producción. No ADB, APK instalados, pruebas físicas, servidor externo ni despliegue.

- P04: scripts/verificar.ps1 correcto. Repetición Gradle --rerun-tasks de tests app/núcleo/interoperabilidad, APK debug/instrumentado/release sin firma de distribución y lint: 130 tareas ejecutadas correctamente. Reportes XML: 61 app + 30 núcleo + 1 interop, cero fallos/errores/omisiones; oráculo Node cinco aprobados, total 97 locales. Diez vectores/78 comprobaciones documentales; lint cero errores/23 avisos. No ADB, instalación, pruebas físicas, comunicación con APK hijo, servidor externo ni despliegue.

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

PADRE-C01-R1 cerrada documentalmente, acuerdo público compatible como propuesta.
Coordinador debe revisar §7 de padre/DISENO_ACREDITACION_VINCULO.md y solicitar
decisión de puertas/orden al propietario. PADRE-C02 sigue sin activar: validador
puro con inputs/supuestos/negativos definidos, sin promoción/IO/UI/órdenes; esperar
asignación explícita. H08/H10/H11 y evidencia física no acreditados por este acuerdo.
No nuevas pruebas/builds ni permisos; A56 disponible no habilita ADB, A13 excluido.
Resultados en documentos/respuesta, sin mensajes de vuelta ni bucles de agentes.
Las indicaciones siguientes son historial, no nuevas asignaciones.

PADRE-C01 cerrada documentalmente. Próxima unidad propuesta PADRE-C02, no activada:
validador puro de revisión y pruebas negativas JVM, sin promoción productiva ni
migración/escritura VINCULADO. Esperar revisión/asignación del coordinador y acuerdo
del hijo sobre su evidencia de commit; leer padre/DISENO_ACREDITACION_VINCULO.md.
Bloqueos H08/H10/H11, migración privada segura, entorno TLS/red y autorización
física posteriores. A56 disponible no permite ADB por esta tarea; A13 excluido.
No responder con otra tarea a la sesión coordinadora: resultados en documentos
y respuesta de cierre, sin bucle de mensajes entre agentes.

Handoff de coordinación añadido en COORDINACION_PROYECTOS.md por petición del propietario: IA de parental_render orquestará trabajo restante, sin transferir permisos de dispositivo/despliegue ni autorizar edición cruzada. Incluye estado, comandos, dependencias hijo/TLS, límites y puerta operativa PREPARADO→VINCULADO todavía pendiente. Cambios documentales: COORDINACION_PROYECTOS.md, AGENTS.md y ESTADO.md; no código, ADB ni pruebas nuevas en este turno. Próximo paso: recibir asignación concreta del coordinador tras identificar hijo y entorno autorizado.

P08-B cerrada localmente; siguiente subtarea P08-C: acordar con propietario/rol hijo entorno para comunicación entre APK reales, confianza TLS local y recuperación presencial acreditada. Antes de instalar/probar A56, avisar y esperar habilitación nueva; A13 excluido. No usar 127.0.0.1 del PC como destino directo en un teléfono ni instalar CA de test sin acuerdo. La integración JVM no autoriza promocionar PREPARADO ni demuestra restricciones del hijo; no entrega/despliegue. P08 completa depende de evidencias reales G1/G3 y firma de distribución. Pendientes ampliables posteriormente en tests: reinicio del proceso relay/ACK totalmente perdido, no solo reconexión del padre y retención de ACK hasta tercer envío.
La siguiente referencia histórica a P08-B ya fue atendida.

P08-A cerrada localmente. Siguiente subtarea P08-B: probar ControlPoliticas/ParentStore contra relay real en JVM aislada con hijo simulado apto solo en tests. Consultar padre/PLAN_PADRE.md P08, SPEC-P P5/P6, CP/1 §7/8 y el handoff del servidor documentacion/INTEGRACION_LOCAL_PADRE.md. No modificar parental_render: acordar su punto de entrada para un harness opcional del padre; conservar verificación independiente y comparar hashes congelados antes de conectar. El driver externo documentado omite ControlPoliticas y no prueba sus guardas. Artefactos del padre recompilados tras corregir latidos. P08 completa/entrega, APK/hijo reales y recuperación física siguen pendientes; PREPARADO no se promociona ni autoriza órdenes. Sin ADB hasta aviso/autorización nueva A56, A13 excluido; no despliegue.
Las próximas tareas descritas a continuación se conservan como historial, no sustituyen P08-B.

P07 implementado localmente. Próxima tarea: P08, coordinación de integración y entrega privada, bloqueada por servidor autenticado e hijo compatible y por aceptación física pendiente. El propietario debe orquestar el servidor en parental_render; probar comunicación local antes de cualquier despliegue. Base padre preparada para iniciar esa coordinación, no producto terminado. P-UI-01/02, texto grande/rotación/fondo, QR/autenticación OFFLINE reales y recuperación presencial siguen pendientes. No promocionar PREPARADO automáticamente ni certificar funciones del hijo por simuladores. Antes de actualizar/probar A56, avisar y esperar autorización nueva. No operar A13: la referencia histórica de P08 a ese teléfono no autoriza su uso. No restablecer teléfonos ni desarrollar servidor aquí.
La indicación histórica siguiente de próxima tarea P07 ya fue atendida en este turno.
La indicación histórica siguiente de próxima tarea P06 ya fue atendida en este turno.

P05 implementado localmente; integración externa H09/H10/R02 pendiente. Próxima tarea local: P06 de padre/PLAN_PADRE.md, SPEC-P P3/P6, interfaz final de acciones/duración/confirmación, última evidencia, causas y pendiente. No habilitar órdenes con PREPARADO ni fingir prueba de recuperación local. El descarte local debe avisar que no revoca una orden recibida; después exige sincronizar y confirmar de nuevo. Selección offline aún sin formulario/cola de nuevas órdenes; no firmar sin contexto válido. Padre todavía no preparado para integración completa: P06/P07 y aceptación física/integrada pendientes. El propietario orquestará el servidor; comunicación local antes de despliegue, sin agentes ni cambios externos desde aquí.
P04 queda implementado localmente; pruebas físicas y autenticación conjunta siguen pendientes. La indicación histórica siguiente de próxima tarea P05 ya fue atendida en este turno.

P04 local implementado, aceptación física/integrada pendiente. Próximo desarrollo: P05 de padre/PLAN_PADRE.md (CP/1 §7, SPEC-P P5), sincronización de STATE y envío idempotente; no se implementó en este turno. R02 del servidor es puerta externa para probar autenticación conjunta, no está acreditada por MockWebServer. Padre aún no preparado para integración completa: faltan P05 y controles/recuperación finales. Avisar al propietario al completar esa parte; él orquestará servidor e integración local antes de despliegue. No acceder por ADB sin aviso/autorización nueva, nunca operar A13.
P02: la lectura óptica básica ya está confirmada por el propietario en A56; solo quedan las pruebas ampliadas descritas en HANDOFF_P02.md. Las referencias históricas siguientes a lectura básica pendiente no exigen repetirla.

Próximo paso físico P03 completado: tres pruebas autorizadas/aprobadas en A56. No repetir por referencias históricas a pendientes. Antes de nuevas instalaciones/pruebas, avisar y esperar permiso; no tocar A13 ni ejecutar connected contra todos los dispositivos.
Próximo paso P02: pedir autorización para actualizar APK y probar cámara/permiso reales en A56 con QR público de pruebas mostrado en PC, sin crear identidad ni vínculo cotidiano. Comprobar aceptar/denegar permiso, cancelar, lectura, giros, salir/fondo y ausencia de cámara al regresar hasta apertura explícita; regresión de diez casos aislados si se autoriza. No cambiar permisos por ADB sin acuerdo ni operar A13. Cámara local implementada pero pendiente físicamente; revisión selector/texto grande/FLAG_SECURE real y PAIR autenticado/hijo real siguen pendientes. Ver HANDOFF_P02.md. Aún falta cliente WSS/órdenes para integrar; avisar al propietario cuando esté preparada parte padre, con integración local antes de despliegue.

## Seguridad

No almacenar claves, códigos, QR sensibles, contraseñas ni datos del menor aquí.
