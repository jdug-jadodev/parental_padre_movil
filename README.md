# Control parental — aplicación del padre

Proyecto Android nativo en Kotlin. Este repositorio
solo implementa el rol del padre. El APK del hijo pertenece a otro
proyecto. La copia del protocolo CP/1 en `compartido/` permanece congelada.

## Separación del servidor

Por acuerdo del propietario, este proyecto es solo de la **app del padre**.
El servidor Render se desarrollará aparte en
`C:\Users\Usuario\Documents\parental_render`. Su código y tests se trasladaron
a `parental_render/relay/`; contrato/fixtures copiados sin modificaciones.
No hay servidor ni dependencias npm en este proyecto. Las especificaciones/evidencias
antiguas se conservan como historia, no como tareas de desarrollo del servidor aquí.
El cliente Android WSS y CP/1 siguen formando parte de la app.

## Qué hay implementado (C00–C02, P00–P06 local parcial)

P06 incorpora pantalla de controles con duración 60–604800 s, advertencias y
confirmación explícita separada del guardado. Una selección offline se conserva
cifrada sin firmar/reservar seq; reemplazar exige confirmación y nunca sustituye
silenciosamente una orden pendiente. Formato privado v3 migra v1/v2 conservando
contador/comando/evidencia/aceptación, sin cambiar CP/1. Vínculo PREPARADO no
habilita acciones; recuperación presencial P07 sigue pendiente.
La UI muestra modo/causa reales de evidencia firmada: APPLIED no significa
permitido si el hijo sigue bloqueado por red/Android. Desconectar degrada a última
evidencia; el tiempo reportado no se descuenta como confirmación remota.
132 pruebas locales correctas; cinco tests de pantalla compilados/no ejecutados.
No se ha demostrado control real de otro teléfono. Ver pruebas/EVIDENCIA_P06.md.

P05 añade sincronización STATE firmada, confirmación de intención no reutilizable,
reserva/firma/persistencia antes de enviar y correlación de ACK por id/seq/hash
del payload/arranque/sesión. ACK perdido permite solo dos reenvíos idénticos,
después estado fresco; reconexión/reapertura nunca reproduce una orden antigua.
Pendiente de aplicación, fallo y rechazo no se confunden con APPLIED. Evidencia
y resolución se guardan atómicamente. Red validada y PONG vigente son puertas
independientes de envío; la nueva conexión sana solicita sincronización.
119 pruebas locales correctas en ese bloque; no aplicación real demostrada. Allí
los controles finales quedaban para P06 y guardas de vínculo/recuperación; PREPARADO no
se promociona aquí. Ver pruebas/EVIDENCIA_P05.md.

P04 incorpora cliente WSS del padre con OkHttp, TLS/hostname del sistema y cp.v1,
reto AUTH firmado con autoridad Keystore y latidos correlacionados. En Controles,
autenticar y conectar son acciones explícitas con vínculo guardado; no crea una
pareja para probar red. Fondo/salida de Controles cierran el canal, sin servicio
oculto. Red validada, canal autenticado/latido y presencia son señales separadas;
ninguna acredita restricciones del hijo. Las órdenes siguen deshabilitadas.
97 pruebas locales correctas; pruebas físicas de este cliente y autenticación
con servidor externo pendientes. Ver pruebas/EVIDENCIA_P04.md. El padre todavía
no está terminado ni conectado al hijo real.

- `app-parent/`: navegación en español e identidad/autenticación Keystore implementadas, pendientes de validación física; sin vinculación real ni envío de órdenes.
- `core-protocol/`: modelos, parser estricto, esquema congelado y serialización determinista CP/1, sin dependencias de Android.
- `pruebas/interoperabilidad.mjs`: oráculo de pruebas criptográficas usando Node estándar; no servidor ni codec de producción.
- `scripts/verificar.ps1`: compilación y pruebas locales, sin acceso a dispositivos.

C01 añade modelos y validaciones Kotlin CP/1; su contraparte Node se conserva
en el proyecto separado del servidor. Ambos conservan los diez vectores comunes.
Se controlan UTF-8, ASCII, duplicados, límites, secuencias, base64url, formatos
SPKI/DER, propósito y coherencia de los campos del estado.

