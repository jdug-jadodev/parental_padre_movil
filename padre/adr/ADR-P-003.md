# ADR-P-003 — Relay sin estado de negocio en Render

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

## Contexto

El propietario prefiere Render, WebSockets y ausencia de base de datos e historial.

## Decisión

Un web service Node 24/TypeScript con `ws`, una instancia y registro público fijo por variables de entorno. Guardar solo sockets, desafíos y límites temporales en memoria.

## Consecuencias y alternativas descartadas

Un reinicio descarta conexiones y órdenes no entregadas. La configuración pública es necesaria para autenticar tras reinicio. No Redis, NestJS, Postgres, Firebase ni Docker obligatorio. Multi-instancia y buzón durable exigirían otro ADR.

## Validación y revisión

R-AUTH-01, R-ROUTE-01, R-FAIL-01. Fuentes [S11–S13, S23, S26].

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.
