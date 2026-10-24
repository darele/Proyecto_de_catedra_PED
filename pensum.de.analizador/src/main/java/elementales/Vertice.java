package elementales;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Vertice {
    private final Materia materia;
    private final List<Vertice> vecinos;
    private int indegree;
    private boolean visitado;

    public int getIndegree() {
        return indegree;
    }

    public void reducirDegree() {
        indegree--;
    }

    public void incrementarDegree() {
        indegree++;
    }

    void reset() {
        visitado = false;
        indegree = 0;
    }

    void visitar() {
        visitado = true;
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
        visitado = false;
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

    public boolean isVisitado() {
        return visitado;
    }

}
