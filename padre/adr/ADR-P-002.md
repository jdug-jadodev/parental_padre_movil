# ADR-P-002 — Autoridad y datos locales

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

## Contexto

No se necesita una cuenta central ni conservar historial. Sí se necesita proteger la autoridad y recuperar la pérdida del padre.

## Decisión

Clave P-256 no exportable en Keystore y autenticación del adulto. Metadatos atómicos cifrados y una intención pendiente local. Sin base de datos ni backup automático. Los códigos de recuperación se preparan presencialmente.

## Consecuencias y alternativas descartadas

No hay inicio de sesión por correo ni restauración de una clave desde Render. Si se pierde la clave se crea un nuevo vínculo mediante recuperación física. El navegador futuro necesitará delegación, no exportación automática de la privada.

## Validación y revisión

P-F02, P-F07, P-KEY-01, H-REC-04. Fuente técnica [S10].

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.
