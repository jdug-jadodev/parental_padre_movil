# Coordinación Padre / Relay / Hijo

Fecha: 2026-10-09. Handoff del proyecto padre para el coordinador en parental_render.
El propietario ha designado a la IA del servidor para orquestar el trabajo restante.
Este archivo describe necesidades y evidencias; no autoriza cambios en otros
repositorios, acceso a dispositivos, aprovisionamiento ni despliegue.

## Resultado de PADRE-C01 — diseño, no promoción

### PADRE-QR01 — fallo QR reproducido, lector no corregido

Diagnóstico sobre 8a6ae91, evidencia pruebas/EVIDENCIA_PADRE_QR01.md.
Presupuesto fijo ejecutado: 64 pares/128 frames, 16 variantes del primer fallo,
control PURE y fixture público fijo; no reruns ilimitados. Tres respuestas
fallidas (índices 39/56/63), ningún reto. Versión 15/ECC L, máscara 4; DER 71/72.
Márgenes/escalas alternativos no corrigen el caso fijo; siete otras máscaras sí
leen mismos bytes con firma válida. No equivale a receta/máscara segura universal.

Confirmado caso: detector selecciona patrón interno (122.5,676.0) en vez de esquina
real inferior izquierda (68.5,698.5). QRCodeReader directo da ChecksumException,
QrLocal/MultiFormatReader da NotFoundException. PURE recupera bytes/firma exactos.
No error demostrado del render RGB de test ni dependencia de orden de suite
para ese caso. Heurística interna exacta sin instrumentar; producción intacta.

Cambios: app-parent/build.gradle.kts (opt-in diagnóstico JVM), DiagnosticoQrTest.kt,
test/resources/qr01_respuesta_publica.json y documentación propia/ESTADO. Recurso
solo sobre/pública ficticios; no privadas/códigos/QR operativos. Caso fijo comprueba
fallo conocido explícito, no concede permisos ni reemplaza lector del test P07.

Resultados: muestreo FALLÓ con tres errores; caracterización inicial FALLÓ porque
lector directo dio Checksum y no NotFound esperado; expectativa precisada PASÓ
con fallos productivos explícitos; suite pertinente PASÓ (132 app, uno de
caracterización, cero fallos/errores/omisiones). Núcleo/Node/relay no reejecutados.
Test P07 aleatorio intacto pasó en suite, **incidencia no corregida**. Registro
incluye todos los fallos, variantes/hipótesis y comandos. Commit de cierre por
mensaje «PADRE-QR01: reproducir fallo de detección QR con fixture público».

No cambio productivo permitido: requiere revisión/asignación aparte. Siguiente
propuesta PADRE-QR02 de revisión técnica del lector, no activada. Sin cambiar C02,
CP/1, puertas/orden, migración/promoción, cámara/UI/órdenes, dependencias o repos
externos. Sin dispositivos/ADB/Keystore/certificados/LAN/subagentes/prompts de vuelta.

### PADRE-C02 — diagnóstico puro de laboratorio completado

Asignación autorizada comunicada por coordinador, sobre revisión 95dfd02 y acuerdo
hijo cb62a73. Sin decisiones de puertas/orden/CP/1. Resultado implementado aislado
en src/revisionAislada/kotlin del padre, solo sourceSet test JVM, fuera de APK/
androidTest. Evidencia: pruebas/EVIDENCIA_PADRE_C02.md. Commit de cierre por
mensaje «PADRE-C02: validar revisión ficticia aislada con pruebas JVM».

Entradas inmutables/defensivas; re-verificación PAIR/STATE/OFFLINE con claves
vinculadas, contexto/tiempos y señales explícitos, cuatro datos de resumen y
declaraciones separadas FICTICIAS_JVM. Salida únicamente diagnóstico: no permiso,
token, vínculo operativo, escritura, migración, UI o órdenes. No valida físicamente
Keystore, autenticación, readback del hijo o consumo OFFLINE; obligaciones del
llamador documentadas. No existe un puerto productivo para esos datos ficticios.

21 tests nuevos correctos; suite final 131 app + 30 núcleo + 1 interop + 5 Node,
**167 independientes**, sin fallos/errores/omisiones. 78 comprobaciones documentales;
29 tareas Gradle finales ejecutadas. Clases Revision: cero en main/debug, 16 en
debugUnitTest. No assemble/lint/instalación/instrumentación/integración externa.
Primer intento completo falló en QR sintético P07 anterior; pasó aislado y suite
repetida. No modificado ni declarado corregido; causa no identificada.

