# Relevo — C01

## Resultado comprobado

C01 terminada en alcance local. Bloques previos: `f7e2044` y `dc50004`; el cierre
se registra en el commit posterior. Se conservaron bloques inferiores a 500
líneas manuales. No se cuentan lockfile, fixtures ni documentación.

`scripts/verificar.ps1`: 21 tests JVM del núcleo, uno de la app y 23 Node
aprobados; APK debug y APK de pruebas compilados, TypeScript y lint correctos.
Lint: cero errores y once avisos de versiones nuevas. Diez vectores preservan
bytes exactos en Kotlin y Node. Los 78 chequeos documentales siguen pasando.

## Interfaces y rutas

`core-protocol/`: DTO sellados Mensaje, CodecProtocolo, parser estricto y formatos.
El esquema se incorpora como recurso directamente desde compartido/ y se fija
por huella. `relay/src/`: modelos.ts, json-estricto.ts, formatos.ts y protocol.ts.
CodecProtocolo de Node recibe bytes del esquema desde inicialización confiable,
verifica huella y usa Ajv 8.20.0 (2020-12). No lee archivos de mensajes del cliente.

Funciones: leerFrame, leerOferta, leerPayloadSinVerificar, codificarFrame,
codificarPayload y bytesPayload. **Ninguna acredita firmas o autorización.**
No conectar resultados no autenticados a confirmaciones de UI ni políticas.

Se actualizaron Gradle/serialization, package.json/lockfile, README, BUILD_ENV,
ESTADO y evidencia/matriz. El contrato, esquema y fixtures compartidos no cambian.

## Pendientes

Las pruebas de pantalla y dos de parser/esquema Android están compiladas pero
no ejecutadas. Se comprobó estáticamente la presencia y huella del esquema en
el APK; falta verificar su carga en Android. El propietario debe habilitar ADB
del teléfono del padre antes de connectedDebugAndroidTest. No hay Device Owner,
órdenes reales ni operaciones destructivas en esa comprobación.

Python/jsonschema sigue opcional y pendiente. Ninguna puerta física aprobada.
La app sigue siendo base inicial y el relay solo escucha en loopback, devuelve
503 en health y rechaza WebSockets/producción.

## Próxima tarea

C02. Leer AGENTS.md, ESTADO.md, sección C02 de compartido/03_INICIO_TECNICO.md,
CP/1 §4 y vectores_crypto.json. Verificar criptografía estándar sobre bytes
originales **antes** de validar/canonizar payload; comprobar interoperabilidad
y firmas alteradas. Mantener firma Keystore para P01: no crear autoridad de
producción con claves exportables. Avisar antes de acceder al teléfono por ADB.