C02 añade firma SHA-256/ECDSA DER en Kotlin mediante un firmante inyectado y
verificación estándar en JVM/Node sobre los bytes originales. La privada del
padre y su autenticación usan en P01 Android Keystore. El relay
no firma ni recibe privadas.

`leerPayloadSinVerificar` sigue siendo solo estructural. Para verificar firmas
se usa `CriptografiaProtocolo.verificarSobre` con propósito esperado y pública
de configuración/vínculo confiable, nunca una pública elegida por el comando.
`DocumentoVerificado` no acredita frescura, autorización contextual ni `APPLIED`:
esas comprobaciones corresponden a los casos de uso futuros. Ninguno de estos
datos se usa todavía para confirmar ni aplicar órdenes.

P00 separa conexión al relay, presencia indicativa del hijo, última evidencia e
intención pendiente. Las cuatro acciones están visibles y deshabilitadas. La
recuperación informativa es accesible desde todas las pantallas, sin conexión ni
vínculo. Explorar el recorrido no crea autoridad. El repositorio de producción es
no operativo y muestra datos desconocidos; el falso se compila solo en tests JVM
e instrumentados. P05 deberá alimentar evidencia autenticada/correlacionada y
calcular su antigüedad/frescura; P00 no implementa ese cálculo ni un reloj remoto.

P01 añade creación/carga explícita de EC P-256 no exportable, autenticación del
sistema (credencial o biometría fuerte), ventana de 60 segundos y bloqueo de
interfaz al ir a segundo plano. Un error no sustituye claves ni habilita firma
alternativa. Se muestra exclusivamente pública SPKI, sin copiarla al portapapeles.
El alias de cifrado de metadatos es distinto del de firma. El botón del recorrido no crea
identidad: hay un botón explícito separado. Las órdenes siguen deshabilitadas.
P01 se comprobó parcialmente en A56: huella, PIN/contraseña, cancelación,
caducidad y persistencia de identidad temporal entre procesos. Ver EVIDENCIA_P01.md
para límites; el A13 permanece excluido.

P03 añade ParentStore: archivo privado atómico AES-256-GCM, nonce nuevo por
escritura y una coroutine escritora. Contador y única intención se persisten
juntos; cancelar conserva contador consumido y evidencia. Recuperar no envía
ni reenvía órdenes. Datos corruptos/clave ausente fallan sin generar alternativas.
P02 conecta almacenamiento con el flujo de vínculo preparado; P05 debe integrarlo
con órdenes y contexto autenticado. Obtener el almacén no inicializa datos.
El formato es local/versionado; no cambia CP/1 ni guarda privadas/códigos.

P02 lee CHILD_OFFER desde una imagen QR local elegida con selector oficial, sin
permisos de galería completa ni servicios remotos. También incorpora CameraX
local con permiso CAMERA al abrir escáner; no guarda fotos, filtra CHILD_OFFER
y cierra al salir/pausar. Cámara real/permiso/lifecycle pendientes de probar en A56.
En
Vincular prepara códigos independientes para guardar fuera del teléfono, huellas
y PAIR_ACCEPT firmado tras autenticación. Códigos solo en memoria, descartados
al salir/segundo plano; FLAG_SECURE protege la ventana. QR firmado/env pública
copiable recuperables al reabrir, sin recrear códigos. Formato privado v2 migra v1
sin reiniciar contador/intención/evidencia. Permanece PREPARADO, sin órdenes ni
revinculación; faltan estado firmado del hijo y recuperación local comprobada.
Antes de separar servidor aprobaron 99 pruebas locales. Después, con autorización, aprobaron en A56 cuatro
pruebas de imagen Android y regresión de almacenamiento. Pantalla/aceptación y
migración v1 física pendientes. Ver `pruebas/EVIDENCIA_P02.md`.

Seguimiento más reciente del padre: 81 tests locales correctos. Se descartan
preparaciones/códigos al cambiar entradas y PairChild no firma preparaciones
descartadas. Diez casos Android de pantalla/persistencia/migración aislada
aprobados en A56 con autorización; no crearon vínculo cotidiano. La firma usada
en el flujo aislado es JCA efímera de test, no autenticación real de autoridad.
El padre aún no está listo para integración completa (cliente WSS/órdenes
pendientes). Se avisará al propietario para coordinar servidor cuando proceda,
con comunicación local probada antes de despliegue.

