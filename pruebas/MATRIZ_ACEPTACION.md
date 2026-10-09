# Matriz de aceptación — CP/1

Todos los casos del producto se entregan **PENDIENTES**. Completar evidencia con versión, entorno, resultado observado y archivo/commit. La verificación de fixtures adjunta es documental/criptográfica; no aprueba estos casos de la aplicación.

## Puertas

G0 = administración/quiosco y emergencia en A13. G1 = identidad, protocolo y recuperación. G2 = persistencia, tiempo, arranque y red. G3 = integración padre/relay/hijo. G4 = proceso muerto, energía, escapes y actualización en release. No omitir un caso crítico porque el emulador no lo soporte. Una llamada real de emergencia no se realiza sin coordinación formal.

## Contrato y seguridad criptográfica

| ID | Caso y preparación | Resultado esperado | Entorno | Estado |
|---|---|---|---|---|
| C-CRYPTO-01 | **Firma válida y verificación cruzada.** Firmar/verificar fixtures P-256 DER en JVM, Android y Node. | Se aceptan bytes originales y se rechazan cambios/clave/propósito incorrectos. | Automática | PENDIENTE |
| C-SCHEMA-01 | **Parser estricto.** Probar campos extra, UTF-8/base64 inválidos, duplicados, números y tamaños fuera de límites. | Todos se rechazan antes de aplicar políticas. | Automática | PENDIENTE |
| C-PAIR-01 | **Vinculación física.** QR vencido, clave del hijo diferente, otra pareja, cancelación y segunda vinculación sin permiso. | No cambian claves; solo oferta física vigente y confirmada crea vínculo. | Automática + A13 | PENDIENTE |
| C-SEQ-01 | **Repetición exacta.** Reenviar último comando en la misma sesión tras perder ACK. | Devuelve resultado, conserva inicio y duración; no vuelve a liberar. | Automática | PENDIENTE |
| C-SEQ-02 | **Secuencia conflictiva.** Mismo seq con otro payload/id; después un seq menor. | SEQUENCE_CONFLICT o STALE_SEQUENCE, sin cambiar estado aceptado. | Automática | PENDIENTE |
| C-SEQ-03 | **Orden concurrente.** Procesar seq7 y seq8 con demoras de enforcement. | No termina aplicando seq7 después de seq8. Un solo escritor. | Automática | PENDIENTE |
| C-SEQ-04 | **Sesión/arranque/desafío viejos.** Entregar ALLOW de sesión anterior, otro boot o nonce vencido. | Rechazo; pedir estado actual y nueva confirmación al padre. | Automática + integración | PENDIENTE |

## Padre

| ID | Caso y preparación | Resultado esperado | Entorno | Estado |
|---|---|---|---|---|
| P-KEY-01 | **Autenticación y pérdida de clave.** Cancelar credencial, expirar ventana, invalidar clave de prueba. | No firma ni crea una autoridad de reemplazo silenciosa. | Android padre | PENDIENTE |
| P-PAIR-01 | **Exportación y datos mínimos.** Inspeccionar QR/archivo público/env y logs de pruebas. | No contienen privadas ni códigos en claro; solo públicas/hashes/configuración. | Automática + manual | PENDIENTE |
| P-STORE-01 | **Persistencia ante cierre.** Fallar después de reservar seq y antes de enviar. | No reutiliza secuencia para otra intención; archivo sigue consistente. | Automática | PENDIENTE |
| P-ACK-01 | **ACK impostor.** Enviar ACK falso/otro hijo/otro hash o solo PEER_STATUS. | No muestra aplicado. | Automática | PENDIENTE |
| P-ACK-02 | **ACK perdido.** Aceptar en hijo y descartar ACK; sincronizar nuevamente. | Reconciliar id/seq/hash sin reiniciar temporizador. | Integración | PENDIENTE |
| P-ACK-03 | **Enforcement pendiente/fallido.** Hijo devuelve pending o failed; ALLOW aceptado pero red no sana. | UI muestra causa/pendiente/error, no permiso efectivo falso. | Automática + UI | PENDIENTE |
| P-NET-01 | **Padre cerrado.** Preparar intención sin hijo; cerrar y abrir después. | No entrega en background; pide autenticación/sincronización/confirmación. | Android padre | PENDIENTE |
| P-UI-01 | **Acciones y duraciones.** Probar cuatro acciones, 59,60,604800,604801s y doble pulsación. | Límites correctos, una orden en curso, explicación de efectos. | UI automática | PENDIENTE |
| P-UI-02 | **Estado antiguo.** Cortar conexión después de STATE válido. | Muestra último estado y antigüedad, no estado actual inventado. | UI automática | PENDIENTE |

## Relay Render

| ID | Caso y preparación | Resultado esperado | Entorno | Estado |
|---|---|---|---|---|
| R-CONFIG-01 | **Config inválida.** Quitar claves/env, usar curva incorrecta o claves iguales. | Falla cerrado; health operativo solo con configuración válida. | Automática | PENDIENTE |
| R-AUTH-01 | **Suplantación de rol.** Autenticar como padre con privada del hijo o pública propuesta por atacante. | Rechaza; no incorpora socket al mapa. | Automática | PENDIENTE |
| R-AUTH-02 | **Reto viejo o reutilizado.** Reutilizar AUTH en otro socket o después de 10s. | Rechaza y consume/limpia correctamente. | Automática | PENDIENTE |
| R-AUTH-03 | **Expulsión de cliente.** Abrir impostor y luego segundo socket autenticado. | Impostor no expulsa; el nuevo auténtico reemplaza limpiamente. | Automática | PENDIENTE |
| R-ROUTE-01 | **Receptor ausente.** Enviar SET_POLICY cuando no existe hijo registrado. | PEER_UNAVAILABLE; no se guarda ni entrega más tarde. | Automática | PENDIENTE |
| R-ROUTE-02 | **Rol y bytes.** Intentar SET_POLICY desde hijo; reenviar uno permitido. | Rechaza rol incorrecto; bytes de contenedor válido no se alteran. | Automática | PENDIENTE |
| R-FAIL-01 | **Reinicio/despliegue.** Reiniciar servicio con móviles conectados. | Sockets reconectan, políticas locales se conservan, sin colas recuperadas. | Render + móviles | PENDIENTE |
| R-LIMIT-01 | **Abuso de frames.** Binarios, >16KiB, compresión, campos desconocidos, inundación. | Cierre/rate limit, sin caída del proceso ni memoria sin límite. | Automática | PENDIENTE |
| R-LIMIT-02 | **Socket lento y memoria.** Receptor deja de leer; muchas reconexiones. | Backpressure y limpieza; mapa no borra el socket nuevo por cierre del viejo. | Automática | PENDIENTE |

