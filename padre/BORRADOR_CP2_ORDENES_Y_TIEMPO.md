# PADRE-C04 — borrador CP/2 de órdenes, estado y tiempo permitido

Fecha: 2026-10-12. Base padre `984693c`. **DRAFT para revisión de hijo/relay;
no contrato congelado, versión acordada, schema, fixture ni implementación.**
Los nombres CP/2, cp.v2 y TODOS los campos/reglas nuevos siguientes son propuestas
técnicas. Los requisitos confirmados se distinguen de las opciones pendientes.
CP/1 y sus copias/codec/fixtures permanecen intactos.

## 1. Base incremental y límites

Reutiliza CP/1 §§3–8/10/11 y PADRE-C03, no los reescribe como norma vigente.
Contexto hijo C04 `4636d30`, C05 `94b1bbe`, C06 `ae1c7eb`, L02 `c6e8d5d`
aportado por coordinador, sin nuevas lecturas externas. L02: 22 nuevos/199 debug
ejecutados reportados, núcleo169 UP-TO-DATE; no reejecutados aquí, no modelo
productivo ni métrica de persona usando el teléfono.

Confirmado por propietario, sustituye alternativas anteriores de C03:

- Código local NUMÉRICO elegido de 5–10 caracteres, conservando ceros iniciales;
  pregunta personalizada offline para establecer otro código, sin revelar anterior.
  No código extra obligatorio. Normalización/KDF/esperas/administración/reset/olvido
  doble siguen diseño privado pendiente, no mensajes remotos de este borrador.
- LOCAL elige tiempo en cada acción, sin default30min. Ambas autoridades cambian;
  última acción válida confirmada en hijo gana, nuevo contexto tras conflicto.
  LOCAL y REMOTO mantienen autorización sin internet hasta vencimiento, bloqueo
  nuevo o reboot. Cada boot necesita autorización nueva.
- Todas las apps normales y HOME propio; instalar/actualizar mientras permitido;
  proteger Ajustes/administración/desinstalación del hijo. No imponer catálogo
  aprobado de C03. Guardas básicas, recuperación y errores permanecen; revisión
  exhaustiva de escapes pospuesta, corregir los encontrados. No prometer inviolabilidad.
- Administración RAM cierra por salida/fondo/proceso sin cancelar permiso normal
  comprometido; emergencia sin código y Keyguard intacto.
- «Uso» aquí NO significa interacción humana. Tiempo permitido/consumido transcurrido,
  no UsageStats, eventos de apps o vigilancia. Desde aceptación duradera vigente,
  no duration solicitada/clic/envío/ACK; no sumar intervalos solapados.

Pantalla apagada/Keyguard no pausan ni renuevan deadline. Cómo afectan al contador,
retención/día/zona y aviso con padre cerrado sigue sin respuesta: §8 propone una
variante, §12 deja pocas elecciones. No FCM, cron, servicios externos, credenciales
relay o garantía de WSS con app muerta. Credencial/pregunta/respuesta/salt/verificador,
catálogo, apps, ubicación e interacción quedan fuera de protocolo y servidor.

## 2. Perfil de bytes, versiones y transporte propuesto

| Elemento | Valor exacto propuesto / diferencia CP/1 |
|---|---|
| Negociación | WSS /ws, TLS validado; cliente migrado ofrece SOLO `cp.v2`, servidor selecciona exactamente `cp.v2` o rechaza upgrade. Nunca fallback automático a cp.v1. |
| Versión | `v:2` en cada frame y payload; sobre y payload deben coincidir con socket. No reinterpretar v1 en v2 ni v2 en v1. |
| Sobre firmado | Campos exactos `{kind,payloadB64,purpose,sigB64,v}`; kind=`SIGNED`, purpose=`AUTH` o `MESSAGE` para mensajes de §4–7. |
| Firma | P-256/secp256r1, SHA256withECDSA, ASN.1 DER estricta (8–72 bytes, r/s válidos 1..n−1); SPKI DER validado de pública vinculada. No JOSE/r\|\|s, alg seleccionable o pública desde orden. |
| Dominio | `UTF8("CPv2/" + purpose + "\n") || payloadBytes`; verificar bytes recibidos, no JSON reconstruido. Firma CPv1 no válida aunque claves sean las mismas. |
| PAIR/OFFLINE | Nombres reservados para futura propuesta conjunta; no admitidos en WSS ni nuevas rutas de reset/ALLOW por QR aquí. No trasladar bytes CPv1 cambiando solo v/prefix. |
| Hash | `SHA256(payloadBytes)` exactos, 32 bytes base64url canónico sin padding, 43 caracteres. No hash de DER/sobre ni de JSON reformateado. |
| Serialización | Claves ASCII ordenadas lexicográficamente en todo objeto, UTF-8 estricto, sin espacios/newline finales/duplicados/desconocidos; arrays ordenados. Strings ASCII imprimible, sin controles. null explícito solo donde se enumera. |
| Números | Enteros seguros JSON no negativos, sin exponentes/decimales/−0. Grandes contadores/monotónicos como cadenas decimales canónicas (tipos §3). No fechas para permitir apps. |
| Base64url | Alfabeto A–Z/a–z/0–9/_/− sin =; validar longitud, bits finales y roundtrip exacto. |
| Límites | Frame UTF-8 ≤16384 bytes, payload decodificado ≤8192 bytes; un solo objeto, no binarios/compresión. Profundidad ≤6, sin arrays en mensajes definidos aquí; comprobar límites antes de crypto/parse anidado. |
| Socket/rol | Uno autenticado por rol y pareja/perfil, nueva AUTH válida sustituye viejo; socket sin autenticar no expulsa. Relay comprueba dirección/tipo/propósito/firma y retransmite bytes exactos sin almacenar historial/contadores. |

