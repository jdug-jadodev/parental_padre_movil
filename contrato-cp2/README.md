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

La fuente tiene **6 campos oferta, 11 aceptación y 5 sobre**, contando v/kind/
protocol. Se preservan los once campos de aceptación del schema íntegro, aunque
la asignación los resumía como10; no se omite ninguno.

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
