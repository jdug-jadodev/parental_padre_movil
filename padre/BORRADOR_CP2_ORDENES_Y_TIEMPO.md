# PADRE-C04-R1 — borrador integrado CP/2 de órdenes, estado y tiempo permitido

Fecha: 2026-10-13. Base padre `1582e66`. **DRAFT para revisión de hijo/relay;
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

R1 integra HIJO-C07 `8b25c24` íntegro (tres partes) y revisión relay13oct íntegra,
**textos suministrados por coordinador**, no archivos externos leídos/verificados
por esta sesión. Decisiones finales13oct suministradas en asignación R1 prevalecen
sobre opciones abiertas de C07/C04. No se repitió ejercicio de tamaño ni laboratorio.

Confirmado por propietario, sustituye alternativas anteriores de C03:

- Código local NUMÉRICO elegido de 5–10 caracteres, conservando ceros iniciales;
  pregunta personalizada offline para establecer otro código, sin revelar anterior.
  No código extra obligatorio. Normalización/KDF/esperas/administración/reset/olvido
  doble siguen diseño privado pendiente, no mensajes remotos de este borrador.
- LOCAL y REMOTO ofrecen ALLOW libre y ALLOW_FOR con minutos enteros elegidos,
  1–1440 minutos, sin preselección/default30min. LOCK inmediato sin apertura automática.
  Ambas autoridades cambian;
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

Contador incluye apagado/Keyguard, sin pausar/renovar deadline. Historia agregada de
varios días se limpia domingo21:00 de zona LOCAL configurada; catch-up antes de
mostrar/enviar si apagado, ocultar/purgar ante calendario incierto sin cancelar
permiso. Push padre cerrado SÍ se evalúa solo como diseño (§8.5), sin proveedor,
cuenta/SDK/token/servicio/permisos o garantía de WSS con app muerta activados.
Credencial/pregunta/respuesta/salt/verificador,
catálogo, apps, ubicación e interacción quedan fuera de protocolo y servidor.

## 2. Perfil de bytes, versiones y transporte propuesto

| Elemento | Valor exacto propuesto / diferencia CP/1 |
|---|---|
| Negociación | WSS /ws, TLS validado; cliente migrado ofrece SOLO `cp.v2`, servidor selecciona exactamente `cp.v2` o rechaza upgrade. Nunca fallback automático a cp.v1. |
| Versión | `v:2` en cada frame y payload; sobre y payload deben coincidir con socket. No reinterpretar v1 en v2 ni v2 en v1. |
| Sobre firmado | Campos exactos `{kind,payloadB64,purpose,sigB64,v}`; kind=`SIGNED`, purpose=`AUTH` o `MESSAGE` para mensajes de §4–7. |
| Firma | P-256/secp256r1, SHA256withECDSA, ASN.1 DER estricta (8–72 bytes, r/s válidos 1..n−1); SPKI DER validado de pública vinculada. No JOSE/r\|\|s, alg seleccionable o pública desde orden. |
| Dominio | `UTF8("CPv2/" + purpose + "\n") || payloadBytes`; verificar bytes recibidos, no JSON reconstruido. Firma CPv1 no válida aunque claves sean las mismas. |
| PAIR/OFFLINE | Propósitos separados `CPv2/PAIR\n` y `CPv2/OFFLINE\n` propuestos en §10; solo intercambio local, nunca WSS/MESSAGE ni ALLOW por recuperación. No trasladar bytes CPv1 cambiando solo v/prefix. |
| Hash | `SHA256(payloadBytes)` exactos, 32 bytes base64url canónico sin padding, 43 caracteres. No hash de DER/sobre ni de JSON reformateado. |
| Serialización | Claves ASCII ordenadas lexicográficamente en todo objeto, UTF-8 estricto, sin espacios/newline finales/duplicados/desconocidos; arrays ordenados. Strings ASCII imprimible, sin controles. null explícito solo donde se enumera. |
| Números | Enteros seguros JSON no negativos, sin exponentes/decimales/−0. Grandes contadores/monotónicos como cadenas decimales canónicas (tipos §3). No fechas para permitir apps. |
| Base64url | Alfabeto A–Z/a–z/0–9/_/− sin =; validar longitud, bits finales y roundtrip exacto. |
| Límites | Frame UTF-8 ≤16384 bytes, payload decodificado ≤8192 bytes; un solo objeto, no binarios/compresión. Profundidad ≤6; único array permitido `permittedTime.days`, máximo8 ordenado por fecha. Comprobar límites antes de crypto/parse anidado; derivación documental §14 pendiente de verificación. |
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
| D | Entero JSON 60..86400 segundos, múltiplo60: elección de 1..1440 minutos enteros convertida exactamente a segundos; sin preselección. |
| Fuente | `NONE`, `LOCAL`, `REMOTE`; no identificar adulto, app ni teclado. |
| Acción | Solo `LOCK`, `ALLOW`, `ALLOW_FOR`. Bloqueo temporal/apertura automática eliminados por decisión13oct; CP/1 no se modifica. |

