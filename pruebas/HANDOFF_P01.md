# Relevo — P01

## Actualización cancelación A56

Modalidad cancelar autorizada y aprobada: un test sin fallos, 21.311 s. El prompt
cancelado no autorizó firma; pública temporal conservada, alias eliminado al
terminar. Solo A56 seleccionado explícitamente; A13 sin operaciones.
No repetir cancelación por las referencias históricas siguientes. Próximo paso:
acordar aceptar con PIN/contraseña; huella/caducidad y cancelación ya aprobadas.
Persistencia tras reiniciar proceso y revisión manual todavía pendientes.

## Actualización interactiva A56

El propietario autorizó aceptar: prueba real aprobada, un test sin fallos en
76.324 s. Prompt oficial confirmado físicamente, firma con clave protegida temporal
verificada y, tras 61 segundos, rechazo Keystore por UserNotAuthenticatedException
y rechazo de sesión por caducidad. Sin simulación ni mensaje al relay; alias temporal
eliminado. El propietario confirmó huella; PIN/contraseña no probado.

Pendiente acordar cancelar físicamente, probar la otra modalidad, persistencia tras
reinicio de proceso y revisión manual. No operar A13: acuerdo obligatorio ya en
ambos AGENTS.md, incluso si está conectado/autorizado. Usar solo selector A56.
Las secciones anteriores de evidencia se conservan como historial, no repetir
aceptar por asumir que sigue pendiente.

## Actualización A56

Lote no interactivo ejecutado con autorización explícita: SM-A566E, Android 16/
API 36. App y APK instrumentado instalados mediante ADB dirigido solo al A56.
Doce tests aprobados, uno interactivo omitido por falta de opt-in, sin fallos;
runner OK (13 tests) no significa trece aprobados. A13 excluido, sin operaciones.
No guardar números de serie ni usar connected contra todos los dispositivos.

Pendientes: acordar respuesta física aceptar/cancelar, caducidad real, reinicio
de proceso y revisión manual. No pedir que habilite otra vez ADB salvo que se
desconecte o pierda autorización. Ambos APK permanecen en el A56. Detalles en
EVIDENCIA_P01.md; lo que sigue es el cierre local anterior.

Estado: implementada localmente, **pendiente de aceptación Android**. Primer
bloque `0f143c4`; cierre local en commit posterior. No iniciar P02 sin resolver
la comprobación física acordada; siguiente paso es habilitación expresa de ADB.

## Código

security/ParentKeyStore: EC P-256 no exportable, alias cp.parent.sign.v1,
autenticación del sistema/biometría fuerte y ventana de 60 segundos. Valida
KeyInfo/SPKI y nunca reemplaza alias incompatible/inaccesible. Separado el alias
reservado cp.parent.metadata.v1; aún no hay archivo cifrado/transacciones.

security/AutoridadPadre integra C02. Firmar exige sesión válida y no genera claves.
SesionAutoridad usa reloj monotónico, invalida background/cancelación y descarta
callbacks viejos/duplicados. LocalAuthentication usa prompt oficial API 30.
MainActivity cierra sesión y cancela en onStop; identidad UI crea/carga de forma
explícita y muestra solo pública. No habilita órdenes ni vincula.

IMPORTANTE: alimentar futuros casos de uso con AutoridadPadre, no llamar al
almacén de bajo nivel para eludir la barrera de interfaz. Keystore impone su propia
autenticación además de la sesión. Una callback correcta no prueba que la clave
siga disponible: la firma real puede fallar y debe exigir autenticar/recuperar,
jamás sustituir identidad. No prometer StrongBox/hardware específico.

## Verificación

scripts/verificar.ps1 correcto y Gradle repetido con --rerun-tasks: 18 tests app,
30 núcleo, 1 interoperabilidad y 30 Node, total 79 locales aprobados. Debug,
instrumentado y release sin firma de distribución compilados. Lint cero errores,
15 avisos. Último ajuste interactivo recompilado. ZIP/DEX no encuentra fake/tests/
fixtures examinados en app debug/release. CP/1 congelado intacto.

Trece tests Android compilados, ninguno ejecutado. Dos nuevos de identidad con
alias temporal y uno interactivo opt-in aceptar/cancelar. El interactivo pide
credencial física, firma y espera 61 segundos para probar caducidad; no inyectar
éxito biométrico. Sin argumento/credencial pueden omitirse: no cuentan aprobados.
Persistencia tras reinicio de proceso no equivale a reabrir almacén y sigue
pendiente. No se usó ADB, teléfono, Render ni otro repositorio.

## Próximo paso y riesgos

Leer pruebas/PRUEBA_FISICA_P01.md y esperar confirmación ADB. Primero lote normal;
acordar interacción física aparte. Validar especialmente credencial sin biometría
y onStop: algunos flujos podrían interrumpirse por cancelación conservadora.
No quitar la barrera para aparentar éxito. No cambiar credenciales, invalidar
claves ni restablecer el teléfono cotidiano. P-KEY-01 y G0–G4 pendientes.

Resultados/límites: pruebas/EVIDENCIA_P01.md y ESTADO.md. Tras aceptación de P01,
P02 vinculación; no implementada en esta sesión. Metadatos cifrados pendientes,
no registrar claves/códigos/QR sensibles en preferencias en claro como atajo.
