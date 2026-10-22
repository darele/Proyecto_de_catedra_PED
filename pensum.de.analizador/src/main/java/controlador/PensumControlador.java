package controlador;

import elementales.Grafo;
import elementales.Materia;
import elementales.Vertice;
import modelo.Carrera;
import modelo.RepositorioPensum;
import vista.VentanaPrincipal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Coordina la vista con el modelo. No contiene componentes Swing. */
public class PensumControlador {
    private final RepositorioPensum repositorio = new RepositorioPensum();
    private final VentanaPrincipal vista;
    private final Map<String, String> estados = new HashMap<>();
    private final Map<String, String> ciclosCursados = new HashMap<>();

    public PensumControlador() {
        vista = new VentanaPrincipal(repositorio.obtenerCarreras().keySet());
        vista.alCambiarCarrera(this::mostrarCarrera);
        vista.alGenerarRuta(this::generarRuta);
        vista.alAbrirMateria(this::abrirMateria);
        mostrarCarrera(vista.getCarreraSeleccionada());
    }

    public void iniciar() {
        vista.setVisible(true);
    }

    private Carrera carreraActual() {
        return repositorio.obtenerCarreras().get(vista.getCarreraSeleccionada());
    }

    private void mostrarCarrera(String nombre) {
        Carrera carrera = repositorio.obtenerCarreras().get(nombre);
        if (carrera != null) {
            actualizarEstados(carrera);
            vista.mostrarCarrera(carrera, estados);
        }
    }

    private void generarRuta() {
        Carrera carrera = carreraActual();
        if (carrera == null) return;
        actualizarEstados(carrera);
        vista.mostrarCarrera(carrera, estados);
        Grafo grafo = construirGrafo(carrera.materias());
        if (grafo.hayCiclo()) {
            vista.mostrarResultado("No se puede generar una ruta: se detectaron prerequisitos cíclicos.");
            return;
        }
        List<Materia> ruta = grafo.ordenTopologico();
        StringBuilder resultado = new StringBuilder("Simulación según tu avance\n\n");
        for (int i = 0; i < ruta.size(); i++) {
            Materia materia = ruta.get(i);
            String estado = descripcionEstado(materia.getCodigo());
            resultado.append(i + 1).append(". ")
                    .append(materia.getNombre()).append(" — ")
                    .append(materia.getCodigo());
            if (!estado.isEmpty()) resultado.append(" · ").append(estado);
            resultado.append("\n");
        }
        long aprobadas = carrera.materias().stream()
                .filter(materia -> "APROBADA".equals(estados.get(materia.getCodigo())))
                .count();
        resultado.append("\nMaterias completadas: ").append(aprobadas)
                .append(" de ").append(carrera.materias().size()).append(". ");
        if (aprobadas == carrera.materias().size()) {
            resultado.append("¡Felicidades! Has completado todo el pensum.");
        } else if (aprobadas > 0) {
            resultado.append("¡Felicidades, sigue avanzando!");
        } else {
            resultado.append("¡Tu ruta está lista para comenzar!");
        }
        vista.mostrarResultado(resultado.toString());
    }

    private String descripcionEstado(String codigo) {
        return switch (estados.getOrDefault(codigo, "PENDIENTE")) {
            case "REPROBADA" -> "REPROBADA (debe repetirse)";
            default -> "";
        };
    }

    private void abrirMateria(String codigo) {
        Carrera carrera = carreraActual();
        if (carrera == null) return;
        Materia materia = carrera.materias().stream().filter(m -> m.getCodigo().equals(codigo)).findFirst().orElse(null);
        if (materia == null) return;
        boolean bloqueada = "PENDIENTE".equals(estados.getOrDefault(codigo, "PENDIENTE")) && !estaDisponible(materia);
        vista.mostrarDetalle(materia, bloqueada, estados.getOrDefault(codigo, "PENDIENTE"), ciclosCursados.get(codigo),
                (estado, ciclo) -> { estados.put(codigo, estado); if ("PENDIENTE".equals(estado)) ciclosCursados.remove(codigo); else ciclosCursados.put(codigo, ciclo); mostrarCarrera(carrera.nombre()); });
    }

    private boolean estaDisponible(Materia materia) {
        return materia.getCodigoPrerequisitos().stream().filter(c -> !c.equals("Bachillerato"))
                .allMatch(c -> "APROBADA".equals(estados.get(c)));
    }

    private void actualizarEstados(Carrera carrera) {
        for (Materia materia : carrera.materias()) {
            if (!"APROBADA".equals(estados.get(materia.getCodigo())) && !"REPROBADA".equals(estados.get(materia.getCodigo()))) {
                estados.put(materia.getCodigo(), estaDisponible(materia) ? "PENDIENTE" : "BLOQUEADA");
            }
        }
    }

    /** Adapta las materias del modelo a la estructura de grafo ya implementada en el proyecto. */
    private Grafo construirGrafo(List<Materia> materias) {
        Map<Materia, Vertice> vertices = new HashMap<>();
        List<Vertice> listaVertices = new ArrayList<>();
        Map<String, Materia> porCodigo = new HashMap<>();
        for (Materia materia : materias) porCodigo.put(materia.getCodigo(), materia);
        for (Materia materia : materias) {
            materia.terminar(porCodigo);
            Vertice vertice = new Vertice(materia);
            vertices.put(materia, vertice);
            listaVertices.add(vertice);
        }
        for (Materia destino : materias) {
            for (Materia origen : destino.getPrerequisitos()) {
                vertices.get(destino).incrementarDegree();
                vertices.get(origen).addVecino(vertices.get(destino));
            }
        }
        return new Grafo(listaVertices);
    }
}
