# Evidencia C02 — firma y verificación local

## Alcance y versiones

Primer bloque de código: `f120557`. Interoperabilidad y cierre en el commit
posterior; consultar git log. Herramientas fijadas y observadas en BUILD_ENV.md.
Se mantiene intacto CP/1, su esquema y sus fixtures.

## Comprobaciones ejecutadas

`scripts/verificar.ps1` terminó correctamente:

- 30 pruebas del núcleo JVM, incluidas nueve criptográficas nuevas.
- Una prueba JVM de la app.
- 30 pruebas Node, incluidas siete criptográficas nuevas.
- Una prueba separada `:core-protocol:testInterop` con 20 intercambios:
  diez firmas nuevas JVM→Node y diez Node→JVM, para ambos roles y todos los
  propósitos de los diez vectores. Todas preservan el hash y bytes del payload.
- Compilación de app debug y APK instrumentado: correctas.
- Chequeo de tipos y compilación TypeScript: correctos.
- Lint Android: cero errores y once avisos de versiones más recientes.
- Verificador documental: diez vectores y 78 comprobaciones correctas.
- npm ci y auditoría automática: instalación correcta, sin vulnerabilidades reportadas.

Total: **62 pruebas locales aprobadas**, sin contar los chequeos documentales.

También se ejecutó `:app-parent:assembleRelease` con éxito. El APK resultante
no está firmado para distribución ni es una entrega funcional aprobada.
Se inspeccionaron ZIP/DEX de app debug, release e instrumentado: los fixtures
solo aparecen en el instrumentado; en los APK de la app no se encontraron los
identificadores de clases criptográficas de prueba examinadas ni su alias temporal.

El primer intento detectó una dependencia implícita de assets/lint. Se resolvió
registrando el recurso generado mediante la API oficial del AGP y se repitió
toda la verificación con éxito, sin ocultar controles de Gradle o lint.

## Seguridad comprobada localmente

Se verifica la firma sobre `UTF8("CPv1/" + purpose + "\n") || payloadBytes`
originales, antes de interpretar/canonizar el payload. Se rechazan otra clave,
payload/firma alterados, propósito cambiado, algoritmo externo elegible y
reclamación del otro rol, incluso si la firma de ese payload es válida.
Una firma válida sobre JSON ambiguo no permite aceptarlo. Los bytes devueltos
son copias; Node congela también el resultado y los datos del payload.

El relay solo verifica con públicas confiables. La firma Kotlin usa un adaptador,
sin almacenar privadas ni sustituir claves tras un fallo. Claves efímeras de
JVM/Node se mantuvieron en memoria: no se guardan ni imprimen privadas.

## Pendientes

Cinco tests instrumentados están compilados, **no ejecutados**: pantalla, dos de
parser y dos de criptografía/Keystore. El de Keystore crea un alias único de prueba,
comprueba no exportabilidad y lo elimina en finally. No es autoridad de producción
ni prueba autenticación del adulto; ese flujo corresponde a P01.

No se usó ADB, teléfono, Render ni el APK real del hijo. No se acredita hardware
seguro específico, biometría, sesión/nonce fresco, vínculo autorizado, aplicación
de restricciones ni puertas G0–G4. DocumentoVerificado acredita firma/estructura,
no autorización contextual ni APPLIED. C-CRYPTO-01 queda validada localmente en
JVM/Node y pendiente de Android/Keystore.
