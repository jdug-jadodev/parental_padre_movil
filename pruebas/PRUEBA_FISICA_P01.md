# Pruebas físicas del padre — P01 (pendientes)

**Lote no interactivo ejecutado en A56:** 12 aprobados/1 interactivo omitido, sin
fallos. **Interactivo aceptar también aprobado:** firma y caducidad tras 61 s,
un test sin fallos, 76.324 s. El propietario confirmó huella. **Cancelar también
aprobado:** firma rechazada, un test sin fallos, 21.311 s. Segunda ejecución aceptar
solicitando PIN/contraseña aprobada (72.282 s); falta confirmar modalidad física.
Reinicio del proceso y revisión manual pendientes. A13 excluido por ambos AGENTS.md.
Avisar y esperar habilitación expresa de ADB antes de listar
dispositivos, instalar o probar. Usar solo el teléfono del padre, Android 11+.
Si hay varios dispositivos, confirmar el objetivo antes de instalar.
No administrar, aprovisionar, restablecer ni modificar credencial/biometría.

## 1. Lote no interactivo

Tras autorización, ejecutar `:app-parent:connectedDebugAndroidTest`. Hay 13 tests;
Con varios teléfonos conectados, no ejecutar connected sin filtro verificado:
instalar y ejecutar el runner por ADB con selector explícito del A56, como se
hizo en este lote. Nunca instalar/probar en el A13.
el interactivo se omite sin argumento. Los dos de identidad exigen bloqueo seguro;
registrar cualquier omisión por separado, no como aprobada. Cada test elimina solo
su alias único cp.test.*, nunca cp.parent.sign.v1.

Comprobar visualmente recorrido, botones deshabilitados, recuperación accesible,
texto grande, rotación y Atrás. No inferir bloqueo del hijo por presencia ausente.

## 2. Autenticación física (acordar cada modalidad)

Filtrar AutenticacionRealTest y aportar explícitamente el argumento acordado:

```powershell
.\gradlew.bat :app-parent:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=dev.controlparental.parent.AutenticacionRealTest" "-Pandroid.testInstrumentationRunnerArguments.autenticacionReal=aceptar"
```

El comando Gradle anterior solo se usa con selección de dispositivo verificada.
Con A13 conectado, preferir runner ADB ya instalado con `-s <A56>` y argumentos
`-e class dev.controlparental.parent.AutenticacionRealTest -e autenticacionReal
aceptar` (o cancelar). Acordar cada modalidad con el adulto antes de iniciarla.

El adulto responde al prompt en 120 segundos con credencial del dispositivo o
biometría fuerte. Después espera 61 segundos **sin autenticar/desbloquear de nuevo**
para comprobar expiración Keystore y sesión. Mantener visible la app; no permitir
que el teléfono se bloquee durante esa espera. El test no cambia timeout del sistema.
Si la app pasa a segundo plano, el test bloquea/cancela y puede fallar; registrarlo.
Si no se puede sostener ese entorno, no afirmar validación de caducidad.

Repetir con `autenticacionReal=cancelar`, cancelando físicamente el prompt. Una
omisión, cancelación en modalidad aceptar o falta de respuesta no es aprobación.
No usar herramientas que simulen éxito biométrico. Las claves del test son
temporales, sus mensajes AUTH ficticios no salen del dispositivo.

Probar credencial del sistema incluso si hay biometría; registrar por separado
cada modalidad realmente probada. Si abrir credencial provoca onStop y cancela
el flujo, detener esa vía y corregir conservando autenticación/bloqueo. No dar
por aprobado el flujo del fabricante porque compile.

## 3. Persistencia y ciclo de vida reales (manual)

- En Primer inicio, pulsar Crear o cargar identidad protegida. Comparar la
  pública en el propio teléfono, sin guardar claves/capturas en el repositorio.
- Cerrar el proceso y abrir de nuevo **sin borrar datos ni reinstalar**. Acordar
  antes cualquier force-stop por ADB. Cargar de nuevo: pública idéntica, sesión
  bloqueada y sin creación de clave distinta.
- Autenticar, ir a Inicio del sistema y regresar: aviso de autoridad bloqueada,
  exige nueva autenticación. Probar también cancelar y rotar durante prompt.
- Recuperación informativa debe seguir accesible si no se puede autenticar.

Invalidación de claves/retirada de credencial solo en banco de pruebas separado
y con autorización específica; nunca retirar bloqueo seguro del teléfono diario.
P-KEY-01 no queda completamente aprobada sin cubrir pérdida/invalidación segura.
Registrar resultados, omisiones, fabricante/API y pendientes en ESTADO.md; no
incorporar pública de familia, códigos ni credenciales reales a documentación.
