# PADRE-C03 — revisión documental de control local y versión

Fecha: 2026-10-10. Base padre `a8e4ffc`; propuesta hijo HIJO-C03 `9ae6c99`.
**Compatible como propuesta con precisiones; no aprobación de opciones, versión,
puertas o implementación.** CP/2 sigue siendo nombre provisional.

Nota incremental 2026-10-12: PADRE-C04, BORRADOR_CP2_ORDENES_Y_TIEMPO.md,
recoge decisiones nuevas comunicadas por coordinador: código numérico5–10/pregunta
local, todas apps normales, permiso LOCAL/REMOTO aun sin internet, última acción
confirmada y tiempo consumido no humano. Sustituyen alternativas históricas de este
documento donde corresponde, sin aprobar versión/schema/puertas ni parámetros técnicos.

## 1. Fuentes y resultado

Consultados en lectura: hijo/DISENO_CONTROL_LOCAL_ADULTO.md y relevo del hijo;
documentacion/CONTROL_LOCAL_ADULTO_PROPUESTA.md y coordinación central de Render.
Fuentes locales: diseño de acreditación §§1–7, CP/1 §§2–5/7/9–11/9.A,
SPEC-P P3/P4/P5/P6, PLAN_PADRE P05–P08 (solo correspondencia, no actualización),
ControlPoliticas.recibir, ParentStore.resolverIntencion y PresentacionControl actuales.

Requisitos confirmados: controles del hijo protegidos por código adulto; bloqueo
y autorización de uso normal locales, aun sin internet en hijo; padre conserva
remoto; cada reboot bloquea hasta nueva autorización local/remota de ese arranque;
emergencia sin código ni bloqueo por intentos, Keyguard Android intacto.

HIJO-C03 separa correctamente autoridad local, permiso normal y recuperación;
propone serialización, persistencia y reporte sin exportar credencial. Se comparte
esa dirección **documental**. Duraciones, precedencia, credencial/KDF, recuperación,
apps y migración no están decididas. No reinterpretar CP/1 mientras se acuerdan.
H05-L01/PADRE-C02 prueban sus laboratorios anteriores, no este cambio ni enforcement.

## 2. Acuerdos y precisiones mínimas a devolver

| Tema hijo | Revisión padre / precisión propuesta |
|---|---|
| Administración RAM distinta de uso | Abrir controles no permite apps; permitir apps no abre controles. Cerrar por fondo/salida/caducidad/proceso sin revocar por ello permiso normal confirmado. Cinco minutos es opción, no requisito. Acción sensible revalida autoridad/contexto justo antes del commit. |
| Permiso normal por boot | Persistir permiso/revisión/plazo original solo si se elige continuidad de proceso del mismo boot; RAM administrativa nunca se restaura. Reboot, continuidad incierta o reloj discontinuo bloquean. No usar hora civil ni archivo ALLOW anterior como autorización nueva. |
| Intentos de credencial | Registro/esperas propios de credencial reutilizable, distintos de consumos/esperas de códigos CP/1. Un acierto no devuelve códigos gastados. Fallos no revocan uso local vigente ni cortan emergencia/remoto/recuperación legítima; los umbrales propuestos no están aprobados. |
| Última acción válida gana | Recomendación razonable para mantener ambas autoridades: escritor único en hijo, revisión durable global independiente de seq remoto. LOCK remoto fresco puede revocar local; permitir local posterior puede sustituirlo. No manda la hora del padre ni el orden de llegada de paquetes sin contexto. |
| Conflictos | Una revisión sustituida invalida desafíos/confirmaciones/timers anteriores. Dos acciones contra la misma revisión no se aplican ambas: la primera comprometida cambia revisión; la segunda debe sincronizar/revisar y obtener confirmación nueva, no refirmarse automáticamente. |
| Red/reconexión | La pérdida de red no invalida LOCAL vigente. Reconectar, PONG, presencia o STATE no son decisiones de uso. Regla para fuente REMOTA es elección pendiente; no reaplicar autorización remota vieja por reconectar ni recuperar un permiso local ya revocado. |
| LOCK_FOR | Su apertura diferida también pertenece a una revisión/boot y fuente determinados. Si hubo acción local/remota posterior, el timer antiguo no abre ni bloquea. Acordar si sobrevive la semántica CP/1 de apertura condicionada o se simplifica; no eliminar este botón ni cambiar significado silenciosamente. |
| Desconocido/fallo | Distinguir intención aceptada, aplicación pendiente/fallida y modo efectivo observado. Fuente LOCAL no equivale a APPLIED. Fallo de integridad/enforcement exige causa real y bloqueo conservador sin ocultarlo como NETWORK_LOST ni afirmar protección comprobada. |

