# PADRE-C01 — diseño de acreditación del vínculo

Fecha: 2026-10-09. Unidad documental dentro de P08. **Propuesta, no implementación.**
Fuentes: CP/1 §5, §6, §7.1 y §9; SPEC-P P3/P4; esquema PairAccept/State/
OfflineChallenge/OfflineResponse; PairChild, MetadatosPadre y ParentStore actuales.
No modifica CP/1, no promociona PREPARADO ni habilita órdenes.

## 1. Decisión y límite de prueba

CP/1 §5.6 requiere autenticación con relay, STATE firmado y una recuperación
local comprobada. Se propone una **ceremonia presencial explícita del adulto**,
asistida por validaciones de software y culminada por una transacción local.
No un callback STATE→VINCULADO ni una casilla genérica «todo funciona».

PAIR_ACCEPT lo firma el padre: demuestra su aceptación, no el commit del hijo.
STATE prueba identidad/pairId/contexto declarados por el hijo, pero **no contiene
recibo del PAIR_ACCEPT ni sus hashes de recuperación, pública del padre u origen**.
OFFLINE_RESPONSE lo firma el padre: demuestra autorización, no consumo/aplicación.
Un STATE MAINTENANCE posterior tampoco correlaciona el nonce OFFLINE utilizado.
Por tanto no se puede certificar criptográficamente desde el padre que el hijo
consumió esa aceptación exacta o aplicó ese desafío específico con CP/1 actual.

La comprobación presencial de §5 no se reemplaza por esos mensajes. No añadir
campos a STATE, reutilizar ACK de SET_POLICY para vinculación ni inventar recibos.
Si se exige acreditación remota totalmente automática, esta vía queda bloqueada:
proponer **CP/2**, con recibos de aceptación/recuperación definidos y acordados
entre roles, nuevo esquema/fixtures y análisis de replay/persistencia. No se
diseñan ni implementan tales mensajes en esta unidad. La ceremonia local descrita
no requiere cambiar CP/1; queda pendiente de revisión del coordinador y del hijo.

## 2. Evidencias separadas

| Evidencia | Verificable por software del padre | Comprobación presencial pendiente |
|---|---|---|
| Aceptación propia | Firma PAIR de pairAcceptanceFrameB64, ambas públicas, pairId, relayOrigin, enrollId/nonce y hashes; identidad local coincide | Adulto comparó huellas y guardó códigos fuera del teléfono |
| Aceptación del hijo | STATE válido de pública guardada y pairId; no prueba aceptación exacta | Hijo real muestra **commit local confirmado, acreditación operativa pendiente**, después de su commit duradero/readback; adulto compara pairId, huellas de ambas claves y origen con aceptación guardada |
| Canal | AUTH_OK correlacionado, PONG vigente, ticket actual y red validada | Ruta/TLS local autorizados; dos APK reales, no simuladores |
| Estado | Firma CHILD/MESSAGE, pairId, syncNonce solicitado, ticket, bootId/childSessionId y stateSeq; frescura local | Device Owner/Lock Task y restricciones se acreditan por pruebas del hijo, no por sus booleanos firmados |
| Recuperación | Reto CHILD/OFFLINE válido; respuesta PARENT/OFFLINE exacta MAINTENANCE/pairId/bootId/nonce con autoridad | Hijo escanea respuesta, consume desafío y entra en herramientas limitadas; mismo QR repetido no abre otra ventana; emergencia sigue accesible |
| Confirmación final | Ticket local no reutilizable, identidad/generación iguales, UI activa y autoridad vigente | Adulto declara **lo observado**, no una firma ficticia del hijo |

La pantalla de commit local confirmado y resumen público del hijo es dependencia de
H08, no una función acreditada hoy. Reapertura de ese resumen sin volver a aceptar
enrolamiento debe comprobarse bajo autorización; no basta ver «QR leído».
Validar hashes guardados, consumo duradero, reinicio y rutas RECOVER_PARENT/RETIRE
requiere evidencia del hijo aparte. El padre no almacena códigos ni pretende
deducirlos de hashes. La ceremonia QR no acredita recuperación por código impreso.

