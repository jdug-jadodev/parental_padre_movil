# Estado de implementación

Última actualización: 2026-10-15
Commits C00: 4cc7a39, e25bff5, 00329bd. C01: f7e2044, dc50004, 6725515. C02: f120557, 68348c2. P00: 37c74a3, 943082c. Primer bloque P01: 0f143c4; cierre local en el commit posterior, consultar git log.
Tarea actual: PADRE-INTEROP01 — verificación vector Node y publicación de vuelta JVM pública
Estado: PUBLICAR_VECTOR_JVM_Y_VERIFICO_NODE — Node→JVM codecs reales aprobado; salida JVM pública autoverificada publicada, pendiente validación Node posterior; sin teléfonos/producción cambiada

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

- PADRE-INTEROP01: fuenteNodeCP2-I01 íntegra recibida, SHA967ae05aecd28c6b7f90b8ae90e8421b408dd70acbf22fd30911085c69a964ec copiada solo core-protocol/src/test/resources/cp2_node_publico.json. Oferta/PAIR/AUTHPARENT/CHILD verificados con codecsCP2reales, bytesoriginales/hash/contexto/identidades y negativosdominio/rol/clave/bytes. Assert cantidad10+conjunto exactoPAIR_ACCEPT/schema; referencia11corregida documentalmente sin codec/schema/comandos históricos alterados. Una salida pruebas/interop_cp2_jvm_publica.json SHAfb830c79349479eca0e7ee5392c05d5a32ca62157af7e00fd1d890dcf2c65d4b generada/autoverificada solo públicas, claves efímerasRAM.4tests nuevos; suitefinal34core+177app=211distintos,0fallos/errores/omisiones. Sin Node invocado, comprobación vueltaNode pendiente. No APKrebuild/lint/ADB/física. JARmain/sourceSets y APKexistentes inspeccionados sin marcadoresnuevos; evidencia pruebas/EVIDENCIA_PADRE_INTEROP01.md. Producción/PAIR02sininstalar/F02cerrada intactos.

- PADRE-PAIR02: MAIN conecta VincularCp2 a identidad/autoridad existentes y área públicaCP2 separada noBackup/AtomicFile/readback. Fuente real validada legadoPREPARADO/SIN_VINCULAR coherentes sin evidencia/secuencias/pendientes, consentimiento nuevoenrolamiento+auth; activo/corrupto/ausente/incierto/identidaddistinta bloqueados. ParentStore.inspeccionarSinMigrar no escribe legado.3códigos/hashCPv2RECOVERY soloRAM, confirmacioneshuellas/origen/códigos antesfirma real; TTL5minoriginal/salida/fondo/contexto/prompt/error invalidanQR/códigos. Persistencia firmada íntegra soloPENDIENTE_CONFIRMACION_HIJO; reinicio no reabreQR/códigos ni otra preparación sobrependiente; exportpúblico etiquetado sinAUTH/WSS/ACTIVO.14JVMnuevos, fixture177app+30core+1interop=208distintos; normalfinal176app sinfallos/errores/omisiones. Nuevo testAtomicAndroid solo compilado, APKdebug/test/releaseunsigned/lint0errores/23avisos versión porvariante. Commitscaf4333/371b07d/2511353; evidencia pruebas/EVIDENCIA_PADRE_PAIR02.md con hashes reales finales/órdenes/límites. Sin ADB/física ni garantizar fuenteA56compatible; sigue instaladoAUTH01F02. SchemaCP1/CP2/fixtures intactos, PREPARADOlegado sinpromoción.

- PADRE-PAIR01-F02 (15oct): autorización física nueva/aviso previo; artefacto0b010d7/hash33ef534732cf2ce0e3a3e1acc1ca2afc2b9d98ac5266373d9ba11cac54d4dd7a comprobado sin rebuild. Una actualización -r A56 Success desde hashQR02; hash instalado y final idéntico al autorizado. Primer descubrimiento abortó por aviso daemon PowerShell antes de dispositivo/install; luego selección única/modelo explícitos, sin repetir instalación. Propietario confirmó cámara MAIN→Vinculación→NUEVO PERFILCP2 lista antes de generar manualmente oferta nueva A13; después «si aparece» a ofertaCP2leída+huella hijo. Cierre sesión/QR A13 y cámara/perfil A56 confirmado «Cerrado, sin permiso nuevo». Sin ADB A13/bytes exactos ópticos/capturas/payload/store/firmar/commit/migración/órdenes. Evidencia pruebas/EVIDENCIA_PADRE_PAIR01_F02.md; legadoPREPARADO sin promoción. AUTH01/PAIR01 pendientes de integración persistencia/snapshot/ceremonia.