Ejemplo conceptual (no fixture/schema): revisión R acepta permitir local; desafío
remoto de R−1 queda inválido. Tras sync de R y confirmación nueva, LOCK remoto
compromete R+1. Adulto autenticado puede permitir local en R+2. ACK de R+1 o timer
de R no alteran R+2. Un bloqueo remoto en tránsito puede ser rechazado por cambio
local: mostrar conflicto/no confirmado y pedir decisión nueva; no prometer entrega
instantánea ni prioridad remota absoluta sin elegir otra regla expresamente.

## 3. Padre: persistencia, reconciliación y presentación propuestas

Hoy ControlPoliticas.recibir correlaciona id/seq/hash y traduce lastResult/result
a fase; ACK puede reemplazar ultimaEvidencia. ParentStore valida correspondencia
con la intención pendiente. Son mecanismos CP/1, **no suficientes por sí solos**
para coexistencia local propuesta; no se modificaron aquí.

Proponer dos planos independientes y un único registro mínimo vigente, sin historial:

1. **Resultado de última orden remota:** id/seq/hash de payload, resultado de esa
   aplicación y contexto/revisión correspondiente. Correlación exacta resuelve
   pendiente solo por evidencia de esa orden; acción local no crea seq/id/ACK padre.
2. **Estado efectivo:** modo/cause, fuente de autorización, boot/revisión y vigencia
   del último STATE verificado y fresco. Permiso y enforcement real diferenciados;
   Keyguard puede impedir interacción aunque la política permita apps.

ACK tardío auténtico puede informar «esa orden se aplicó entonces», no «sigue
vigente» ni sustituir un STATE de revisión posterior. ACK sin vuelo/contexto
correlacionable no se acepta como confirmación por conveniencia. Pedir sync fresco
para estado efectivo. Incluso STATE fresco solo confirma su snapshot al emitirlo:
no garantiza que el adulto no actúe localmente inmediatamente después.

| Evidencia recibida | Tratamiento propuesto |
|---|---|
| STATE actual LOCAL, última remota coincide y APPLIED | Resolver resultado remoto correlacionado; mostrar por separado decisión local posterior y estado efectivo informado. No «remoto actualmente aplicado». |
| STATE LOCAL, última remota no coincide/resultado ausente | Resultado pendiente desconocido; conservar reserva/pendiente y pedir sync/decisión explícita. No inferir APPLIED/REJECTED de modo o revisión mayor. |
| ACK APPLIED viejo tras STATE más nuevo | No retroceder modo/revisión ni reloj de frescura del estado; solo resultado remoto verificable. |
| ACCEPTED_PENDING_ENFORCEMENT o ENFORCEMENT_FAILED | Pendiente/error de esa orden separado del modo efectivo; no convertir en éxito por ver apps abiertas. |
| Sin conexión/STATE vencido | «Último estado confirmado», fuente/antigüedad; modo actual desconocido. No afirmar que está bloqueado por falta de internet: podría seguir LOCAL. |

Reservar seq e intención/comando firmado/evidencia con escritor único y transacción
privada versionada del padre; preservar máximos incluso si orden rechazada por
revisión vieja. Nunca reutilizar seq reservado para bytes nuevos. Duplicado exacto
consulta resultado retenido sin reaplicar ni renovar tiempo; orden antigua fuera
del registro mínimo retorna desconocido/rechazo seguro, no reconstruye historial.
Los criterios exactos de resolución de pendiente y retención deben acordarse en
contrato nuevo. Descartar pendiente local no revoca una aceptación en hijo.

## 4. Credencial, olvido, vínculo y límites físicos

Credencial reutilizable **propia de instalación**, no universal, ni contraseña del
padre o secreto relay. Padre/relay no almacenan ni verifican código maestro; no
reciben secreto, salt, verificador o QR reutilizable. Autenticación Keystore del
padre sigue siendo su autoridad remota, no prueba de conocimiento del código hijo.
Copiar credencial fuera del hijo es decisión presencial; no transporte por PAIR,
STATE, exportación pública, logs o sincronización. Datos de intentos tampoco son
telemetría necesaria para padre/relay.

Aleatoria larga da entropía calculable pero mala entrada manual; frase escogida
facilita uso, entropía incierta; PIN corto es cómodo y expuesto a observación/brute
force. Recomendar aleatoria de alta entropía como opción preferente, **sin aprobar
26 caracteres** ni formato. Reutilización amplifica riesgo de observación/teclado,
capturas y acceso del adulto anterior; ocultar UI no prueba protección física.
KDF/versionado/verificador protegido y coste quedan para diseño/medición autorizada;
no garantizar coste, hardware o antirollback por inspección. No habilitar teléfonos
para medir por esta revisión; A13 sigue excluido en este proyecto.

