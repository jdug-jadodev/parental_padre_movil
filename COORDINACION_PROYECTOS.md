# Coordinación Padre / Relay / Hijo

Fecha: 2026-10-14. Handoff del proyecto padre para el coordinador en parental_render.
El propietario ha designado a la IA del servidor para orquestar el trabajo restante.
Este archivo describe necesidades y evidencias; no autoriza cambios en otros
repositorios, acceso a dispositivos, aprovisionamiento ni despliegue.

## PADRE-AUTH01 — codec/firmante/borde puros CP/2, sin red

Base0c66d96, código **0b010d7**. Fuente íntegra schema CP2-A01 Renderd99ddac recibida
en conversación, copiada contrato-cp2/autenticacion.schema.json sin leer repoexterno.
DTOs CHALLENGE10/AUTH_PROOF/AUTH_OK/PING/PONG/sobreAUTH cerrados; bytes canónicos
ASCII/UTF8/duplicados/base64/UUID/nonce32/depth6/límites8192-16384/DERr,s/P256.
DominioCPv2/AUTH LFreal sobre payloadoriginal, hashSHA256, copias defensivas. CP1intacto.

AuthClienteCp2 puro sin transporte/UI/store: snapshot externo explícito identidad/
pairId/rolPARENT/perfil/revisión/autorización; defaultausente→NO_DISPONIBLE, no inferir
desde reto/CP1. Generaciones por apertura/cierre, timeout10s conservador desde abrir,
sin renovar por reto tardío; nonce/connectionId exactos y payloadfirmado comparado.
AUTH_OK solo tras ticket confirmado enviado, mismo contexto y plazo. Stale callbacks
ignorados, replay/cierre/fondo/snapshot/reloj/firmafueraplazo invalidan. Adaptador
FirmanteAuthCp2 sobre almacén existente exige sesión/públicaesperada, sin crear/rotar.
No conectado MAIN/Keystore/runtime/WSS; snapshotPAIR01/migración/persistencia pendientes.
PING/PONG solo codec; AUTH_OK no remoto listo/hijo/STATE/APPLIED ni promociónPREPARADO.

12tests nuevos; fixture163app+30core+1interop=194distintos, normal162app, sin fallos/
errores/omitidos; buildsdebug/test/releaseunsigned+lint0errores/23warningsversiones.
APKpostcommit0b010d7: debug33ef534732cf2ce0e3a3e1acc1ca2afc2b9d98ac5266373d9ba11cac54d4dd7a;
releasee90c0da649277592d1bb0003dfdbcdbe92cce13fb38eef91707dda582b4d00b2.
DEX/ZIPdirigido148clasesJVMtest contrastadas,0coincidencias/diagnóstico/C02/falso/
marcadoresfixture/schemaAUTH;3clasesAUTHpresentes,518propias/APKapp. No auditoríauniversal.
Sin APKinstalado/ADB/dispositivos/red/reposajenos;125testsRenderA01 y avances hijo
solo reportados, no pruebas nuestras/autorizaciónceremonia. Rutas/comandos/hashes y
límites en pruebas/EVIDENCIA_PADRE_AUTH01.md. Detener; siguiente integración requiere
asignación snapshot/transportecp2 compatible, no otra tarea automática/prompts.

## PADRE-PAIR01 — MAIN lector CP/2 y preparación RAM; PREPARADO_FISICO

Basebeecf75, código **82734c7**. Fuentes CP2-V01Render4ac4962 schema/README recibidos
íntegros en conversación, sin lectura externa. Schema copiado en contrato-cp2/;
fuente tiene6/11/5 campos (aceptación11, no10 del resumen), preservados íntegros.
Codec Kotlin separado en core-protocol/.../cp2: canonicidad ASCII/UTF8/duplicados/
campos/UUID/base64/SPKIP256punto/DERrango/origen/depth6/límites8192-16384, dominio
CPv2/PAIR sobre bytes originales y hashpayload, pública esperada externa. CP1intacto.

