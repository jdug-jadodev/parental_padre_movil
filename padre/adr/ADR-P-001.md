# ADR-P-001 — Dos APK y núcleo compartido

Estado: aceptado para el diseño v1; implementación por validar. Fecha: 2026-10-08.

## Contexto

La misma app con un selector de rol simplifica instalación, pero facilita mezclar permisos y lógica. Se necesita una separación entendible para un principiante.

## Decisión

Crear `app-parent` y `app-child` con applicationId diferentes, en el mismo repositorio Kotlin, y una biblioteca `core-protocol`. El padre no incluye DeviceAdminReceiver. El hijo no incluye una opción para convertirse en padre.

## Consecuencias y alternativas descartadas

Se comparten DTO, firma y pruebas, pero no se comparten bases de datos ni claves privadas. Se generan dos APK. Usar una sola app, un framework web o dos repositorios independientes queda rechazado para v1.

## Validación y revisión

P-F01, P-F03, H-SEC-01. Revisar solo al incorporar otra plataforma.

Fuentes y límites: `compartido/02_FUENTES_Y_COMPATIBILIDAD.md`. No modificar este ADR sin actualizar las especificaciones afectadas.
