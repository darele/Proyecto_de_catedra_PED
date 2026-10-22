# Bitácora de dificultades — Fase 2

| Fecha | Dificultad | Impacto | Decisión / abordaje | Estado |
|---|---|---|---|---|
| 2026-10-21 | El núcleo existente operaba por consola y no estaba conectado a una pantalla. | No había demo visual del análisis. | Se añadió una vista Swing y un controlador que transforma las materias del piloto en el `Grafo` existente. | Resuelto para el piloto |
| 2026-10-21 | Las materias podían reutilizarse para calcular rutas más de una vez; los prerrequisitos terminados quedaban acumulados. | Podían duplicarse aristas en cálculos posteriores. | Se limpia la colección antes de reconstruir relaciones en `Materia.terminar`. | Resuelto |
| 2026-10-21 | Los datos de carreras reales aún deben validarse con su fuente académica. | El catálogo presentado no debe asumirse oficial. | Se usa un repositorio temporal claramente identificado como datos de demostración. | Pendiente |
| 2026-10-21 | El ambiente de desarrollo debe contar con JDK y Maven para compilar y ejecutar. | No se puede verificar localmente sin esas herramientas. | Documentar requisitos y ejecutar la verificación en la máquina del equipo / CI. | Pendiente |

## Aprendizajes

El grafo y el ordenamiento topológico resolvieron bien el flujo de dependencias. Separar los datos temporales de la vista permitirá sustituirlos por archivos o una base de datos sin rediseñar las pantallas.