- PADRE-AUTH01: schemaAUTH Renderd99ddac suministrado en conversación copiado separado, codecDTOs/firmasCPv2AUTH LFreal/originalbytes/DERrango/perfil estricto. Borde puro por generación/reto10s/envío confirmado antesAUTH_OK/callbackstale/replay/cierre-fondo/snapshotreloj; sin snapshotcompatibleexterno→NO_DISPONIBLE. Firmante inyectable sobre almacén existente/sesión sin crear/rotar; no conexión MAIN/WSS/ParentStore o migraciónCP1.12tests nuevos, fixture163app+30core+1interop=194distintos y normal162app,0fallos/errores/omisiones. Buildsdebug/test/releaseunsigned/lint0errores/23avisosversión. Código0b010d7; evidencia pruebas/EVIDENCIA_PADRE_AUTH01.md con hashesAPKpostcommit/comandos/inspecciónDEXdirigida sin148clasesJVMtest/diagnóstico/C02/falso/fixture/schemaAUTH en APKapp. Sin red/dispositivos/ADB. AUTH_OK no hijo operativo/APPLIED. PAIR01snapshotMAIN/persistencia pendientes intactos.

- PADRE-PAIR01: codecCP2 separado+schema íntegro recibido de Render4ac4962, sin lectura externa/CP1modificado. MAIN ofrece NUEVO PERFILCP2 para cámara/huellas sin pareja, RAM5min/salida/fondo; todo legado/incierto REQUIERE_MIGRACION y firma/códigos nuevos bloqueados. PrepararPairCp2 puro sin store/commit probado: snapshot autorizado/coherente, ticket/gen/boot/TTL/auth/huellas/origen/códigos,3Base32/hashCPv2RECOVERY y firma bytesoriginales CPv2PAIR; no conexión productiva de firma/alta/migración.9tests nuevos; suitefixture151app+30core+1interop=182distintos, normal150app,0fallos/errores/omitidos. APKdebug/test/releaseunsigned+lint0errores/23avisos versión por variante. Código82734c7; evidencia pruebas/EVIDENCIA_PADRE_PAIR01.md con rutas/comandos/hashes postcommit y empaquetado dirigido sin test/fixture en APKapp. Sin ADB/instalaciones/física; A56 conservaQR02F01. No promoción PREPARADO ni listo hijo/relay acreditados.

- PADRE-QR02-F01: autorización específica y aviso previos; una actualización app debug -r reportó Success, sin datos borrados deliberadamente. Ante contradicción del propietario se suspendió atribución y se comprobó presencia física actual: retirar cable A56→0 A56, reconectar→1 A56; modelo y SHA256 del APK propio confirmado 7f3947394c5bb921a11539a66ed7162029818702d0fc1a3181c53a5b55d6f534, sin reinstalar. Propietario confirma escáner Recuperación deshabilitado: no preview/lectura física ni CAMERA concedido. No alta ficticia ni guarda retirada; código ff9aa4c intacto, PREPARADO sin promoción. Evidencia pruebas/EVIDENCIA_PADRE_QR02_F01.md. No se acredita retrospectivamente conexión de primera instalación ni lectura de bytes ópticos.

- PADRE-QR02: reproducido fixture fijo original antes del fix; corrección productiva ff9aa4c en QrLocal,4 orientaciones geométricas RGB/Y con límite cooperativo400ms, errores checksum/formato explícitos y UTF8 estricto/texto sin normalizar. Sin PURE/máscara fija/nueva firma/deps/codec/CP1/UI cambiados.9 tests nuevos normales+1 estrategia; test fijo diagnóstico exige éxito real, baseline directo conserva fallo conocido.48 variantes=96 lecturas exactas/firma,4 rotaciones,cizalla limitada/negativos/límites. Suite fixture142app+30core+1interop=173 ejecutados distintos; suite normal141app, todas sin fallos/errores/omisiones. Builds debug/testAPK/release unsigned/lint debug-release correctos,23warnings de versiones por variante. Evidencia pruebas/EVIDENCIA_PADRE_QR02.md; hashes finales postcommitff9aa4c y DEX/ZIP inspeccionados sin141 clases JVMtest/C02/diagnóstico/falso o marcadores fixture en dos APK app. FIX_VALIDADO_SINTETICO/PREPARADO_FISICO, no cámara/hijo/enforcement/vínculo/CP2 aprobados. Sin dispositivos/ADB/muestreo diagnóstico nuevo ni pruebas ajenas. Historial siguiente conserva NO corregido en cierres anteriores.

- PADRE-C04-R1: consolidado mismo padre/BORRADOR_CP2_ORDENES_Y_TIEMPO.md sobre1582e66. Fuentes HIJO-C07 8b25c24 y revisión relay13oct íntegras como textos suministrados, no lecturas externas. Decisiones finales: ALLOW libre ambas fuentes, LOCK inmediato, ALLOW_FOR60..86400 múltiplo60 (minutos enteros sin preselección), sin apertura automática; autorización incluye apagado/Keyguard. Historia varios días domingo21 local/array8 con retentionSeq/historyRevision/época/tombstone/readback/catch-up/purga conservadora y ledger activo separado sin cancelar permiso ni resetear seq/consumos/esperas. LastExpiry limitado ventana/detección; push hint CONSULT_STATE evaluable no servicio activo/estado factual. §10 propone mínimos PAIR/OFFLINE/legado seguro bloqueantes de versión; no nuevas autoridades/reset remoto/recibos exactos o CP1 modificado. §14 candidatos bytes derivados manualmente de tipos, no cotas probadas ni tests; relay ejercicio2966/4122 reportado no reejecutado/crypto/protocolo. Pocos bloques pendientes §12 y mapa lab/productivo separados sin habilitar H05/H06/puertas. QR a8e4ffc NO corregido. Textos de C04 siguientes son históricos donde opciones supersedidas.

