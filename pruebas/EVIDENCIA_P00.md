# Evidencia P00 — navegación y estados

Primer bloque: `37c74a3`; cierre en el commit posterior. Herramientas fijadas
en BUILD_ENV.md; CP/1, esquema y fixtures se mantienen intactos.

## Implementación

Cuatro pantallas Compose: Primer inicio, Vincular, Control y Recuperación.
ParentViewModel expone StateFlow y recorrido simple con retroceso, sin firmar ni
abrir sockets. MainActivity recoge el estado con lifecycle; AppContainer inyecta
la fuente no operativa de producción por constructor. El falso existe únicamente
en soporteTest y se compila en JVM/instrumentados, nunca en la app.

Se presentan vínculo, relay, presencia indicativa, evidencia/antigüedad e intención
pendiente por separado. Hijo ausente sin evidencia no equivale a bloqueado. Al
desconectar se muestra última evidencia, no estado actual. La intención pendiente
no modifica evidencia ni acredita APPLIED. P05 tendrá que construir este resumen
desde evidencia autenticada/correlacionada y calcular frescura/antigüedad.

Las cuatro acciones y escáneres están deshabilitados. El botón configurar solo
abre el recorrido y lo avisa expresamente. No crea autoridad/vínculo, no solicita
permisos ni toca dispositivos. Recuperación informativa permanece accesible sin
vínculo ni conexión; no genera códigos ni autoriza mantenimiento/retirada.
El contenido tiene scroll por pantalla y el acceso inferior respeta insets del
sistema. La validación física de disposición/accesibilidad está pendiente.

## Pruebas realmente ejecutadas

- `scripts/verificar.ps1`: correcto; npm ci/check/build/test correctos, 30 tests
  Node aprobados, diez vectores y 78 comprobaciones documentales correctas.
- `gradlew.bat :app-parent:testDebugUnitTest :core-protocol:test
  :core-protocol:testInterop :app-parent:assembleDebug
  :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease
  :app-parent:lintDebug --rerun-tasks --console=plain`: correcto, 130 tareas
  ejecutadas. Reportes XML: 9 pruebas app, 30 núcleo y 1 interoperabilidad;
  cero fallos/errores. Total con Node: **70 pruebas locales aprobadas**.
- Lint: cero errores, 15 avisos de versiones nuevas, sin suprimir comprobaciones.
- Release sin firma de distribución compilada: no es entrega operativa aprobada.
- ZIP/DEX inspeccionados para debug/release: no fixture ni identificadores
  RepositorioPadreFalso, EstadoPantallaTest o cp.test.crypto. Esta inspección es
  limitada y no reemplaza análisis completo de seguridad.

Ocho pruebas JVM nuevas validan estado desconocido, conexión/presencia sin
evidencia, pérdida de frescura, antigüedad negativa, intención independiente,
recorrido/retroceso, publicación del repositorio y fuente no operativa.
Primera ejecución de dos tests ViewModel falló porque se restauraba Dispatchers.Main
antes de liberar el modelo. Se corrigió con ViewModelStore.clear antes de
resetMain y se repitieron con éxito; no se ocultaron errores de APIs Android.

npm reportó cero vulnerabilidades y un aviso de postinstall de esbuild sin
autorización individual. No se alteraron permisos de instalación.

## Pendientes y límites

Cinco nuevas pruebas UI compiladas: recorrido sin autoridad/órdenes, recuperación
desde primer inicio, hijo ausente, desconexión conservando evidencia/intención y
aceptación pendiente sin mostrar aplicada. Se usan exclusivamente datos ficticios.
En total hay diez tests Android compilados (incluidos los cinco anteriores),
**ninguno ejecutado**. P00 queda completada localmente y pendiente de aceptación
UI Android; P-UI-01/P-UI-02 no se marcan aprobadas.

Pendientes: teléfono del padre Android 11+, habilitación expresa de ADB, ejecución
connectedDebugAndroidTest y revisión visual con Atrás del sistema, rotación y
texto grande. No se usó teléfono, ADB, Render ni otro repositorio. No se acreditan
restricciones reales, autoridad Keystore de producción, biometría ni G0–G4.
El recorrido no persiste tras muerte del proceso; vuelve al primer inicio.

Siguiente tarea: P01, identidad y autenticación. No implementada en este turno.