## 3. Flujo propuesto, sin emitir políticas

1. Exigir PREPARADO, almacén íntegro y aceptación propia firmada persistida.
   Si falta (por ejemplo migración antigua), detener; no fabricar una aceptación
   ni iniciar otra vinculación. Recuperación presencial permanece accesible.
2. Crear revisión local ligada a snapshot de identidad, pairId, origen y hash
   SHA-256 de **payload original** PAIR_ACCEPT (no hash de firma DER). Comparar
   resumen público de ambas pantallas y obtener confirmación específica del adulto
   de aceptación duradera observada. No confiar en un alias o número de dispositivo.
3. Autenticar adulto y conectar al relay configurado con TLS normal y AUTH/PONG.
   Solicitar SYNC_REQUEST con nonce nuevo; verificar STATE correlacionado mediante
   las reglas existentes, ventana conservadora local de 20 s desde solicitud.
   No imponer lastResult=APPLIED ni enviar LOCK/ALLOW como prueba de vinculación.
4. Ejecutar recuperación de prueba **MAINTENANCE por QR**, estando PREPARADO;
   obtener reto firmado del hijo de esta pareja y boot y autenticar al adulto
   para respuesta exacta. El hijo decide vigencia/consumo; padre no inventa timestamp.
   Observar función limitada, rechazo de repetición sin renovar ventana y acceso
   de emergencia no obstruido (sin llamada real). Nunca usar RETIRE/restablecimiento
   ni gastar RECOVER_PARENT para esta ceremonia.
5. Volver a estado normal tras mantenimiento y obtener **otro STATE fresco**;
   reconexión usa ticket/syncNonce nuevos. La ventana de 20 s no se extiende desde
   el estado anterior. Puede cambiar childSessionId al reconectar: usar solo el
   contexto nuevo. Si cambia boot durante la ceremonia, descartar revisión y repetir
   comprobaciones; no reutilizar reto/confirmaciones del arranque previo.
6. Mostrar resumen y declaraciones separadas: «aceptación observada en el hijo»
   y «mantenimiento local comprobado»; códigos conservados fuera del teléfono.
   Exigir autoridad vigente y confirmación final explícita ligada a esta revisión.
   No son certificados criptográficos ni acreditación de Device Owner/enforcement.
7. Proponer un commit atómico local del vínculo y evidencia según §4. Descartar
   tickets después del resultado. Cualquier futura orden exige nueva sincronización
   y confirmación propias: no aprovechar commandNonce de la ceremonia ni encolar
   políticas al completar vínculo. Ninguna de estas acciones se implementa ahora.

Salida/fondo/cancelación invalida tickets y avances provisionales en memoria;
reapertura no reanuda una confirmación pendiente ni considera el paso completo.
Si el almacén es corrupto o la identidad falta, fallar sin reemplazar claves.

## 4. Persistencia propuesta

Extensión **privada local versionada**, no del esquema CP/1. Proponer un único
registro de acreditación vigente, no historial. Campos conceptuales: versión del
procedimiento, hash de aceptación ligada a snapshot, evidencia STATE final con
contexto y declaraciones presenciales del adulto claramente etiquetadas. Guardar
solo mínimo necesario; nunca códigos, privadas, imágenes, identificadores de
dispositivo o token/QR OFFLINE reutilizable. Hora de pared, si se usa para mostrar
fecha declarada, no prueba frescura ni autoridad tras reabrir.

La operación futura de ParentStore usa su único escritor y archivo AES-GCM atómico:

- Releer y comparar snapshot de revisión con documento actual; exigir PREPARADO,
  aceptación exacta y ausencia de intenciones incompatibles. No modificar claves,
  origen, contador, comandos o evidencia de políticas para eludir inconsistencias.