`controlRevision`: C global durable de instalación (inicia 0 solo en instalación
nueva autorizada), NO seq remoto ni stateSeq. Cambia por decisión local/remota
comprometida, transición de timer vigente, boot nuevo/incierto, revocación por error
o migración autorizada. Una conexión/sync/PONG no la incrementa. Cada cambio invalida
desafíos y confirmaciones previas. Enforcement del mismo objetivo actualiza estado
sin generar una nueva decisión ni renovar su deadline. Máximos no retroceden.

`seq`: C+ exclusivamente padre; `lastAcceptedSeq`: C máximo durable hijo, 0 si
nunca aceptó orden. `stateSeq`: C+ creciente por sesión WSS del hijo, no por boot;
nueva sesión inicia 1. Revisión global sigue creciente entre boots/sesiones.
`retentionSeq`: C+ durable creciente de limpieza/configuración; `historyRevision`:
C durable creciente por materialización/purga/calidad. No son seq/controlRevision/
stateSeq ni reloj. `epochId`: U identidad de ventana retenida, **no UUID ordenable**.
Purga cambia época/generación/revisión contable, no autoridad/seq. Máximos agotados
no se reinician: bloquear publicación histórica/operación afectada, conservar
emergencia/recuperación y permiso íntegro independiente, sin overflow ni fabricar cero.

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
| durationSec | D solo ALLOW_FOR; null LOCK/ALLOW. Campo de apertura diferida inexistente, no null tolerado. |

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
Antes de validar CAS o emitir sync/STATE, consolidar boot/revocaciones y expiración
normal ya vencida en escritor: timer tardío no deja viva revisión/reto de permiso
agotado. Luego revalidar expectedRevision/nonce; conflicto exige confirmación nueva.

Local no envía SET_POLICY ni usa seq. Ticket RAM con boot/revisión/action/autoridad
local y caducidad; confirmar con escritor revalidando generación administrativa y
credencial/contexto. Commit local incrementa revisión y anula reto remoto/timer
reemplazado. Salir/fondo/proceso invalida tickets RAM, no permiso ya comprometido.

| Acción | Semántica propuesta v2 |
|---|---|
| LOCK | Revocar permiso normal ahora; sin deadline de apertura. |
| ALLOW | LOCAL y REMOTO: libre desde aceptación hasta decisión/revocación válida/reboot, incluso offline; selección explícita, no duración infinita escondida en ALLOW_FOR. |
| ALLOW_FOR | Autoriza desde aceptación t hasta t+durationSec×1000, aun offline. Al vencer pasa a autorización bloqueada, nueva revisión; intento de bloqueo/enforcement se reporta aparte. |

Solo ALLOW_FOR tiene timer normal: expiración incrementa revisión una vez, nunca
abre ni renueva permiso. Callback tardío consolida deadline original, cuenta hasta
ese límite y distingue detección/aplicación tardías. Overflow de t+duración impide
aceptar. Acción nueva sustituye entero el plan sin acumular horas/callbacks.
LOCK no caduca; ni reconexión/cron/recuperación abre uso automáticamente.
LOCK_FOR, deferredAllowSec y WAITING_ALLOW se mencionan aquí únicamente como
elementos prohibidos: action/campo/fase vieja => BAD_SCHEMA, no alias ni tolerancia.

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
| lastExpiry | objeto §8.4 o null |
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
- phase=`BLOCKED|PERMITTED|EXPIRED|NONE`; expresa autorización,
  NO éxito físico. `enforcement=PENDING|VERIFIED|FAILED|UNKNOWN` para objetivo actual.
  ALLOWED solo si permiso vigente y aplicación comprobada. Keyguard se explica con
  SYSTEM_KEYGUARD sin cancelar phase/contador: no prueba bloqueo parental aplicado.
  Si impide evaluar efecto normal, mode UNKNOWN; LOCKED solo con bloqueo parental
  comprobado, no por ver PIN Android. No fingir éxito por objetivo solicitado.
- acceptedElapsedMs=M? inicio original de decisión en este boot;
  phaseStartedElapsedMs=M? igual a acceptedElapsedMs en decisión normal, sin apertura.
  deadlineElapsedMs=M? límite de fase vigente, null si indefinida/sin temporizador.
- remainingSec=entero 0..86400?; con deadline es
  ceil(max(0,deadline−sampled)/1000), sin deadline null. Nunca plazo nuevo del receptor.
  Deadline≤sampled implica fase no PERMITTED tras consolidar timer.
  ALLOW normal tiene phase PERMITTED y deadline null; LOCK BLOCKED/null;
  ALLOW_FOR PERMITTED o EXPIRED. LOCK BLOCKED y ALLOW PERMITTED no tienen deadline.

Restricciones de combinación: sin decisión, action/policyRevision/acceptedElapsedMs/
phaseStartedElapsedMs/deadlineElapsedMs/remainingSec null y phase=NONE. Con decisión,
action/policyRevision/acceptedElapsedMs/phaseStartedElapsedMs no null,
accepted=phaseStarted≤sampled. policyRevision≤controlRevision. EXPIRED conserva
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