- PADRE-C04: borrador separado padre/BORRADOR_CP2_ORDENES_Y_TIEMPO.md sobre 984693c, incorpora contexto/requisitos íntegros enviados por coordinador sin lecturas externas. Tablas exactas de perfil/sobre/firma/canonización/hash/roles/tipos/mensajes AUTH/SYNC/SET_POLICY/STATE/ACK/errores; revisión global distinta de seq, desafío operación/boot/revisión, último remoto distinto de snapshot efectivo, ACK tardío/duplicados/desconocido sin replay. Contador absoluto de autorización CONSUMIDA no humano ni UsageStats; continuidad/calidad/reconstrucción durable mismo boot/gaps/reboot/día parametrizado, sin sumar duration ni deltas/historial. Rango/LOCK_FOR y pantalla-Keyguard/día-retención/avisos propuestos pendientes (cuatro elecciones §12). Nuevos requisitos de código numérico 5–10/pregunta local/apps normales y dos fuentes offline registrados; no secretos en protocolo/relay, ni permiso/código fabricado al migrar. Trazas documentales no fixtures, matriz futura por rol y mapa revisable, sin aprobación versión/puertas/H05/H06. QR a8e4ffc reproducido NO corregido y no reejecutado.

- PADRE-C03: revisado HIJO-C03 9ae6c99 y propuesta/relevo centrales solo en lectura. Resultado documental en padre/REVISION_CONTROL_LOCAL_ADULTO.md: administración RAM distinta de permiso normal por boot y recuperación única, última acción fresca serializada propuesta, timers/desafíos viejos invalidados (incluido LOCK_FOR), STATE efectivo/fuente/revisión distinto del resultado remoto id/seq/hash. ACK tardío/modo local no prueban APPLIED actual. Lista de decisiones y mapa mínimo de subunidades/puertas; duraciones/credencial/KDF/precedencia/recuperación/apps/migración pendientes de propietario y versión de tres roles. Apps aprobadas es interpretación recomendada no impuesta; recibos exactos no requeridos automáticamente. Sin código/tests/builds/CP1/fixtures/planes/ADR aceptados/dispositivos/repos externos modificados. H05/enforcement/puertas siguen pendientes; H05-L01/PADRE-C02 no acreditan requisito nuevo.

- PADRE-QR01: muestra única de 64 pares/128 sobres CP/1 efímeros: retos 64 correctos, respuestas 61 correctas y tres NotFoundException (39/56/63). Caso 39 público conservado; versión15/máscara4, original siempre falla, variantes de margen/escala fallan, otras siete máscaras leen bytes/firma exactos. PURE como control aislado recupera contenido válido; detector escoge falsa esquina inferior izquierda, lector QR directo ChecksumException convertido en NotFound observable por QrLocal. Confirmado camino de detección productivo, no error de render test ni orden necesario para caso fijo; sin corrección autorizada de producción/dependencia. Evidencia pruebas/EVIDENCIA_PADRE_QR01.md. No cambiar test P07/C02 o CP/1 ni declarar estabilidad por suite verde.

- PADRE-C02: ValidadorRevisionVinculo en sourceSet solo JVM test (revisionAislada), sin APK/androidTest, IO, Android, UI, callbacks, permisos, token, ParentStore/migración o escritura VINCULADO. Modelos inmutables y frames defensivos; verificación CP/1 PAIR/MESSAGE/OFFLINE con claves del snapshot, resumen exacto, generación/contexto/secuencia/tiempos y observaciones FICTICIAS_JVM. Diagnóstico coherente/incompleto/rechazado, no acreditación física. 21 tests nuevos correctos; evidencia pruebas/EVIDENCIA_PADRE_C02.md. No decisiones de puertas/orden hijo ni cambio CP/1; sin ADB/repos externos/subagentes.

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

INTEROP01: core-protocol/build.gradle.kts; src/test/resources/cp2_node_publico.json;
src/test/kotlin/dev/controlparental/protocol/cp2/{InteropCp2Test.kt,GenerarVectorPublicoCp2.kt};
pruebas/interop_cp2_jvm_publica.json y EVIDENCIA_PADRE_INTEROP01.md; contrato-cp2/README.md,
evidenciasPAIR01/PAIR02 y relevo (conteo10), ESTADO.md. Sin producción/schema/CP1fixtures.

