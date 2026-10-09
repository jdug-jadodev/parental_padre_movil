# Relevo — P00

## Resultado

P00 completada localmente, pendiente de UI Android. Primer bloque `37c74a3`;
cierre en commit posterior. Cuatro pantallas navegables, estados separados y
recuperación informativa accesible. Cuatro acciones visibles/deshabilitadas;
ningún endpoint, clave, vínculo ni orden real. No hubo ADB ni despliegue.

## Estructura

MainActivity obtiene ParentViewModel desde AppContainer/factory manual y recoge
StateFlow con lifecycle. Modelos y recorrido en presentation; interfaz y fuente
no operativa en data; Compose en ui. DatosPadre tipoEvidencia solo modela
presentación: P05 debe garantizar firma, correlación y frescura al alimentarlo.
No conectar directamente a payloads sin verificar. Evidencia pasada permanece
como última conocida tras desconexión; intención independiente nunca crea APPLIED.

RepositorioPadreFalso vive en app-parent/src/soporteTest/kotlin, incluido solo
en tests JVM/androidTest. Producción usa RepositorioPadreNoOperativo con datos
desconocidos. No cambiarlo por un fake en release. No existe historial ni timer
de interfaz. Recuperación informa límites y pérdida del padre, no firma QR.

## Comprobaciones

Verificación completa correcta; repetición explícita Gradle --rerun-tasks correcta.
30 tests núcleo, 9 app, 30 Node y 1 interoperabilidad: 70 aprobados. APK debug,
instrumentado y release sin firma de distribución compilados. Lint cero errores
y 15 avisos de versiones nuevas. Inspección limitada ZIP/DEX no encuentra fixture
ni fake/código de prueba examinado en app debug/release. Evidencia detallada:
pruebas/EVIDENCIA_P00.md. Dependencias Lifecycle 2.9.1 y coroutines 1.10.2 fijadas.

Hay diez tests Android compilados, no ejecutados: tres inicio/navegación, tres
estados, dos parser, dos criptografía/Keystore. No acreditar UI ni hardware por
compilación. Esperar habilitación expresa de ADB antes de instalar o ejecutar.
No requiere administración, restablecimiento ni integración con el hijo.

## Próximo paso

P01: PLAN_PADRE P01, SPEC_PADRE P4 y CP/1 §4. Identidad no exportable, bloqueo
seguro y autenticación del adulto. Mantener una tarea por turno y commits por
bloques de 500 líneas. La aceptación UI P00 sigue pendiente del teléfono del padre.
No se inició ninguna implementación de P01. G0–G4 siguen pendientes.
