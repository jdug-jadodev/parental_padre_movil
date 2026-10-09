# Relevo — P01

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