## 8. Autorización consumida, retención semanal y avisos

### 8.1 Ledger activo distinto de historia

Base confirmada: unión disjunta de intervalos de autorización normal desde aceptación
duradera hasta min(deadline, sustitución, revocación válida, fin de boot conocido).
Incluye apagado/Keyguard/offline; ALLOW libre solo acumula transcurrido, nunca infinito
ni presupuesto futuro. Abrir/cerrar administración RAM no abre/cierra permiso ni
contador paralelo. Sustitución cierra tramo anterior y abre nuevo al mismo instante
lógico del escritor. Pendiente/fallo de enforcement no acredita Android funcionando;
si no revoca autoridad, su tiempo lógico sigue contando. Revocación por error corta
intervalo/revisión, pero conocer error no prueba hora de revocación anterior.

Antes de STATE materializar cursor/subtotal en único escritor y readback, después
firmar snapshot inmutable de sampled; no timestamp clic/envío/ACK ni modo posterior
backdateado. IO incierto no confirma aceptación, purga o incremento; relectura íntegra
recupera inicio/deadline originales sin retry que amplíe. Readback no prueba energía/
antirollback. Fallo de historia impide publicar historia, no cancela por sí solo
permiso íntegro ni corta emergencia/recuperación. Corrupción de autoridad sí falla seguro.

Mismo boot: reconstruir solo [cursor,sampled] intersecado con permiso durable aún
válido hasta min(deadline,cierre conocido); probar boot/reloj/registro íntegros y
ausencia de evento revocador perdido. BootContext o archivo válido solos no bastan.
RECONSTRUCTED es autorización, no persona/observación. Gap incierto: subtotal PARTIAL
o null UNKNOWN, no cero/duration. Boot nuevo bloquea, no rellena hueco del boot viejo
con fecha civil; conservar solo agregados comprobables aún retenibles. L02 no se
promueve ni su regla de observación/gap se importa al ledger productivo.

Registro privado mínimo activo: boot/revisión/inicio/deadline/cursor de permiso,
independiente de buckets/último aviso. Tras purga conserva lo necesario para sufijo
posterior, sin reconstruir prefijo borrado ni duplicar duración desde aceptación.
No intervalos, apps, personas o catálogo en protocolo/relay. Total es informativo,
no cupo diario ni nueva orden de reset.

### 8.2 permittedTime cerrado y absoluto

Campos exactos: `basis, epochId, retentionSeq, historyRevision, consumedMs, quality,
throughElapsedMs, retention, days`.

| Campo | Tipo/regla |
|---|---|
| basis | Constante `AUTHORIZED_ELAPSED`, incluye apagado/Keyguard, no humano. |
| epochId | U época de retención vigente; cambia por corte/cambio autorizado, no sync/proceso/boot normal. |
| retentionSeq | C+ generación durable creciente de limpieza/configuración. |
| historyRevision | C revisión durable creciente por materialización/purga/cambio de calidad, no PONG; persiste entre épocas/boots. |
| consumedMs | C? suma absoluta conocida solo de ventana retenida, no vitalicia. Null sin subtotal fiable; cero solo comprobado. |
| quality | `EXACT|RECONSTRUCTED|PARTIAL|UNKNOWN`; UNKNOWN exige null, demás exigen C. PARTIAL es límite inferior conocido, no total exacto. |
| throughElapsedMs | M igual sampledElapsedMs del boot STATE, no fecha universal. |
| retention | Objeto cerrado §8.2.1, no null. |
| days | Array único de 0..8 objetos §8.2.2, fecha creciente sin duplicados; reemplaza day, no ambas variantes. |

Dentro de misma época/configuración subtotal no disminuye, historyRevision no
retrocede y cambio de contenido exige incremento. Igual historyRevision implica
idéntico contenido contable salvo throughElapsedMs de reporte sin cambio lógico;
no curar gaps/calidad por reconectar. Nueva época total puede empezar cero solo con
cobertura comprobable desde corte. [] UNKNOWN no significa «ningún uso»; [] con
cero EXACT solo antes de cualquier tramo/bucket contabilizado y con cobertura válida.
No omitir un día de cobertura para ocultar incertidumbre: bucket cero comprobado
o null UNKNOWN si necesario. Array no lleva días futuros ni fuera de ventana.

### 8.2.1 retention: calendario y nulos

Campos exactos: `policyId:U, zoneId, purgedThroughUtc, nextCutoffUtc,
calendarQuality`. zoneId IANA ASCII1..64 validada, **zona local configurada por
adulto**, no ubicación ni UTC impuesta; configuración solo local autorizada.
UTC de corte: string20 `YYYY-MM-DDTHH:mm:ssZ`, años0001..9999/fecha válida, sin
fracción/offset alternativo. schedule fijo domingo21:00 local, sin campo remoto
para hora/día. Regla DST propuesta: primera ocurrencia si ambigua; primer instante
válido posterior si inexistente. Ratificación técnica pendiente (§12), no dato
elegible por peer ni cambio de deadline monotónico.