PAIR02: app-parent/.../data/{ParentStore.kt,PairStoreCp2.kt,AlmacenPairCp2Android.kt};
pairing/{PrepararPairCp2.kt,VincularCp2.kt}; security/AutoridadPadre.kt;
ui/OfertaCp2Screen.kt; MainActivity.kt; test/PairIntegradoCp2Test.kt;
androidTest/PairStoreCp2AndroidTest.kt; pruebas/EVIDENCIA_PADRE_PAIR02.md,
contrato-cp2/README.md, ESTADO.md y relevo. Sin schema/deps/codecCP1/CP2 cambiados.

PADRE-PAIR01-F02: pruebas/EVIDENCIA_PADRE_PAIR01_F02.md, ESTADO.md y
COORDINACION_PROYECTOS.md (solo documentación). Sin código/build/tests nuevos.

PADRE-AUTH01: contrato-cp2/{autenticacion.schema.json,README.md}; core-protocol/.../cp2/{AutenticacionCp2.kt,VinculacionCp2.kt(3helpersinternal)};
app-parent/.../{network/AuthClienteCp2.kt,security/FirmanteAuthCp2.kt}; test/.../AuthCp2Test.kt;
app-parent/build.gradle.kts; pruebas/EVIDENCIA_PADRE_AUTH01.md, ESTADO.md y relevo.
CP1/compartido/MAIN/UI/RelayConnection/ParentStore intactos.

PADRE-PAIR01: contrato-cp2/{vinculacion.schema.json,README.md}; core-protocol/src/main/kotlin/dev/controlparental/protocol/cp2/VinculacionCp2.kt;
app-parent/src/main/java/dev/controlparental/parent/{MainActivity.kt,pairing/PrepararPairCp2.kt,ui/OfertaCp2Screen.kt,ui/CamaraOferta.kt};
app-parent/src/test/java/dev/controlparental/parent/PairCp2Test.kt; app-parent/build.gradle.kts;
pruebas/EVIDENCIA_PADRE_PAIR01.md, este estado y COORDINACION_PROYECTOS.md. CP1/compartido/QR02intactos.

PADRE-QR02-F01 (solo documentos): pruebas/EVIDENCIA_PADRE_QR02_F01.md,
COORDINACION_PROYECTOS.md y ESTADO.md. Auxiliar/PNG públicos solo temporal PC,
no repo/APK. Sin código/build/tests nuevos.

PADRE-QR02: app-parent/src/main/java/dev/controlparental/parent/pairing/QrLocal.kt;
app-parent/src/test/java/dev/controlparental/parent/{DiagnosticoQrTest,EstrategiaQrTest,QrLocalRegresionTest}.kt;
pruebas/EVIDENCIA_PADRE_QR02.md, COORDINACION_PROYECTOS.md y ESTADO.md.
Fixture QR01/evidenciaQR01/compartido/codec/C02/main restante intactos; sin config/deps.

PADRE-C04-R1: padre/BORRADOR_CP2_ORDENES_Y_TIEMPO.md (consolidado, no segundo draft),
INTEGRACION_FUTURA.md, COORDINACION_PROYECTOS.md y ESTADO.md. Solo documentos propios;
fuentes/CP1/codec/schema/fixtures/PLAN/ADR aceptados/C02/main intactos.

PADRE-C04: padre/BORRADOR_CP2_ORDENES_Y_TIEMPO.md (nuevo draft),
padre/REVISION_CONTROL_LOCAL_ADULTO.md (nota incremental), INTEGRACION_FUTURA.md,
COORDINACION_PROYECTOS.md y ESTADO.md. Solo documentación; siguientes listas históricas.

PADRE-C03: padre/REVISION_CONTROL_LOCAL_ADULTO.md (nueva revisión);
padre/DISENO_ACREDITACION_VINCULO.md (nota de alcance, sin reescribir diseño CP/1);
INTEGRACION_FUTURA.md (propuesta pendiente), COORDINACION_PROYECTOS.md y ESTADO.md.
Solo documentación; lista siguiente conserva cambios históricos de QR01.

PADRE-QR01: app-parent/build.gradle.kts (diagnóstico JVM opt-in);
app-parent/src/test/java/dev/controlparental/parent/DiagnosticoQrTest.kt;
app-parent/src/test/resources/qr01_respuesta_publica.json;
pruebas/EVIDENCIA_PADRE_QR01.md, COORDINACION_PROYECTOS.md y ESTADO.md.
Solo test/configuración/documentación; fuentes productivas sin cambios.

PADRE-C02: app-parent/build.gradle.kts;
app-parent/src/revisionAislada/kotlin/dev/controlparental/parent/pairing/RevisionVinculo.kt;
app-parent/src/test/java/dev/controlparental/parent/RevisionVinculoTest.kt;
pruebas/EVIDENCIA_PADRE_C02.md; padre/DISENO_ACREDITACION_VINCULO.md,
COORDINACION_PROYECTOS.md y ESTADO.md.

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

