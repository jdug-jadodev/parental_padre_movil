# P07 — autorización presencial sin internet

## Resultado local (2026-10-09)

Implementación local parcial; no acredita recuperación/retirada en un hijo real.
Primer commit: 90d3eff; cierre en commit posterior de esta tarea.

- OfflineChallengeSigner verifica firma CHILD con pública guardada, propósito
  OFFLINE, tipo OFFLINE_CHALLENGE y pairId; respuesta PARENT exacta con misma
  acción, bootId y nonce. Verifica también la firma generada con pública guardada.
- Autoridad vigente antes/después de firma. Preparación única por referencia,
  invalidada al reemplazar entrada/salir/fondo; margen local de cuatro minutos
  desde lectura. No inventa timestamp CP/1: cinco minutos, arranque y consumo
  los comprueba el hijo en su contexto.
- RecoveryScreen reutiliza cámara y selector de imagen locales; texto alternativo
  acotado, advertencia y casilla destructiva RETIRE. Nunca usa relay ni ALLOW.
  RETIRE todavía exige confirmación y BORRAR en el hijo.
- QR en memoria, retirado al salir/fondo, vencer preparación o detectar caducidad
  de autoridad (comprobación UI cada 500 ms). No se persiste/exporta respuesta.
  No se garantiza borrado físico de objetos String JVM.
- PREPARADO permite firmar recuperación de pareja guardada, sin promocionar
  vínculo ni habilitar órdenes. No altera contador, intención o metadatos.

## Verificación ejecutada

```powershell
.\scripts\verificar.ps1
.\gradlew.bat :core-protocol:test :core-protocol:testInterop :app-parent:testDebugUnitTest :app-parent:assembleDebug :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease :app-parent:lintDebug --rerun-tasks --console=plain
```

Ambos correctos; repetición completa: 130 tareas ejecutadas.
XML: 104 app, 30 núcleo y 1 interoperabilidad; cero fallos/errores/omisiones.
Oráculo Node: cinco aprobados. Total: **140 pruebas locales aprobadas**.
Diez vectores/78 comprobaciones documentales. Lint: cero errores/23 avisos.
Release compilada sin firma de distribución; no es una entrega aprobada.

Ocho OfflineChallengeSignerTest: firma y campos exactos, ausencia de mutación
del almacén, preparación no reutilizable, advertencia RETIRE, rechazo de ALLOW,
pareja/clave/tipo/propósito ajenos, descarte/caducidad, autoridad/cancelación
durante firma, ventana de presentación, hijo simulado con repetición/arranque/
acción cruzada y roundtrip QR sintético de reto/respuesta.

Dos PantallaRecuperacionTest compilados, **no ejecutados**. Usan almacén RAM,
claves JCA efímeras y autoridad ficticia; no acreditan Keystore/autenticación
reales ni borran/retiran dispositivos.

## Límites y siguiente paso

No ADB, instalaciones, restablecimientos, modificaciones de credenciales,
servidor externo, agentes ni despliegue. A13 excluido.
Pendientes: cámara/selector OFFLINE físicos, autenticación/QR con Keystore,
rotación/fondo/texto grande y aceptación del hijo real, incluido consumo duradero,
vencimiento y reinicio. Nunca probar retirada borrando el teléfono diario.

Base padre preparada para coordinar P08 local con el propietario. Él organiza
la IA del servidor separado. Comunicación local primero; no habilitar vínculo
ni acreditar recuperación mediante simuladores o STATE. Nuevas pruebas A56
requieren aviso y autorización expresa previa.
