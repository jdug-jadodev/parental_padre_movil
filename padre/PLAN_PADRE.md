# Plan de implementación — padre y relay

Estado de todas las tareas: **PENDIENTE**. Ninguna aplicación ha sido compilada o probada por este paquete. Cada tarea termina con código, tests y actualización de `ESTADO.md`. Leer únicamente la sección indicada y las dependencias necesarias; no enviar todo el documento completo a una IA pequeña.

## Índice de ejecución

| Tarea | Entregable | Depende de |
|---|---|---|
| P00 | Esqueleto y navegación | C00 |
| P01 | Clave del padre y autenticación | C02, P00 |
| P02 | Vinculación QR y configuración pública | P01, P03; H08 solo para prueba conjunta |
| P03 | Archivo cifrado y pendiente única | P01 |
| R01 | HTTP, configuración y límites básicos | C00 |
| R02 | Autenticación por reto | R01, C02 |
| R03 | Relay, latidos y despliegue | R02 |
| P04 | Cliente WSS del padre | P01, R02 |
| P05 | Sincronización y envío idempotente | P03, P04; H09/H10 para integración |
| P06 | Interfaz de control final | P05 |
| P07 | Autorización local sin internet | P01, P02; H05/H11 solo para integración |
| P08 | Integración y entrega privada | P06, P07, R03, H10, H11 |

## P00 — Esqueleto y navegación

**Depende de:** C00.

**Leer:** SPEC-P P2–P3.

**Archivos/componentes previstos:** MainActivity, ParentUiState, ParentViewModel.

**Implementar:** Crear navegación Primer inicio → Vincular → Control → Recuperación. Usar un repositorio falso exclusivamente en tests/debug. Dibujar estados separados de relay/hijo/confirmación.

**Criterio de aceptación:** Las cuatro acciones se muestran; ningún botón llama aún a un endpoint real. Se puede representar hijo ausente sin pintarlo bloqueado.

**Comprobación:** UI test con estados no vinculado, desconectado y confirmado.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

## P01 — Clave del padre y autenticación

**Depende de:** C02, P00.

**Leer:** SPEC-P P4; CP/1 §4.

**Archivos/componentes previstos:** ParentKeyStore, LocalAuthentication, tests de identidad.

**Implementar:** Comprobar bloqueo seguro, crear P-256 no exportable y ventana de autenticación. Implementar exportación solo de pública SPKI. Separar alias de cifrado de metadatos del de firma.

**Criterio de aceptación:** No se firma sin autenticación válida; una cancelación no crea clave alternativa. La clave persiste al reiniciar el proceso.

**Comprobación:** P-KEY-01 y pruebas instrumentadas de autenticación; no usar una biometría simulada como evidencia final.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

## P02 — Vinculación QR y configuración pública

**Depende de:** P01, P03; H08 solo para prueba conjunta.

**Leer:** CP/1 §5 y §9; SPEC-P P3.2/P4.

**Archivos/componentes previstos:** PairChild, PairingScreen, RecoveryCodeGenerator.

**Implementar:** Leer CHILD_OFFER, crear pairId, generar códigos por propósito, firmar PAIR_ACCEPT y comparar huellas. Exportar únicamente env pública; probar ambos QR sin internet.

**Criterio de aceptación:** El hijo rechaza oferta vencida, clave que no coincide, propósito falso o nueva vinculación ya estando configurado. Ningún secreto aparece en exportación.

**Comprobación:** P-PAIR-01, C-PAIR-01 y revisión manual de datos exportados.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

## P03 — Archivo cifrado y pendiente única

**Depende de:** P01.

**Leer:** SPEC-P P4–P5; CP/1 §10.

**Archivos/componentes previstos:** ParentStore, PendingIntentRecord, tests de recuperación.

**Implementar:** Implementar AtomicFile con AES-GCM y nonce nuevo por escritura. Reservar contador e intención transaccionalmente. Recuperar tras fallos y cancelar intención sin borrar evidencia remota.

**Criterio de aceptación:** Un fallo entre reserva y envío no reutiliza seq para otra orden. Reinstalar no restaura automáticamente una identidad.

**Comprobación:** P-STORE-01 y simulación de fallo de escritura/cierre.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

## Dependencias externas R01–R03