- INTEROP01: generadorJavaExec una vez BUILD SUCCESSFUL(3ejecutadas/3UP-TO-DATE), focal4tests(2/4), final34core+177app=211distintos(28/0),0fallos/errores/omitidos. No sumar repeticiones/UP-TO-DATE/autoverificación como tests; no Node/testInteropCP1/normal separado/APKbuild/lint/instrumentación nuevos. HashNode exacto/ambosvectores8cadenas+LF comprobados; JARmain y APKPAIR02existentes0marcadoresnuevos/testhelpers/públicas/sobres. JARmain recompuesto en tareasJVM, APKsinrebuild; evidencias/hashes/límites en INTEROP01. JVM→Node aún no comprobado por Node aquí.

- PAIR02:5órdenes Gradle BUILD SUCCESSFUL; focal21JVM(5ejecutadas/20UP-TO-DATE); focal23JVM+APKtest(9/45); fixture177app+30core+1interop(29/0); normal176app+build/lint(131/0); ajusteUIfinal2511353 y normal176app+build/lint(30/101). No sumar repeticiones/UP-TO-DATE; test normal sí ejecutado final, APKtest final reutilizado de buildanterior.0fallos/errores/omisiones; lintAnalyze final ejecutado, reportesidénticosUP-TO-DATE0errores/23warningsversiones porvariante. AtomicFileinstrumentado compilado/noejecutado. DEX/ZIP dirigido235clasesJVM+AndroidTest contrastadas,0coincidencias/falso/C02/diagnósticos/marcadoresfixtures enAPKapp;568propias/4clasescontroller-store-adaptador-autoridad presentes. Hashespostcommit2511353 y límites en evidenciaPAIR02. Sin dispositivos/ADB ni tests de otrosrepos.

- F02física: lectorlisto confirmado por propietario, ofertaCP2yhuella hijo visibles confirmadas por chat; sin verificación de bytes ópticos exactos. HashAPKA56 medido previo7f3947…f534, instalado/final33ef5347…d4dd7a completo en evidencia. Una install-r Success, sin testAPK/Gradle/instrumentación/logcat/capturas/lecturaQRbody. Cierre manual confirmado sin CAMERA nuevo. Pruebas JVM de AUTH01 anteriores no se reejecutaron ni se suman aquí.

- AUTH01: focal11tests BUILD SUCCESSFUL(7ejecutadas/18UP-TO-DATE); final12nuevos incluidos en163app+30core+1interop(29tareas ejecutadas); normal162app+APKdebug/test/releaseunsigned/lint(131tareas ejecutadas). Cero fallos/errores/omitidos; sin sumar repeticiones/UP-TO-DATE. Lint0errores/23warnings versión/deps por variante; .so sinstrip advertidas. DEX/ZIP dirigido148clasesJVMtest contraAPKapp:0coincidencias/diagnóstico/C02/falso/marcadoresfixture/schemaAUTH,3clasesAUTH y518propias presentes/APKapp. Comandos/hashes concretos evidenciaAUTH01; no AndroidTest ejecutado/ADB/red/125testsRender reportados.

- PADRE-PAIR01: focal8tests BUILD SUCCESSFUL(8ejecutadas/17UP-TO-DATE); suitefixture final9nuevos incluidos en151app+30core+1interop(29tareas ejecutadas); normal150app+APKdebug/test/releaseunsigned/lint(131tareas ejecutadas). Cero fallos/errores/omitidos, sin contar repeticiones/UP-TO-DATE. Corregido warning nullable de property en test antes del build final; sin errores de pruebas. Lint23warnings versiones/0errores por variante. DEX/ZIP dirigido143clases JVMtest contrastadas,0coincidencias/fixture/schema en APKapp;503clases propias/codec y preparadorCP2 presentes. Comandos/hashes concretos en evidenciaPAIR01; no dispositivos/instrumentación/relay externo ni físicas.

- PADRE-QR02: seis comandos Gradle BUILD SUCCESSFUL con25/25/25/29/131/113 tareas ejecutadas. Baseline fixture viejo caracterizó3NotFound+Checksum directo; estrategia fija8scans (2Checksum esperados,6éxitos); focal29tests; final fixture142app+30core+1interop; normal141app+build/lint; rebuild APK postcommitff9aa4c. No contar ejecuciones repetidas/UP-TO-DATE como nuevos tests.9regresión+1estrategia nuevos,48variantes/96lecturas sin fallo; negativos explícitos. Lint0errores/23warnings cada variante, .so sin strip advertido. DEX/ZIP de dos APK app contrastado contra141 clases JVMtest:0 coincidencias/fixture; QrLocal sí presente. Hashes/órdenes completos en evidencia. Sin test Android ejecutado/Node/relay/CP2/QR cámara ni validación física. Sin nuevos secretos o repos externos. Cierre códigoff9aa4c y documental por mensaje «PADRE-QR02: registrar regresión y APK preparados para prueba física».

