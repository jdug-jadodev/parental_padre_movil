# PADRE-AUTH01 — codec y borde cliente AUTH CP/2 sin red

Fecha: 2026-10-14. Base0c66d96; implementación **0b010d7**.
Estado: **AUTH_CP2_LOCAL_PROBADO / BORDE_SIN_SNAPSHOT_NO_DISPONIBLE**.
Sin conexión real, activación MAIN, migración o resolución del snapshot PAIR01.

## Fuente y formatos

Schema CP2-A01 Renderd99ddac recibido íntegro en conversación; no lectura de otros
repositorios ni verificación independiente de ese commit. Copia exacta nueva:
`contrato-cp2/autenticacion.schema.json`, SHA256
`db96b66ec81a379836751491244bb498a3b45ad75428593d9ea2c8755e0dd17a`.
Draft75eb56e§4 consultado. Render125testsA01 y avances hijo son reportados, no
ejecutados aquí, no aprobación de interop/ceremonia/hardware.

`core-protocol/.../cp2/AutenticacionCp2.kt`: DTOs separados y cerrados de CHALLENGE
(timeoutSec10), AUTH_PROOF(rolePARENT/CHILD), AUTH_OK, PING/PONG y sobreAUTH.
v2/protocolcp.v2 solo donde schema exige; sobre exactamente5campos. No claves
públicas en estos mensajes. El codec valida perfil explícitamente, no interpreta
JSONSchema en runtime. Test compara properties/required y borra cada campo.

Reutiliza primitivas CP/2 de PAIR01; solo3helpers cambian visibilidad privada→internal
en VinculacionCp2 (campos/texto/documento), sin cambiar comportamiento PAIR. Reutiliza
primitivas CP1 sin modificarlas. UTF8estricto/ASCII/canonicalidad/duplicados/UUIDv4/
nonce32base64url roundtrip, profundidad6 antes de parser, payload8192/frame16384,
SPKIcanónico/puntoP256, DER8..72/r,s1..n−1. Firma SHA256ECDSA sobre bytes exactos
**UTF8("CPv2/AUTH\n") + payload**, LF real byte0A (no barra+n). Firma inválida,
dominioCPv1/PAIR/dobleescape/pública o rol distintos rechazados; no fallback.
Documento verificado ofrece copias defensivas del frame/payload y SHA256payload.
Verificar es criptografía, no autorización contextual/replay/frescura de socket.

## Borde de cliente y firmante Android inyectable

`network/AuthClienteCp2.kt`: clase pura sin Socket/TransporteRelay/Context/UI/store,
generaciones monotónicas por apertura/cierre, métodos sincronizados y callbacks
con generación capturada. Proveedor externo por defecto null→NO_DISPONIBLE. Snapshot
explícito lleva pairId/pública padre/revisión/compatibleAutorizado/perfil/rolPARENT;
no se infiere de CHALLENGE, una oferta o ParentStoreCP1. Incompatible/malformado/
subprotocolocp.v1→NO_DISPONIBLE. No crear clave, abrir red o cambiar datos por ello.

Abrir solo inicia estado local de una conexión que un futuro transporte ya haya
identificado; límite conservador10s desde abrir, sin renovar por reto tardío. Reloj
monotónico inyectado, retroceso invalida; guardas antes/después de firma y envío/OK.
El reto exacto aporta solo connectionId/nonce: prueba se construye con identidad/
pairId/rol del snapshot. Se verifica la firma y que el payload devuelto coincide
byte a byte con la prueba solicitada (impide devolver prueba de otro nonce/contexto).

Retorna ticket opaco PRUEBA_LISTA con bytes defensivos: **firmar no equivale a enviar**.
Solo confirmarEnvio del mismo ticket/generación con enviado=true permite esperar
AUTH_OK del connectionId actual antes del deadline. Callback de generación vieja se
ignora sin cambiar la nueva. Replay dentro del contexto/error/envío fallido invalida.
Cierre/segundoPlano descarta snapshot/reto/ticket y cambia generación; cierre/reapertura
reentrantes durante firma no entregan prueba vieja ni dañan nueva conexión.

El futuro dueño del transporte debe capturar ticket/generación por socket, efectuar
envío real y notificar resultado, llamar cerrar/segundoPlano y avanzarReloj según
su ciclo. Esta unidad NO lo conecta a ese ciclo ni abre socket; no acredita las
carreras de un transporte real. El codec PING/PONG está probado; no scheduler de
heartbeat ni política de consumo/red/enforcement nuevos. AUTH_OK solo transporte,
sin canalSano/STATE/hijo operativo/ACK/APPLIED/órdenes o promoción PREPARADO.

`security/FirmanteAuthCp2.kt`: adaptador inyectable sobre AlmacenClavePadre existente
(implementación Android ParentKeyStore disponible, no modificada). Comprueba pública
esperada y SesionAutoridad antes/durante/después; error invalida sesión. No llama
crearSiFalta ni crearIdentidad, no rota ni deriva autoridad de públicas recibidas.
No instancia Android Keystore/cliente CP2 en MAIN; sin proveedor compatible el borde
aislado queda NO_DISPONIBLE. Test usa almacén falso y claveRAM real, no prueba hardware.
PAIR01firmaMAIN/snapshot/persistencia/readback pendientes intactos; sin UI nueva.