MAIN Vinculación ofrece selector NUEVO PERFILCP2 para cámara/huellas sin pareja.
REQUIERE_MIGRACION en todo almacén legado/incierto; firma/códigosCP2 bloqueados en
MAIN incluso sin archivo (no alta automática). Sin crear/rotar identidad/store/
fakeparentconfirm ni retirar guarda de Recuperación. OfertaRAM5min/cierre-fondo-
disposal; errores ópticos vs decodificación-schema distintos sin logsbody.

PrepararPairCp2 es caso uso puro RAM sin store/commit: alta explícita coherente/
autorizada, snapshot identidad/generación/boot, ticket5min, descarte/cambio contexto,
confirmaciones humanas+auth,3códigos20bytesBase32/hashCPv2RECOVERY, firma real separada.
**No conectado a firma/Keystore/persistencia MAIN**, por falta de snapshot compatible
aprobado; probado con fakes, no adulteración de producción. Entrega parcial expresa,
no aceptación definitiva o promoción PREPARADO/operativo.

9tests nuevos; fixture151app+30core+1interop=182distintos, normal150app,0fallos/
errores/omitidos. Builds debug/testAPK/release unsigned/lint0errores/23avisos por
variante. APK postcommit82734c7: debug8c919e1ec052f017e47668d4ae07fbe9087cdecc952fe5100f72d7cee247970b;
releasec39b7c1991a252c62e5fd4bc778a9977a5d8a1997dc6284b1cf1e0af13b645e9.
Inspección DEX/ZIP dirigida sin143clases JVMtest/diagnóstico/C02/falso ni marcadores
fixture/schema en APKapp; codec/preparadorCP2 presentes. No auditoría universal.
No instalar ni ADB: A56 sigue APKQR02F01, pruebas físicas requieren nuevo permiso.

Evidencia/rutas/órdenes/hashes completos: pruebas/EVIDENCIA_PADRE_PAIR01.md.
Próxima decisión separada snapshot/persistencia compatible o prueba física A56 de
lector, no ceremonia/CP1migración/QRhijo por permisoF01. Contexto hijo/relay reportado
no reejecutado, sin otra tarea automática/dispositivos/repos externos/subagentes/
sesiones/prompts de vuelta. Apartados siguientes son históricos.

## PADRE-QR02-F01 — APK en A56 confirmado; cámara no ejecutada

Base94d1746/códigoff9aa4c, autorización específica recibida y aviso antes de operar.
Primera instalación única -r reportó Success/hash QR02. Propietario cuestionó conexión:
se suspendió atribución; tras nuevo «listo», se confirmó físicamente acceso actual
por cable: desconectar A56→0 A56 disponibles; reconectar→1; modelo y hash APK propio
7f3947394c5bb921a11539a66ed7162029818702d0fc1a3181c53a5b55d6f534 comprobados.
No reinstalación adicional; no inferir retrospectivamente conexión inicial.

Fixture público original renderizado QrLocal768×768 en temporal PC y abierto, sin
firmas nuevas/fixtures cambiados. Propietario confirmó dos veces escáner deshabilitado,
la última tras verificación USB/hash. No cámara/preview/bytes ópticos/semántica/firma
física observados. RecoveryScreen habilita solo con pareja conocida; sin leer store
ni crear pareja ficticia/autoParentConfirm/retirar guarda. Sin permisos concedidos,
APKtest/instrumentación/compilación/tests nuevos o acceso A13/hijo/otros repos.

Resultado APK_DEBUG_INSTALADO_VERIFICADO / CAMARA_NO_EJECUTADA; corrección sintética
QR02 vigente, vínculo PREPARADO intacto. Evidencia pruebas/EVIDENCIA_PADRE_QR02_F01.md;
rutas modificadas: esa evidencia, ESTADO.md y este relevo. Próxima decisión separada:
diagnóstico aislado con permiso específico APKtest/tests propios públicos o desarrollo
de ruta productiva de vinculación; ninguna opción ejecutada/autorizada por F01.
Sin nueva tarea automática/prompts de vuelta. Apartados siguientes son históricos:
«no instalado» corresponde al cierre QR02 anterior, no al estado actual A56.

