# Instrucciones de OpenCode — proyecto PADRE

**Proyecto autónomo del padre.** Solo implementar aplicación del padre. El servidor Render pertenece al proyecto separado `C:\Users\Usuario\Documents\parental_render` y se desarrollará allí en otro turno. No crear ni modificar el repositorio del otro rol.

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
- Separación realizada: servidor y sus tests en parental_render/relay, contrato/fixtures congelados copiados a ese proyecto. No desarrollar ni añadir servidor aquí. Las referencias históricas a «Padre + Render» no autorizan mezclar proyectos. Verificación local del padre independiente de npm/servidor; cruce con codec del servidor solo opcional mediante -PrutaRelay. No cambiar CP/1 unilateralmente. Desarrollar servidor posteriormente solo en parental_render.
- Al completar la parte del padre preparada para integración, avisar al propietario para que orqueste una IA en parental_render. La comunicación entre ambas apps debe probarse primero en entorno local antes del despliegue. No lanzar agentes ni desplegar por este acuerdo; el propietario organizará ese trabajo. Mantener los permisos de dispositivo y la exclusión del A13.
- Trabajar una tarea por turno. Empezar por C00 y respetar las dependencias del plan.
- Hacer un commit al llegar a cada bloque de 500 líneas de código escritas o modificadas manualmente; cerrar también cada tarea con un commit aunque el bloque sea menor. Contar código y pruebas, no documentación, dependencias, archivos generados ni fixtures congelados. No añadir relleno para alcanzar 500 líneas. Revisar lo preparado antes de cada commit y no incluir secretos.
- Antes de instalar, ejecutar pruebas o cambiar algo en un dispositivo por ADB, avisar y esperar a que el propietario lo habilite. Las compilaciones y pruebas locales no necesitan ese dispositivo.
- No restablecer ni aprovisionar teléfonos sin autorización específica. No usar el teléfono cotidiano como banco de pruebas destructivas.
- El teléfono autorizado para este proyecto es el padre A56. **No tocar el A13**: no ejecutar en él comandos de dispositivo, instalaciones, pruebas ni cambios, aunque esté conectado o autorizado por ADB. Seleccionar explícitamente el A56 en cada operación; no usar pruebas contra todos los dispositivos conectados. Esta exclusión solo cambia con autorización expresa nueva del propietario.
- Mantener `ESTADO.md` actualizado con lo demostrado, lo pendiente, los archivos afectados y la próxima tarea. No declarar pruebas físicas aprobadas sin ejecutarlas.
- El propietario autoriza avanzar autónomamente con edición, compilación, pruebas locales y commits, sin pedir permiso por cada paso. Respetar una tarea por turno y detener el acceso al dispositivo hasta que habilite ADB; no extender esta autorización a restablecimientos, aprovisionamiento, despliegue público o cambios de CP/1.
