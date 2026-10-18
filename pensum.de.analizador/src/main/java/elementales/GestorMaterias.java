package elementales;

import java.util.Map;
import java.util.TreeMap;

public class GestorMaterias {
    private final Map<String, Materia> listaMaterias = new TreeMap<>();

    public Map<String, Materia> getListaMaterias() {
        return listaMaterias;
    }

    public void addMateria(Materia materia) {
        if (listaMaterias.containsKey(materia.getCodigo())) {
            return;
        }
        listaMaterias.put(materia.getCodigo(), materia);
    }

    public boolean crearMaterias() {
        for (Materia materia : listaMaterias.values()) {
            for (String codigo : materia.getCodigoPrerequisitos()) {
                if (codigo.equals("Bachillerato")) {
                    continue;
                }
                if (!listaMaterias.containsKey(codigo)) {
                    System.err.println(
                            "No se puede crear la materia con el " +
                            "codigo: " + codigo + " porque los prerequisitos " +
                            "no existen en el pensum");
                    return false;
                }
            }
            System.out.println("materia terminada: " + materia.getCodigo());
            materia.terminar(listaMaterias);
        }
        return true;
    }
}