Archivos: app-parent/build.gradle.kts, src/revisionAislada/kotlin/.../pairing/RevisionVinculo.kt,
src/test/java/.../RevisionVinculoTest.kt; padre/DISENO_ACREDITACION_VINCULO.md §8,
pruebas/EVIDENCIA_PADRE_C02.md, COORDINACION_PROYECTOS.md y ESTADO.md.
Sin ParentStore/Metadatos/fuentes productivas/CP1/plan modificados. Sin dispositivos,
certificados/LAN, subagentes, repos externos editados o prompts de vuelta.

Siguiente unidad propuesta, no activada: diagnóstico acotado del fallo QR sintético
previo; coordinador debe revisar/asignar antes. Ceremonia/persistencia/migración,
H08/H10/H11, decisiones del propietario y aceptación física siguen pendientes.
Las secciones C01/R1 siguientes conservan su contexto histórico, no revocan esta
asignación ni constituyen nuevas tareas.

### PADRE-C01-R1 — acuerdo público revisado

Revisión documental cerrada sobre HIJO-C02 **cb62a73** / H05-L01 **c7fb842**.
Resultado: formato/estatus/errores y declaraciones **compatibles como propuesta**;
detalle en §7 de padre/DISENO_ACREDITACION_VINCULO.md. Commit de cierre consultable
por mensaje «PADRE-C01-R1: acordar resumen público y acotar validador puro».

Acuerdo explícito: pairId completo, SHA-256 SPKI DER P-256 de ambas públicas
(64 hex mayúsculos, 16 grupos de cuatro) y relayOrigin HTTPS exacto sin truncar.
Un snapshot íntegro/readback post-commit origina el resumen; texto de datos guardados
con acreditación pendiente. Reapertura no revincula; error/corrupción/legado sin
procedencia/identidad perdida no producen confirmación ni reemplazo de clave.
Precisión mínima incorporada al diseño del padre: «commit local confirmado», no
«vínculo operativo confirmado». Sin corrección necesaria en el formato del hijo.

Hash privado PAIR del padre no pasa al resumen ni se exige al hijo. Resumen/STATE
no certifican aceptación exacta/consumo OFFLINE. Commit y mantenimiento observados
son declaraciones del adulto separadas de firmas, nunca prueba de Device Owner.

Matriz A/B/C1/C2 compatible como propuesta: no H06 por JVM, C1 salida real previa
a endurecimiento, C2 ceremonia posterior con STATE/WSS, no sustitución ni promoción.
Decisión de puertas/orden sigue pendiente del propietario; plan/CP/1 sin cambios.

PADRE-C02 futuro acotado: entradas inmutables de snapshot/aceptación, revisión/
resumen, STATE/contexto verificados, OFFLINE/observaciones y señales locales de
autoridad/lifecycle/canal; salida diagnóstico puro, no autorización. Llamador no
puede delegar comprobación física o Keystore al validador. Negativos definidos,
**no implementados/ejecutados**. Esperar revisión y asignación explícita; no activa
UI, migración, escritura VINCULADO o órdenes.

Solo lectura/revisión documental y diff --check. Resultados hijo 17 tests nuevos/
346 JVM y relay históricos son reportados, no reejecutados ni evidencia física
nueva. Sin código/tests/builds, dispositivos, certificados/LAN, repos ajenos,
subagentes o prompts de vuelta. Siguiente paso: coordinador revisa acuerdo y
presenta decisión pendiente al propietario antes de asignar implementación.

Unidad documental completada: `padre/DISENO_ACREDITACION_VINCULO.md`.
Base de implementación permanece 77c2b10; diseño original en **0e7b5db**,
mensaje «PADRE-C01: diseñar acreditación presencial de vínculo».

CP/1 §5 permite una ceremonia presencial: validación de aceptación propia,
observación explícita del commit/resumen del hijo, STATE firmado correlacionado,
recuperación MAINTENANCE real observada y confirmación final del adulto con
autoridad. Se propone persistir etiqueta y evidencia mínima juntas en transacción
privada versionada, sin alterar contador/claves ni reutilizar contexto para órdenes.

Límite crítico: STATE no certifica PAIR_ACCEPT exacto; OFFLINE_RESPONSE no prueba
consumo/aplicación y STATE no correlaciona nonce OFFLINE. No hay recibo automático
inventado. Si se exige prueba remota criptográfica de esos hechos, se necesita
propuesta CP/2 acordada y se detiene esa vía; no cambios CP/1 en esta unidad.

Diseño incluye pruebas negativas y aceptación futura, **ninguna ejecutada ahora**.
Dependencias: H08 commit/resumen del hijo, H10 estado/WSS, H11 recuperación;
acuerdo de migración local, entorno y permiso físico posteriores. Disponibilidad
del A56 no autoriza operarlo; A13 sigue excluido aquí.

