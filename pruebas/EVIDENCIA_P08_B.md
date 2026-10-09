# P08-B — políticas del padre contra relay real local

Fecha: 2026-10-09. Integración JVM parcial, no entrega ni cierre de G3 física.
Relay externo observado: commit ce59ef6, limpio antes/después. Solo lectura y
ejecución de sus módulos dist existentes; no se compiló/modificó el servidor.

## Flujo demostrado

IntegracionPoliticasRelayTest inicia un proceso Node con pruebas/relay-politicas.mjs.
El harness importa crearServidorLocal y codec/crypto reales desde la raíz externa
explícita. Compara SHA-256 de contrato, esquema y vectores antes de escuchar.
No MockWebServer, eco ni transporte falso en este recorrido.

1. Certificado TLS con SAN 127.0.0.1 e identidades P-256 efímeros. Privadas solo
   RAM; bootstrap TLS pasa por pipe, nunca env/archivo/log. Listener solo loopback,
   puerto efímero. Cliente test confía únicamente en ese certificado; no trustAll
   ni bypass de hostname/TLS. Cliente de producción no cambia.
2. Hijo Node ficticio y padre RelayConnection autentican AUTH con firmas CP/1.
   Padre solo obtiene ticket de políticas tras PING/PONG correlacionado.
3. ControlPoliticas/CanalPoliticasRelay reales bloquean sync con red/autoridad
   ficticias ausentes. STATE firmado de hijo no apto bloquea ALLOW. Otro almacén
   PREPARADO tampoco permite órdenes, aun con STATE apto; no se promociona.
4. Almacén ficticio VINCULADO y STATE apto permiten preparar LOCK_FOR de 60 s.
   Autoridad ausente impide envío sin reservar; luego confirmación explícita
   reserva seq=1, firma y persiste comando antes de enviarlo por canal real.
   Callback de test lee ParentStore y compara bytes/etapa antes de cada envío.
5. Hijo retiene ACK de los primeros dos envíos. Se avanza solo reloj de políticas
   a 5/10 s: dos reintentos de bytes idénticos. RelayConnection usa reloj real.
   Hijo verifica firma/contexto y compara contenedor original en los tres envíos.
6. Hijo envía ACK firmado con hash ajeno: ControlPoliticas lo rechaza, intención
   intacta. ACK pendiente correcto conserva intención y detiene retransmisión;
   no se interpreta como APPLIED ni genera una duración/orden nueva.
7. Segundo plano invalida canal. Se cierra/reabre escritor ParentStore sobre el
   mismo cifrado en RAM, sin perder contador/intención. Reconexión autenticada y
   ControlPoliticas nuevo no reproducen el comando antiguo. Cuarto STATE tiene
   sesión nueva y registro id/seq/hash coincidente, ENFORCEMENT_FAILED simulado;
   resuelve intención atómicamente, conserva contador=1 y presenta fallo.
8. Harness verifica cuatro sync y exactamente tres órdenes y cierra sockets,
   proceso y listener. Timeout global de 60 s en harness y esperas JVM acotadas.

## Ejecución y resultados

```powershell
.\gradlew.bat :app-parent:testDebugUnitTest '-PrutaRelayIntegracion=C:\Users\Usuario\Documents\parental_render' --tests '*IntegracionPoliticasRelayTest' --rerun-tasks --console=plain
.\gradlew.bat :core-protocol:test :core-protocol:testInterop :app-parent:testDebugUnitTest :app-parent:assembleDebug :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease :app-parent:lintDebug '-PrutaRelayIntegracion=C:\Users\Usuario\Documents\parental_render' --rerun-tasks --console=plain
.\scripts\verificar.ps1
```

Prueba focalizada aprobada y repetida. Suite completa: 130 tareas ejecutadas;
111 app (incluye una integración), 30 núcleo, 1 interop, cero fallos/errores/
omisiones. Script independiente posterior: 110 app, 30 núcleo, 1 interop y
cinco oráculo Node, total 146; integración excluida por falta de propiedad, no
contada como aprobada/omitida en ese script. **147 demostradas con la opcional.**
Diez vectores/78 comprobaciones documentales. Lint: cero errores/23 avisos.
Debug/instrumentado/release sin firma de distribución compilados. Ningún
instrumentado ejecutado. Sin npm/descargas ni modificaciones en repo externo.

## Límites y handoff

Solo pruebas y configuración Gradle opcional: no cambios a fuentes de producción,
permisos, dependencias o CP/1. Test/hijo ficticio no se incorporan al APK operativo.
ParentStore y AES-GCM son reales; archivo, claves AES/P-256, autoridad y red son
JVM/RAM, no AtomicFile/Keystore/NetworkCallback Android. Reapertura de escritor
no equivale a muerte de proceso/corte eléctrico. No hay restricción aplicada,
consumo persistido ni temporizador real del hijo, y nunca se simula APPLIED.

No prueba ACK totalmente perdido hasta agotar reintentos, reinicio del relay,
conformidad de latidos prolongados ni APK/hijo físicos. Las pruebas unitarias
previas mantienen cobertura adicional de esos caminos del padre con fakes.

Siguiente P08-C: coordinar APK hijo compatible y entorno autorizado/confianza
TLS para comunicación real antes de despliegue. No conectar teléfono al loopback
del PC sin diseñar esa ruta. No instalar CA de test ni promocionar PREPARADO
para facilitar la prueba. Aviso y autorización nueva para cualquier operación
A56; A13 excluido. Sin ADB, despliegue, agentes ni cambios de credenciales aquí.
