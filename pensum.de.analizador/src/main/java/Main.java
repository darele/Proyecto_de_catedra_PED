import elementales.Grafo;
import elementales.Materia;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        String caso1 = "pensum.de.analizador/src/test/materias.txt";
        try {
            Scanner sc = new Scanner(new File(caso1));
            LectorPorCaso lector =  new LectorPorCaso(sc);
            Grafo pensum = lector.crearGrafo();
            if (pensum != null) {
                System.out.println("exito, grafo leido:\n\n" + pensum);
            }
            if (pensum.hayCiclo()) {
                System.err.println("Imposible cursar la carrera, hay dependencias ciclicas");
                return;
            }
            List<Materia> orden = pensum.ordenTopologico();
            System.out.println("Orden de cursado encontrado\n");
            for (Materia materia : orden) {
                System.out.print(materia.getCodigo() + "->");
            }
            System.out.println("Graduado");
        } catch (FileNotFoundException e) {
            System.out.println("No existe el archivo " + caso1);
        }
    }
}