- PADRE-C04-R1: cero tests/builds/scripts de tamaño/crypto/mediciones/dispositivos. Solo lecturas locales/revisión de textos suministrados/documental y Git(diff --check, revisión preparado/estado). Candidato §14 por conteo/álgebra manual sujeto a revisión y futuros tests asignados, no demostración formal/codec; relay2966payload/4122frame caso sintético inválido reportado no reejecutado. L02/PADRE-C02 históricos no acreditan ledger/retención/enforcement productivos. Sin lectura o edición externa/ampliación de permisos, Android/UI/Keystore/notificaciones/WSS/push/despliegue/agentes/sesiones/QR02. Commit «PADRE-C04-R1: consolidar borrador de control y retención semanal», hash entregado al cierre.

- PADRE-C04: ninguna prueba/build/medición humana/Android nueva. Solo lectura/revisión documental, git diff --check y revisión de preparado/estado. L02 hijo22 nuevos/199 debug ejecutados y núcleo169 UP-TO-DATE son reportados por coordinador, no reejecutados ni garantía productiva. No QR rerun. Código/main/C02/CP1/codec/schema/fixtures/PLAN/ADR aceptados intactos; sin dispositivos/repos externos/agentes/sesiones/notificaciones/LAN/push/despliegue. Commit «PADRE-C04: proponer contrato de órdenes y tiempo permitido», hash entregado al cierre.

- PADRE-C03: ninguna prueba/build nuevo ni medición KDF o evidencia física; revisión documental/lectura de fuentes y comprobaciones Git (git diff --check, revisión de preparado y estado). No reejecutar suite QR ni núcleo/interop/relay. Resultado solo de correspondencia propuesta; versión/semántica/compatibilidad/migración pendientes antes de código. Commit de cierre por mensaje «PADRE-C03: revisar control local y transición de versión»; hash devuelto en entrega.

- PADRE-QR01: cuatro ejecuciones Gradle JVM, 25 tareas cada una. Muestreo único FALLÓ (128 lecturas, 3 fallos); 16 variantes de caso39 con 9 NotFound y control PURE correcto. Caracterización fija inicial FALLÓ por expectativa de excepción del lector directo (Checksum real, no NotFound); precisada la aserción diagnóstica, caracterización PASÓ con tres NotFound productivos explícitos, Checksum directo y control bytes/firma válido. Suite pertinente única con -PdiagnosticoQr=fixture/--rerun-tasks PASÓ: 132 app, cero fallos/errores/omisiones; un test caracteriza limitación conocida, no corrección. Todas las lecturas fallidas registradas: 21 NotFound QrLocal y 3 Checksum directos entre los experimentos. No repetir núcleo/interop/Node/relay, ni APK/lint/script completo/instrumentación/ADB. Fallo productivo NO corregido.

- PADRE-C02: test JVM focalizado inicial (17 nuevos) correcto; tras cuatro añadidos, suite completa app/núcleo/interop --rerun-tasks. Primer intento: 131 app con un fallo QR anterior (ZXing NotFoundException en OfflineChallengeSignerTest.ambosQrSeLeenLocalmenteYLaRespuestaConservaFirmaOffline), 21 revisión correctos. Test QR aislado repetido correcto; suite completa repetida correcta, 29 tareas ejecutadas, 131 app + 30 núcleo + 1 interop sin fallos/errores/omisiones. Cinco oráculo Node y 78 comprobaciones documentales correctos: 167 independientes finales. Fallo QR no reproducido, causa no diagnosticada, sin cambios a ese test/QR. Inspección de clases: cero Revision en main/debug, 16 en debugUnitTest. Sin assemble/lint/script completo verificar.ps1, instrumentación/ADB o integración externa en este turno.

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

INTEROP01 verifica Node→JVM y autoverifica salida públicaJVM; falta comprobación de
esa salida por Node/relay posterior. No demuestra confirmaciónhijo/persistencia
Android/recuperación/cámara/STATE/APPLIED o autoridad de fixtures. ConteoPAIR10
corregido, sin desacuerdo real de schema/codec. PAIR02/transporteyfísica pendientes
no resueltos por criptografía local; no despliegue/permisos heredados.

PAIR02 implementado localmente, no actualizado/probadoA56. Fuente actualA56 no leída;
si preparación incompleta/legadoactivo/incierto, bloquear sin fabricar snapshot.
ÁreaCP2persistida solo pública/pendiente, sin recibo/acreditación hijo. Reinicio o
QRcancelado tras persistir requiere reconciliación separada, no reabrir/reemplazar
códigos/QRporarchivo. Falta aceptaciónhijo/recuperación/ceremonia y transportecp2.
AtomicFile/prompt/KeystoreAndroid no probados físicamente. Sin garantía rollback/
corteenergía/multiproceso ni vínculo operativo por firma/persistencia/AUTH_OK.

F02 acredita UI de lectura física, no bytes exactos/frescura criptográfica/huellas
comparadas/ceremonia/commit/vínculo operativo/STATE/APPLIED. A56 ahora contiene APK
AUTH01conlectorPAIR01, hash33ef5347…d4dd7a; referencias siguientes a APKQR02 instalado
son históricas. No nuevo snapshot/migración/persistencia CP2; firmaMAIN sigue bloqueada.