## PADRE-QR02 — implementación corregida, PREPARADO_FISICO

Base75eb56e, commit código **ff9aa4c**. Evidencia pruebas/EVIDENCIA_PADRE_QR02.md.
Reproducido primero fixture público original sin nueva firma/muestreo:0° falla,
90/180/270 pasan. QrLocal productivo ahora QRCodeReader geométrico/UTF8/TRY_HARDER,
hasta4 orientaciones plano RGB/Y, presupuesto cooperativo400ms antes de otro scan
(no interrumpir scan síncrono). Sin PURE productivo, máscara/contenido alterados,
checksum convertido en texto, dependencias/cámara/UI/codec/CP1 nuevos.

9 tests regresión normales+1 estrategia;48 variantes/96 lecturas RGB/Y,4 rotaciones,
cizalla/traslación acotadas, límites/noQR/dañado/múltiple candidato/texto inválido/
firma mala, bytes exactos+crypto SPKI real ficticio. Test fijo diagnóstico ahora exige
éxito productivo, mantiene baseline geométrico; original P07 intacto/incluido.
Suite fixture142app+30core+1interop=173 ejecutados distintos; normal141app. Cero
fallos/errores/omitidos. Sin opt-in muestreo aleatorio ni reruns hasta verde. Builds
debug/testAPK/release unsigned y lint debug/release correctos (23warnings versiones
cada uno,0errores); sin instrumentación real/Node/relay nuevos. Seis comandos Gradle
ejecutados documentados, final APK reconstruido tras commit código.

Hashes finales: debug7f3947394c5bb921a11539a66ed7162029818702d0fc1a3181c53a5b55d6f534;
release unsigned3025fedfea1634c843ce89842e85654ceda9d8955c95e92b4b6274bf78951857;
testAPK2074540e73558d22ea5241c108ff432ad125d77a0839a16caa29a0f53931be25.
Rutas/procedencia/hash previo diferentes en evidencia; no APK instalado. DEX/ZIP
de dos APK app:477 propias/1QrLocal,0 coincidencias con141 clases JVMtest/C02/
diagnóstico/falso,0 marcadores fixture público. Inspección dirigida, no auditoría
universal/identidad/Keystore/privadas reales. Fixture y evidenciaQR01 intactos.

Resultado FIX_VALIDADO_SINTETICO/PREPARADO_FISICO: no aprobación cámara/A56/vínculo/
APPLIED/consumo/CP2. A56 requiere aviso y autorización específicos antes de instalar
o probar, primera prueba solo fixture público PC y rol/tipo puede ser rechazado por
UI correctamente. A13/hijo excluidos, sin dispositivos/ADB/CA/LAN/push/despliegue/
sesiones/subagentes/prompts de vuelta. PREPARADO no promovido. Contexto LOCAL01–08/
45f7a44 y cambios POSTC08 reportados por Render, no reejecutados ni integrados aquí:
NORMAL10s/excepción OFFLINE explícita/boot/DNSVPN no cambian CP1 por este fix.

Rutas: QrLocal.kt, DiagnosticoQrTest.kt, EstrategiaQrTest.kt, QrLocalRegresionTest.kt,
pruebas/EVIDENCIA_PADRE_QR02.md, este relevo y ESTADO.md. Cierre documental por mensaje
«PADRE-QR02: registrar regresión y APK preparados para prueba física»; hash devuelto.
Esperar revisión/autorización específica, no otra tarea automática. Secciones
siguientes históricas: donde QR figura NO corregido corresponde a esos cierres previos.

## PADRE-C04-R1 — borrador integrado tras revisión hijo/relay y decisiones13oct

Base1582e66. Se consolida **el mismo documento**
padre/BORRADOR_CP2_ORDENES_Y_TIEMPO.md, sin contrato/schema/codec nuevos.
Fuentes hijo HIJO-C07 8b25c24 y revisión relay13oct íntegras suministradas por
coordinador en conversación (3+1 partes), no lecturas externas de esta sesión.
Decisiones finales asignación13oct prevalecen; no se verifican archivos ajenos.

