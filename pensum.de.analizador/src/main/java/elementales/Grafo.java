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

    private void reset() {
        for (Vertice v : vertices) {
            v.reset();
        }
        ordenTopologico.clear();
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

        int uvAcumuladas = 0;
        for (Vertice v : vertices) {
            if (v.getIndegree() == 0 && uvAcumuladas >= v.getMateria().getPrerequisitoUV()) {
                q.add(v);
            }
        }
        while (true) {
            if (q.isEmpty()) {
                for (Vertice v : vertices) {
                    if (!v.isVisitado()
                            && v.getIndegree() == 0
                            && uvAcumuladas >= v.getMateria().getPrerequisitoUV()) {
                        q.add(v);
                    }
                }
                if (q.isEmpty()) {
                    break;
                }
            }
            Vertice u = q.poll();
            u.visitar();
            Materia materia = u.getMateria();
            ordenTopologico.add(materia);
            uvAcumuladas += materia.getUnidadesValorativas();
            for (Vertice v : u.getVecinos()) {
                v.reducirDegree();
                if (v.getIndegree() == 0 && uvAcumuladas >= v.getMateria().getPrerequisitoUV()) {
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

    public void addMateria(Materia materia) {
        reset();
        Map<String, Materia> mapCodigoMatreria = new HashMap<>();
        Map<Materia, Vertice> mapMateriaVertice = new HashMap<>();
        for (Vertice v : vertices) {
            Materia m = v.getMateria();
            mapCodigoMatreria.put(m.getCodigo(), v.getMateria());
            mapMateriaVertice.put(m, v);
        }
        materia.terminar(mapCodigoMatreria);
        Vertice vertice = new Vertice(materia);
        for (Materia m : materia.getPrerequisitos()) {
            mapMateriaVertice.get(m).addVecino(vertice);
        }
        vertices.add(vertice);
        for (Vertice u : vertices) {
            for (Vertice v : u.getVecinos()) {
                v.incrementarDegree();
            }
        }
    }
    
        public List<Materia> getMateriasBloqueadas() {
        hayCiclo();
        List<Materia> bloqueadas = new ArrayList<>();
        for (Vertice v : vertices) {
            if (!v.isVisitado()) {
                bloqueadas.add(v.getMateria());
            }
        }
        return bloqueadas;
    }

    public boolean bloqueoPorUV() {
        hayCiclo();
        for (Vertice v : vertices) {
            if (!v.isVisitado() && v.getIndegree() == 0) {
                return true;
            }
        }
        return false;
    }
}