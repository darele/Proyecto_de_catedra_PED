package elementales;

import java.util.*;

public class Grafo {
    public List<Vertice> getVertices() {
        return vertices;
    }

    private final List<Vertice> vertices;
    private final List<Materia> ordenTopologico;
    private boolean ciclo;

    public Grafo(List<Vertice> vertices) {
        this.vertices = vertices;
        this.ordenTopologico = new ArrayList<>();
        ciclo = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Vertice v : vertices) {
            sb.append(v.toString()).append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    private void calcularOrdenTopologico() {
        Queue<Vertice> q = new ArrayDeque<>();
        for (Vertice v : vertices) {
            if (v.getIndegree() == 0) {
                q.add(v);
            }
        }
        while (!q.isEmpty()) {
            Vertice u = q.poll();
            ordenTopologico.add(u.getMateria());
            for (Vertice v : u.getVecinos()) {
                v.reducirDegree();
                if (v.getIndegree() == 0) {
                    q.add(v);
                }
            }
        }
        if (ordenTopologico.size() != vertices.size()) {
            ciclo = true;
        }
    }

    public List<Materia> ordenTopologico() {
        if (this.ordenTopologico.isEmpty()) {
            calcularOrdenTopologico();
        }
        if (ciclo) {
            return null;
        }
        return this.ordenTopologico;
    }

    public boolean hayCiclo() {
        if (this.ordenTopologico.isEmpty()) {
            calcularOrdenTopologico();
        }
        return this.ciclo;
    }
}