Confirmados incorporados, sin reabrir opciones: ALLOW libre LOCAL/REMOTE, LOCK
inmediato, ALLOW_FOR1min..24h en minutos enteros sin preselección (durationSec60..86400
múltiplo60); sin bloqueo temporal/apertura/campo/fase correspondiente. Contador de
autorización transcurrida incluye apagado/Keyguard; ambas fuentes offline/reboot
bloqueado/última acción/adminRAM/emergencia. Historia agregada varios días, hasta8
fechas domingo21→domingo21 de zona LOCAL configurada, catch-up y purga conservadora
aceptados sin cancelar permiso. Guardas básicas/código5–10/pregunta/apps normales
siguen; escapes exhaustivos pospuestos no inviolabilidad/retiro de barreras.

STATE integra permittedTime.days cerrado, retentionSeq/historyRevision separados
de controlRevision/seq; total solo ventana retenida, calidad/nulos/cortes/calendario
cerrados, materialización/readback y overflow sin reset. Purga preserva ledger activo,
deadline/identidad/seq/pending/consumos/esperas; lastExpiry solo ventana vigente con
detectedElapsedMs/retentionSeq. Padre tombstone offline sin totales ni generation
hijo inventada; sync/cutoff/barreras impiden resurrección por STATE/backup/push viejo.
ACK histórico no reemplaza estado ni historia; firma no frescura/APPLIED físico.

Push SÍ evaluación mínima hint CONSULT_STATE, sin contenido factual/historial/
órdenes y sin provider/token/endpoints/servicio/permisos implementados. Sin reporte
causa desconocida, no inferir internet/humano de PONG/presencia. Limpieza previa al
aviso/sync, sin garantía con app muerta. Historial no servidor/push ni cupo diario.

§10 propone mínimos cerrados de CHILD_OFFER/PAIR_ACCEPT perfilv2 y OFFLINE reto/
respuesta ligados boot/revisión/acción, mismos propósitos limitados. Alta usa hash
CPv2/RECOVERY propuesto; legado conserva CPv1/RECOVERY y consumo/esperas identificados
localmente, nunca MESSAGE/OFFLINEv1 como autoridadv2. Readback/ceremonia/capacidades
y recuperación segura bloquean corte de versión si faltan. Sin recibos exactos
adicionales/reset remoto/credencial transmitida/generación libre. Tres roles aún
deben aprobar formatos/perfil/migración antes de freeze/sync.

§14 registra deltas y candidato matemático derivado de inventario/tipos (STATE91
miembros/84escalares/15objetos/array8): payload7116/frame9788; límite general8192
da candidato frame11224. PAIR candidato1355/2108, offer524, OFFLINE731/1276.
Son **derivaciones manuales condicionales, no cotas probadas de codecs**; revisión
independiente/fronteras pendientes. Relay2966payload/4122frame/profundidad4 es solo
ejercicio sintético inválido reportado, no reejecutado aquí/crypto/cota formal.
No aumentar8192/16384. QR a8e4ffc reproducido NO corregido, ninguna lectura garantizada.

Restantes pocos bloques §12: ratificación técnica DST/ancla/zona; PAIR/OFFLINE/
recuperación/versionado conjunto; diseño push emisor/proveedor/privacidad/permisos
posterior. KDF/normalización/admin/cupos/reset/olvido doble siguen privados/técnicos
sin elección por campos. Mapa laboratorio puro de ledger/retención **distinto L02**
separado de implementación productiva/migración/evidencia física; H05/H06/puertas
no activados. CP/1 intacto, CP/2/cp.v2 todavía provisionales.

Rutas propias: borrador consolidado, INTEGRACION_FUTURA.md, este relevo y ESTADO.md.
Lectura local/revisión documental/Git(diff --check, preparado/estado), sin tests/
builds/scripts de tamaño/Android/APK/UI/Keystore/notificaciones/KDF/WSS/productivo/
C02/dispositivos/ADB/CA/LAN/git push/despliegue/repos externos o QR02. Sin sesiones/
subagentes/prompts de vuelta. Commit «PADRE-C04-R1: consolidar borrador de control y
retención semanal»; hash devuelto al cierre. Esperar revisión/coordinador antes de
freeze/sincronización/código; no iniciar otra unidad. Secciones siguientes históricas.

