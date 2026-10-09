# P08-A — latidos del padre antes de integración

Fecha: 2026-10-09. Subtarea local de P08, no cierre de integración/entrega.

## Hallazgo y corrección

El handoff del servidor `documentacion/INTEGRACION_LOCAL_PADRE.md` señala que
RelayConnection programaba PING 10 s después del PONG. CP/1 §2.3 indica cada
15 s. Se corrigió el intervalo desde el envío de PING para no añadir la demora
de respuesta a la cadencia. Primer PING inmediato tras AUTH_OK, §8 intacto.

Además, una pausa sin tick ni PING pendiente dejaba disponible sesionPoliticas
con el último PONG vencido. Ahora la puerta de envío comprueba directamente
45 s desde último PONG válido. El tick cierra/reintenta al detectar caducidad;
un PONG recibido después de caducar no rehabilita ese contexto.

Se mantiene el timeout conservador existente de 15 s para un PING sin respuesta:
puede cerrar antes del límite global de 45 s, nunca ampliar la ventana. Solo un
PING pendiente; tras pausa no emite ráfagas para recuperar intervalos perdidos.
Desconexión descarta el último PONG; presencia no renueva caducidad.

## Pruebas ejecutadas

Seis tests nuevos en RelayConnectionTest, reloj y transporte simulados:

1. PING a 0/15/30 s aunque PONG demore 2/4 s; no a 10 s ni 14,999 s.
2. Sesión disponible a 44,999 s, envío rechazado a 45 s sin tick; luego cierre.
3. Pausa de 40 s emite un solo PING y no duplica desafío pendiente.
4. PONG renueva el límite; PEER_STATUS no lo renueva.
5. Nueva conexión exige PONG propio y no hereda cadencia/estado anterior.
6. PONG tardío a 45 s no resucita canal aunque su timeout individual siga abierto.

Test anterior de timeout actualizado a nueva cadencia (15 + 15 = 30 s).

```powershell
.\gradlew.bat :app-parent:testDebugUnitTest --rerun-tasks --console=plain
.\gradlew.bat :core-protocol:test :core-protocol:testInterop :app-parent:testDebugUnitTest :app-parent:assembleDebug :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease :app-parent:lintDebug --rerun-tasks --console=plain
.\scripts\verificar.ps1
```

Correctos. Gradle completo: 130 tareas ejecutadas. XML: 110 app + 30 núcleo +
1 interop, cero fallos/errores/omisiones; cinco oráculo Node: **146 locales**.
Diez vectores/78 comprobaciones documentales. Lint: cero errores/23 avisos.
APK debug, instrumentado y release sin firma de distribución compilados.

## Alcance y siguiente paso

Rutas: RelayConnection.kt, RelayConnectionTest.kt bajo app-parent/src;
README.md, ESTADO.md y esta evidencia. Contrato/esquema/fixtures sin cambios.
El servidor solo se leyó; no se ejecutó ni modificó su harness aquí.
No ADB, dispositivos, instrumentados, credenciales, despliegue ni agentes.

No acredita temporización Android física, comunicación de APK, restricciones
del hijo ni conformidad completa. El envío queda protegido aun sin tick, pero
la publicación visual de canalSano se actualiza en el siguiente tick/callback.
Siguiente P08-B: integración JVM de ControlPoliticas/ParentStore contra relay
real con hijo simulado solo en tests, sin saltar guardas de producción.
Conservar pruebas independientes del padre y TLS/hostname. A56 requiere aviso
y autorización nueva antes de pruebas; A13 permanece excluido.
