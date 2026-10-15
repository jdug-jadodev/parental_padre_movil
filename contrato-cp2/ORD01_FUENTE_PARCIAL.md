# ORD01 — fuente parcial de mensajes del padre

2026-10-15. Fuentes suministradas en conversación: lista cerrada del propietario
en PADRE-ORD01 y, tras el primer cierre, schema íntegro parcial CP2-O01 Render
copiado sin cambios en `ordenes.schema.json`. No se leyó otro repositorio ni se
ejecutó Node. Siete tests focales Render correctos solo reportados por el emisor.
El propietario confirmó el conteo12/6 y que13 era error de la asignación.
Esto no congela MESSAGE/STATE ni CP/2 completo, ni altera CP/1, PAIR o AUTH.
SHA256 local del schema UTF8 con LF final:
`e5acd941ea0739ad875dcd493ea6685a2a3ec9d07a440f1c89a578fc064e8906`.
No se recibió hash externo para cotejar; no atribuir este hash a Render.

El test de fuente compara required/properties, cantidades6/12/5, const y enums,
y límites condicionales de duración con el codec existente. No es un intérprete
JSON Schema universal. Las reglas normativas adicionales siguen en runtime:
topeLong.MAX_VALUE (el regex de19dígitos del schema no basta), base64roundtrip,
DER/SPKI, firmas/canonicalidad/tamaños/profundidad. No se relajan por el schema.

`SYNC_REQUEST`: exactamente **6** campos: `v`, `kind`, `pairId`, `sender`,
`syncNonce`, `requestedAction`. Valores fijos: 2, SYNC_REQUEST, PARENT;
pairId UUIDv4 minúscula; syncNonce base64url canónico de 32 bytes;
requestedAction obligatorio, null o LOCK/ALLOW/ALLOW_FOR.

`SET_POLICY`: exactamente **12** campos: `v`, `kind`, `sender`, `pairId`,
`bootId`, `childSessionId`, `commandId`, `seq`, `expectedRevision`,
`commandNonce`, `action`, `durationSec`. La asignación decía 13: se comunicó
la lista de 12 sin inventar campo. Valores fijos: 2, SET_POLICY, PARENT;
cuatro UUIDv4 minúscula; seq cadena decimal positiva hasta Long.MAX_VALUE;
expectedRevision cadena decimal de 0 hasta ese máximo, sin ceros iniciales.
commandNonce base64url canónico de 32 bytes. action LOCK/ALLOW/ALLOW_FOR;
durationSec obligatorio, null para LOCK/ALLOW, entero 60..86400 múltiplo de 60
solo para ALLOW_FOR. Sin LOCK_FOR, deferredAllowSec, WAITING_ALLOW ni extras.

Sobre SIGNED de exactamente 5 campos: v=2, kind=SIGNED, purpose=MESSAGE,
payloadB64, sigB64. Firma SHA256withECDSA P256 DER (r,s entre 1 y n−1), pública
SPKI DER esperada externa. Dominio UTF8 `CPv2/MESSAGE` seguido de LF real y
payload original. AUTH conserva su dominio separado existente. Canonización,
UTF8 estricto, claves ASCII, duplicados prohibidos, base64url roundtrip,
frame máximo16384, payload máximo8192 y profundidad máxima6 antes del parser.

Implementación: `MensajesPadreCp2`, `AutoridadPadre.firmarMessageCp2` y
`PrepararMensajeCp2`. Preparación pura RAM; fuente externa de snapshot confirmado
y STATE ya verificado, sin implementación productiva ni parser STATE parcial.
La fuente futura debe retirar contexto al perder vigencia/conexión/autoridad.
Presupuesto padre estricto menor de 20s desde inicioSYNC; sin renovar por STATE
o PONG; conflicto/cambio exige sync y confirmación nuevos. Selección de minutos
explícita 1..1440 enteros, sin preselección. No UI/transporte activos.

La secuencia propuesta se valida, pero **no se reserva ni persiste**. No es
una API de envío ni autorización exactly-once: reserva duradera, contador local,
intención/reintento/reconciliación STATE quedan para otra unidad. No convertir
PAIR pendiente en activo; AUTH_OK/PONG/QR no son STATE ni confirmación del hijo.
Las semánticas offline antiguas del draft §5 están superadas por NORMAL revocable
tras caída Android validada10s y OFFLINE local explícita. Aquí no se implementan
políticas Android ni autorización offline remota, ni se añaden campos por ello.