| calendarQuality | Combinación admitida / consecuencias |
|---|---|
| KNOWN | Ambos cortes no null; next>purgedThrough, conversión coherente con policy/zone; latest corte vencido consolidado antes de publicar. |
| UNKNOWN | next null; purgedThrough null si ninguno fiable o máximo fiable conservado. days=[], consumedMs=null, quality UNKNOWN y lastExpiry null; sin publicar agregados inciertos. |

purgedThrough es high-water mark durable, nunca retrocede aun si hora se atrasa.
La zona/policy se establecen presencialmente antes de habilitar el perfil/registro
v2; calendario UNKNOWN puede conservar esa configuración íntegra con ancla incierta.
Si también se perdió configuración/procedencia de registro, no inventar zona/policy/
epoch para producir STATE válido: error de integridad y reconciliación segura;
la autoridad íntegra independiente no se reemplaza por ausencia de historia.
En alta/migración a media semana se usa última frontera semanal para no ampliar
ventana, pero cobertura empezó en configuración: no fabricar resto del día/semana.
Instantes reportados no prueban hora real: receptor comprueba su propia barrera y
frescura, relay solo tipos/coherencia estructural acordada.

### 8.2.2 days: buckets diarios de ventana, no vida completa

Cada objeto cerrado: `date, consumedMs, quality`. date ISO string10 YYYY-MM-DD,
fecha válida años0001..9999; contador/calidad mismas reglas de null/subtotal.
Policy/zone/epoch están arriba, no se duplican. Ventana [corte,siguiente corte)
toca hasta8 fechas: domingo inicial desde21, lunes–sábado y domingo final hasta21.
Domingos parciales/de configuración tardía son PARTIAL respecto a día entero; UI
«desde limpieza». Total ventana puede EXACT aunque días extremos sean parciales
deliberadamente; debe estar justificada cobertura completa **de ventana**, no exigir
que día civil completo haya sido autorizado. Días completos sin huecos EXACT o
RECONSTRUCTED; gap no cuantificable PARTIAL o UNKNOWN.

consumedMs total suma subtotales no null, usando aritmética acotada sin overflow.
Si algún bucket null/gap, total solo PARTIAL con subtotal fiable, o UNKNOWN/null
si ninguno recuperable; nunca sumar null como cero exacto. Parcialidad deliberada
por recorte civil no degrada por sí sola total ventana; parcialidad por gap sí.
Ancla civil/monotónica y zona estables necesarias para dividir; DST23/25h no se
normaliza a24h. Más de8 fechas por incoherencia no se trunca/redistribuye: UNKNOWN
y purga conservadora. Máximos C agotados no resetean historia para continuar.
Combinaciones: total EXACT/RECONSTRUCTED exige KNOWN, ningún bucket UNKNOWN/null
o PARTIAL interior; PARTIAL de frontera solo es compatible si es recorte deliberado
sin gap. Total PARTIAL requiere subtotal recuperable no null. Total UNKNOWN exige
null; UNKNOWN calendario obliga array vacío. Calidad declarada no prueba ausencia
física de gap: receptor no deduce exactitud solo de fechas parciales o firma.

### 8.3 Purga local, catch-up y anti-resurrección

Domingo21 local limpia buckets/total/último agotamiento en hijo y padre sin red.
Si proceso apagado/muerto, catch-up obligatorio antes de leer/renderizar/enviar
historia al iniciar/recrear/boot/desbloqueo/STATE/push; varios cortes omitidos
consolidan último vencido, no reconstruyen semanas eliminadas. Aceptado catch-up,
no garantía física de borrar bytes mientras apagado, ni borrado forense flash.

Hijo: transacción materializa hasta corte con ancla fiable, elimina historia previa,
rota epochId e incrementa retentionSeq/historyRevision, watermark durable/readback.
Permiso largo/libre atraviesa corte intacto: conserva inicio/deadline/boot/revisión
y cursor activo para contar solo sufijo. No borrar controlRevision/seq/pending/
lastRemote/reservas/identidad/credenciales/consumos/esperas; lastRemote nunca canal
alternativo para recuperar historia eliminada. Revisión contable no invalida reto
operativo salvo que cambie autoridad por causa independiente.

Ancla/hora/zona inciertas: ocultar/publicación suspendida, purgar agregados/aviso
no demostrablemente posteriores al corte aún aplicable, elevar barrera durable,
UNKNOWN hasta configuración/ancla coherentes. No cancelar permiso ni reiniciar
deadline. Zona cambiada explícitamente abre nueva época, no redistribuye pasado.
Si nueva programación contradice watermark máximo, UNKNOWN, no ampliar retención
o rollover doble. Fallo de purga: ocultar historia y no confirmar borrado antes
de readback; no reenviar snapshot viejo para «seguir funcionando».

Padre guarda tombstone local **sin totales**: máximo retentionSeq conocido del hijo,
epoch/policy admitidos, máximo historyRevision, cutoff propio y barrera local de
caché. Limpia offline aumentando barrera propia, no inventa generation hijo.
Antes de admitir historia exige nuevo sync firmado con corte igual/posterior al
cutoff propio y fechas de ventana local permitida; si barreras no comparables,
ocultar hasta reconciliación segura. No impide procesar evidencia operativa válida.