El servidor externo sigue siendo base local, sin WSS público operativo ni despliegue.

## Compilar y probar en Windows

Instalar JDK 21, SDK Android 36 y Node 24. Abrir la raíz del proyecto con Android
Studio o ejecutar desde PowerShell:

```powershell
.\scripts\verificar.ps1
```

Para un módulo: `-Modulo nucleo` o `-Modulo app`.
El script detiene la verificación si falla cualquier comando.
La verificación completa también ejecuta `:core-protocol:testInterop`: firmas
nuevas cruzadas en ambos sentidos JVM/Node para los diez vectores. Requiere Node
y únicamente módulos integrados de Node, sin npm ni proyecto servidor. El test
JVM puro no exige Node y no incluye ese caso de interoperabilidad.

Comandos Android individuales:

```powershell
.\gradlew.bat :core-protocol:test
.\gradlew.bat :app-parent:testDebugUnitTest :app-parent:assembleDebug
.\gradlew.bat :app-parent:assembleDebugAndroidTest :app-parent:lintDebug
```

El APK debug se genera en `app-parent/build/outputs/apk/debug/`. Se excluye de
Git, igual que claves, archivos `.env`, resultados y configuración local.

## Interoperabilidad opcional entre proyectos

El test predeterminado verifica cruce criptográfico JVM/Node contra los payloads
congelados, no el codec del servidor. Para probar también el codec Node externo,
tras instalar sus dependencias en su propio proyecto:

```powershell
.\gradlew.bat :core-protocol:testInterop '-PrutaRelay=C:\Users\Usuario\Documents\parental_render\relay' --rerun-tasks
```

Es integración explícita opcional, no una dependencia de compilación de la app.
Inicio/desarrollo/pruebas del servidor: README del proyecto parental_render.

## Pruebas físicas

Se avisará antes de necesitar ADB. Cuando el propietario habilite el **teléfono
del padre**, se podrá instalar el APK debug y ejecutar
`:app-parent:connectedDebugAndroidTest` con selección de dispositivo verificada.
El primer lote ya se ejecutó en el A56: 12 aprobados y 1 interactivo omitido.
El A13 queda excluido; con ambos conectados se usó runner ADB dirigido
explícitamente al A56, no Gradle contra todos los teléfonos.
Además de la pantalla, el APK instrumentado verifica el parser y el acceso al
esquema dentro de Android. Estas pruebas aprobaron en el A56.
Incluye además pruebas de navegación y presentación con hijo ausente, estado
confirmado, desconexión e intención sin confirmar. Son datos ficticios exclusivos
del APK instrumentado, no evidencias de un dispositivo hijo.
P01 incluye dos pruebas de identidad con alias temporales y una prueba interactiva
de autenticación real, desactivada salvo argumento explícito. No habilitarla sin
acordar la interacción física. Un test omitido por ausencia de bloqueo seguro o
de argumento no cuenta como aprobado. Ver `pruebas/PRUEBA_FISICA_P01.md`.
También comprueba vectores criptográficos y una clave temporal no exportable en
Keystore. Esa clave existe solo en el test instrumentado y se elimina en su
`finally`; no es identidad de producción ni prueba autenticación del adulto.
Esta app no administra ni restablece teléfonos. Las pruebas del hijo real y
la integración se planifican por separado.

Estado real y próxima tarea: `ESTADO.md`. Herramientas: `BUILD_ENV.md`.
Acuerdos del propietario, idioma y frecuencia de commits: `AGENTS.md`.
Evidencia criptográfica local: `pruebas/EVIDENCIA_C02.md`.
Evidencia de navegación y estados: `pruebas/EVIDENCIA_P00.md`.
Evidencia de identidad/autenticación: `pruebas/EVIDENCIA_P01.md`.
Evidencia de almacenamiento: `pruebas/EVIDENCIA_P03.md`. Las tres pruebas Android
nuevas de almacenamiento aprobaron en A56; no equivalen a validar QR/órdenes reales.