No permitir dos perfiles activos de la misma pareja para eludir migración. Relay
configura perfil público de pareja explícitamente; no lo cambia por AUTH/STATE.
Un servicio que soporte ambos perfiles separa parejas/perfiles; no puentea mensajes.
Heartbeat conserva propuesta CP/1 de PING15s/PONG45s, AUTH10s y backoff acotado;
en v2 diagnostica transporte, **no revoca permisos LOCAL/REMOTO**.

## 3. Diccionario cerrado común

Todos los campos listados por mensaje son obligatorios; sin opcionales omitidos.
Objetos anidados también cerrados. `T?` significa T o null explícito.

| Tipo | Validación propuesta |
|---|---|
| U | UUIDv4 minúsculo, 36 caracteres, variante RFC; pair/boot/session/command/connection/epoch. |
| N/H | N=32 bytes aleatorios, H=hash32, ambos base64url canónico de 43 caracteres. |
| C | Cadena decimal `0` o [1–9][0–9]*, valor 0..9223372036854775807. C+=1..máximo; overflow bloquea nuevas operaciones, no wrap/reset. |
| M | C en milisegundos de elapsedRealtime del hijo, válido solo para boot del objeto. No comparar relojes padre/hijo. |
| D | Entero JSON 60..604800 segundos, **rango heredado propuesto**, no elección de propietario. |
| Fuente | `NONE`, `LOCAL`, `REMOTE`; no identificar adulto, app ni teclado. |
| Acción | `LOCK`, `LOCK_FOR`, `ALLOW`, `ALLOW_FOR`, sin eliminar funciones de CP/1. |

`controlRevision`: C global durable de instalación (inicia 0 solo en instalación
nueva autorizada), NO seq remoto ni stateSeq. Cambia por decisión local/remota
comprometida, transición de timer vigente, boot nuevo/incierto, revocación por error
o migración autorizada. Una conexión/sync/PONG no la incrementa. Cada cambio invalida
desafíos y confirmaciones previas. Enforcement del mismo objetivo actualiza estado
sin generar una nueva decisión ni renovar su deadline. Máximos no retroceden.

`seq`: C+ exclusivamente padre; `lastAcceptedSeq`: C máximo durable hijo, 0 si
nunca aceptó orden. `stateSeq`: C+ creciente por sesión WSS del hijo, no por boot;
nueva sesión inicia 1. Revisión global sigue creciente entre boots/sesiones.

## 4. AUTH, sync y transporte mínimo: campos exactos

Frames relay/latido sin firma de dispositivo; solo sobre conexión TLS actual.
Son información de transporte, nunca autoridad de uso o confirmación de aplicación.

| kind / emisor | Campos además de kind y v=2 |
|---|---|
| CHALLENGE / relay | connectionId:U, nonce:N, timeoutSec:10 |
| AUTH_PROOF / rol | pairId:U, role:`PARENT\|CHILD`, connectionId:U, nonce:N, protocol:`cp.v2`; sobre AUTH |
| AUTH_OK / relay | connectionId:U, protocol:`cp.v2` |
| PING / cliente, PONG / relay | nonce:N |
| PEER_STATUS / relay | peerRole:`PARENT\|CHILD`, online:boolean |
| PEER_UNAVAILABLE / relay | reason:`PEER_OFFLINE`, refCommandId:U? |
| ERROR / relay | code:enum de errores §9, refCommandId:U? |
| SYNC_REQUEST / PARENT | pairId:U, sender:`PARENT`, syncNonce:N, requestedAction:Acción?; sobre MESSAGE |

Relay compara pareja/rol/perfil público, nonce/connectionId no usados de ese socket,
10s monotónicos y dominio AUTH; consume reto RAM. No usa clock civil ni KEY del
cliente. AUTH_OK no confirma identidad/enforcement del hijo para padre: requiere
STATE firmado. Error de handshake/subprotocolo detiene conexión; no AUTH v1 de rescate.

SYNC_REQUEST no consume seq ni altera permiso. `requestedAction:null` consulta sin
habilitar envío. Acción concreta solicita reto para **esa operación**. Sync nuevo
reemplaza reto previo aun de misma revisión. Padre muestra acción/duración y contexto
antes de confirmación; cambio de acción exige sync nuevo. Retransmitir SYNC no firma
política ni habilita uso. Solo hijo genera desafío operativo vivo.

## 5. SET_POLICY y escritor único

Payload PARENT/MESSAGE cerrado:

| Campo | Tipo / restricción |
|---|---|
| v, kind, sender | 2, `SET_POLICY`, `PARENT` |
| pairId, bootId, childSessionId, commandId | U cada uno |
| seq | C+; mayor que lastAcceptedSeq para orden nueva |
| expectedRevision | C; igual a controlRevision en commit |
| commandNonce | N; reto vivo de sync actual, asociado a pareja/boot/sesión/revisión/action |
| action | Acción solicitada y confirmada |
| durationSec | D para ALLOW_FOR/LOCK_FOR; null para LOCK/ALLOW |
| deferredAllowSec | D solo LOCK_FOR; null para demás acciones; propuesta de apertura temporal segura |

