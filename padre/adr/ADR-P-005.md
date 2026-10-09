# ADR-P-005 — Recuperación fuera de la nube y retirada presencial

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

## Contexto

La administración no debe quedar irrecuperable si falla internet o se pierde el padre. Un PIN universal sería una debilidad.

## Decisión

Códigos aleatorios por propósito y un solo uso; alternativa de desafío QR firmado para mantenimiento/retirada. La retirada exige presencia física y confirmación explícita de borrado.

## Consecuencias y alternativas descartadas

No hay borrado remoto por WebSocket ni un endpoint maestro de soporte. Recuperación de red no habilita uso normal offline. Es obligatorio probar recuperación antes de endurecer el equipo.

## Validación y revisión

H-REC-01/02/03/04, H-RET-01. Referencia a las APIs de retirada/borrado [S03].

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.
