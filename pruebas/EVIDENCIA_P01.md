# Evidencia P01 — identidad y autenticación

## Segunda aceptación: credencial solicitada

El propietario autorizó probar PIN/contraseña, sin huella. Se ejecutó únicamente
en A56 el test aceptar: **un test aprobado, cero fallos**, status 0/OK (1 test),
72.282 segundos. Firma protegida verificada y, tras 61 segundos, Keystore y sesión
rechazaron otra firma. Alias temporal eliminado; sin operaciones sobre A13.

El propietario confirmó expresamente que usó **PIN/contraseña, no huella**.
La modalidad se documenta por su confirmación, no por inferencia del runner.
No se solicitó ni registró el valor de la credencial.
Las referencias posteriores a probar credencial corresponden al historial.

## Actualización física: cancelación

El propietario autorizó la modalidad cancelar. Ejecución dirigida únicamente al
A56 con filtro AutenticacionRealTest y autenticacionReal=cancelar: **un test
aprobado, cero fallos**, status 0/OK (1 test), 21.311 segundos.

El adulto canceló físicamente el prompt; no se confirmó autenticación y
AutoridadPadre rechazó la firma con AutenticacionRequerida. La pública temporal
se conservó al reabrir almacén y el alias se eliminó en finally. No hubo firma
alternativa, orden al relay ni operación sobre A13. PIN/contraseña, reinicio real
del proceso y pérdida/invalidación segura siguen pendientes. Las referencias
posteriores a cancelación pendiente pertenecen a etapas históricas previas.

## Actualización física: autenticación, firma y caducidad

Prueba interactiva aceptar autorizada por el propietario y ejecutada exclusivamente
en A56 mediante runner ADB con selector explícito, filtro AutenticacionRealTest y
argumento autenticacionReal=aceptar. Resultado: **un test aprobado, cero fallos**,
status 0/OK (1 test), 76.324 segundos.

El adulto respondió al prompt oficial: la callback de éxito abrió la sesión y
la clave temporal P-256 protegida firmó un AUTH ficticio verificado por CP/1.
Después de esperar 61 segundos, la llamada directa al proveedor rechazó firmar
con UserNotAuthenticatedException y AutoridadPadre rechazó por sesión caducada.
No se inyectó éxito biométrico, no hubo red ni clave alternativa. El alias temporal
se eliminó en finally; no se creó cp.parent.sign.v1 por este test.

El propietario confirmó que usó **huella** en esta ejecución. Credencial del
dispositivo PIN/contraseña no probada; no afirmar que ambas aprobaron. Siguen pendientes
cancelación física, reinicio del proceso, revisión manual y pérdida/invalidación
de clave en entorno seguro. El resultado demuestra el flujo aceptar en este A56,
no compatibilidad universal ni puertas G0–G4.

Se persiste en AGENTS.md y padre/AGENTS.md la prohibición de operar el A13,
aunque esté conectado/autorizado. Solo A56 seleccionado explícitamente; cambiar
esta exclusión requiere nueva autorización expresa del propietario.

## Actualización física: lote no interactivo del A56

El propietario habilitó ADB e identificó el A56 como padre, prohibiendo operar
el A13. Se verificó SM-A566E, Android 16/API 36 y se seleccionó explícitamente
el A56 en todos los comandos de dispositivo. El A13 no recibió shell,
instalaciones ni pruebas. No se registran números de serie en el repositorio.

Instalación `adb -s <A56> install -r` de app debug y APK instrumentado: ambas
correctas. Ejecución dirigida `adb -s <A56> shell am instrument -w -r
dev.controlparental.parent.test/androidx.test.runner.AndroidJUnitRunner`:
**12 aprobados, 1 omitido, cero fallos**, 8.571 segundos.

El runner imprime OK (13 tests), pero el test AutenticacionRealTest informó
status -4 por ausencia del argumento opt-in: no cuenta como aprobado. Los otros
12 informaron status 0: tres de inicio/navegación, tres de presentación, dos de
parser/esquema, dos de criptografía y dos de identidad protegida.

Se demuestra ejecución real del esquema/parser/vectores en Android, firma con
clave temporal no exportable, parámetros de identidad protegida y conservación
de pública al reabrir almacén. No se demuestra aún biometría/credencial real,
caducidad de Keystore, persistencia tras reiniciar proceso, invalidación real
ni revisión manual con texto grande/rotación. No se modificaron credenciales,
biometría, administración ni restricciones; alias de prueba temporales eliminados
por sus finally. App y APK instrumentado permanecen instalados en el A56.

