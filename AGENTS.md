# Instrucciones de OpenCode — proyecto PADRE

**Proyecto autónomo.** Solo implementar aplicación del padre y relay Render. No crear ni modificar el repositorio del otro rol.

1. Leer `00_EMPIEZA_AQUI.md`, `ESTADO.md` y `padre/AGENTS.md`.
2. Elegir una sola tarea de `padre/PLAN_PADRE.md` o C00-C02 en `compartido/03_INICIO_TECNICO.md`.
3. Consultar solamente las secciones precisas de las SPEC/ADR y del contrato `compartido/00_CONTRATO_V1.md` indicadas para la tarea.
4. Implementar código y pruebas; ejecutar lo que permita el entorno; no afirmar pruebas que no se hicieron.
5. Actualizar `ESTADO.md`, registrar rutas modificadas y próxima tarea. Para otra sesión ver `pruebas/HANDOFF_PLANTILLA.md`.

**Regla de integración:** `compartido/` contiene una copia local de CP/1 y fixtures congelados. No cambiar el esquema, tipos de mensajes, algoritmo de firmas o semántica unilateralmente. Proponer el cambio en `INTEGRACION_FUTURA.md` y, antes de sincronizar, acordar versión nueva y actualizar ambos repositorios juntos.

**Regla de seguridad:** no incorporar secretos, claves privadas ni códigos reales en repositorio; emergencia siempre accesible; no deshabilitar recuperación por un intento de endurecimiento; no indicar que un ACK de relay significa bloqueo aplicado.

**Primer paso:** C00 de `compartido/03_INICIO_TECNICO.md`.

## Acuerdos con el propietario — obligatorios para futuras sesiones

- Comunicación, documentación, comentarios, textos de la app y mensajes de commit en español claro. Conservar los nombres exigidos por CP/1, APIs y herramientas; no traducir campos del protocolo.
- Trabajar una tarea por turno. Empezar por C00 y respetar las dependencias del plan.
- Hacer un commit al llegar a cada bloque de 500 líneas de código escritas o modificadas manualmente; cerrar también cada tarea con un commit aunque el bloque sea menor. Contar código y pruebas, no documentación, dependencias, archivos generados ni fixtures congelados. No añadir relleno para alcanzar 500 líneas. Revisar lo preparado antes de cada commit y no incluir secretos.
- Antes de instalar, ejecutar pruebas o cambiar algo en un dispositivo por ADB, avisar y esperar a que el propietario lo habilite. Las compilaciones y pruebas locales no necesitan ese dispositivo.
- No restablecer ni aprovisionar teléfonos sin autorización específica. No usar el teléfono cotidiano como banco de pruebas destructivas.
- Mantener `ESTADO.md` actualizado con lo demostrado, lo pendiente, los archivos afectados y la próxima tarea. No declarar pruebas físicas aprobadas sin ejecutarlas.