Reto dura 30s desde emisión en reloj hijo. Padre limita emisión a 20s desde inicio
de sync (incluye tránsito), además de UI/autoridad/snapshot vigentes; hijo decide
caducidad final. No enviar al borde ni rejuvenecer por recibir otro STATE/PONG.
Conflicto exige sync/reto y confirmación nueva; no refirmado automático.

Padre reserva `max(contadorLocal,lastAcceptedSeq)+1`, guarda bytes/intención/hash
duraderamente antes de enviar; seq reservado nunca reutilizado para bytes distintos.
Hijo valida tamaños/sobre/firma/canonización/tipos/rol/pareja/perfil/contexto, luego
en único escritor revalida revisión/reto/boot y caducidad al commit. Persiste juntos
decisión, revisión+1, seq/hash/id/resultado pendiente, inicio/deadline y contabilidad
cerrada de intervalo anterior; consume reto antes de ampliar permisos. Fallo/IO
incierto no concede permiso ni firma éxito; no separar contador de aceptación.

Local no envía SET_POLICY ni usa seq. Ticket RAM con boot/revisión/action/autoridad
local y caducidad; confirmar con escritor revalidando generación administrativa y
credencial/contexto. Commit local incrementa revisión y anula reto remoto/timer
reemplazado. Salir/fondo/proceso invalida tickets RAM, no permiso ya comprometido.

| Acción | Semántica propuesta v2 |
|---|---|
| LOCK | Revocar permiso normal ahora; sin deadline de apertura. |
| ALLOW | REMOTO: desde aceptación hasta nueva decisión/error/reboot, incluso offline. LOCAL no ofrece tiempo indefinido implícito: acción permitir local utiliza ALLOW_FOR con tiempo elegido, sin default. Función ALLOW remota se conserva. |
| ALLOW_FOR | Autoriza desde aceptación t hasta t+durationSec×1000, aun offline. Al vencer pasa a autorización bloqueada, nueva revisión; intento de bloqueo/enforcement se reporta aparte. |
| LOCK_FOR | Bloquea desde aceptación hasta t+durationSec×1000; en ese mismo boot y solo si sigue revisión/acción vigentes, abrir por deferredAllowSec elegido al confirmar originalmente. Transición atómica revisión+1 y deadline de permiso = deadline original de bloqueo+deferredAllowSec×1000, no despertar tardío+duración. |

LOCK_FOR es cambio técnico **pendiente de acuerdo**, conserva apertura diferida,
pero propone duración de apertura explícita para no permiso indefinido oculto.
Si timer llega tras ambos plazos, no abrir fugazmente: consolidar expiración y
bloqueo. Cada transición lógica vigente incrementa revisión: al consolidar apertura
y expiración juntas son dos incrementos en una transacción, sin abrir UI/apps entre
ellas; comprobar overflow antes. Acción nueva sustituye entero el plan, no acumula
horas ni callbacks.
Red no es condición de apertura v2; integridad/enforcement/Keyguard/boot sí.
No abrir si continuidad/estado no comprobables. Rango/deferredAllowSec deben decidirse
antes de schema. No retirar botones ni aplicar esta semántica en CP/1.

Duplicado exacto del último aceptado: pair/id/seq/hash/boot/sesión originales iguales,
firma válida; responde solo resultado retenido, sin exigir reto consumido ni revisión
actual igual, **sin reaplicar/deadline/revisión nuevos**. Contexto de socket actual
debe seguir misma childSessionId/boot como CP/1; tras reconexión/boot usar STATE.
Mismo seq/id con hash distinto = SEQUENCE_CONFLICT. Orden antigua fuera del registro
único o contexto cambiado = resultado desconocido/rechazo de solicitud actual;
no prueba de que la orden original no se aceptó. No cola/historial de ACK.

## 6. STATE: snapshot efectivo separado de última remota

Payload CHILD/MESSAGE cerrado:

| Campo | Tipo / restricción |
|---|---|
| v, kind, sender | 2, `STATE`, `CHILD` |
| pairId, bootId, childSessionId | U |
| syncNonce | N del último SYNC_REQUEST de sesión actual |
| stateSeq | C+; incrementa por cada emisión |
| sampledElapsedMs | M; instante del snapshot hijo, no hora civil |
| controlRevision, lastAcceptedSeq | C cada uno |
| commandChallenge | objeto §6.1 o null |
| effective | objeto §6.2 |
| lastRemote | objeto §6.3 o null |
| permittedTime | objeto §8.2 |
| lastExpiry | objeto §8.3 o null |
| networkValidated, channelHealthy | boolean? cada uno, null si sin observación local fiable |
| isDeviceOwner, lockTaskActive | boolean? cada uno; no prueba física por firma/booleano |

### 6.1 commandChallenge

Campos exactos: `nonce:N, action:Acción, revision:C, issuedElapsedMs:M,
expiresElapsedMs:M`. Expires=issued+30000; revision igual a controlRevision y
issued≤sampled. null si consulta sin acción, reto consumido/caducado o no apto.
Actualizaciones espontáneas no generan reto nuevo ni extienden expiración; snapshot
nuevo cambia stateSeq/sample pero no ventana de emisión del padre.

### 6.2 effective

Campos exactos: `mode, reason, source, policyRevision, action, phase,
acceptedElapsedMs, phaseStartedElapsedMs, deadlineElapsedMs, remainingSec,
enforcement`.