Coordinación relay 8385410/documentación e26aebb y sus 104 tests/seis variantes
JVM leídos como evidencia externa, no reejecutados ni reinterpretados como APK/
Device Owner/enforcement. No se repite cobertura local ya terminada del relay.

Siguiente unidad propuesta **PADRE-C02**: validador puro y negativos JVM, sin
promoción productiva ni habilitación de órdenes. Requiere revisión/asignación;
no activada ni enviada por otra API. Resultado se devuelve aquí y en respuesta
para evitar bucles. No código productivo, dispositivos o cambios externos.

## 1. Estado del padre

Repositorio: `C:\Users\Usuario\Documents\parental_padre`.
Último commit de implementación/pruebas: **77c2b10** (P08-B).

- C00–C02 y P00–P07 implementados localmente; aceptación física e integrada parcial.
- Identidad P-256 no exportable en Keystore y autenticación del adulto.
- Vinculación QR presencial, almacenamiento privado cifrado y transaccional.
- Cliente WSS con TLS/hostname del sistema, cp.v1, AUTH y PING/PONG.
- ControlPoliticas: STATE firmado, confirmación explícita, reserva/persistencia
  antes de enviar, máximo dos reintentos idénticos y reconciliación por id/seq/hash.
- Pantallas de control y recuperación OFFLINE; no ALLOW offline. RETIRE advierte
  posible borrado y todavía exige confirmación destructiva en el hijo.
- Cadencia PING corregida a 15 s desde envío (48e681b); caducidad de canal 45 s
  desde último PONG verificada también antes de enviar sin esperar al tick.
  Timeout conservador de respuesta de 15 s mantenido.

**Demostrado:** 146 pruebas independientes y una integración JVM opcional contra
relay real local, total 147. APK debug/instrumentado y release sin firma de
distribución compilados; lint cero errores/23 avisos.

La integración P08-B usa relay externo compilado observado en **ce59ef6**,
loopback TLS y un hijo Node ficticio. Prueba guardas de red/autoridad/hijo apto/
vínculo, persistencia, reintentos exactos, rechazo de ACK con hash ajeno,
ACK pendiente y reapertura/reconexión sin reproducir orden antigua. STATE nuevo
reconcilia fallo simulado. No simula APPLIED ni demuestra restricciones reales.

Referencias: `ESTADO.md`, `pruebas/EVIDENCIA_P08_A.md`,
`pruebas/EVIDENCIA_P08_B.md`, `pruebas/EVIDENCIA_P07.md`.

## 2. Bloqueo crítico: vínculo operativo

La vinculación de producción conserva **PREPARADO**. No se promociona por
STATE, presencia, ACK del relay ni porque un test haya pasado. No permite
órdenes mientras no se acredite vínculo del hijo y recuperación presencial.

**Pendiente del padre:** acordar y completar un flujo seguro para acreditar esas
condiciones y habilitar VINCULADO en producción. P08-B inicializa VINCULADO solo
en un almacén ficticio de test; no resuelve esta puerta operativa.
Asignar este trabajo al proyecto padre, sin cambiar guardas desde el servidor.
Si necesita cambiar semántica CP/1, detener y acordar versión nueva entre roles.

## 3. Qué necesitamos del coordinador

### Proyecto hijo

1. Identificar carpeta, IA responsable, estado y APK/build compatible CP/1.
   No hay APK hijo ni dispositivo hijo autorizado acreditados por este proyecto.
2. Confirmar implementación de identidad/firma CHILD, vínculo presencial,
   STATE/ACK, bootId/childSessionId/commandNonce y persistencia de aceptación.
3. Confirmar Device Owner/Lock Task mediante APIs oficiales y evidencia propia;
   un booleano del simulador no prueba aprovisionamiento ni restricciones.
4. Preparar recuperación presencial y emergencia antes de cualquier endurecimiento.
   OFFLINE_CHALLENGE/RESPONSE debe consumir desafío duraderamente, rechazar
   acción distinta, repetición, vencimiento y arranque anterior. RETIRE exige
   confirmación local/BORRAR. No probar borrado en un teléfono cotidiano.
5. Acordar con el propietario un dispositivo hijo de pruebas y el alcance
   específico de operaciones permitidas. No inferir autorización de esta entrega.

### Relay / entorno local

1. Mantener autenticación CP/1 y rutas WSS reales, sin colas históricas ni privadas
   del padre. Un relay no certifica aplicación de políticas.
2. Proponer endpoint local HTTPS/WSS alcanzable desde las APK y confianza TLS
   válida para Android. **127.0.0.1 del PC no es el PC desde un teléfono.**
