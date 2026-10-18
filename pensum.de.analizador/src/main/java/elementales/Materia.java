package elementales;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Materia {
    public String getCodigo() {
        return codigo;
    }

    private String codigo;
    private String nombre;
    private int unidadesValorativas;
    private final List<Materia> prerequisitos;

    public List<String> getCodigoPrerequisitos() {
        return codigoPrerequisitos;
    }

    private final List<String> codigoPrerequisitos;

    public Materia(String codigo, String nombre, int uV, List<String> prerequisitos) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.unidadesValorativas = uV;
        this.codigoPrerequisitos = prerequisitos;
        this.prerequisitos = new ArrayList<>();
    }

    public void terminar(Map<String, Materia> listaMaterias) {
        for (String codigo : codigoPrerequisitos) {
            if (codigo.equals("Bachillerato")) {
                continue;
            }
            Materia materia = listaMaterias.get(codigo);
            prerequisitos.add(materia);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Materia{").append("\n");
        sb.append("\tnombre").append(nombre).append("\n");
        sb.append("\tcodigo=").append(codigo).append("\n");
        sb.append("\tUV=").append(unidadesValorativas).append("\n");
        sb.append("\tprerequisitos=").append("\n");
        for (Materia materia : prerequisitos) {
            sb.append("\t\t" + materia.getCodigo()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