## Pruebas ejecutadas

12tests nuevos `AuthCp2Test`: firmas P256 efímerasRAM reales/copias/hash/bytesfirma
con LFexacto; dominios cruzados/clave/rol; canonicidad/campos extra/ausentes/duplicados/
tipos/versión/profundidad/UTF8/nonce inválidos; límites payload/frame; DERcero y payload
decodificado excesivo; schema5objetos; PING/PONG roundtrip; snapshots faltantes/CP1/
incompatibles/roleCHILD; AUTH_OK solo tras envío/correctoID; fallo/replay/reconnect/
generación/cierre/fondo/timeout/reloj/snapshot; cierre durante firma; prueba nonce
ajeno y firma fuera de tiempo; adaptador authsincrearclaves. Privadas nunca persistidas,
sin secretos/códigos reales en repositorio/logs. Sin fixtures nuevos en APK.

```powershell
.\gradlew.bat :app-parent:testDebugUnitTest --tests '*AuthCp2Test' --console=plain
.\gradlew.bat :core-protocol:test :core-protocol:testInterop :app-parent:testDebugUnitTest -PdiagnosticoQr=fixture --rerun-tasks --console=plain
.\gradlew.bat :app-parent:testDebugUnitTest :app-parent:assembleDebug :app-parent:assembleDebugAndroidTest :app-parent:assembleRelease :app-parent:lintDebug :app-parent:lintRelease --rerun-tasks --console=plain
```

Primero11tests iniciales, BUILD SUCCESSFUL7tareas ejecutadas/18UP-TO-DATE. Agregado
test12negativo, corregido paréntesis por revisión antes de compilar (sin fallo de
build/test). Segundo comando **163app+30core+1interop=194distintos**,29tareas
ejecutadas; tercero **162app normales**,131tareas ejecutadas junto builds/lint.
Todos0fallos/errores/omitidos. No sumar repeticiones/UP-TO-DATE; núcleo/interop sí
ejecutados en segundo comando. PAIR01/QR02/P02/P07 incluidos sin rebajar/excluir tests.
Sin muestreo diagnóstico true ni tests del relay/125A01 reportados.
Lint debug/release0errores/23warnings versiones/dependencias por variante; aviso .so
sin strip empaquetadas como existentes. Sin dependencias nuevas. No AndroidTest
ejecutado; APKtest solo compilado, sin dispositivos/ADB o instrumentación.

## APK reales finales e inspección dirigida

Builds después del commit **0b010d7**, sin cambios fuente posteriores; cierre solo
documentos. No instalados; no atribuir a A56. Todos bajo app-parent/build/outputs/apk/:

| Ruta | SHA256 |
|---|---|
| debug/app-parent-debug.apk | 33ef534732cf2ce0e3a3e1acc1ca2afc2b9d98ac5266373d9ba11cac54d4dd7a |
| release/app-parent-release-unsigned.apk | e90c0da649277592d1bb0003dfdbcdbe92cce13fb38eef91707dda582b4d00b2 |
| androidTest/debug/app-parent-debug-androidTest.apk | 2074540e73558d22ea5241c108ff432ad125d77a0839a16caa29a0f53931be25 |

Release sin firma de distribución. APK instalado A56 sigue QR02F01 según evidencia
anterior, no consultado aquí. Script Python por stdin sobre ZIP y tablas DEX:
518clases propias por APKapp,3clases codec/borde/firmanteAUTH presentes. Contraste
contra148clases JVMtest compiladas y nombres diagnóstico/C02/falso:0coincidencias.
Nombre schemaAUTH y marcadores fixture públicoQR01 (nombre/SPKI/frameB64/framebytes)
en ZIP descomprimido:0. Inspección dirigida de empaquetado, no auditoría universal
de secretos/clases/certificados/Keystore/hardware o firmas de distribución.

## Rutas y cierre

Nuevo schemaAUTH separado; core-protocol/.../cp2/AutenticacionCp2.kt y3helpers
VinculacionCp2.kt; app-parent/.../network/AuthClienteCp2.kt;
security/FirmanteAuthCp2.kt; test/.../AuthCp2Test.kt; app-parent/build.gradle.kts
(ruta schema solo test). Docs: esta evidencia, contrato-cp2/README.md, ESTADO.md y
COORDINACION_PROYECTOS.md. Código0b010d7:423altas/3bajas incluyendo55schema copiado,
368altas/3bajas código/tests/config, menor500manuales. diff/preparado revisados.

Sin modificar CP1/schema/fixtures/RelayConnection/MAIN/UI/ParentStore/authCP1,
PAIR01snapshot/productivización/ceremonia; no otros repos/servidor/hijo/subagentes/
sesiones/promptsdevuelta/ADB/A56/A13/QR/cámara/códigos reales/CA/LAN/TLSconfigs/
Push/gitpush/despliegue. Contexto hijo/Render reportado no autorización física ni
listo remoto. Siguiente decisión por asignación: fuente snapshot CP2 compatible y
unión autorizada al transporte; no iniciar por este cierre. Detenido tras entrega.