| Snapshot contable recibido | Tratamiento padre propuesto |
|---|---|
| retentionSeq menor / cutoff anterior a barrera | Rechazar historia aunque firma/stateSeq válidos. |
| Misma generation, epoch/policy distintos | Incoherente; ocultar, nuevo sync, no comparar UUID como número. |
| Misma época, historyRevision menor o subtotal retrocede | No reemplazar por dato viejo; si inconsistencia actual, desconocido/sync, no fabricar total. |
| Nueva época/policy | Exigir retentionSeq mayor, historyRevision no retrocedido y corte coherente con barrera; nuevo subtotal reemplaza, no sumar semanas. |
| UNKNOWN actual válido | Borrar/ocultar caché factual, conservar tombstones/máximos; null no cero. |
| Nueva sesión | Sync nuevo y stateSeq propio, pero watermarks durables no se reinician. |

Frescura §6.4 y sesión/stateSeq también requeridas; firma solo origen/integridad.
Limpiar render/notificaciones/cachés/copia local vencida; backup/restauración valida
barrera antes de exponer. Tombstone perdido/reinstalación/rollback no se soluciona
por firma: sin procedencia íntegra/calendario fiable no aceptar historia hasta
reconciliación segura, no enrolamiento libre/borrado de vínculo. Excluir historia
de backups y revisar temporales en diseño posterior; no prometer antirollback universal.

### 8.4 lastExpiry: solo último agotamiento dentro de retención

Objeto cerrado: `bootId:U, policyRevision:C+, expiredRevision:C+,
source:LOCAL|REMOTE, deadlineElapsedMs:M, detectedElapsedMs:M,
enforcement:PENDING|VERIFIED|FAILED|UNKNOWN, retentionSeq:C+`.
Solo expiración ALLOW_FOR retenible de ventana actual, null sin dato fiable;
detected≥deadline en boot del registro, expiredRevision>policyRevision.
retentionSeq igual a permittedTime actual, revisión≤controlRevision. Monotónicos
de boot viejo no se comparan con sampled de boot nuevo. El ledger privado liga
deadline a calendario fiable para retención; campos públicos no prueban esa fecha.
Callback tras corte con deadline anterior no recrea aviso borrado: effective puede
EXPIRED sin lastExpiry. Si no puede probarse que expiró dentro de ventana, null.
Purga elimina aviso, no «hasta próxima expiración» indefinido. Permiso posterior
no borra aviso aún vigente, pero lo presenta histórico, no modo actual/APPLIED.
Detección tardía no renueva deadline. Enforcement solo evidencia del bloqueo de
ese vencimiento; sustituido antes de comprobar conserva UNKNOWN/PENDING.

### 8.5 Evaluación push mínima, solo diseño

Admitida evaluación con padre cerrado, sin proveedor/cuenta/SDK/token/endpoint/
servicio/permisos activados. Contrato conceptual mínimo de hint: constante
`CONSULT_STATE`, sin contenido de estado/historia/orden/duración/APPLIED/secreto.
No frame cp.v2 adicional ni routing/emisión/tokens en relay por esta propuesta.
Si direccionar hint exige identificador de instalación/pareja/generación, definir
minimización/consentimiento/emisor separadamente antes de formato/transporte.

Hint no autenticado es rate-limited/deduplicado y nunca factual ni autoridad: no
SET_POLICY automático, ni SYNC solicitado de acción. Padre limpia/catch-up antes
de mostrar y obtiene STATE CHILD vigente correlacionado por WSS cuando lifecycle/
autenticación autorizados lo permitan. Sin sync: «aviso pendiente de verificar»,
no hijo offline/bloqueado/tiempo exacto. Si clave requiere adulto, pedirlo, no bypass
para background. Push atrasado no revive semanas o avisos, no cola de órdenes.
Proveedor/emisor/coste/retención de tokens/consentimiento/permisos/notificaciones/
FGS/app muerta/red ausente son evaluación posterior, no garantía de recepción.
PONG/PEER no internet hijo ni humano. Sin reporte causa desconocida; VALIDATED=false
solo último reporte Android, no diagnóstico permanente. Historial nunca en push.

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
ni permiso. Se concretan mínimos propuestos siguientes para revisión conjunta;
su existencia documental no cierra alta/recuperación ni habilita versión.

### 10.1 Alta v2 local: formatos mínimos cerrados propuestos

En todos v=2, protocol=`cp.v2`, bytes/canonización §2. `K` pública SPKI P-256
base64url canónica100..200 caracteres, DER/curva/punto validados; `O` origen HTTPS
ASCII≤200 con gramática CP/1 (sin ruta/query/fragmento/credenciales, puerto válido),
igualdad exacta al snapshot, no normalización silenciosa. Límites heredados, no
secretos del código adulto. Los hashes de recuperación solo en intercambio PAIR
local autorizado, nunca STATE/relay/push; no son verificador de código maestro.

