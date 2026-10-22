# Proyecto de cátedra PED — Analizador de pensum

## Avance de Fase 2

Este repositorio contiene un piloto funcional de escritorio para explorar pensums universitarios. La interfaz permite seleccionar una carrera de ejemplo, consultar su malla y generar una ruta de cursado basada en el grafo de prerrequisitos.

El objetivo de esta fase no es entregar el sistema completo, sino demostrar que el modelo de grafo y el ordenamiento topológico sustentan la funcionalidad principal.

### Flujo demostrable

1. Abrir la aplicación.
2. Seleccionar una carrera en el catálogo.
3. Consultar asignaturas, UV y prerrequisitos.
4. Presionar **Generar ruta sugerida**.
5. Ver el orden topológico de cursado hasta la graduación.

### Arquitectura

Se usa una separación MVC ligera:

- `elementales`: modelo de dominio y estructuras de datos (`Materia`, `Vertice`, `Grafo`).
- `modelo`: datos de carreras del piloto.
- `controlador`: coordina la interfaz con el grafo.
- `vista`: interfaz Java Swing.

### Ejecución

Se requiere JDK 25 y Maven. Desde la carpeta `pensum.de.analizador`:

```powershell
mvn test
mvn exec:java
```

> Si el objetivo `exec:java` no está disponible en tu instalación, ejecuta `Main` directamente desde IntelliJ IDEA.

### Documentación de Fase 2

- [Casos de prueba](documentacion/CASOS_DE_PRUEBA.md)
- [Bitácora de dificultades](documentacion/BITACORA_DIFICULTADES.md)
- [Plan de cierre](documentacion/PLAN_DE_CIERRE.md)
