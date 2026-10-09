# Evidencia P05 — sincronización y envío idempotente local

## Alcance

Solo padre; CP/1, esquema/fixtures y proyecto parental_render intactos.
Sin ADB, instalaciones ni pruebas físicas en A56/A13. No servidor externo,
APK hijo, controles de dispositivo ni despliegue. Implementación local parcial:
casos de uso y ruta al relay, no interfaz final P06 ni recuperación P07.

## Implementación y barreras

- ControlPoliticas usa Mutex, reloj monotónico y firma inyectada desde
  AutoridadPadre/Keystore. SYNC_REQUEST firmado con nonce aleatorio nuevo de
  32 bytes; no consume secuencia. Firma contra pública del hijo guardada,
  propósito MESSAGE y rol CHILD; nunca una clave elegida por el mensaje.
- STATE exige pairId/syncNonce de petición y ticket de canal actuales. En misma
  sesión exige stateSeq creciente y lastAcceptedSeq no decreciente, incluso
  entre sincronizaciones de la instancia. Cambios de boot/sesión requieren
  sincronización nueva. La instancia nueva no declara fresca evidencia del disco.
- Ventana local de 20 s desde petición: recepción tardía no la reinicia. No es
  una extensión ni sustituto de los 30 s que decide el hijo; no hay timestamp nuevo.
  Una actualización no rearma desafío consumido ni confirmación descartada.
- Preparación valida acción/duración con codec CP/1 (60–604800 s para acciones
  temporales), vínculo VINCULADO, hijo Device Owner/configurado, red/canal sanos
  y autoridad vigente. Devuelve un ticket de intención que el adulto deberá
  confirmar explícitamente en P06. Referencias ajenas/viejas/dobles se rechazan.
- Reservar seq=max(local,lastAcceptedSeq verificado)+1; no wrap ni reutilización.
  Firma exacta, persistencia de frame y ENVIADA_SIN_CONFIRMAR antes de encolar.
  Ese registro conservador no asegura que haya salido ni llegado el comando.
  Revalidar canal/autoridad tras esperas/firma/almacén impide envío al fondo.
- ACK requiere firma y correlación pairId/bootId/childSessionId/commandId/seq/
  hash del payload. APPLIED, pendiente de aplicación, fallo y rechazo separados.
  ACK tardío del envío existente en conexión actual puede confirmarlo sin
  habilitar una nueva orden con estado/desafío caducado.
- resolverIntencion verifica de nuevo firma y correlación, y guarda evidencia
  más pendiente resuelta/conservada en una transacción. Una persistencia fallida
  no publica aplicación. Resultado pendiente de aplicación detiene retransmisión;
  un resultado terminal evita que un ACK viejo lo degrade.
- Sin ACK: como máximo dos reenvíos a 5 s, mismos bytes del comando duradero,
  sin firma/id/seq/duración nueva. Después sincroniza. Tras reconexión/reapertura
  solo STATE id/seq/hash coincidentes acredita resultado anterior. Si no coincide,
  conserva pendiente y exige decisión explícita; no reproduce desbloqueos antiguos.
- Descartar pendiente solo descarta intención local: NO revoca una orden ya
  recibida. Conserva contador/evidencia; cualquier otra orden requiere estado
  nuevo y confirmación nueva. Esta advertencia debe aparecer en P06.

## Ruta Android

RelayConnection recibe sobres MESSAGE sin acreditarlos, en cola de 16 con ticket
de generación; cola llena detiene canal, callbacks antiguos no se incorporan.
enviarPolitica acepta solo SYNC_REQUEST/SET_POLICY firmados en conexión actual
autenticada, visible y con PONG vigente. CanalPoliticasRelay exige además red
validada por Android. Un error o presencia del relay no confirma órdenes.

MainActivity observa nueva conexión sana/red validada, solicita sincronización,
consume respuestas y ejecuta reloj solo con lifecycle STARTED. Sin servicio
oculto. Sincronización manual disponible si falló la automática. No se habilitan
botones de órdenes ni se transforma PREPARADO en VINCULADO por recibir STATE:
la recuperación local debe acreditarse antes de activar vínculo operativo.

## Pruebas ejecutadas

1. Primer bloque: `:app-parent:testDebugUnitTest`, cuatro pruebas nuevas correctas.
2. Ampliación: veinte ControlPoliticasTest y dos RelayConnectionTest adicionales.
   Incluyen firmas impostoras, identidad/nonce/hash/sesión ajenos, secuencias fuera
   de orden, falta de DO/red/canal, PREPARADO, agotamiento, límites de duración,
   caducidad desde petición, fondo durante reserva/firma/persistencia, pérdida
   de ACK, fallos antes/después de commit de evidencia, reconexión/reapertura,
   descarte explícito sin reutilizar seq y doble confirmación concurrente.
3. Recorrido en JVM con transporte relay simulado: CHALLENGE/AUTH/AUTH_OK/PONG →
   SYNC_REQUEST → STATE firmado → confirmación → SET_POLICY → ACK firmado.
   Red/PONG/ticket independientes; fondo cierra y ticket previo no envía.
4. Temporizador en simulador de hijo: ACK de primer envío/reintento perdido,
   tercer ACK informa 50 s restantes de los 60 originales; una sola aplicación
   simulada, mismos bytes. No demuestra temporizador ni restricciones Android.
5. `scripts/verificar.ps1` correcto; Node cinco pruebas/78 comprobaciones de diez
   vectores. Gradle con --rerun-tasks: tests app/núcleo/interoperabilidad,
   assembleDebug/assembleDebugAndroidTest/assembleRelease/lintDebug, correcto,
   130 tareas ejecutadas. Tras ajustes finales, script/release repetidos correctos.
   XML final: 83 app + 30 núcleo + 1 interop, cero fallos/errores/omisiones.
   Total con oráculo Node: 119 locales. Lint 0 errores/23 avisos; release sin
   firma de distribución. Instrumentados compilados, no ejecutados.

Un caso de reapertura falló durante ampliación: simulador devolvía
lastAcceptedSeq=0 tras informar 1. Se corrigió el simulador conservando guarda
no decreciente en producción; verificación final correcta. Una firma JCA efímera
de tests no acredita uso físico de Keystore/autenticación para SET_POLICY.

## Pendientes y siguiente tarea

Integración P-ACK/C-SEQ con servidor R02 y APK hijo H09/H10 reales, caducidad real
del desafío del hijo, persistencia/temporizador físicos y restricciones efectivas
no demostrados. Ninguna prueba necesita ni autoriza operar el A13 excluido.
Acceso a A56 requiere aviso/autorización nueva y selector explícito.

P06: interfaz de acciones/duración/confirmación/causas/pendiente y última
evidencia, sin pintar permitido solo por ALLOW/APPLIED si mode/reason siguen
bloqueados. Formulario offline y presentación final aún no implementados;
no cola ni firma nueva sin contexto válido. P07: recuperación presencial y
guardas finales de vinculación, sin deshabilitar emergencia/recuperación.

Avisar al propietario cuando padre esté preparado para integración completa;
él coordinará IA del servidor y comunicación local antes de despliegue.
No lanzar agentes ni desarrollar servidor desde este repositorio.
