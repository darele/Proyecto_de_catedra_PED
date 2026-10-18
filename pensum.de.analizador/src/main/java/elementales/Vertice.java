package elementales;

import java.util.ArrayList;
import java.util.List;

public class Vertice {
    private final int indice;
    private final Materia materia;
    private final List<Vertice> vecinos;

    public int getIndice() {
        return indice;
    }

    public Materia getMateria() {
        return materia;
    }

    public List<Vertice> getVecinos() {
        return vecinos;
    }

    public void addVecino(Vertice vertice) {
        vecinos.add(vertice);
    }

    public Vertice(int indice, Materia materia) {
        this.indice = indice;
        this.materia = materia;
        this.vecinos = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.materia.getCodigo() + ": ");
        for (Vertice v : vecinos) {
            sb.append(v.materia.getCodigo() + ", ");
        }
        return sb.toString();
    }
}