- Revalidar firma/correlación del STATE final, ticket/frescura, identidad de Keystore,
  generación de UI y autoridad inmediatamente antes del commit; revisión solo
  procede si ambas declaraciones presenciales pertenecen a ese snapshot.
- Escribir **VINCULADO y registro de acreditación juntos**; nunca etiqueta sola
  primero y evidencia después. Ante fallo atómico queda documento anterior o
  nuevo completo; estado incierto falla cerrado, sin reintento/promoción automática.
- Salida/caducidad después de commit no borra un commit duradero ni envía órdenes:
  exige nueva autenticación/sync para operar. No prometer atomicidad del reloj/
  lifecycle con el disco; las guardas de envío siguen siendo independientes.
- Migración v1/v2/v3 conserva datos y no inventa acreditación. VINCULADO legado
  sin registro no cuenta como evidencia nueva: tratamiento seguro deberá diseñarse
  y probarse antes de migrar, sin destruir datos ni conceder órdenes por la etiqueta.

Hoy MetadatosPadre es v3 y ParentStore no tiene esta operación: no cambios ahora.

## 5. Pruebas negativas propuestas (no ejecutadas)

| Grupo | Rechazo/invariante esperado |
|---|---|
| Evidencias insuficientes | Solo STATE, AUTH_OK, presencia, ACK relay, test aprobado, QR emitido o declaraciones solas: sigue PREPARADO |
| Aceptación | PAIR inválido, clave/pairId/origen/enroll/contexto distintos o frame ausente: no revisión ni commit |
| Estado | Firma impostora/rol/propósito falsos, nonce/ticket viejos, stateSeq repetido/decreciente, sesión ajena o frescura vencida: no commit |
| Recuperación | Otra pareja/boot/acción, solo respuesta firmada sin observación, repetición aceptada o resultado no observado: no acreditar; no RETIRE/ALLOW |
| Contexto | Cambio de snapshot, fondo/salida, cancelación, pérdida de red/canal, clave no utilizable, autoridad vencida o doble confirmación: no reutilizar revisión |
| Persistencia | Fallo antes/durante/después de escritura, reapertura y migraciones: evidencia/etiqueta inseparables; sin reset de contador ni promoción por archivo parcial |
| Falsos positivos | Booleanos aptos/APPLIED/MAINTENANCE de un simulador no generan declaración física ni cierran aceptación real |

Prueba positiva futura JVM: revisión explícita con observaciones **ficticias
etiquetadas solo en tests**, validación y transacción; jamás evidencia física.
Prueba positiva física futura: protocolo de aceptación autorizado con dos APK,
confirmaciones reales, recuperación limitada, persistencia del hijo y relectura
del padre, sin políticas destructivas. Registrar resultado/versión/alcance, no secretos.

## 6. Criterio de aceptación y dependencias

Diseño aceptable si no existe camino automático desde STATE/presencia/tests,
están separadas las declaraciones del adulto de firmas, la transacción no rompe
datos previos y todas las entradas negativas dejan el vínculo no acreditado.
Esto **no** aprueba G1/G3 ni autoriza operar teléfonos.

Bloqueos: H08 aceptación/commit/resumen público del hijo, H10 STATE/WSS, H11
mantenimiento/recuperación y evidencia real; aprobación del diseño/migración;
entorno TLS/red acordado y autorización física específica posterior. Relay reporta
8385410/e26aebb, 104 tests/seis variantes JVM: leído, no reejecutado por el padre;
no prueba ninguna de esas condiciones físicas. Health 503/producción bloqueada.

Siguiente unidad **PADRE-C02 propuesta, no activada**: especificar/implementar un
validador puro de revisión y sus pruebas negativas JVM, sin ruta productiva de
promoción, sin UI que habilite órdenes y sin migración/escritura VINCULADO.
Requiere revisión del coordinador y acuerdo del hijo sobre qué muestra tras su
commit. La transacción y ceremonia físicas se asignarán por separado después.
Sin ADB, certificados, LAN, reset, quiosco, push/despliegue ni repos ajenos.
A56 disponible no es permiso; A13 permanece excluido en esta sesión.