- mode=`LOCKED|ALLOWED|MAINTENANCE|SETUP|UNKNOWN`; reason enum de causas §9.
- source=Fuente. policyRevision=C? origen de decisión actual; action=Acción?;
  ambos null y source=NONE si sin decisión normal aplicable al boot.
- phase=`BLOCKED|WAITING_ALLOW|PERMITTED|EXPIRED|NONE`; expresa autorización,
  NO éxito físico. `enforcement=PENDING|VERIFIED|FAILED|UNKNOWN` para objetivo actual.
  ALLOWED solo si permiso vigente y aplicación comprobada, sin Keyguard impeditivo;
  ante imposibilidad de conocer efecto mode=UNKNOWN, sin fingir bloqueo aplicado.
- acceptedElapsedMs=M? inicio original de decisión en este boot;
  phaseStartedElapsedMs=M? inicio lógico de fase (incluida apertura LOCK_FOR).
  deadlineElapsedMs=M? límite de fase vigente, null si indefinida/sin temporizador.
- remainingSec=entero 0..604800?; con deadline es
  ceil(max(0,deadline−sampled)/1000), sin deadline null. Nunca plazo nuevo del receptor.
  Deadline≤sampled implica fase no PERMITTED/WAITING_ALLOW tras consolidar timers.
  ALLOW normal tiene phase PERMITTED y deadline null; LOCK BLOCKED/null;
  ALLOW_FOR PERMITTED o EXPIRED; LOCK_FOR WAITING_ALLOW/PERMITTED/EXPIRED.

Restricciones de combinación: sin decisión, action/policyRevision/acceptedElapsedMs/
phaseStartedElapsedMs/deadlineElapsedMs/remainingSec null y phase=NONE. Con decisión,
action/policyRevision/acceptedElapsedMs/phaseStartedElapsedMs no null,
accepted≤phaseStarted≤sampled. policyRevision≤controlRevision. EXPIRED conserva
deadline agotado/remaining0 del permiso vencido para explicar causa; permiso vigente
finito exige deadline>sampled. mode LOCKED/ALLOWED exige efecto observado suficiente;
solicitar bloqueo pendiente sin verificar no permite mode LOCKED ficticio.

policyRevision puede ser menor que controlRevision tras transición de su timer;
decisión reemplazada no sigue efectiva. Al reboot no exponer política vieja como
permiso vigente: source NONE, fase NONE, causa BOOT_AUTH_REQUIRED. Emergencia y
Keyguard no otorgan administración ni crean permiso normal.

### 6.3 lastRemote

Campos exactos: `commandId:U, seq:C+, payloadSha256B64:H, bootId:U,
childSessionId:U, acceptedRevision:C+, result, resultReason, resultElapsedMs:M?`.
result=`APPLIED|ACCEPTED_PENDING_ENFORCEMENT|ENFORCEMENT_FAILED`;
resultReason enum §9 (`OK` para APPLIED/PENDING; ENFORCEMENT_ERROR para FAILED).
resultElapsedMs null mientras pendiente, instante de comprobación para terminal,
medido en bootId de **la orden**, no sampledElapsedMs de STATE actual.

Null solo si nunca hubo registro aceptado (lastAcceptedSeq=0) o migración con
resultado no representable conocido como ausente; en segundo caso lastAcceptedSeq
se conserva y pendiente padre queda desconocido. Rechazo no sobrescribe lastRemote.
Acción local/timer no inventa registro remoto ni cambia éxito histórico a actual.
Si aplicación quedó pendiente al ser sustituida, conserva pendiente/desconocido;
no inventa APPLIED ni FAILED para resolver UI. Un resultado terminal no se reevalúa
para otra revisión: fallo actual se expresa en effective, no reescribe pasado.
Un callback de enforcement atrasado no confirma APPLIED midiendo solo un modo
coincidente de una decisión posterior: debe demostrar aplicación de esa orden
en su contexto/revisión antes de la sustitución; sin prueba conserva desconocido.
Si lastRemote no null, seq=lastAcceptedSeq y acceptedRevision≤controlRevision;
su boot/sesión pueden ser anteriores a STATE y no se fuerzan al contexto actual.
Correlacionar siempre con contexto original guardado por padre, no solo seq/revisión.

### 6.4 Frescura y padre

Padre exige firma CHILD vinculada/MESSAGE, pareja, syncNonce solicitado y socket/
ticket propios, STATE inicial en ≤20s desde solicitud; stateSeq creciente y sesión/
boot coherentes. Si boot cambia pedir nuevo sync, no mezclar relojes/reto. Una
actualización válida informa snapshot nuevo, pero no prolonga autorización de envío.
Frescura de presentación propuesta: ≤20s desde inicio de SYNC que ancla el reporte,
no solo recepción de frame; actualizaciones mantienen esa ventana, no la rejuvenecen.
Para prolongarla, nueva consulta correlacionada (sin acción si no se va a operar),
no cron/garantía background. Mostrar siempre antigüedad/alcance de snapshot; nunca
prometer presente continuo frente a acción local inmediatamente posterior.
Sin reporte vigente: último estado/antigüedad, **modo actual desconocido**, causa
de falta de comunicación desconocida. PONG solo relay, PEER_STATUS indicativo.
networkValidated es último reporte Android VALIDATED del hijo, no internet inferido
de WSS. Red local del padre tampoco mide red hijo. Ni permiso ni PONG dicen «persona
usando». No descontar UI como nuevo estado confirmado ni inventar cero offline.

## 7. ACK: resultado de una orden, nunca reemplazo de STATE

