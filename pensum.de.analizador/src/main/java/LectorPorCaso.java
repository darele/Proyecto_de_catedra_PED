import elementales.GestorMaterias;
import elementales.Grafo;
import elementales.Materia;
import elementales.Vertice;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LectorPorCaso {
    private Scanner sc;

    public LectorPorCaso(String filename) {
        try {
            this.sc = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Archivo no encontrado", e);
        }
    }

    public void close() {
        this.sc.close();
    }

    public LectorPorCaso(Scanner sc) {
        this.sc = sc;
    }

    private Materia leerMateria(String line) {
        Materia materia;
        String nombre, codigo;
        int unidadesValorativas;
        List<String> prerequisitos;
        nombre = line;
        if (sc.hasNextLine()) {
            line = sc.nextLine().trim();
            codigo = line;
        } else {
            return null;
        }
        if (sc.hasNextLine()) {
            line = sc.nextLine().trim();
            String[] splitLine = line.split(",");
            for (int i = 0; i < splitLine.length; i++) {
                splitLine[i] = splitLine[i].trim();
            }
            prerequisitos = Arrays.asList(splitLine);
        } else {
            return null;
        }
        if (sc.hasNextLine()) {
            line = sc.nextLine().trim();
            try {
                unidadesValorativas = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.err.println("Error: no se pudo leer unidades valorativas: " + line);
                return null;
            }
        } else {
            return null;
        }
        materia = new Materia(codigo, nombre, unidadesValorativas, prerequisitos);
        return materia;
    }

    public Grafo crearGrafo() {
        Grafo grafo;
        GestorMaterias gestorMaterias = new GestorMaterias();
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            Materia materia = leerMateria(line);
            if (materia == null) {
                System.err.println("Error: no se pudo leer materia, faltan datos");
                continue;
            }
            gestorMaterias.addMateria(materia);
        }
        if (gestorMaterias.crearMaterias()) {
            Map<Materia, Vertice> materiaVerticeMap = new HashMap<>();
            List<Vertice> vertices = new ArrayList<>();
            for (Materia materia : gestorMaterias.getListaMaterias().values()) {
                Vertice vertice = new Vertice(materia);
                materiaVerticeMap.put(materia, vertice);
                vertices.add(vertice);
            }
            grafo = new Grafo(vertices);
            for (Materia materia1 : gestorMaterias.getListaMaterias().values()) {
                for (Materia materia2 : materia1.getPrerequisitos()) {
                    Vertice verticeOrigen = materiaVerticeMap.get(materia2);
                    Vertice verticeDestino = materiaVerticeMap.get(materia1);
                    verticeDestino.incrementarDegree();
                    verticeOrigen.addVecino(verticeDestino);
                }
            }
            return grafo;
        }
        return null;
    }
}