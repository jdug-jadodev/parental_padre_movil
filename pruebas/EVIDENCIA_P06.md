# Evidencia P06 — controles y confirmación, local parcial

## Alcance

Solo padre. No ADB, APK instalados ni pruebas físicas. A13 excluido, sin
operaciones. Sin cambios en parental_render, CP/1, esquema/fixtures ni permisos.
P06 local implementado; aceptación visual/accesibilidad física e integración
real pendientes. No demuestra restricciones aplicadas en otro dispositivo.

## Interfaz y guardas

- MainActivity usa ControlScreen/AccionesControlReal en lugar del control de
  recorrido. AplicacionPadre conserva el control de recorrido por defecto para
  tests previos, sin conceder autoridad ni simular aplicación.
- Cuatro acciones con avisos: LOCK_FOR apertura condicionada a conexión/no
  reinicio; ALLOW hasta bloqueo/pérdida de conexión/reinicio; ALLOW_FOR vuelve
  a bloquear. Duración en segundos, entero 60–604800, sin redondear/convertir
  entradas inválidas. El tiempo comienza con aceptación del hijo, no al guardar.
- Guardar selección offline no firma ni consume seq. Solo una selección/orden
  pendiente. Reemplazo local requiere diálogo; un comando incierto no se
  reemplaza automáticamente. Al reabrir se muestra selección guardada y pide
  revisión/confirmación explícita, incluidos ALLOW y ALLOW_FOR.
- Revisar prepara ticket de P05; Confirmar y enviar es una acción separada.
  No se encola por elegir botón, guardar, cerrar diálogo o reconectar. Ticket
  incorpora id de selección; reemplazo posterior, aun con misma acción/duración,
  no puede usar confirmación antigua. Backend revalida firma/contexto/autoridad.
- Guardia síncrona contra doble pulsación antes de coroutine. ON_STOP cierra
  diálogos y cancela operación UI; salida no borra intención duradera. Sin
  rememberSaveable de confirmación antigua. Una reserva interrumpida sigue
  consumida; no se crea una orden nueva por reabrir.
- PREPARADO/no vínculo/almacén no disponible no habilitan acciones. No se
  crea identidad para visitar Controles, no se promociona vínculo ni certifica
  recuperación presencial. No se afirma pendiente vacía si el almacén no puede
  leerse. Barra Recuperación permanece disponible.
- Descartar selección local no envía cancelación. Descartar comando incierto
  exige sincronización fresca y muestra que NO revoca una orden recibida por
  el hijo; no afirma cancelación remota. Cambiar una orden aceptada exige otra
  orden confirmada con estado nuevo.

## Evidencia, frescura y estado

PresentacionControl muestra mode/reason firmados, última respuesta de orden,
antigüedad y pendiente por separado de relay/presencia. APPLIED con LOCKED y
NETWORK_LOST muestra «Orden aplicada; bloqueo por falta de conexión»; enviar
ALLOW no pinta permitido. Pendiente de aplicación, fallo y rechazo no se
presentan como aplicado. Sin Device Owner muestra configuración incorrecta.

VistaPoliticas verifica otra vez evidencia del almacén. Frescura exige contexto
actual y canal válido; sincronización nueva no rejuvenece evidencia anterior.
La señal reactiva de red/canal degrada presentación sin esperar sondeo de 500 ms.
Antigüedad conocida solo si recibió ese mensaje la instancia; tras reabrir no
se inventa hora. remainingSec se muestra como tiempo informado, sin descontarlo
como si el hijo confirmara cambios. No hay simulación de permiso/APPLIED en release.

## Formato privado v3

MetadatosPadre.intencionLocal guarda UUID/acción/duración, sin frame/seq; es
excluyente de pendiente reservado. ParentStore valida UUID/duración mediante
codec CP/1 y exige VINCULADO. Reemplazo/descarte comprueba id actual; reservar
consume selección en la misma escritura que contador y pendiente.

Carga v1/v2 valida canonización del documento original sin campos nuevos y
migra a v3; conserva contador, comando original, evidencia y aceptación sin
inventar intención. V1 sigue sin aceptación inventada. Versiones desconocidas,
duplicados y campos nuevos bajo versiones antiguas fallan, no se reinicializan.
Cambio privado del padre, no cambio de protocolo/contrato común.

## Pruebas realmente ejecutadas

`scripts/verificar.ps1`: correcto. Gradle con `--rerun-tasks`:
`:core-protocol:test`, `:core-protocol:testInterop`, `:app-parent:testDebugUnitTest`,
`:app-parent:assembleDebug`, `:app-parent:assembleDebugAndroidTest`,
`:app-parent:assembleRelease`, `:app-parent:lintDebug`: correcto, 130 tareas.
XML final: 96 app, 30 núcleo, 1 interop, cero fallos/errores/omisiones; Node
oráculo cinco correctos, total 132 locales. Diez vectores/78 comprobaciones
documentales. Lint: 0 errores/23 avisos. Release sin firma de distribución.
Tras ajustar el texto conservador de pendiente no disponible, tests app,
debug/instrumentado/release y lint repetidos correctamente (126 tareas,
22 ejecutadas y 104 actualizadas).

Trece JVM nuevos: seis presentación/duración/causas/antigüedad; tres almacén
(selección offline persistente, descarte/no promoción PREPARADO, migración v2
con comando y evidencia firmados); cuatro casos de uso/vista (reemplazo invalida
confirmación, cerrar conserva selección, sincronizar no rejuvenece evidencia,
acciones UI no firman hasta confirmación). Regresión P05 incluida.

Cinco PantallaControlTest compilados, NO ejecutados: duración/guardar sin envío,
ALLOW exige diálogo y envío no pinta permitido, volver sin enviar conserva
selección, PREPARADO/recuperación, texto 2x y formulario desplazable. Dobles solo
instrumentados, sin claves/almacén/red reales. Test previo de migración Android
actualizado para v3 y compilado, no repetido físicamente.

Primer intento de compilación instrumentada falló por lambda pasada en posición
del nuevo parámetro Boolean de ControlScreen. Se corrigió llamada con argumento
autenticar nombrado; la repetición completa pasó sin relajar guardas.

## Pendientes y siguiente tarea

P-UI-01/02 reales, texto grande/rotación/cancelación/fondo, teclado/lector de
pantalla y no recorte visual requieren A56 con aviso y autorización nuevos.
La compilación no acredita legibilidad, accesibilidad ni comportamiento físico.
No ejecutar pruebas contra todos los dispositivos ni operar A13.

P07: recuperación/retirada presencial y guardas finales. Vinculación operativa
no se acredita solo por recibir STATE. Servidor R02/hijo H09/H10 y comunicación
local entre apps reales siguen pendientes. Padre no terminado; avisar al
propietario cuando esté preparado para integración completa. Él orquestará IA
del servidor y pruebas locales antes de despliegue; no lanzar agentes aquí.