Payload CHILD/MESSAGE cerrado: `v:2, kind:ACK, sender:CHILD, pairId:U,
bootId:U, childSessionId:U, commandId:U, seq:C+, commandPayloadSha256B64:H,
acceptedRevision:C+?, evaluatedRevision:C, result, reason, resultElapsedMs:M?`.
boot/session son contexto de solicitud evaluada; evaluatedRevision es revisión
actual al evaluar en ese boot. No ACK para boot/session ajenos: ERROR informativo
o sync requerido. ResultElapsed para terminal aceptado pertenece a boot de orden.

| result | acceptedRevision / reason / significado |
|---|---|
| APPLIED | Revisión aceptada no null, reason OK, comprobación real duradera de esa orden; resultado histórico. |
| ACCEPTED_PENDING_ENFORCEMENT | Revisión no null, OK, resultElapsed null; aceptación durable sin aplicación comprobada. |
| ENFORCEMENT_FAILED | Revisión no null, ENFORCEMENT_ERROR, instante de fallo; no protegido ficticio. |
| REJECTED | Revisión null/resultElapsed null; error de orden nueva válida identificable no aceptada en esta evaluación (STALE_REVISION, STALE_NONCE, etc.). |
| UNKNOWN_COMMAND | Revisión null/resultElapsed null; UNKNOWN_COMMAND, duplicado viejo/no retenido o aceptación original incierta; no resuelve pendiente como rechazado. |

ACK no contiene mode, fuente, restantes o total: evita hacer pasar respuesta tardía
por snapshot actual. Padre correlaciona id/seq/hash y contexto con bytes persistidos,
guarda resultado remoto separado, jamás retrocede effective/controlRevision/stateSeq
ni rejuvenece antigüedad de STATE por ACK. REJECTED solo resuelve solicitud nueva
con vuelo conocido, no borrado de aceptación histórica por rechazo de reenvío.
UNKNOWN_COMMAND/error relay no resuelven pendiente original. APPLIED terminal puede
resolver esa intención correlacionada, no afirmar modo actual. Cambio exige orden
nueva tras sync/confirmación, reservas anteriores se conservan.

Reintentos: mismo frame como máximo dos, sesión/boot iguales y sin cambio de revisión
conocido; después sync. No emitir nuevas órdenes como retry ni repetir timer al
reconectar. Si STATE no identifica pendiente, conservar incertidumbre y explicar al
adulto antes de descartar local o confirmar acción nueva; no asumir falló/aplicó.

## 8. Tiempo permitido CONSUMIDO y último agotamiento: propuesta mínima

### 8.1 Variante recomendada NO aprobada: autorización transcurrida

Medir unión de intervalos de **autorización normal vigente** desde aceptación
duradera/activación contextual hasta min(vencimiento, nueva decisión, boot/error
revocador). LOCK_FOR no consume en fase bloqueada; su apertura empieza en deadline
original, no al callback tardío. ALLOW indefinido acumula solo transcurrido.
No sumar durationSec ni presupuestos futuros; permitir de nuevo cierra tramo anterior
y abre siguiente sin doble cuenta. Mantenimiento/administración no son uso normal.

Recomendación: incluir pantalla apagada/Keyguard en tiempo de autorización, pues
deadline sigue transcurriendo; **elección aún pendiente**, etiqueta explícita
«Tiempo de permiso transcurrido; no mide interacción ni pantalla encendida».
Si propietario quiere solo pantalla desbloqueada, habrá que acordar semántica/
fuente/continuidad distinta antes de implementar contador, sin cambiar deadline
ni recurrir a UsageStats. No confundir el subtotal de esta variante con métrica humana.
Pendiente/enforcement fallido se reporta aparte: contador de autorización no certifica
que Android permitió efectivamente interactuar ni concede APPLIED.

Una transacción actualiza punto de corte/contador y estado; reporte calcula fracción
vigente hasta sampled, sin exigir escritura cada ms. **Antes de emitir STATE**
materializa punto de corte/subtotal calculado en escritura atómica/readback; evita
que total ya reportado retroceda al perder proceso/boot. Si falla no publica ese
incremento como confirmado: contador UNKNOWN/null o subtotal durable PARTIAL sin
contradecir máximo ya comprometido. Al persistir cierra fracción desde último punto
**una sola vez**; mismo reporte no se suma nuevamente en padre. Snapshot es del
instante sampled previo a escritura/firma; no backdate de autoridad actual.
Todas las acciones/revocaciones que cortan intervalo deben serializarse/durabilizarse.
En apertura diferida, el plan futuro ya fue aceptado duraderamente en LOCK_FOR:
solo permite reconstruir intervalo lógico si se demuestra que ninguna revisión/
revocación lo invalidó y que todas sus condiciones no temporales seguían verificables.
Si no, no adjudicar gap como autorizado: PARTIAL. Esto no acredita apertura efectiva
mientras proceso no ejecutó enforcement ni da APPLIED por backdating del timer.

Reconstrucción tras proceso mismo boot admisible solo si registro íntegro contiene
boot contrastable, revisión, inicio/deadline originales, punto ya contabilizado y
eventuales cierres; monotónico no retrocede y no hay pérdida de transacción/evento
revocador o continuidad desconocida. Reconstruir intersección temporal de permiso
durable aún válido con gap, sin extender deadline. Calidad RECONSTRUCTED, no observado
ni humano; probar readback/crash/timers en laboratorio y después entorno autorizado.
No reutilizar política L02 de gap ciego: aquí justifica intervalo autorización
durable, no actividad/pantalla, y **su prueba productiva aún no existe**.

