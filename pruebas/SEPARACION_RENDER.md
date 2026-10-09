# Separación autorizada del servidor

Origen parental_padre, commit anterior 308c2b1. Destino parental_render, carpeta
vacía comprobada antes del traslado. No se sobrescribió trabajo existente.
Servidor ya comprometido en su repositorio independiente: `4b9309d`, sin remoto
configurado ni push. Cierre del padre en commit posterior.

relay/ trasladado completo al destino, conservando node_modules/dist locales
ignorados. Dieciocho archivos propios verificados por SHA-256 antes/después,
sin editar servidor/modelos/codecs/tests heredados. Contrato/ejemplos y vectores
copiados sin cambios; no se cambió CP/1 ni su algoritmo/semántica. SPEC del
relay/plan original/evidencias C01/C02 copiados como documentación histórica.
El historial anterior sigue conservado en Git del padre; el servidor empieza
como proyecto independiente. No se copió local.properties ni claves Android.

Padre: script sin módulo relay/npm. Interop JVM/Node usa oráculo local estándar,
restringido a payloads/roles/propósitos de fixtures. No valida codec Node de
producción; el cruce original se conserva opcional con -PrutaRelay y apunta al
script heredado del servidor separado. Cinco tests propios de oráculo comprueban
fixture/hash, propósito/rol, impostora/firma alterada, payload arbitrario/campo
extra y firma efímera Node sin privada exportada.

Verificación local del padre correcta: 38 app + 30 núcleo + 1 interop + 5 Node
oráculo = 74. Gradle tests repetidos --rerun-tasks; script completo correcto,
APK debug/instrumentado/lint sin cambios reutilizables. Servidor: script propio,
npm ci/check/build y 30 tests correctos. Diez vectores/78 comprobaciones en cada
proyecto. Interop codec externo también repetido --rerun-tasks, un test/20 cruces
correctos. No hubo ADB, deploy ni nuevas funciones del servidor.

Referencias antiguas a relay/ en evidencias/documentación de especificación son
históricas; plan activo Android sustituye R01–R03 por dependencias externas.
No mover apps al servidor ni desarrollar servidor dentro del padre de nuevo.
Próximo padre: pendientes P02; próximo servidor: R01 cuando se pida aparte.
