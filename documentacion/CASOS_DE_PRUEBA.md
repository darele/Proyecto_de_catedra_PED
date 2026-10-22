# Casos de prueba — Fase 2

| ID | Caso | Entrada / acción | Resultado esperado | Estado |
|---|---|---|---|---|
| CP-01 | Lectura de pensum simple | Archivo `materias.txt` | Se crean cuatro materias y sus relaciones. | Automatizado |
| CP-02 | Detección de ciclos | Grafo con dependencias cíclicas | `hayCiclo()` devuelve `true` y no se ofrece ruta. | Automatizado |
| CP-03 | Ordenamiento topológico | Pensum sin ciclos | Cada materia aparece después de sus prerrequisitos. | Automatizado |
| CP-04 | Consulta de carrera | Elegir una carrera en la interfaz | Se actualizan título, resumen y tabla de materias. | Manual (demo) |
| CP-05 | Ruta sugerida | Botón **Generar ruta sugerida** | Se visualiza una secuencia válida terminada en “Graduación”. | Manual (demo) |

La prueba automatizada actual se encuentra en `pensum.de.analizador/src/test/java/TesterGrafo.java`.

## Criterios de aceptación del flujo principal

- Una carrera muestra código, nombre, UV y prerrequisitos de cada materia.
- Las asignaturas sin prerrequisitos aparecen primero en la ruta.
- El sistema informa la imposibilidad de generar ruta ante una dependencia cíclica.