Rotación ordinaria: autoridad actual revalidada, entrega solo en hijo y commit/readback
atómicos de generación/verificador/esperas conforme política acordada. Retirar RAM
y confirmaciones viejas; proponer bloquear uso tras rotación para nueva autorización.
Fallo conserva registro anterior o nuevo íntegro; no dos credenciales válidas sin fin.

Olvido con padre disponible: recomendable autorización parental autenticada para
rotación **presencial y específica**, sin revelar código ni habilitar apps/controles
por MAINTENANCE. Si se elige esta opción necesita semántica/acción propias acordadas;
no imprescindible para la primera versión del control normal. Olvido sin padre:
decidir si crear recuperación dedicada de consumo único o aceptar vías limitadas
existentes/retirada presencial autorizada. Código dedicado añade alcance y ceremonia,
**no se exige automáticamente**; nunca convertir los tres CP/1 en código maestro.

Cambio de padre no deriva credencial de pública ni la envía al sucesor. Recomendar
decisión presencial de rotación obligatoria al cambiar adulto responsable (pendiente),
preservando vínculo anterior hasta commit autorizado y sin reponer consumos gastados.
Identidad/protección perdida no admite generar reemplazo o enrolamiento libre;
la vía concreta de recuperación/retirada y sus pruebas independientes debe acordarse.

## 5. Versión, migración y ceremonia: delta mínimo por rol

| Rol | Delta mínimo propuesto tras acuerdo |
|---|---|
| Hijo | Credencial/verificación/esperas locales y RAM administrativa; permiso por boot con fuentes, revisión/escritor, desafío/confirmación/timers; persistencia íntegra y enforcement; proyección/STATE diferenciados; migración y ceremonia. |
| Padre | Modelos/parser y confirmación contextual nuevos; secuencia/pending preservados, reconciliación de dos planos y presentación; versión/capacidad acordadas en ceremonia/migración. Sin credencial local ni autoridad nueva por STATE. |
| Relay | Negociación/validación estricta de versión/subprotocolo, autenticación/firmas/dominios acordados y routing de mensajes nuevos. Sin lógica de permiso, verificador, endpoint maestro o APPLIED. |

Mantener P-256/ECDSA DER y serialización exacta salvo motivo acordado distinto;
definir dominios de firma, versiones de sobre/payload, propósitos y subprotocolo
juntos antes de nuevos campos. No usar CPv1 para autoridad de nueva semántica ni
aceptar campos desconocidos para «compatibilidad». Compatibilidad requiere matriz
de pares de versiones por tres roles, rechazos cruzados y límites explícitos;
sin fallback/downgrade CP/1 que conserve uso local nuevo.

Migración física explícita, no por recibir STATE/instalar APK/presencia. Preservar
identidad y vínculo existentes, seq/reservas/pending y resultados inciertos,
consumos/esperas de recuperación, esperas/generaciones locales si ya existen;
readback/transacción privada y errores conservadores. No fabricar credencial,
autorización de boot ni acreditación desde etiqueta/hash/fixture. Adulto configura
credencial en hijo y verifica ambos roles/versión; instalación bloqueada al concluir
hasta autorización nueva. Datos viejos retenidos no rearman comandos/timers.

Incompatibilidad: remoto indisponible con causa de versión, no borrado/revinculación
automáticos. LOCAL solo funciona si versión nueva íntegra, configurada y autorizada
para boot vigente. Protección ante APK/registro antiguo y rollback de datos debe
diseñarse/probarse; «fallar cerrado» de código nuevo no garantiza que APK antiguo
respete marcador nuevo. No prometer antirollback absoluto sin capacidad comprobada.

Reutilizar comparación pública padre/hijo de PADRE-C01-R1 (pairId, dos huellas y
origen del mismo snapshot/readback), sin secreto ni quinto dato inventado. Acordar
cómo comparar versión/capacidades/procedimiento nuevos; evidencia antigua no migra
a acreditación nueva. La ceremonia añade observaciones separadas de entrada protegida,
uso normal offline, bloqueo local/remoto fresco, reboot, emergencia y recuperación
del adulto según opciones elegidas; MAINTENANCE sigue limitado y no las sustituye.
Firmas/STATE prueban declaraciones/contexto, no enforcement físico o consumo real.
Recibos exactos de pairing/consumo siguen propuesta independiente: control local
**no los necesita automáticamente**. PADRE-C02 queda intacto como laboratorio CP/1.

