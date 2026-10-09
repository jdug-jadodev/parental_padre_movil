# ADR-P-004 — Confirmación firmada y envío presencial del padre

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

## Contexto

Un mensaje enviado no significa bloqueo ejecutado. Mantener la app padre activa permanentemente añade complejidad y debilita el control de su clave.

## Decisión

La app padre conecta y firma con la UI activa y autenticación vigente. Antes de cada emisión solicita estado/desafío fresco. Solo ACK firmado o STATE correlacionado permiten confirmar resultado.

## Consecuencias y alternativas descartadas

Cerrar el padre interrumpe envíos pendientes, pero no las reglas del hijo. No se prometen notificaciones ni envíos en background. La pérdida de ACK no reinicia temporizadores. Se rechaza pintar éxito por confirmación del relay.

## Validación y revisión

P-ACK-01/02/03, C-SEQ-04. Contrato CP/1 secciones 7–8.

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.