| Formato / propósito | Campos exactos además de v/kind/protocol |
|---|---|
| CHILD_OFFER / objeto local no firmado como CP/1 | childPublicKeyB64:K, enrollId:U, enrollNonce:N |
| PAIR_ACCEPT / PARENT firmado PAIR | pairId:U, enrollId:U, enrollNonce:N, parentPublicKeyB64:K, childPublicKeyB64:K, relayOrigin:O, recoveryHashes:objeto cerrado MAINTENANCE:H/RECOVER_PARENT:H/RETIRE:H |
| OFFLINE_CHALLENGE / CHILD firmado OFFLINE | pairId:U, sender:CHILD, bootId:U, expectedRevision:C, action:`MAINTENANCE|RETIRE`, nonce:N |
| OFFLINE_RESPONSE / PARENT firmado OFFLINE | pairId:U, sender:PARENT, bootId:U, expectedRevision:C, action:`MAINTENANCE|RETIRE`, nonce:N |

CHILD_OFFER solo válido en preparación presencial autorizada/enrolamiento existente,
enrollId/nonce RAM de ventana5min, nunca reabrir por archivo ausente/corrupto.
PAIR_ACCEPT liga oferta viva/identidad/perfil y confirmación humana en ambos equipos;
clave propuesta padre se valida para aceptación inicial solo aquí, no como sustituto
en orden. Guardar/readback snapshot íntegro juntos antes de exponer commit confirmado;
perfil/capacidad/red/firma no son acreditación operativa. Comparar pairId/públicas/
origen/perfil, no recibos exactos nuevos. Credencial/pregunta se configuran solo en
hijo por procedimiento local; no agregarlas a QR/códigos CP de recuperación.

OFFLINE reto5min monotónicos RAM ligado boot/revisión/acción/pareja, sustitución
invalida reto viejo. Respuesta reproduce exactamente contexto autorizado bajo
dominio CPv2/OFFLINE, verificación pública vinculada/revalidación en escritor,
consumo durable antes de función. No ventana administrativa persistente/restaurada
por proceso/boot. MAINTENANCE herramientas limitadas5min, nunca ALLOW/reset de
credencial; RETIRE requiere confirmación destructiva adicional local BORRAR como
CP/1, no ejecución autorizada por este documento. Esperas de códigos no afectan
respuesta QR firmada legítima ni emergencia.

Tres códigos únicos CP de recuperación mantienen propósitos MAINTENANCE/
RECOVER_PARENT/RETIRE,20bytes aleatorios/Base32 de32, normalización/consumo/esperas
CP/1 (distintos del código NUMÉRICO/pregunta locales). Para **alta v2 nueva** hash
propuesto `SHA256(UTF8("CPv2/RECOVERY\n"+pairId+"\n"+purpose+"\n"+codigoNormalizado))`.
RECOVER_PARENT conserva ventana presencial5min, consumo antes de abrir; vínculo
previo se mantiene hasta nuevo commit, allí revoca padre anterior, nuevo pairId/
públicas padre/códigos únicos por ceremonia, actualización pública relay antes de
remoto. No regeneración libre, retorno de consumos o permiso normal por recuperación.
No se añade cuarto código, acción remota reset o recibo de pairing/consumo.

### 10.2 Migración y procedencia de recuperación legada

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
Propuesta mínima de continuidad: cada hash impreso migrado conserva esquema de
hash **CPv1/RECOVERY**, pairId/purpose de origen, bits de consumo y esperas; verificar
localmente ese esquema identificado en registro íntegro, nunca reinterpretarlo
CPv2 ni necesitar recuperar código claro para rehash. Nuevos desafíos QR usan SOLO
OFFLINEv2 y públicas preservadas; v1 OFFLINE/MESSAGE/PAIR no autorizan operación
nueva en perfilv2. El verificador local de código legado no es fallback WSS.

Antes de corte deben existir lectores locales versionados coherentes de hashes/
consumos, herramientas recuperación/migración probadas y capacidad offline padre/
hijo de generar/verificar esos formatos propuestos. Registrar perfil/versión del
snapshot en ceremonia y no confundir ventana de código legado con autoridad normal
v2. Todo puente adicional exige acuerdo explícito de propósito/capacidad/versión/
consumo, no endpoint ni aceptación dual implícita. Si implementación/readback/
capacidad faltan, transición BLOQUEADA, no deshabilitar recuperación ni convertir
firma v1 a v2 cambiando prefix. QR actual defectuoso no se declara operativo por
estas tablas. Opciones de continuidad/hashes nuevos aún requieren acuerdo de tres
roles y evidencia local/física autorizada antes de freeze/migración.
Downgrade falla cerrado en código nuevo; APK antiguo/rollback adversarial no queda
resuelto por marcador de versión. No borrar estado para reparar ni prometer antirollback.

## 11. Ejemplos documentales y matriz de pruebas FUTURAS

Son trazas con etiquetas, no JSON firmable/fixtures ni datos de dispositivos.
R=controlRevision, S=seq remoto, t=ms relativos del boot de laboratorio.