## 6. Elecciones concretas para el propietario (ninguna aprobada aquí)

| Elección | Recomendación razonada / alternativa |
|---|---|
| Duración normal local y timers remotos | Duración explícita por acción y rango acordado, sin default silencioso de 30 min. Hasta boot posible si lo acepta; definir apertura diferida LOCK_FOR antes de conservarla. |
| Administración/ciclo de vida | RAM breve con cierre inmediato al fondo/salida/proceso y revalidación sensible; elegir una acción o sesión corta (5 min no aprobados). Permiso normal durable continúa solo con mismo boot/plazo comprobados. |
| Precedencia y red fuente REMOTA | Última acción fresca serializada; ambas autoridades pueden cambiar decisión. Solo LOCAL offline recomendado; decidir si REMOTA pierde uso o continúa sin canal y qué ocurre al recuperarlo, sin pisar LOCAL posterior. |
| Credencial y esperas | Preferir aleatoria alta entropía frente a PIN corto; ofrecer frase como alternativa explícita por usabilidad. Elegir formato/entrega/normalización/esperas; KDF/coste se decidirán con evaluación, no por cifra inventada. |
| Olvido/rotación/cambio de adulto | Rotación presencial específica con padre autenticado y rotar al cambiar adulto; decidir vía sin padre y si justifica código único adicional. No exigir recuperación nueva como condición implícita del requisito. |
| Alcance de «uso normal» | Lista de apps aprobadas conserva quiosco CP/1 y limita escapes; recomendarla por verificabilidad, pero confirmar con propietario si significa eso o uso más amplio. No imponerla como interpretación ya aceptada ni prometer cualquier app. |
| Transición/versión | Migración presencial bloqueada hasta nueva autorización, preservando identidad/seq/consumos/esperas; aceptar indisponibilidad remota entre versiones. Acuerdo de tres roles antes de contrato/ADR/fixtures, nunca downgrade encubierto. |

## 7. Mapa mínimo de subunidades y puertas, pendiente de acuerdo/asignación

1. Decisiones del propietario y revisión conjunta → propuesta ADR/versión/contrato,
   firmas/subprotocolo/contextos/errores/compatibilidad; luego actualizar planes y
   puertas autorizados. No alterar dependencias actuales por este mapa.
2. Hijo: modelos puros/escritor/boot y credencial/esperas; padre: dos planos de
   evidencia y reconciliación; relay: validadores/versiones. Casos negativos futuros:
   carreras local/remoto, ACK viejo, timer viejo, duplicado, proceso/boot/contexto,
   fallo IO/enforcement y mezclas de versiones. Son subunidades, no cierres H05/P08.
3. Persistencia/migración por rol y ceremonia de identidad/credencial/versión;
   H08 y ciclo H05/H08/H11 deben resolverse expresamente, no en forma circular.
4. C1 revisada: emergencia/Keyguard y recuperación/olvido/control offline/reboot
   reales **antes de endurecer**; A/B JVM no habilitan H06. H05/enforcement siguen
   pendientes. Quiosco gradual solo tras aprobación y repetir rutas reales bajo él.
5. H10/WSS/STATE y P05/P06: cruce local y reconexión/precedencia de dos APK con
   versión acordada; C2 revisada de acreditación posterior, no sustituto de C1.
   Actualizar P07/P08 y pruebas G1/G3 según acuerdo sin promoción automática.

El orden exacto de implementación y pruebas físicas sigue decisión del propietario/
coordinador. PADRE-QR01 `a8e4ffc`: defecto reproducido NO corregido; QR02 exige
asignación separada y cualquier ceremonia QR futura debe considerar esa limitación.
No se repitió diagnóstico ni se adoptó PURE_BARCODE como solución productiva.

## 8. Evidencia y entrega

Solo lectura/revisión documental y comprobaciones Git de cambios propios. Sin
tests, builds, validador C02 modificado, código/UI/Keystore/cámara, contratos/fixtures,
planes/ADR aceptados modificados, dispositivos/ADB/certificados/LAN/push/despliegue,
repos ajenos editados o subagentes/sesiones/prompts de vuelta. No evidencia física
nueva ni medición KDF. Rutas: este documento, diseño anterior (nota de alcance),
INTEGRACION_FUTURA.md, COORDINACION_PROYECTOS.md y ESTADO.md.

Cierre documental PADRE-C03; esperar decisiones/revisión/asignación explícitas.
No iniciar versión, implementación, QR02 ni siguiente unidad automáticamente.
