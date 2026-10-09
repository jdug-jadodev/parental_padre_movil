# Relevo — C00

## Resultado comprobado

Base del proyecto padre y relay implementada. Commit de acuerdos: `4cc7a39`;
commit de implementación: `e25bff5`; cierre y correcciones en el commit posterior.
Consultar `git log` para su identificador. Primer bloque: 484 líneas manuales de
código, pruebas y configuración; wrapper y lockfile generados no se cuentan.

`scripts/verificar.ps1` terminó correctamente: una prueba del núcleo, una de la
app, seis del relay, compilación de ambos APK debug y de TypeScript, lint y
78 comprobaciones de diez vectores documentales. Lint: cero errores y nueve
avisos de actualización de dependencias. No se suprimieron validaciones.

El ejecutable compilado del relay arrancó en loopback y respondió 503 en
`/healthz`. No hay sockets operativos; upgrades e inicio en producción se
rechazan. No desplegar todavía en Render. El simulador de hijo existe solo
en tests; no acredita Device Owner, políticas ni aplicación de restricciones.

## Archivos e interfaces

Módulos `app-parent/`, `core-protocol/`, `relay/`; wrapper y catálogo en `gradle/`;
scripts de verificación, README, BUILD_ENV y ESTADO. `local.properties` es local
y no está versionado. Solo se expone VERSION en el núcleo y las funciones de
servidor local/puerto en el relay. Modelos, parser y criptografía aún pendientes.

## Incidencias resueltas y pendientes

El primer intento Android excedió 120 segundos. El segundo detectó un error de
escape del disco Windows en local.properties y falta de icono. Se corrigieron
y se repitió toda la verificación con éxito.

Python no pudo validar el esquema porque falta jsonschema. No se instalaron
paquetes globales. No se usó ADB, ningún teléfono ni Render. La prueba de pantalla
está compilada, pendiente de habilitación del teléfono del padre (Android 11+).
No aprovisionar ni restablecer: esta prueba no necesita administración.

## Próximo paso

C01, leyendo AGENTS.md, ESTADO.md y su sección de
`compartido/03_INICIO_TECNICO.md`, además de las secciones precisas del contrato,
esquema y fixtures. Mantener CP/1 congelado y no crear el APK del hijo.
SPEC-P P2 y ADR-P-001 conservan referencias antiguas a monorepo; seguir el alcance
independiente explícito del paquete. Los acuerdos del propietario están en
AGENTS.md: español claro, commits por bloques de 500 líneas manuales y aviso
antes de usar un dispositivo por ADB.