3. Acordar hostname, certificado, ruta de red y exposición antes de abrir LAN o
   instalar certificados. No trustAll, HTTP/WS ni bypass de hostname en la app.
4. Preparar escenarios de reinicio del relay, receptor ausente, ACK perdido,
   reconexión y ausencia de reproducción de órdenes antiguas. Reinicio real del
   relay y ACK completamente perdido no se ejecutaron en P08-B.
5. No desplegar en Render antes de verificar comunicación local y revisar las
   puertas de aceptación. Un APK release sin firma de distribución no es entrega.

### Proyecto padre

Asignar aquí, una tarea por turno:

- Flujo seguro de acreditación del vínculo descrito en §2.
- Ajustes del cliente/configuración solo si el endpoint acordado los requiere,
  preservando TLS/CP/1 y confirmación del adulto.
- Actualización autorizada de APK en A56: el instalado conserva una versión
  anterior a P04–P08, no acredita estas funciones.
- Pruebas de AUTH/órdenes/OFFLINE con Keystore y adulto reales; QR/cámara,
  selector, rotación, fondo, texto grande y accesibilidad ampliada.
- Evidencia de ACK/STATE auténticos e interfaz sin estado permitido optimista.
- Preparación posterior de firma de distribución y auditoría de logs/exportación,
  sin incorporar secretos en el repositorio.

No pedir al coordinador que implemente estos cambios directamente en este repo;
trasladar tareas y resultados a su IA responsable.

## 4. Contrato y claves

Comparar copias antes de cada integración:

| Archivo del padre | SHA-256 congelado |
|---|---|
| compartido/00_CONTRATO_V1.md | deee863f7587736d3e209f5a11a6e972a93004d0700c47eb3a2d136ad8f16a3c |
| compartido/protocolo.schema.json | eccf964fbb41869c47d86ddb0bfda6585136430bab297568d3c20b56607372bb |

Comparar también `pruebas/vectores_crypto.json` y fixtures entre repositorios.
No cambiar esquema, campos, firmas o semántica unilateralmente. No enviar
privadas/códigos de recuperación en archivos de coordinación, prompts, env o logs.
Configuración del relay usa únicamente claves públicas y pairId acordados.

## 5. Reproducir la evidencia JVM sin dispositivos

Desde parental_padre, con JDK 21, SDK Android 36 y Node 24:

```powershell
.\scripts\verificar.ps1
.\gradlew.bat :app-parent:testDebugUnitTest '-PrutaRelayIntegracion=C:\Users\Usuario\Documents\parental_render' --tests '*IntegracionPoliticasRelayTest' --rerun-tasks --console=plain
```

Segundo comando opcional: requiere módulos dist/dependencias del servidor ya
preparados en su propio proyecto. No instala npm ni compila/modifica servidor.
Compara hashes antes de escuchar; certificados/privadas efímeros solo RAM/pipe.
Sin propiedad, la prueba externa queda excluida y la verificación es independiente.
Reapertura probada de ParentStore cifrado en RAM no equivale a muerte de proceso
Android ni a AtomicFile/Keystore físicos.

## 6. Permisos y orden de ejecución

- Comunicación, documentación y commits en español claro.
- El padre autorizado es **A56**; avisar y esperar habilitación nueva antes de
  instalar, ejecutar tests o cambiar algo por ADB. Seleccionarlo explícitamente.
- **A13 excluido**, incluso conectado/autorizado por ADB. No usarlo para hijo ni
  pruebas por referencias históricas; solo una autorización expresa nueva cambia
  esta exclusión. No guardar identificadores de dispositivos en este archivo.
- No restablecer/aprovisionar teléfonos, cambiar credenciales ni borrar datos
  sin autorización específica del propietario. Emergencia siempre accesible.
- Orquestación no implica permiso para lanzar subagentes, operar dispositivos,
  editar el repo de otro rol ni publicar/desplegar.

Orden recomendado: identificar hijo/dispositivo → comparar CP/1 → acordar red/TLS
local → acreditar vínculo/recuperación → autorizar actualización A56 → probar
las APK localmente → probar fallos/reconexión → revisar evidencias y entrega.
G0–G4 no se aprueban por simulación ni compilación.

## 7. Qué devolver al padre

Enviar un handoff con: tarea asignada al padre, repositorio/commit del relay e
hijo, pruebas efectivamente ejecutadas, endpoint/confianza TLS acordados,
condiciones aún bloqueadas y autorización concreta del propietario si corresponde.
No incluir secretos. El padre actualizará código/evidencias dentro de su proyecto.