Boot nuevo: permiso bloqueado, no sumar desde último checkpoint al arranque como
si hubiera seguido todo el gap (reloj reinicia y fin de boot desconocido). Preservar
subtotal comprobable y marcar PARTIAL hasta cerrar época contable autorizadamente.
No falsear cero ni consumir plazo viejo en boot nuevo. Error/corrupción/IO incierto
también pierde continuidad; tiempo desconocido nunca se reemplaza por durationSec.

### 8.2 permittedTime: snapshot absoluto, no delta

Objeto exacto de STATE: `basis, epochId, consumedMs, quality, throughElapsedMs,
day`.

- basis=`AUTHORIZED_ELAPSED` en variante recomendada; no otro valor ni v2 compatible
  que signifique interacción. Una decisión distinta requiere revisar borrador.
- epochId:U época contable local estable (no por sync/proceso/reboot); consumedMs:C?
  subtotal absoluto conocido de unión de intervalos desde época. Null si ningún
  subtotal fiable recuperable, **0 solo si cero comprobado**, no «sin reporte».
- quality=`EXACT|RECONSTRUCTED|PARTIAL|UNKNOWN`. EXACT continuidad completa de
  autorización contabilizada; RECONSTRUCTED completa por registro durable, no observación;
  PARTIAL subtotal/límite inferior conocido con gaps no cuantificables; UNKNOWN exige
  consumedMs=null. PARTIAL nunca se presenta «total exacto». Dentro de época subtotal
  no disminuye; una calidad degradada no vuelve EXACT solo por reconectar.
- throughElapsedMs:M igual a sampledElapsedMs del STATE en boot exterior: punto de
  evaluación, NO prueba de continuidad completa del pasado ni timestamp universal.
- day: objeto §8.2.1 o null mientras política diaria no acordada/configurada o datos
  no fiables. Null no equivale a «hoy 0». Padre reemplaza snapshot absoluto si más
  fresco/coherente, no suma ACK/STATE/deltas ni multiplica por reconexiones.

Propuesta mínima de retención: subtotal de época + **un** bucket de día actual +
último agotamiento, sin días anteriores/historial servidor. No aprobada. Cambiar
época requiere procedimiento privado autorizado y mostrar discontinuidad; no reset
automático al boot/upgrade ni como reparación de máximos/corrupción. No se añade
mensaje de reset remoto o cupo diario: total día informativo, no política de límites.

### 8.2.1 day parametrizado, no día civil elegido silenciosamente

Objeto exacto si configurado: `policyId:U, date, zoneId, consumedMs:C?, quality`.
date ASCII ISO YYYY-MM-DD (fecha válida), zoneId ASCII 1..64 del identificador IANA
acordado y validado localmente, quality misma enum y reglas de null/subtotal arriba.
policyId identifica configuración contable privada, no configuración remota; cambio
de zona/política invalida comparación entre buckets, no suma como mismo día.
Cobertura diaria empieza como máximo en inicio de época: si empezó a mitad del día,
quality PARTIAL para «total del día», aunque subtotal época sea EXACT. El bucket
solo cubre intervalos de esa época atribuibles al día; no incorpora pasado fabricado.
EXACT/RECONSTRUCTED de día completo requieren cobertura desde su límite inicial.

UTC fija es recomendación técnica por menos ambigüedad, no decisión de propietario;
alternativa zona local fija explícita con DST. Deadline siempre monotónico. Dividir
intervalos en límites día exige anclaje civil/monotónico fiable y zona/política
estable durante tramo; salto civil/DST mal resuelto/reboot/gap no asignable produce
PARTIAL o day=null, no trasladar todo al día nuevo ni inventar cero. Nunca usar fecha
para autorizar/revocar. Si solo queda bucket actual y llega gap cruzando días sin
anclaje, conservar subtotal época comprobable y día desconocido. Sin array de días.

### 8.3 lastExpiry: mínimo aviso y separación de enforcement

Objeto cerrado: `bootId:U, policyRevision:C+, expiredRevision:C+,
source:LOCAL|REMOTE, deadlineElapsedMs:M, enforcement:PENDING|VERIFIED|FAILED|UNKNOWN`.
Una única expiración de permiso normal más reciente (no expiración del bloqueo
LOCK_FOR, administración o código), comprometida con revisión al consolidar timer.
Null si no registro fiable. Mantener duraderamente hasta siguiente expiración;
retención concreta pendiente de aprobación. Un nuevo permiso NO borra el dato,
pero aviso dice «Permiso anterior agotado», no «ahora bloqueado» si effective permite.
Enforcement refiere al objetivo de bloqueo de ese vencimiento, nunca éxito inventado;
si nuevo permiso lo sustituye antes de comprobar, queda UNKNOWN/PENDING histórico.
Instantáneo de expiración es deadline original, no hora callback/recepción.

Aviso recomendado para padre: con app abierta y STATE fresco, mensaje deduplicado
localmente por (pairId,bootId,expiredRevision). Cerrada: **sin garantía de entrega**;
al abrir/autenticar/sync mostrar último dato retenido y snapshot actual/antigüedad,
no una cola de notificaciones. Es propuesta, no Android/notificación implementados.
Sin comunicación: «Sin reporte reciente; causa desconocida», no «hijo sin internet».
Reporte VALIDATED=false permite decir «hijo reportó red no validada entonces», no
diagnóstico permanente. No push externo ni nuevos mensajes EVENT/TIMEOUT de relay.