## 7. PADRE-C01-R1 — acuerdo documental con HIJO-C02

Revisión de `hijo/DISENO_PUERTAS_Y_RESUMEN_PUBLICO.md` y relevo del hijo, commit
documental **cb62a73**, sobre H05-L01 **c7fb842**. Coordinación central del relay
consultada solo en lectura. Los 17 tests nuevos/346 resultados JVM del hijo son
reportados por ese rol, no reejecutados aquí ni evidencia física nueva.

**Resultado: compatible como especificación propuesta.** No requiere corregir
el formato del hijo ni cambiar CP/1. Se precisa aquí el vocabulario anterior
«vínculo confirmado»: significa únicamente **commit local confirmado**, nunca
acreditación operativa. Este acuerdo no aprueba puertas/orden ni implementa H08.

### Formato y correspondencia aceptados

| Resumen propuesto del hijo | Comparación presencial del padre |
|---|---|
| pairId completo, UUID v4 válido | Igualdad exacta con pairId de aceptación propia guardada; no alias ni prefijo |
| Huella pública padre | SHA-256 de SPKI DER decodificado/validado P-256 de parentPublicKeyB64 guardada |
| Huella pública hijo | Mismo cálculo sobre childPublicKeyB64 guardada; hijo contrasta su identidad existente |
| Origen HTTPS exacto completo | Igualdad con relayOrigin firmado, sin truncar/normalizar, añadir /ws ni sustituir hostname |

Huellas: **64 hex mayúsculos en 16 grupos de cuatro separados por espacios**, sin
elipsis. Coincide por inspección con PreparacionVinculo.huella; no se ejecutó prueba
de formato nueva. Los espacios son presentación, el hash es sobre bytes DER, no
texto base64, QR, payload PAIR o firma. Las claves deben estar validadas antes de
proyectar la huella, aunque la función de presentación no valide sola P-256.

El hijo deriva los cuatro datos del **mismo snapshot persistido íntegro y readback
post-commit**, no del formulario/QR/callback. Texto aceptado: «Datos del vínculo
guardados en este dispositivo; acreditación operativa pendiente». Oferta/captura
son provisionales; lectura/firma/etiqueta interna no generan ese estatus. Reapertura
reconstruye la proyección sin revincular, generar claves/códigos ni restaurar sesión.

Fallos compatibles: escritura/readback fallido o registro dudoso/corrupto/legado
sin procedencia no producen resumen confirmado. Corrupción no se borra como
ausencia. Identidad perdida/distinta no se reemplaza ni permite afirmar vínculo
utilizable; antes del desbloqueo no se inventa coincidencia. Una proyección histórica,
si se muestra, queda inequívocamente no verificada y no sirve para declarar commit
comprobado en la ceremonia. Conserva recuperación/emergencia según sus guardas.

No privadas, códigos, hashes de recuperación, alias internos, desafíos, datos de
dispositivo o historial en este resumen. El **hash privado del payload PAIR_ACCEPT**
de §§3/4 solo liga la revisión del padre a su snapshot: no se muestra/exige al hijo,
no es quinto dato de comparación, recibo remoto ni igualdad criptográfica nueva.
Igualdad de los cuatro datos públicos no certifica aceptación exacta, hashes de
recuperación o consumo OFFLINE. Se mantiene íntegro el límite de §1.

Declaraciones separadas del adulto aceptadas: commit/resumen observado en el hijo,
mantenimiento limitado/repetición sin renovación observado y emergencia accesible.
El software puede cotejar firmas/contexto, pero no convertir booleanos de esas
declaraciones en firmas CHILD o prueba de Device Owner/enforcement. Tampoco un
STATE MAINTENANCE sustituye la declaración de consumo específico observado.

### Matriz A/B/C1/C2 — compatible como propuesta, no habilitación

