package modelo;

import elementales.Materia;
import java.util.*;

/** Pensum oficial de referencia para el avance funcional. */
public class RepositorioPensum {
    private final Map<String, Carrera> carreras = new LinkedHashMap<>();
    public RepositorioPensum() { carreras.put("Ingeniería en Ciencias de la Computación", crearComputacion()); }

    private Carrera crearComputacion() {
        List<Materia> m = new ArrayList<>(); Map<String,Integer> c = new HashMap<>();
        a(m,c,1,"CAD941","Cálculo Diferencial","Bachillerato"); a(m,c,1,"EOE901","Expresión Oral y Escrita","Bachillerato"); a(m,c,1,"PRE941","Programación Estructurada","Bachillerato"); a(m,c,1,"QUG941","Química General","Bachillerato");
        a(m,c,2,"AVM941","Álgebra Vectorial y Matrices","Bachillerato"); a(m,c,2,"CAI941","Cálculo Integral","CAD941"); a(m,c,2,"CDP941","Cinemática y Dinámica de Partículas","CAD941"); a(m,c,2,"MDB941","Modelamiento y Diseño de Base de Datos","PRE941"); a(m,c,2,"POO941","Programación Orientada a Objetos","PRE941");
        a(m,c,3,"ADS941","Análisis y Diseño de Sistemas Informáticos","MDB941","POO941"); a(m,c,3,"CVV941","Cálculo de Varias Variables","AVM941","CAI941"); a(m,c,3,"ESA941","Estadística Aplicada","CAI941"); a(m,c,3,"EYM941","Electricidad y Magnetismo","CAI941","CDP941","QUG941"); a(m,c,3,"PED941","Programación con Estructuras de Datos","POO941");
        a(m,c,4,"CAA941","Cálculo Avanzado","CVV941"); a(m,c,4,"DMD941","Datawarehouse y Minería de Datos","MDB941"); a(m,c,4,"EDI941","Ecuaciones Diferenciales","CVV941"); a(m,c,4,"LIC941","Lenguajes Interpretados en el Cliente","MDB941","POO941"); a(m,c,4,"OFC941","Oscilaciones, Fluidos y Calor","CAI941","CDP941");
        a(m,c,5,"ACE941","Análisis de Circuitos Eléctricos","EYM941"); a(m,c,5,"ACO941","Arquitectura de Computadoras","EYM941","PRE941"); a(m,c,5,"AEE941","Análisis y Evaluación Económica","ESA941"); a(m,c,5,"ANF901","Antropología Filosófica","Bachillerato"); a(m,c,5,"GEA941","Gestión Ambiental","QUG941");
        a(m,c,6,"DDP941","Dirección de Proyectos","AEE941","GEA941"); a(m,c,6,"DRD941","Diseño de Redes de Datos","Bachillerato"); a(m,c,6,"PSC901","Pensamiento Social Cristiano","Bachillerato"); a(m,c,6,"SIO941","Sistemas Operativos","ACO941");
        a(m,c,7,"AMN941","Aplicación de Métodos Numéricos","ACE941","EDI941","PRE941"); a(m,c,7,"IRD941","Interconexión de Redes de Datos","DRD941"); a(m,c,7,"ISO941","Ingeniería de Software","ADS941","DDP941"); a(m,c,7,"LIS941","Lenguajes Interpretados en el Servidor","LIC941");
        a(m,c,8,"AYC941","Autómatas y Compiladores","AMN941","PED941"); a(m,c,8,"DPS941","Diseño y Programación de Software Multiplataforma","DMD941","PED941"); a(m,c,8,"DSS941","Diseño de Sistemas de Seguridad para Redes de Datos","DRD941","IRD941"); a(m,c,8,"GCS941","Gestión de la Calidad del Software","ISO941");
        a(m,c,9,"ASR941","Administración de Servicios de Red Propietarios","DRD941","IRD941"); a(m,c,9,"DSM941","Desarrollo de Software para Móviles","DPS941"); a(m,c,9,"NTI941","Normalización de Tecnologías de la Información","ISO941"); a(m,c,9,"SGC941","Seminario de Gestión del Conocimiento"); a(m,c,9,"SSI941","Seminario de Seguridad Informática"); a(m,c,9,"STI941","Seminario de Gobierno de TI");
        a(m,c,10,"ASI941","Administración de Servicios de Red Libres","DRD941","IRD941"); a(m,c,10,"AUS941","Auditoría de Sistemas","ISO941"); a(m,c,10,"DSE941","Desarrollo de Software Empresarial","DMD941","LIS941"); a(m,c,10,"SCN941","Seminario de Gestión de Continuidad del Negocio","STI941"); a(m,c,10,"SGA941","Seminario de Aplicación del Conocimiento","SGC941"); a(m,c,10,"SIN941","Seminario de Inteligencia de Negocios","SSI941");
        return new Carrera("Ingeniería en Ciencias de la Computación", "UDB Virtual", "Pensum oficial: consulta los requisitos y registra el avance académico.", m, c);
    }
    private void a(List<Materia> l, Map<String,Integer> c, int ciclo, String cod, String nom, String... req) { l.add(new Materia(cod,nom,4,List.of(req))); c.put(cod,ciclo); }
    public Map<String,Carrera> obtenerCarreras() { return Collections.unmodifiableMap(carreras); }
}
