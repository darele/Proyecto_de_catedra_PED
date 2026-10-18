import elementales.GestorMaterias;
import elementales.Grafo;
import elementales.Materia;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class LectorPorCaso {
    private final Scanner sc;
    public LectorPorCaso(Scanner sc) {
        this.sc = sc;
    }

    private Materia leerMateria(String line) {
        Materia materia = null;
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
            grafo = null;
            for (Materia materia : gestorMaterias.getListaMaterias().values()) {
                System.out.println(materia);
            }
            return grafo;
        }
        return null;
    }
}