AUTH01 es borde aislado sin transporte ni activación MAIN; default sin snapshot
compatible→NO_DISPONIBLE. Proveedor autorizado de identidad/pareja/perfil, ciclo
de socket real/confirmaciónenvío/callbacksTLS/heartbeat/snapshotPAIR01/persistencia
e integración aún pendientes. AUTH_OK no STATE/hijo operativo/APPLIED. No inferir
interop por pruebas locales/125testsRender reportados; sin permisos físicos nuevos.

PAIR01: MAIN lectorCP2 compilado, no instalado/prueba física; sigue autorización
nueva específica antes de A56. Firma/códigosCP2 MAIN bloqueados: falta snapshot de
alta/migración compatible autorizado y persistencia/readback/ceremonia posterior.
Caso puro RAM firmado probado con fakes no acredita integración Keystore/child/relay
ni commit/replay/enforcement. Fuentes schema parciales no freezeCP2completo ni AUTH
operativo. No retirar guardas/crear pareja ficticia para avanzar. PREPARADO intacto.

F01: cámara no ejecutada porque propietario confirma escáner deshabilitado.
RecoveryScreen exige pareja conocida; no leer store ni crear una ficticia para
sortearlo. APK QR02 actual sí verificado en A56 por USB; observación de preview/
bytes/firma/semántica no realizada. Datos no inspeccionados, PREPARADO no promovido.
Referencias posteriores a APK A56 previo son históricas: ya tiene hash QR02.

PADRE-QR02 corregido **en sintético**; PREPARADO_FISICO sin autorización A56 nueva.
No garantía de cualquier QR/cámara/varios candidatos ni deadline duro (presupuesto
cooperativo ZXing400ms). Leer QR no valida vínculo/identidad/consumo/APPLIED. A56 APK
previo sigue instalado; hashes nuevos son archivos locales no instalación. CP/2
sin schema/versión acordados; contexto POSTC08 hijo NORMAL10s/OFFLINE explícito/boot/
DNSVPN solo reportado, no integración o pruebas nuestras. Riesgos QR NO corregido
de párrafos históricos siguientes no describen el estado actual sintético.

PADRE-C04-R1: revisión conjunta de borrador antes freeze/contrato/vectores/sync.
Pendiente ratificación técnica DST/ancla/zona, formatos/contextos/capacidades de
PAIR/OFFLINE y continuidad recuperación legada durante migración; sin ruta segura
no versión activada. Push emisor/proveedor/consentimiento/privacidad/permisos a
evaluar separado, sin garantía activa. Rango/libre/contador apagado-Keyguard/
domingo21 local/catch-up/purga conservadora YA confirmados13oct, no preguntas abiertas.
Reglas/cotas manuales necesitan verificación independiente, no inviolabilidad,
borrado forense o APPLIED por firmas. Bloqueos posteriores C04 son historial.

PADRE-C04: acuerdo de tres roles/versiones/subprotocolo/recuperación de migración
pendiente antes de sincronizar schema/vectores. Base contador vs pantalla/Keyguard,
día/zona/retención, último aviso/entrega padre cerrado y rango/apertura LOCK_FOR
sin decisiones previas supuestas. Normalización/KDF/admin/cupos/reset/olvido doble
siguen técnicas privadas propuestas; posponer escapes exhaustivos no quita protección
básica/recuperación/errores. Incertidumbre de gap/enforcement no equivale a APPLIED
ni humano/cero/total exacto. Bloqueos de elecciones C03 siguientes son historial
donde nuevos requisitos confirmados las sustituyen (ver borrador C04 §1).

Control normal local offline exige nueva versión acordada (CP/2 provisional), no
modificar ALLOW CP/1 ni convertir MAINTENANCE. Pendientes elecciones §6 de revisión,
contrato/firmas/subprotocolo de tres roles, migración presencial y puertas/orden.
Defecto QR01 a8e4ffc NO corregido; QR02 no asignado ni activado en PADRE-C03.

G0–G4 pendientes. A13 excluido, sin validación física ni permiso para operarlo. El padre A56 tiene validación parcial P00/C02/P01 registrada; no certifica el producto completo ni compatibilidad universal.
Falta jsonschema para el verificador estructural Python.
Antes de usar ADB, avisar y esperar habilitación expresa del propietario.
P01 aceptar con huella y PIN/contraseña pasó físicamente en A56, incluida caducidad; cancelación y persistencia entre procesos pasaron. Rotación/texto grande/ciclo de vida manual ampliado e invalidación permanente siguen pendientes; no quitar barreras para aparentar éxito.
La referencia plantillas/HANDOFF.md de padre/AGENTS.md no existe; usar pruebas/HANDOFF_PLANTILLA.md.

## Próxima tarea y lectura mínima

Tras INTEROP01 detener/esperar revisión. EvidenciaINTEROP01 y salida pública
pruebas/interop_cp2_jvm_publica.json con hash publicado. Verificación Node posterior
a cargo del rol correspondiente, no ejecutada aquí ni prompt/tarea automática.