Las tareas de servidor se trasladaron al proyecto parental_render. Sus entradas
originales se conservan en documentacion/PLAN_ORIGEN_PADRE.md de ese proyecto.
No implementar servidor en este repositorio. Las dependencias R02/R03 de P04/P08
son puertas de integración con el servicio externo, no módulos locales del padre.

## P04 — Cliente WSS del padre

**Depende de:** P01, R02.

**Leer:** CP/1 §6/8; SPEC-P P5–P6.

**Archivos/componentes previstos:** RelayConnection, conexión a lifecycle, tests.

**Implementar:** Conectar solo con pantalla activa, comprobar TLS, subprotocolo y challenge, autenticar con Keystore. Reconectar con backoff sin sockets duplicados.

**Criterio de aceptación:** Al volver a foreground se solicita autenticación cuando la clave lo requiere; cerrar app no deja un servicio oculto.

**Comprobación:** P-NET-01, R-AUTH-01 y TLS inválido.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

## P05 — Sincronización y envío idempotente

**Depende de:** P03, P04; H09/H10 para integración.

**Leer:** CP/1 §7; SPEC-P P5.

**Archivos/componentes previstos:** RequestFreshState, SubmitPolicy, ReconcilePending.

**Implementar:** Solicitar nonce de sincronización, validar STATE, reservar seq, construir y firmar SET_POLICY, correlacionar ACK y retransmitir solo exactamente el mismo comando.

**Criterio de aceptación:** Pérdida de ACK no reinicia duración. Reconexión exige estado/desafío nuevo y confirmación para una nueva intención.

**Comprobación:** P-ACK-01/02/03, C-SEQ-01/04.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

## P06 — Interfaz de control final

**Depende de:** P05.

**Leer:** SPEC-P P3/P6.

**Archivos/componentes previstos:** ControlScreen, formularios de duración, UI tests.

**Implementar:** Conectar botones a casos de uso, validar 60–604800s, mostrar causa real, última evidencia y pendiente. Evitar doble pulsación concurrente.

**Criterio de aceptación:** LOCK_FOR explica apertura posterior condicionada; ALLOW enviado no se pinta como permitido cuando el hijo sigue bloqueado por otra causa.

**Comprobación:** P-UI-01/02 y pruebas de accesibilidad de texto/botones.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

## P07 — Autorización local sin internet

**Depende de:** P01, P02; H05/H11 solo para integración.

**Leer:** CP/1 §9; SPEC-P P3/P4.

**Archivos/componentes previstos:** RecoveryScreen, OfflineChallengeSigner.

**Implementar:** Escanear desafío del hijo, autenticar al adulto y firmar OFFLINE con mismo propósito/boot/nonce. Presentar advertencia de borrado para RETIRE; no enviar por relay.

**Criterio de aceptación:** Token MAINTENANCE no sirve para RETIRE ni ALLOW. Repetir token o reiniciar hijo lo invalida.

**Comprobación:** H-REC-01/02, H-RET-01; no borrar dispositivo diario.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

## P08 — Integración y entrega privada

**Depende de:** P06, P07, R03, H10, H11.

**Leer:** Matriz completa y README.

**Archivos/componentes previstos:** BUILD_ENV.md, ESTADO.md, evidencias de release.

**Implementar:** Compilar releases, verificar firmas de APK, integrar con A13, reiniciar Render, perder ACK, apagar padre y probar reconexión. Auditar logs/exportaciones.

**Criterio de aceptación:** No queda historial, DB, endpoint maestro, clave de prueba ni credencial en repositorio. Todas las pruebas P/R/C pertinentes tienen evidencia.

**Comprobación:** G1 y G3; no declarar G0/G4 sin evidencia del hijo.

**Cierre de tarea:** registrar archivos cambiados, comando/test realmente ejecutado, resultado observado y bloqueos restantes. Si una API no existe o exige privilegios no disponibles, detener esa vía y documentar el problema; no reemplazarla por reflexión o una falsa confirmación.

## Dependencias de integración, sin ciclos

Las referencias a otro rol para una prueba conjunta no bloquean desarrollar y cerrar sus tests unitarios con fakes locales. P02 y H08 se construyen por separado y se integran en P08; lo mismo ocurre con P07 y H11. P08 y H12 usan componentes implementados, no esperan que la otra tarea final ya esté cerrada. La entrega habitual requiere finalmente la evidencia de **ambas**. Dentro del padre, implementar P03 antes de finalizar P02 para persistir el vínculo de forma segura.