## 9. Causas y errores cerrados del draft

Causas effective.reason (una causa principal, orden prioridad integridad/configuración,
boot/Keyguard, mantenimiento, política): `OK`, `STATE_CORRUPT`, `ENFORCEMENT_ERROR`,
`KEY_UNAVAILABLE`, `NOT_DEVICE_OWNER`, `NOT_PROVISIONED`, `BEFORE_USER_UNLOCK`,
`BOOT_AUTH_REQUIRED`, `SYSTEM_KEYGUARD`, `MAINTENANCE_ACTIVE`, `LOCAL_LOCK`,
`PARENT_LOCK`, `TIMER_EXPIRED`, `CONTROL_UNKNOWN`. NETWORK_LOST/CHANNEL_EXPIRED
no bloquean política v2; conexión va en observaciones separadas.

Errores evaluables: `BAD_SCHEMA`, `BAD_SIGNATURE`, `WRONG_PAIR`, `WRONG_ROLE`,
`UNKNOWN_VERSION`, `PROTOCOL_MISMATCH`, `STALE_BOOT`, `STALE_SESSION`,
`STALE_NONCE`, `STALE_REVISION`, `STALE_SEQUENCE`, `SEQUENCE_CONFLICT`,
`UNKNOWN_COMMAND`, `NOT_DEVICE_OWNER`, `NOT_PROVISIONED`, `BEFORE_USER_UNLOCK`,
`STATE_CORRUPT`, `KEY_UNAVAILABLE`, `ENFORCEMENT_ERROR`, `COUNTER_EXHAUSTED`,
`PEER_OFFLINE`, `RATE_LIMITED`. ACK reason admite estos y OK; lastRemote solo subset
§6.3. Ningún texto libre/credencial/código/interacción en errores.

Firma/esquema inválido no genera ACK aplicado/rechazado parental basado en datos
no autenticados; cerrar o ERROR sin correlación fiable (ref null). Relay ERROR no
confirma aplicación/aceptación. UNKNOWN_VERSION/subprotocolo cruzado impiden routing.

## 10. Pareja, snapshot y migración física explícita

Compatible en identidad: preservar públicas P-256, pairId/origen del snapshot
íntegro existente, comparar dos huellas/pairId/origen como C03. PAIR_ACCEPT CP/1
guardado sigue evidencia histórica validada **bajo CPv1/PAIR**; no aceptación v2
ni permiso. Nueva instalación/pairing v2 y OFFLINE requieren revisión aparte de
formatos/contextos; esta unidad no inventa recibos exactos o reset remoto.

Migración acordada de tres roles y presencial: ambos clientes/relay con mismo
perfil público, confirmar versión/procedimiento y protección/emergencia/recuperación;
conservar identidad/vínculo, contador máximo/reservas/intención pendiente/bytes v1,
última remota y resultados inciertos, consumos/esperas de códigos existentes y
credencial/pregunta/esperas si ya existen. Privado versionado/readback atómico,
sin reemplazo de claves, fabricar código/pregunta/respuesta/permiso/acreditación.
Registrar procedencia v1 del pending: reconciliar por vía compatible autorizada
antes de transición cuando posible; si no, mantener desconocido, NO enviar en v2,
reinterpretar hash ni firmarlo con v2 como retry. Nueva acción v2 requiere decisión
expresa/seq mayor/sync fresco. Época contable comienza sin fabricar pasado v1; UI
explica inicio de cobertura, no «total histórico cero». Bloqueado al migrar hasta
nueva autorización del boot actual. STATE/versión/presencia nunca disparan migración.

No aceptar CPv1 como autoridad operativa en perfil v2 aunque privada sea misma.
Preservar pruebas/consumos de recuperación anteriores sin reactivar códigos; cómo
mantener rutas de recuperación CP/1 durante corte de versión exige decisión conjunta
antes de habilitar migración (sin deshabilitar recuperación). No firmar OFFLINE v2
con codec v1. Si no hay ruta segura, transición bloqueada, no parche de compatibilidad.
Downgrade falla cerrado en código nuevo; APK antiguo/rollback adversarial no queda
resuelto por marcador de versión. No borrar estado para reparar ni prometer antirollback.

## 11. Ejemplos documentales y matriz de pruebas FUTURAS

Son trazas con etiquetas, no JSON firmable/fixtures ni datos de dispositivos.
R=controlRevision, S=seq remoto, t=ms relativos del boot de laboratorio.