Tras PAIR02 detener/esperar revisión/asignación. Leer pruebas/EVIDENCIA_PADRE_PAIR02.md.
Nueva física requerirá autorización propia; F02nohereda permiso. No solicitar ni
ejecutar automáticamente ceremonia, instalación, reconciliaciónpendiente oWSS.

Tras F02 detener/esperar revisión o asignación nueva. Leer evidenciaF02; no repetir
instalación ni asumir permisos físicos heredados. Snapshot/persistencia/ceremonia
compatibles y conexiónCP2 siguen fuera; sin otra tarea automática.

Tras AUTH01 detener/esperar revisión y asignación separada; leer evidenciaAUTH01 y
contrato-cp2/README.md. No activar red ni convertir snapshotCP1 para conectar.
PAIR01snapshotMAIN/persistencia/ceremonia no se resuelven por AUTH. Sin otra tarea
automática/operacionesA56/A13/QR/dispositivos o prompts de vuelta.

Tras PADRE-PAIR01, detener y esperar revisión/asignación. Leer evidenciaPAIR01 y
contrato-cp2/README.md. Siguiente decisión separada: snapshot/persistencia CP2 compatible
o permiso nuevo de instalación/escaneoA56 con oferta públicaCP2; F01 no cubre ceremonia
o QRhijo. Sin esperar implementación hijo para este cierre local ni iniciar otra unidad.

Prioridad tras F01: esperar decisión/asignación separada; leer evidencia F01.
Posible diagnóstico físico aislado requiere autorización específica APKtest/tests
propios públicos; no ejecutado/autorizado aquí. No cambiar pareja/guards para abrir
cámara ni repetir instalaciones. F01 detenido por impedimento, sin otra tarea automática.

PADRE-QR02 cerrado localmente: esperar revisión/coordinador o autorización específica
de instalación/prueba física A56 en esta sesión. Leer pruebas/EVIDENCIA_PADRE_QR02.md;
primera cámara solo fixture público ficticio en pantalla PC, sin ceremonias reales
o dispositivo hijo. No operar por «listo/disponible»; A13 excluido. Sin siguiente
tarea automática ni nuevas rondas CP2/historia/push/remoto por este cierre. Las
asignaciones/referencias NO corregido posteriores son historial supersedido en sintético.

PADRE-C04-R1 cerrado documentalmente. Esperar revisión/coordinador sobre borrador
integrado §§10/12/14 antes freeze/sync/código; ningún laboratorio/modelo/protocolo/
push ni siguiente tarea automática activados. CP/1 congelado, CP/2/cp.v2 provisional.
QR reproducido NO corregido requiere unidad aparte. A56 no permiso para operar;
A13 excluido. Texto posterior conserva historial, no autorización actual.

PADRE-C04 documental cerrado; leer padre/BORRADOR_CP2_ORDENES_Y_TIEMPO.md para
revisión hijo/relay y decisiones §12 antes de acuerdo/sincronización. No activar
nueva tarea ni implementar mapa/versión/schema/UI/contador por este cierre. CP/2
provisional y CP/1 congelado. A56 disponible no autoriza operar; A13 excluido.
QR a8e4ffc NO corregido, QR02 asignación aparte. Texto posterior conserva historial.

PADRE-C03 cerrado documentalmente: esperar revisión y decisiones del propietario/
coordinador sobre padre/REVISION_CONTROL_LOCAL_ADULTO.md §§6–7 antes de versión,
ADR/planes/puertas o implementación. Sin nueva unidad automática ni prompts de vuelta.
No adaptar PADRE-C02 ni producción por requisitos nuevos. QR02 requiere asignación
independiente; A56 disponible no habilita uso, A13 excluido. Texto siguiente es historial.

PADRE-QR01 cerrado como diagnóstico reproducible; detenida vía de corrección
productiva por límite de asignación. Propuesta PADRE-QR02: revisión técnica del
lector con fixture público y criterios de bytes/firmas, previo acuerdo de alcance
para cambiar producción/dependencias. No iniciada ni prompts de vuelta. Leer
pruebas/EVIDENCIA_PADRE_QR01.md; no confundir caracterización verde con solución.
Puertas/orden hijo, vínculo operativo y físicos siguen pendientes. A56 disponible
no autoriza operarlo; A13 excluido. Indicaciones siguientes son históricas.

PADRE-C02 aislado cerrado; esperar revisión/asignación del coordinador, no tareas
automáticas ni prompts de vuelta. Propuesta siguiente: diagnóstico de incidencia
QR sintética P07 observada, sin ADB/cámara productiva/CP1/integración relay; no
iniciada. Leer pruebas/EVIDENCIA_PADRE_C02.md y resultado §8 del diseño. H08/H10/H11,
puertas/orden del hijo, persistencia/migración/ceremonia y evidencia física siguen
bloqueadas por decisiones/asignaciones separadas. A56 disponible no autoriza uso;
A13 excluido. Secciones siguientes conservan indicaciones históricas.

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
