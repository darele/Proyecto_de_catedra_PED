import elementales.Grafo;
import elementales.Materia;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        String directorio = "pensum.de.analizador/src/test/Casos_de_prueba/";
        String[] casos = {
//                "materias.txt",
//                "caso1_cadena_prerrequisitos.txt",
//                "caso2_multiples_prerrequisitos.txt",
//                "caso3_ciclo_prerrequisitos.txt",
                "caso4_prerrequisito_inexistente.txt",
//                "pensum_completo_UDB.txt"
        };
        for  (String s : casos) {
            System.out.println("\n\nProcesando " + s + "\n\n");
            String caso = directorio + s;
            try {
                Scanner sc = new Scanner(new File(caso));
                LectorPorCaso lector =  new LectorPorCaso(sc);
                Grafo pensum = lector.crearGrafo();
                if (pensum != null) {
                    System.out.println("exito, grafo leido:\n\n" + pensum);
                } else {
                    continue;
                }
                if (pensum.hayCiclo()) {
                    System.err.println("Imposible cursar la carrera, hay dependencias ciclicas");
                    continue;
                }
                List<Materia> orden = pensum.ordenTopologico();
                System.out.println("Orden de cursado encontrado\n");
                for (Materia materia : orden) {
                    System.out.print(materia.getCodigo() + "->");
                }
                System.out.println("Graduado");
            } catch (FileNotFoundException e) {
                System.out.println("No existe el archivo " + caso);
            }
        }
    }
}