Las secciones siguientes conservan el cierre local previo; sus referencias a
pruebas físicas pendientes se actualizan con este lote, sin dar por aprobadas
las comprobaciones interactivas todavía pendientes.

Primer bloque `0f143c4`; cierre local en commit posterior. **Implementada localmente,
pendiente de aceptación Android**. CP/1, esquema y fixtures intactos.

## Implementación

ParentKeyStore crea/carga explícitamente EC P-256 en `cp.parent.sign.v1`, con
autenticación requerida, SHA-256, propósito SIGN y 60 segundos para credencial del
sistema o biometría fuerte. Comprueba KeyInfo y pública SPKI antes de usarla.
Privada no exportada; no StrongBox obligatorio ni promesa de hardware específico.
Un alias existente incompatible o inaccesible no se elimina/reemplaza; firmar
nunca crea identidad. El alias de metadatos está reservado por separado, **sin
implementar todavía archivo cifrado/transacciones**.

AutoridadPadre integra el firmante con CP/1 y comprueba sesión antes, durante y
después de firmar. Errores invalidan la sesión sin generar alternativas.
SesionAutoridad usa reloj monotónico y exige primer plano: caduca exactamente a
60 segundos, descarta callbacks anteriores/duplicados y bloquea en segundo plano.
Esto es una barrera adicional: Keystore debe imponer su propia autenticación.

LocalAuthentication usa BiometricPrompt oficial API 30, sin autenticadores débiles,
PIN propio ni simulación en producción. MainActivity cancela el prompt/invalida
sesión en onStop. Pantalla inicial permite creación/carga explícita y autenticación,
y muestra solo pública SPKI sin portapapeles/logs. Explorar controles no crea
autoridad. Crear explícitamente identidad y luego cancelar autenticación conserva
la misma clave protegida, no una clave alternativa. Órdenes siguen deshabilitadas.

## Comprobaciones ejecutadas

- `scripts/verificar.ps1`: correcto; npm ci/check/build/test correctos; 30 tests
  Node y diez vectores/78 comprobaciones documentales correctos. Cero
  vulnerabilidades reportadas; aviso de postinstall de esbuild sin autorización
  individual, sin cambiar permisos npm.
- Gradle app/núcleo/interoperabilidad + APK debug/instrumentado/release + lint
  repetidos con `--rerun-tasks`: 130 tareas ejecutadas con éxito.
- XML: 18 tests app (nueve nuevos), 30 núcleo y uno de interoperabilidad, cero
  fallos/errores. Con 30 Node: **79 pruebas locales aprobadas**.
- Lint: cero errores y 15 avisos de versiones. Último ajuste del test interactivo
  compilado nuevamente con assembleDebugAndroidTest y lintDebug correctos.
- Release sin firma de distribución compilada, no entrega operativa aprobada.
- Inspección limitada ZIP/DEX de app debug/release no encuentra fixture,
  RepositorioPadreFalso, AlmacenFalso, IdentidadKeystoreTest, AutenticacionRealTest
  ni alias cp.test.auth/cp.test.identity. No reemplaza análisis de seguridad completo.

Nueve tests nuevos prueban frontera de caducidad, cancelación/background, callbacks
viejos/duplicados, reloj regresivo, reinicio de sesión, ausencia de firma sin
autenticación, adaptación CP/1 y rechazo sin crear alternativas. Sus almacenes y
claves efímeras son exclusivos de tests; **no prueban Keystore/biometría reales**.

## Pruebas físicas pendientes

Trece tests Android compilados y no ejecutados. Dos nuevos de identidad prueban
parámetros/no exportabilidad, reabrir el almacén y no reemplazar alias incompatible.
Un tercero opt-in solicita autenticación/cancelación física del adulto, firma
con clave protegida temporal y comprueba caducidad; jamás inyecta éxito biométrico.
Sin argumento se omite; sin credencial segura los tests pertinentes se omiten.
No contabilizar omisiones como aprobadas.

Reabrir el almacén no prueba reiniciar proceso. Persistencia real, bloqueo al
salir de la app, credencial sin biometría, cancelación real, expiración Keystore
y clave invalidada siguen pendientes. Validar especialmente interacción entre
prompt de credencial y onStop en el fabricante: cancelar conservadoramente podría
interrumpir ese flujo. Si ocurre, registrar/corregir sin eludir autenticación.

No usar el teléfono cotidiano para retirar credenciales, invalidar claves o
restablecer. No hubo ADB, teléfono, Render ni otro repositorio. P-KEY-01 y G0–G4
siguen pendientes. Próximo paso: habilitación de ADB y PRUEBA_FISICA_P01.md.
