# Vinculación CP/2 — copia local separada

Fuente: texto íntegro de `contrato-cp2/vinculacion.schema.json` y README de
Render **4ac4962**, suministrados por el propietario en conversación. No se leyó
ni verificó ningún repositorio externo. El JSON se copia tal como fue suministrado;
no implica freeze CP/2 completo, migración móvil o endpoint operativo.

Implementación Kotlin separada: `dev.controlparental.protocol.cp2.VinculacionCp2`.
Valida manualmente el perfil cerrado equivalente al schema; no intérprete JSON
Schema en runtime ni empaquetado del schema en APK. Primitivas técnicas de CP/1
reutilizadas sin modificarlas (parser, UTF8, base64, SPKI, DER, origen).
Profundidad6 se comprueba antes del parser, canonicidad también oferta/sobre,
r/s1..n−1 y punto P256 se comprueban adicionalmente en CP/2.

Corrección PADRE-INTEROP01: la fuente y el codec siempre tuvieron **6 campos oferta,
10 aceptación y 5 sobre**, contando v/kind/protocol. La referencia previa a once
era un error documental; no hubo cambio de schema/codec ni desacuerdo de formatos.

Solo PAIR local: prefijo `CPv2/PAIR\n` y pública padre esperada externa. No AUTH,
OFFLINE, MESSAGE, STATE, WSS, replay durable o commit. La oferta no tiene firma.
Verificación criptográfica no acredita vigencia, autoridad humana ni vínculo.
El codec CP/1 y `compartido/` permanecen intactos; sin fallback de perfiles.

## Ampliación PADRE-AUTH01 separada

SchemaAUTH íntegro suministrado en conversación, fuente CP2-A01 Renderd99ddac,
copiado en autenticacion.schema.json sin acceso externo. AutenticacionCp2 y
AuthClienteCp2 prueban codec/firmante/handshake puro, sin activar red/MAIN/migración.
Proveedor de snapshot compatible por defecto ausente→NO_DISPONIBLE; no adaptar CP1.
PING/PONG solo codec, no nuevo scheduler de latidos. AUTH_OK no demuestra hijo
operativo. Evidencia: pruebas/EVIDENCIA_PADRE_AUTH01.md; PAIR01 sigue pendiente de
snapshot/persistencia/ceremonia, no se resuelve por AUTH.

## PADRE-PAIR02 — actualización del alcance MAIN

MAIN integra preparación/firma con identidad existente y autorización adulta,
solo legado SIN_VINCULAR/PREPARADO íntegro no operativo. Área pública CP2v1 separada
noBackup con AtomicFile/readback: siempre PENDIENTE_CONFIRMACION_HIJO, nunca ACTIVO.
No convierte ni escribe legado/contrato CP1; firma/hashescp2 nuevos. Reinicio no
reconstruye códigos/QR; pendiente no se reemplaza sin reconciliar. AUTH/transportecp2
siguen sin activarse por esto. Fuentes/codecs/schemas intactos. Evidencia ejecutada,
límites y hashes finales en pruebas/EVIDENCIA_PADRE_PAIR02.md; falta física autorizada.

## PADRE-ORD01 — MESSAGE parcial, sin emisión activa

Fuente nueva parcial: [ORD01_FUENTE_PARCIAL.md](ORD01_FUENTE_PARCIAL.md), lista cerrada
recibida del propietario y schema íntegro parcial CP2-O01 suministrado después,
copiado en ordenes.schema.json sin leer otros repositorios; no freeze completo. Codec/firma
de SYNC_REQUEST6campos y SET_POLICY12campos (lista exacta, no13), aislados de PAIR/
AUTH/CP1. Preparación solo RAM con puerto externo STATE verificado sin implementación
productiva; MAIN/transporte no conectados. No reserva/persistencia/envío de órdenes.
Evidencia ejecutada y límites: pruebas/EVIDENCIA_PADRE_ORD01.md.
