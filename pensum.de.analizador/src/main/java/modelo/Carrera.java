package modelo;

import elementales.Materia;

import java.util.List;
import java.util.Map;

/** Datos que la interfaz presenta para una carrera. */
public record Carrera(String nombre, String facultad, String descripcion, List<Materia> materias,
                      Map<String, Integer> ciclosPorCodigo) {
    public int totalUV() {
        return materias.stream().mapToInt(Materia::getUnidadesValorativas).sum();
    }

    public int cicloDe(Materia materia) { return ciclosPorCodigo.getOrDefault(materia.getCodigo(), 0); }
}