## PADRE-C04 — draft técnico de órdenes/STATE/ACK y tiempo, no versión aprobada

Base984693c. Borrador incremental: padre/BORRADOR_CP2_ORDENES_Y_TIEMPO.md.
Contexto hijo C04 4636d30/C05 94b1bbe/C06 ae1c7eb/L02 c6e8d5d recibido del
coordinador, sin lecturas externas ni reejecución. L02 reporta22 nuevos/199 debug
ejecutados, núcleo169 UP-TO-DATE; no modelo productivo/métrica humana acreditada.

Requisitos nuevos recogidos: código numérico5–10 elegido/ceros iniciales, pregunta
local offline para código nuevo sin revelar anterior/no código extra obligatorio;
todas apps normales/HOME propio/instalar-actualizar permitido y protección básica
Ajustes/admin/desinstalar hijo; LOCAL tiempo elegido sin default30min; ambas
fuentes conservan permiso offline; última acción confirmada/contexto nuevo tras
conflicto, boot bloquea. RAM administrativa separada; emergencia/Keyguard intactos.
Revisión exhaustiva escapes pospuesta, no retirar guardas ni prometer inviolable.

Draft define tabla exacta de perfil/sobre/dominios/bytes/tipos/rangos/causas/rechazos;
AUTH/SYNC/SET_POLICY con revisión global distinta de seq y desafío ligado operación;
STATE efectivo/fuente/boot/revisión/vigencia separado de última remota id/seq/hash/
resultado; ACK solo resultado, no snapshot actual. Duplicado no reaplica ni renueva,
UNKNOWN_COMMAND no resuelve pendiente. Rango60..604800 y LOCK_FOR con apertura
finita elegida/contextual propuestos NO acordados; ninguna función/botón eliminado.

Tiempo: snapshot absoluto/subtotal/calidad, no sumas de duration ni deltas/historial,
ni UsageStats/interacción humana. Recomienda autorización transcurrida incluso
pantalla apagada/Keyguard, **no aprobado**; deadline no se pausa. Reconstruir mismo
boot solo con permiso durable y continuidad probada, no política L02 de gap ciego;
reboot/gap no cuantificable conserva subtotal PARTIAL/null fiable, nunca cero ficticio.
Día parametrizado/política de zona propuesta, un bucket actual y último agotamiento
mínimo: sin decisión previa supuesta. Deadline agotado distinto de bloqueo aplicado.
Aviso anterior y permiso posterior separados; cerrado padre no garantía WSS/push.
Sin reporte causa desconocida, no deducir internet hijo de timeout/presencia/PONG.

Cuatro elecciones bloqueantes §12: base contador/pantalla; día-zona/retención;
entrega avisos/último agotamiento; rango/apertura LOCK_FOR. Pregunta/KDF/admin/
normalización/cupos/reset/olvido doble siguen privadas/técnicas sin aprobación.
Ningún secreto/pregunta/salt/verificador/catálogo/ubicación en protocolo/relay;
no nuevas rutas remotas reset/recibos exactos. Identidad/pairId/snapshot compatible,
migración física preserva seq/reservas/pending v1/consumos/esperas/incertidumbre,
no autorización/código fabricados, fallback o promoción por STATE/versión/presencia.

Incluye trazas documentales no fixtures, matriz de invariantes/tests futuros por
rol y mini mapa revisable; no modifica PLAN/ADR aceptados ni aprueba H05/H06/puertas.
CP/2 y cp.v2 son provisionales; PAIR/OFFLINE/recuperación de transición necesitan
revisión conjunta antes de migrar. Coordinador revisará ambos clientes/relay para
acuerdo y sincronización; no implementación/asignación siguiente automática.

