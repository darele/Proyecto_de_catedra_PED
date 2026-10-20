import elementales.Grafo;
import elementales.Materia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TesterGrafo {
    private LectorPorCaso lector;
    private String rutaCasos = "src/test/Casos_de_prueba/";
    private List<String> archivosDeCasos = List.of(
            "materias.txt"
    );
    List<String> respuesta1 = List.of("CAD", "CAI", "CVV", "EDI");

    @Test
    void test1() {
        String fileName = rutaCasos + archivosDeCasos.getFirst();
        System.out.println("Revisando que se pueda abrir el archivo de caso de prueba");
        assertDoesNotThrow(() -> new LectorPorCaso(fileName));
        lector =  new LectorPorCaso(fileName);
        Grafo pensum = lector.crearGrafo();
        System.out.println("Revisando que en el caso de prueba 1 no se encontraron ciclos");
        assertFalse(pensum.hayCiclo());
        lector.close();
        lector = new LectorPorCaso(fileName);
        pensum = lector.crearGrafo();
        List<Materia> ans = pensum.ordenTopologico();
        System.out.println("Revisando que el tamano de la respuesta es el mismo que la cantidad de materias en el pensum");
        assertEquals(ans.size(), pensum.getVertices().size());
        int index = 0;
        System.out.println("Revisando que el orden topologico sea el correcto");
        for (Materia materia : ans) {
            assertEquals(materia.getCodigo(), respuesta1.get(index++));
        }
        System.out.println("Test 1 exitoso");
    }
}
