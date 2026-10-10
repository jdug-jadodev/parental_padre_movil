# PADRE-QR01 — diagnóstico QR sintético reproducible

Fecha de sesión: 2026-10-09. Base 8a6ae91. **Fallo reproducido, no corregido.**
No cambio de QrLocal, cámara, dependencias, CP/1, PADRE-C02 o repos externos.

## Presupuesto y método

Antes de ejecutar: 64 pares nuevos, 128 sobres (reto CHILD y respuesta PARENT),
identidades P-256 efímeras por par, firmas DER variables; una única ejecución de
muestreo. Misma pareja/boot/nonce ficticios del test anterior. Se reutilizan
QrLocal.generar/leer y codec/crypto CP/1 reales. Cada lectura correcta compara
**bytes exactos** y verifica firma con pública del rol; no solo igualdad de campos.

Primera muestra fallida: 16 ensayos fijos adicionales (3 imágenes idénticas,
5 variantes tamaño/margen, 8 máscaras), más control PURE_BARCODE de laboratorio.
No regenerar firmas ni buscar un caso que pase. Version/ECC/máscara se obtienen
con Encoder ZXing y las mismas opciones UTF-8/ECC L del writer de producción.
El render RGB reproduce exactamente el test previo: negro/blanco opacos y
pixel[i] = matrix[i % width, i / width]. No suavizado/rotación/cámara/compresión.

Después: guardar **solo un sobre firmado y su pública ficticios** como fixture
permitido, sin privada/código/QR de dispositivo. Ejecutar caracterización fija
aislada y una suite pertinente. No repetir el muestreo ni hacer reruns hasta verde.

## Todos los fallos del muestreo

128 lecturas: **125 correctas, 3 NotFoundException**. Retos: 64/64 correctos;
respuestas: 61/64 correctas. No estimación de tasa general a partir de esta muestra.

| Índice de par | Etapa | Bytes sobre | DER bytes | Versión | Máscara | Tamaño/margen/escala | Resultado |
|---|---|---|---|---|---|---|---|
| 39 | respuesta | 465 | 71 | 15 | 4 | 768 / 4 / 9 | NotFoundException |
| 56 | respuesta | 465 | 71 | 15 | 4 | 768 / 4 / 9 | NotFoundException |
| 63 | respuesta | 466 | 72 | 15 | 4 | 768 / 4 / 9 | NotFoundException |

Todos los sobres fueron versión QR 15 (77 módulos), ECC L; longitudes 464–466
bytes para DER 70–72 bytes. Distribución observada:

| Etapa | DER 70 / 71 / 72 | Máscaras y recuentos |
|---|---|---|
| reto | 14 / 37 / 13 | 1:30, 4:29, 7:5 |
| respuesta | 18 / 21 / 25 | 4:64 |

DER 71/72 también tuvo éxitos; máscara 4 también tuvo éxitos. La longitud DER o
la máscara por sí solas no explican todos los resultados. La firma/contenido
variable cambia la disposición de módulos; no cambia el contrato ni los bytes
durante un roundtrip. No se forzaron versiones diferentes en este presupuesto.

## Variantes del índice 39 (sin cambiar bytes/firma)

| Ensayo | Resultado lector original |
|---|---|
| Misma matriz, tres repeticiones | 3 NotFoundException |
| 752 px, margen 4 | NotFoundException |
| 510 px, margen 4, escala exacta 6 | NotFoundException |
| 680 px, margen 4, escala exacta 8 | NotFoundException |
| 768 px, margen 8 | NotFoundException |
| 744 px, margen 8, escala exacta 8 | NotFoundException |
| Máscaras forzadas 0,1,2,3,5,6,7 a 768/margen 4 | 7 lecturas exactas y firmas válidas |
| Máscara 4 a 768/margen 4 | NotFoundException |

Control PURE_BARCODE: bytes originales y firma válidos. Es un **control de imagen
perfecta aislado**, no fallback del test anterior o del producto, ni lectura de
cámara acreditada. No se cambió el generador/lector del test anterior para ocultar
la limitación productiva. Tampoco se fijó otra máscara como solución.

## Causa delimitada y caso fijo

`app-parent/src/test/resources/qr01_respuesta_publica.json` conserva el índice 39:
solo pública SPKI P-256 y sobre público ficticio. No permite firmar, y no contiene
códigos, hashes de recuperación, datos personales o identificadores de dispositivo.

Sin nueva firma/generación de contenido, QrLocal produce siempre versión 15/máscara 4.
Caracterización aislada y dentro de suite confirma el mismo fallo y geometría:

- Centros detectados: (68.5,68.5), (698.5,68.5), **(122.5,676.0)**; alineación
  (671.5,671.5). Esquina inferior izquierda real del render: **(68.5,698.5)**.
- Detector selecciona el patrón interno falso en vez de esa esquina. Geometría
  incorrecta produce muestreo incorrecto y QRCodeReader directo da ChecksumException.
- QrLocal usa MultiFormatReader: su resultado observable es NotFoundException,
  no un error de firma CP/1. PURE_BARCODE evita esa detección y recupera exactamente
  el sobre, cuya firma se vuelve a verificar con la pública del fixture.

**Confirmado:** matriz/bytes/firma válidos y fallo del camino de detección de
ZXing utilizado por QrLocal para este contenido. No es un error demostrado del
copiado RGB del test. La condición interna exacta de selección/fin anticipado
de FinderPatternFinder no se instrumentó ni se corrigió. No se generaliza a
todas las máscaras 4, cámaras o versiones. Este caso se reproduce fuera de la
suite: no requiere orden de tests; otros posibles efectos de orden no se excluyen.

## Comandos, presupuesto ejecutado y resultados completos

```powershell
# Una sola muestra; FALLÓ con los tres casos de arriba, aserción final conserva fallo.
.\gradlew.bat :app-parent:testDebugUnitTest -PdiagnosticoQr=true --tests '*DiagnosticoQrTest' --rerun-tasks --console=plain

# Caracterización pública aislada: primera ejecución FALLÓ por expectativa incorrecta
# NotFoundException del lector QR directo; realmente dio ChecksumException.
.\gradlew.bat :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture --tests '*DiagnosticoQrTest.casoPublicoFijoCaracterizaFalloNoCorregidoSinFirmarDeNuevo' --rerun-tasks --console=plain

# Misma orden, después de precisar esa aserción diagnóstica: PASÓ, manteniendo
# tres NotFoundException de QrLocal y ChecksumException directo esperados/documentados.

# Una suite final con caracterización fija; muestreo no se repite.
.\gradlew.bat :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture --rerun-tasks --console=plain
```

Cuatro ejecuciones Gradle, 25 tareas ejecutadas en cada una: muestreo fallido,
caracterización inicial fallida, caracterización precisada correcta y suite
correcta. La precisión de expectativa no cambia producción ni suprime aserciones:
se comprueba explícitamente cada fallo y también bytes/firma del control.

Lecturas QrLocal con NotFound observadas: 3 muestreo + 9 variantes + 3 en cada una
de las tres ejecuciones de fixture = **21**. Lector QR directo: **3 ChecksumException**
(la primera inesperada, las siguientes verificadas). Control PURE: **3 correctos**
(uno en variantes, uno aislado precisado, uno en suite). Detector independiente
añadido en suite confirma geometría incorrecta. Todas las observaciones registradas;
ningún nuevo muestreo ni reintento como corrección.

Suite final: **132 tests app, cero fallos/errores/omisiones**, de los cuales uno
caracteriza un fallo conocido, **no una corrección**. Test P07 original sin cambios
pasó esa ejecución aleatoria: no prueba estabilidad. Núcleo/interop/Node no se
reejecutaron aquí; no sumar pruebas históricas como ejecución nueva.

## Cambios permitidos y cierre de la vía productiva

Añadidos solo DiagnosticoQrTest.kt, recurso público JVM, configuración Gradle
opt-in y documentación. Sin -PdiagnosticoQr el diagnóstico especializado queda
excluido; **OfflineChallengeSignerTest original sigue incluido e intacto**. Con
`fixture` se ejecuta caso fijo sin muestra aleatoria; con `true` el muestreo mantiene
aserción de cero fallos y puede devolver salida fallida. No es un test ignorado,
retry, relajación de firmas ni declaración de fallo resuelto.

No corrección de test/soporte porque la causa confirmada alcanza al lector
productivo/dependencia. Detenida esa vía según alcance: requiere revisión y nueva
asignación antes de cambiar QrLocal/dependencias/cámara. No elegir PURE_BARCODE
para cámara sin revisión: asume geometría conocida que el producto no garantiza.

Siguiente propuesta **PADRE-QR02**, no iniciada: revisión técnica de detección
productiva con este caso fijo y preservación de bytes/firmas, definiendo permiso
de cambios/criterios antes de implementar. Sin receta de bypass o máscara fija.
Siguen pendientes puertas/orden del hijo, vinculación operativa y evidencia física.
Sin Android/Keystore/instrumentación/ADB/teléfonos, integración relay, certificados,
LAN, push/despliegue, subagentes o repos ajenos. A13 excluido; A56 no operado.