| Caso | Traza / resultado exigido del draft |
|---|---|
| Carrera | Sync acción LOCK en R=10; local ALLOW_FOR confirma R=11; LOCK con expected10 rechaza STALE_REVISION. Padre conserva reserva S y pide sync/confirmación nueva; LOCK nuevo compromete12; local posterior puede permitir13. |
| Duplicado y ACK viejo | Remota S7 aplicada en R12; local R13 permite. ACK S7 accepted12 resuelve solo esa orden; no reemplaza STATE R13. Duplicado S7 exacto retorna histórico, no revoca local ni reinicia t. |
| Timer viejo | ALLOW_FOR R13 sustituido por R14: callback R13 no bloquea14. LOCK R15 permanece bloqueado sin apertura hasta nueva autorización explícita. |
| Agotado offline y después permitir | ALLOW_FOR acepta t0 duración60, offline deadline60000 consolida revisión+1 y lastExpiry; bloqueo real PENDING no APPLIED. Local permite después nueva revisión; STATE al reconectar conserva expiración anterior y permiso actual separado. |
| Expiración tardía | ALLOW_FOR60 aceptado t0, detecta t90000: consumido solo hasta60000, deadline60000/detected90000; jamás nuevo plazo, reporte bloqueo PENDING hasta comprobar efecto. |
| Tiempo sin solapamiento | Acepta permiso t10000; reemplaza con otro t40000; reporta t70000: consumido60000ms, no suma dos duraciones solicitadas. Apagado/Keyguard cuentan y no pausan deadline. |
| Proceso mismo boot | Punto persistido t40000, permiso hasta100000, reabre t70000: reconstruye30000ms adicionales solo bajo prueba de continuidad; no renueva deadline. Si cierre perdido/gap incierto, PARTIAL y subtotal conocido. |
| Reboot/gap | Nuevo boot sin permiso, no rellenar fin boot viejo. Si calendario KNOWN y ventana actual, conserva90000 comprobables/PARTIAL; calendario UNKNOWN purga/oculta days=[], consumed null. No cero ni estimación humana. |
| Corte con ALLOW libre | Domingo21 cambia época/retentionSeq/historyRevision, permiso/deadline/seq intactos; contar solo sufijo posterior. Snapshot previo firmado no reconstruye prefijo. |
| Padre offline/push viejo | Padre purga/tombstone local antes de mostrar; hint previo al corte no informa hechos. STATE vieja generación/cutoff rechazado; nuevo sync apto reemplaza array, no suma semanas. |
| Ocho fechas/DST | Domingo inicial desde21 + lunes–sábado + domingo final hasta21; hasta8 fechas, millis monotónicos con ancla. Más de8/coherencia perdida: UNKNOWN/purga sin cancelar permiso. |
| Mezcla perfiles | cp.v1/v2 incompatibles o CPv1/MESSAGE en sobrev2: rechazo; no fallback/promoción. Claves iguales no bastan. |

| Rol / grupo | Invariantes y pruebas a asignar, NO ejecutadas |
|---|---|
| Compartido futuro | Bytes exactos, duration60..86400 múltiplo60/null/combinaciones cerradas; acción/campo/fase eliminados rechazados; array9/fecha desordenada/duplicada, nulos/cortes/zona/overflow; dominios/roles cruzados y PAIR/OFFLINE, hash payload no firma; cota/frontera §14. |
| Hijo | Escritor serial y crash en commit/IO; local vs remoto vs timer; retos consumidos/revisión/sesión/boot; permiso offline de dos fuentes; deadline vs enforcement; contador unión/subtotal/calidad/reconstrucción/día; credencial/pregunta/esperas privadas y emergencia independientes. |
| Padre | Reserva nunca reutilizada; desconocido no resuelve; ACK histórico no sustituye STATE; stateSeq/frescura/boot; tombstone y caché/backup/push viejo/época no ordenable; snapshot absoluto reemplaza sin deltas; aviso vencido no reaparece por STATE; compatibilidad/migración/pendingv1. |
| Relay | AUTH/perfil/rol/root pública, sustitución socket autenticado, validación/routing exactos, cruces de versiones/reinicio; cero historial/colas/secretos/lógica de tiempo/permisos, PEER no APPLIED. |
| Android/físico posterior | Autoridad RAM/fondo/proceso, boot/Keyguard, protección básica Ajustes/admin/desinstalar hijo e instalar/actualizar permitido; errores/emergencia/recuperación antes y bajo endurecimiento. Continuidad contable/enforcement real por autorización específica, sin acreditar por tests/firma. |

## 12. Pocas elecciones bloqueantes y mapa revisable

Ya decididos: ALLOW libre ambas autoridades, temporal1min..24h en minutos enteros
sin preselección, LOCK inmediato sin apertura; contador incluye apagado/Keyguard;
varios días hasta corte domingo21 zona LOCAL; catch-up/purga conservadora sin cancelar
permiso; evaluar push cerrado. No volver a ofrecer alternativas supersedidas.

