package elementales;

import java.util.ArrayList;
import java.util.List;

public class Vertice {
    private final Materia materia;
    private final List<Vertice> vecinos;
    private int indegree;

    public int getIndegree() {
        return indegree;
    }

    public void reducirDegree() {
        indegree--;
    }

    public void incrementarDegree() {
        indegree++;
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

    public Vertice(Materia materia) {
        this.materia = materia;
        this.vecinos = new ArrayList<>();
        this.indegree = 0;
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