Rutas propias: borrador nuevo, nota incremental C03, INTEGRACION_FUTURA.md, este
relevo y ESTADO.md. Verificación solo lectura/revisión documental y Git(diff --check,
preparado/estado); cero tests/builds/codec/CP1/schema/fixtures/C02/main/UI/Android/
notificaciones/dispositivos/ADB/CA/LAN/push/despliegue o repos externos modificados.
Sin agentes/sesiones/prompts de vuelta. QR a8e4ffc reproducido NO corregido; no QR02.
Commit cierre «PADRE-C04: proponer contrato de órdenes y tiempo permitido» y hash
devuelto tras commit; detener y esperar revisión sin ejecutar mini mapa.

## PADRE-C03 — revisión del control local del adulto, solo documental

Base padre a8e4ffc; HIJO-C03 9ae6c99 y documentos centrales consultados solo en
lectura. Resultado: compatible como propuesta con precisiones, **sin aprobación
de opciones, versión CP/2, puertas u orden**. Revisión completa y decisiones en
padre/REVISION_CONTROL_LOCAL_ADULTO.md; propuesta registrada en INTEGRACION_FUTURA.md.

Acuerdos propuestos: administración RAM distinta de permiso normal por boot;
credencial reutilizable propia de instalación y esperas independientes de códigos
CP/1; emergencia/Keyguard independientes. Última acción fresca serializada puede
permitir LOCK remoto posterior y luego permitir local posterior, previa decisión
del propietario. Revisión global distinta de seq remoto; desafíos/confirmaciones/
timers viejos quedan invalidados, incluida apertura diferida de LOCK_FOR. Elegir
regla para fuente REMOTA/red sin pisar LOCAL offline ni revivir permiso revocado.

Padre debe separar modo efectivo/fuente/revisión/vigencia de STATE fresco de
resultado de última orden remota id/seq/hash. ACK tardío no reemplaza estado nuevo;
modo local/revisión mayor no inventan APPLIED. Preservar pendientes/reservas cuando
resultado sea desconocido. ControlPoliticas/ParentStore/presentación CP/1 actuales
no bastan para coexistencia nueva y no fueron modificados. Ningún código maestro,
salt, verificador o QR reutilizable en padre/relay; ningún recibo exacto de pairing/
consumo añadido automáticamente.

Elecciones para propietario (con recomendaciones razonadas en revisión): duración
normal/timers; RAM/ciclo de vida; precedencia/red remota; tipo de credencial/esperas
y evaluación KDF; rotación/olvido/cambio de adulto; apps aprobadas frente a uso más
amplio; versión/migración presencial. 30 min, 5 min, 26 caracteres, KDF/coste y
recuperación dedicada **no aprobados**. Apps aprobadas es recomendación a confirmar,
no restricción ya aceptada del requisito nuevo. No prometer coste/antirollback físico.

Delta mínimo por rol y mapa pendiente: acuerdo → versión/ADR/puertas → modelos/
persistencia/migración → ceremonia/identidad → C1 real antes de endurecer → quiosco
autorizado y regresión → WSS/reconciliación/C2 según dependencias revisadas. No cambia
planes ni cierra H05/enforcement/H08/H10/H11; H05-L01/PADRE-C02 no acreditan cambio.
Migración conserva identidad/seq/consumos/esperas/incertidumbre, sin fabricar código
o permiso ni downgrade CP/1 con semántica nueva. No promoción por STATE/presencia.

Evidencia: lecturas y revisión documental; git diff --check y revisión de preparado,
sin tests/builds/mediciones físicas nuevas. Rutas propias: revisión nueva, nota en
diseño de acreditación, INTEGRACION_FUTURA.md, este relevo y ESTADO.md. Commit de
cierre por mensaje «PADRE-C03: revisar control local y transición de versión»;
hash entregado al cierre. Sin código/contrato/fixtures/plan/ADR aceptados o repos
externos modificados; sin dispositivos/ADB/Keystore/LAN/push/despliegue/subagentes.
PADRE-QR01 a8e4ffc reproducido NO corregido; QR02 pendiente de asignación aparte.
Esperar decisiones/revisión/asignación; sin prompts de vuelta ni siguiente tarea.

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