Quedan tres bloques de revisión, no nuevos requisitos aprobados:
1. **Calendario técnico:** ratificar resolución DST propuesta y cómo establecer
   ancla/zona local fiable; fallback UNKNOWN/purga ya aceptado, no retención infinita.
2. **Alta/recuperación/versionado:** ratificar formatos §10, dominios/hash nuevo y
   conservación identificada de hashesv1/capacidades durante corte de tres roles;
   sin esto no freeze/migración aunque MESSAGE draft esté cerrado.
3. **Push posterior:** decidir emisor/proveedor/consentimiento/privacidad/coste y
   permisos/recepción real en unidad de diseño separada, sin prometer ni activar servicio.

KDF/normalización/cupos/admin/reset/olvido doble son subunidades privadas pendientes;
no multiplicar preguntas de contrato ni aprobar parámetros al definir campos aquí.

Mapa: revisión hijo+relay de esta propuesta y compatibilidad/recuperación → acuerdo
de decisiones/ADR/versionado de tres roles → schema/vectores nuevos separados y
matriz interop → laboratorio puro aislado de ledger/retención con calendario inyectado
(no promoción L02) y pruebas negativas de bytes/contexto → diseño/implementación
productiva por rol explícitamente asignados, reconciliación/escritor → persistencia y
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

## 14. Deltas R1 y derivación de límites para revisión (no prueba ejecutada)

| Frente a1582e66 | Consolidación actual |
|---|---|
| Acciones/plazos | LOCK/ALLOW/ALLOW_FOR ambas fuentes; duration60..86400 múltiplo60, sin preselección; sin campo/fase/timer de apertura. |
| Tiempo | AUTHORIZED_ELAPSED incluye apagado/Keyguard confirmado, no opción de observación. |
| Retención | Ventana domingo21 local, array hasta8, generation/revisión contable y permiso activo separados; sin total vitalicio/bucket único/UTC impuesta. |
| Aviso | lastExpiry ligado a retención/detección y purgado al corte; hint push a evaluar, no exclusión permanente ni servicio activo. |
| Receptor | Tombstone durable/barrera propia offline, snapshots antiguos/backups/push no resucitan historia. |
| Alta/recuperación | Formatos cerrados mínimos §10 y esquema legado identificado propuestos; aprobación/evidencia previa bloqueantes de versión. |

Ejercicio relay **reportado en texto suministrado**, no ejecutado aquí: payload2966,
frame4122, profundidad4; zona ficticia/combos incompatibles/firma relleno no DER.
Solo longitud sintética, NO cota formal, validación crypto/protocolo o garantía.

Derivación manual candidata de este inventario cerrado, para revisión antes de
congelar: strings de identificadores/enum/fechas/zone usan alfabeto sin comillas,
backslash/controles; zone solo identificador IANA validado (letras/dígitos/._+− y /).
Canonización propuesta no escapa slash ni ASCII imprimible innecesariamente.
Claves fijadas aquí≤32bytes; overhead por miembro≤36 (comillas/colon/coma).
Scalar STATE≤45bytes serializados (N/H43+comillas), salvo zone64+comillas=66;
C/M≤19+comillas, enums≤43, restantes numéricos/boolean/null menores. No texto libre.

| Inventario STATE (todos objetos no null para cota conservadora) | Miembros |
|---|---:|
| Raíz / challenge / effective / lastRemote | 20 / 5 / 11 / 9 |
| permittedTime / retention / ocho buckets / lastExpiry | 9 / 5 / 24 / 8 |
| Total | 91 miembros, 84 escalares, 15 objetos, 1 array |

Candidato B_STATE≤91×36 +84×45 +(66−45) +15×2 +2 +7 comas array = **7116bytes**.
Profundidad propuesta4 (raíz1→permittedTime2→days3→bucket4), menor que6.
El máximo sobre usa base64≤4×ceil(B/3) y overhead conservador≤300:
STATE≤9488+300=9788bytes. Con límite general8192, frame≤10924+300=11224bytes,
siempre sujeto a inventario/canonización validados; no aumentar8192/16384.

PAIR_ACCEPT:13 miembros (10raíz+3hash), máximo883bytes de valores serializados
(2K de202 +O202 +3H de45 +resto142), overhead13×36+2objetos×2:
candidato payload1355, frame≤1808+300=2108. CHILD_OFFER sin firma≤524;
OFFLINE≤9×(36+45)+2=731payload, frame≤976+300=1276. Proponer QR local máximo2500
bytes/frame para esos intercambios, sin aumentar lector/producto ni afirmar lectura
óptica fiable. Son cotas documentales **condicionales**, no cotas probadas de codec.

Revisión independiente pendiente: corroborar recuento/valores (incluido resto142 de
PAIR), null/combos válidos, escaping/base64/sobre/DER/profundidad y todos formatos;
generar fronteras válidas e inválidas solo después de acuerdo y en unidades de tests
asignadas de tres codecs. Si discrepancia supera margen, corregir inventario/diseño,
no agrandar límites por costumbre. No se ejecutó script/test para esta derivación.
No firma real, fixture nuevo, secreto ni ejemplo de dispositivo en documento.
