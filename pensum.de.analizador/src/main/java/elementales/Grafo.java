package elementales;

import java.util.List;

public class Grafo {
    List<Vertice> vertices;

    public Grafo(List<Vertice> vertices) {
        this.vertices = vertices;
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
}
