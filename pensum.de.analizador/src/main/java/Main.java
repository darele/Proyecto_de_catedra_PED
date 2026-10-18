import elementales.Grafo;

import java.io.File;
import java.io.FileNotFoundException;
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
                System.out.println("exito, grafo leido:\n\n" + pensum.toString());
            }
        } catch (FileNotFoundException e) {
            System.out.println("No existe el archivo " + caso1);
        }
    }
}