- A: evidencia aislada JVM/Android histórica no acredita salida operativa ni H06.
- B: evidencia local previa sigue siendo propuesta; no habilita H08 con el plan
  actual ni cierra H05. El ciclo funcional H05/H08/H11 sigue bloqueando esa vía.
- C1: salida/recuperación **real antes de endurecer**, independiente de esperar
  WSS. No se sustituye por C2 ni tests; requiere pruebas específicas por propósito
  y repetir emergencia/recuperación bajo restricciones reales autorizadas.
- C2: ceremonia posterior de §3 con STATE/WSS y observaciones del adulto; no
  sustituye C1, no activa H06 y no promociona al padre por existir el resumen.

La decisión sobre puertas y orden corresponde al propietario; esta revisión
solo confirma correspondencia documental. No cambia PLAN_HIJO ni dependencias.

### PADRE-C02 futuro — límites, entradas y aceptación

**No activado ni implementado.** Propuesta de validador puro aislado, sin Android,
IO, red, reloj oculto, UI, escritura/migración, transición VINCULADO o envío.

Entradas conceptuales inmutables, aportadas explícitamente por su llamador:

1. Snapshot actual PREPARADO y aceptación propia verificable: identidad/pairId/
   origen/públicas y payload PAIR original; versión/procedencia local conocida.
2. Revisión ligada a ese snapshot/hash privado, generación de UI y snapshot del
   resumen público comparado con estatus post-commit declarado. El origen persistido
   del resumen solo puede ser reportado/observado, no probado por leer texto.
3. STATE validado criptográficamente y contexto local de solicitud: syncNonce,
   ticket, boot/sesión, secuencias, instante monotónico de solicitud/evaluación.
   No aceptar un payload estructural como DocumentoVerificado ni renovar frescura
   por recepción. Firmas/clave/purpose se validan con el codec/crypto CP/1 existente.
4. Contexto OFFLINE verificado de MAINTENANCE y declaraciones del adulto ligadas
   a revisión/boot: aceptación observada, mantenimiento/replay/emergencia observados
   y códigos guardados fuera. Sin código, privada o QR reutilizable persistido.
5. Datos locales de visibilidad, autoridad, cancelación/generación y canal/red para
   el instante evaluado; no inferidos de presencia o de la declaración del hijo.

Supuestos: el llamador debe obtener señales locales fiables, comprobar autoridad
en vivo y distinguir observación real de ficticia. El validador no puede autenticar
al adulto, comprobar Keystore, observar teléfonos o certificar veracidad física.
No se define un puerto productivo que acepte observaciones de tests como reales.

Salida propuesta: diagnóstico de **revisión coherente/incompleta/rechazada**, con
causa acotada sin datos sensibles. Nunca VINCULADO, token de autorización o recibo.
Una revisión coherente con datos ficticios solo prueba consistencia en JVM; no
aprueba puertas. Consumo de confirmación y commit atómico siguen fuera de C02.

Criterios/negativos futuros: rechazo de diferencias en cualquiera de los cuatro
datos, formato inválido, aceptación ausente/ajena, resumen provisional/no verificable,
solo STATE/presencia/test, observación faltante, contexto viejo/boot distinto,
acción RETIRE/ALLOW, autoridad ausente, fondo/cancelación y tiempo discontinuo o
vencido. Entrada igual produce mismo diagnóstico sin mutarla ni conceder permisos.
La ruta positiva requiere todas las evidencias **declaradas y etiquetadas**, pero
su resultado no habilita una transición. No añadir pruebas de migración/IO/commit
en C02; pertenecen a otras unidades aún pendientes de asignación.

Antes de asignar C02, coordinador revisa este acuerdo; decisiones de puertas,
implementación H08/H10/H11, entorno y permisos físicos permanecen pendientes.
Si se exige recibo remoto exacto en lugar de declaración presencial, detener
esa vía y acordar versionado según §1, sin ampliar CP/1 silenciosamente.