| Caso | Traza / resultado exigido del draft |
|---|---|
| Carrera | Sync acción LOCK en R=10; local ALLOW_FOR confirma R=11; LOCK con expected10 rechaza STALE_REVISION. Padre conserva reserva S y pide sync/confirmación nueva; LOCK nuevo compromete12; local posterior puede permitir13. |
| Duplicado y ACK viejo | Remota S7 aplicada en R12; local R13 permite. ACK S7 accepted12 resuelve solo esa orden; no reemplaza STATE R13. Duplicado S7 exacto retorna histórico, no revoca local ni reinicia t. |
| Timer viejo | ALLOW_FOR R13 sustituido por R14: callback R13 no bloquea14. LOCK_FOR R15 sustituido por16: apertura15 no concede permiso. |
| Agotado offline y después permitir | ALLOW_FOR acepta t0 duración60, offline deadline60000 consolida revisión+1 y lastExpiry; bloqueo real PENDING no APPLIED. Local permite después nueva revisión; STATE al reconectar conserva expiración anterior y permiso actual separado. |
| Apertura tardía contextual | LOCK_FOR60 + deferredAllow60 aceptado t0; callback t90000 permite solo hasta120000 (30s restantes), si misma revisión/boot e integridad. Callback t130000 no abre; consolida agotamiento, tiempo lógico autorizado según continuidad comprobable §8. |
| Tiempo sin solapamiento | Acepta permiso t10000; reemplaza con otro t40000; reporta t70000: consumido60000ms, no suma dos duraciones solicitadas. Pantalla apagada no pausa deadline; inclusión en contador sigue opción §12. |
| Proceso mismo boot | Punto persistido t40000, permiso hasta100000, reabre t70000: reconstruye30000ms adicionales solo bajo prueba de continuidad; no renueva deadline. Si cierre perdido/gap incierto, PARTIAL y subtotal conocido. |
| Reboot y día incierto | Último subtotal90s; fin boot no cuantificable; nuevo boot sin permiso. consumedMs conserva90000, PARTIAL, day=null si asignación desconocida; no cero ni estimación humana del gap. |
| Mezcla perfiles | cp.v1/v2 incompatibles o CPv1/MESSAGE en sobrev2: rechazo; no fallback/promoción. Claves iguales no bastan. |

| Rol / grupo | Invariantes y pruebas a asignar, NO ejecutadas |
|---|---|
| Compartido futuro | Bytes exactos, null/tipos/rangos/extras/duplicados/base64/DER/overflow; dominios/roles cruzados, versión/sobre/subprotocolo; hash payload no firma. |
| Hijo | Escritor serial y crash en commit/IO; local vs remoto vs timer; retos consumidos/revisión/sesión/boot; permiso offline de dos fuentes; deadline vs enforcement; contador unión/subtotal/calidad/reconstrucción/día; credencial/pregunta/esperas privadas y emergencia independientes. |
| Padre | Reserva nunca reutilizada; desconocido no resuelve; ACK histórico no sustituye STATE; stateSeq/frescura/boot; no inferir persona/red/total offline; snapshot absoluto reemplaza sin deltas; aviso anterior separado de permiso posterior; compatibilidad/migración/pendingv1. |
| Relay | AUTH/perfil/rol/root pública, sustitución socket autenticado, validación/routing exactos, cruces de versiones/reinicio; cero historial/colas/secretos/lógica de tiempo/permisos, PEER no APPLIED. |
| Android/físico posterior | Autoridad RAM/fondo/proceso, boot/Keyguard, protección básica Ajustes/admin/desinstalar hijo e instalar/actualizar permitido; errores/emergencia/recuperación antes y bajo endurecimiento. Continuidad contable/enforcement real por autorización específica, sin acreditar por tests/firma. |

## 12. Pocas elecciones bloqueantes y mapa revisable

1. **Base contador:** recomendar autorización transcurrida incluyendo apagado/Keyguard,
   distinto de humano; o solo pantalla desbloqueada con nuevo diseño de observación.
   No afecta deadline confirmado. Elegir antes de cerrar basis/contador productivo.
2. **Día/retención:** recomendar época/subtotal + solo día actual parametrizado UTC
   fijo, sin historial, calidad degradable; confirmar zona civil deseada y retención
   del último agotamiento. Ninguna respuesta previa está asumida.
3. **Entrega de avisos:** recomendar app abierta/consulta al volver y último agotamiento
   mínimo, sin promesa con padre cerrado. Si pide entrega con app muerta, otro diseño/
   permisos explícitos; no introducir servicios por este draft.
4. **Acciones temporales:** rango60..604800 y LOCK_FOR con apertura limitada elegida
   propuestos; confirmar rango y apertura finita o alternativa indefinida explícita.
   LOCAL siempre tiempo elegido, no botón removido ni default implícito.

KDF/normalización/cupos/admin/reset/olvido doble son subunidades privadas pendientes;
no multiplicar preguntas de contrato ni aprobar parámetros al definir campos aquí.

Mapa: revisión hijo+relay de esta propuesta y compatibilidad/recuperación → acuerdo
de decisiones/ADR/versionado de tres roles → schema/vectores nuevos separados y
matriz interop → modelos puros/reconciliación/escritor/contador → persistencia y
migración por rol → integración local y evidencia física autorizada de protección
básica/emergencia/recuperación/enforcement. Planes/puertas se actualizan solo después
del acuerdo; H05/H06 y C1/C2 no aprobados por draft ni L02/PADRE-C02. Revisión exhaustiva
de escapes pospuesta no autoriza quitar guardas ni ignorar fallos encontrados.

## 13. Alcance de cierre

Una unidad documental. Sin código/tests/builds/schema/fixtures/codec/CP1/plan/ADR
aceptados cambiados, validador C02/main/UI/Keystore/Android/notificaciones/UsageStats,
dispositivos/ADB/CA/LAN/push/despliegue, repos externos, sesiones/subagentes/prompts
de vuelta ni siguiente tarea automática. QR `a8e4ffc` sigue reproducido NO corregido;
QR02/lector necesitan asignación aparte. No se repitió laboratorio QR/L02.
Verificaciones de cierre solo Git sobre documentos propios; hash se entrega tras
commit. Coordinador revisará propuestas de ambos clientes/relay antes de acuerdo
y sincronización. No se autoriza ejecución del mapa anterior.
