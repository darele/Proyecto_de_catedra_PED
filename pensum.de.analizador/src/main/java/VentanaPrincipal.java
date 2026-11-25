import elementales.Grafo;
import elementales.Materia;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class VentanaPrincipal extends JFrame {

    /** Una materia tal como la registra el usuario. */
    private static class Registro {
        int num;                                  // número correlativo de la guía
        String codigo;
        String nombre;
        int uv;
        List<String> prereqs = new ArrayList<>(); // códigos de materias previas
        int uvPrevias;                            // 0 = no requiere UV previas
    }

    private final List<Registro> registros = new ArrayList<>();
    private final JTextField txtCarrera = new JTextField();

    private final DefaultTableModel modeloRegistro = new DefaultTableModel(
            new String[]{"Nº", "Código", "Nombre", "UV", "Prerrequisitos"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaRegistro = new JTable(modeloRegistro);
    private final JScrollPane scrollRegistro = new JScrollPane(tablaRegistro);
    private final JLabel estado = new JLabel("Agrega las materias de tu pensum o carga un archivo.");

    public VentanaPrincipal() {
        setTitle("Analizador de Pensum");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Botones
        JButton btnAgregar = new JButton("Agregar materia");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar todo");
        JButton btnCargar = new JButton("Cargar archivo...");
        JButton btnGuardar = new JButton("Guardar archivo...");
        JButton btnAnalizar = new JButton("Analizar pensum");
        btnAnalizar.setFont(btnAnalizar.getFont().deriveFont(Font.BOLD));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        botones.add(btnAgregar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(btnCargar);
        botones.add(btnGuardar);
        botones.add(btnAnalizar);

        // Nombre de la carrera
        JPanel filaCarrera = new JPanel(new BorderLayout(8, 0));
        filaCarrera.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        filaCarrera.add(new JLabel("Nombre de la carrera:"), BorderLayout.WEST);
        filaCarrera.add(txtCarrera, BorderLayout.CENTER);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(filaCarrera, BorderLayout.NORTH);
        norte.add(botones, BorderLayout.CENTER);

        // Tabla de materias agregadas
        tablaRegistro.setRowHeight(26);
        tablaRegistro.getTableHeader().setReorderingAllowed(false);
        int[] anchos = {40, 90, 380, 45, 420};
        for (int i = 0; i < anchos.length; i++) {
            tablaRegistro.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col : new int[]{0, 1, 3}) {
            tablaRegistro.getColumnModel().getColumn(col).setCellRenderer(centrado);
        }
        tablaRegistro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarSeleccionada();
                }
            }
        });

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        centro.add(scrollRegistro, BorderLayout.CENTER);

        estado.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
        refrescarTabla();

        btnAgregar.addActionListener(e -> agregarMaterias());
        btnEditar.addActionListener(e -> editarSeleccionada());
        btnEliminar.addActionListener(e -> eliminarSeleccionadas());
        btnLimpiar.addActionListener(e -> limpiarTodo());
        btnCargar.addActionListener(e -> cargarArchivo());
        btnGuardar.addActionListener(e -> guardarArchivo());
        btnAnalizar.addActionListener(e -> analizar());
    }

    // =====================================================================
    //  Utilidades
    // =====================================================================

    private void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
    }

    private boolean esNumero(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int valor(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException ignorada) {
            // se queda con el último valor válido
        }
        return ((Number) spinner.getValue()).intValue();
    }

    private void ordenarRegistros() {
        registros.sort(Comparator.comparingInt(r -> r.num));
    }

    private int siguienteNum() {
        int max = 0;
        for (Registro r : registros) {
            max = Math.max(max, r.num);
        }
        return max + 1;
    }

    private Registro buscar(String codigo) {
        for (Registro r : registros) {
            if (r.codigo.equals(codigo)) {
                return r;
            }
        }
        return null;
    }

    /** Texto de prerrequisitos para la tabla: "QUG501 (Nº 3), 40 UV previas" o "Bachillerato". */
    private String textoPrereq(Registro r) {
        List<String> partes = new ArrayList<>();
        for (String c : r.prereqs) {
            Registro p = buscar(c);
            partes.add(p == null ? c : c + " (Nº " + p.num + ")");
        }
        if (r.uvPrevias > 0) {
            partes.add(r.uvPrevias + " UV previas");
        }
        return partes.isEmpty() ? "Bachillerato" : String.join(", ", partes);
    }

    private void refrescarTabla() {
        ordenarRegistros();
        modeloRegistro.setRowCount(0);
        for (Registro r : registros) {
            modeloRegistro.addRow(new Object[]{r.num, r.codigo, r.nombre, r.uv, textoPrereq(r)});
        }
        scrollRegistro.setBorder(BorderFactory.createTitledBorder(
                "Materias agregadas (" + registros.size() + ")"));
        scrollRegistro.repaint();
    }

    // =====================================================================
    //  Ventana emergente: agregar / editar materia
    // =====================================================================

    /** Muestra el formulario. Devuelve la materia, o null si se cancela. */
    private Registro pedirMateria(Registro original) {
        final boolean editando = original != null;
        final JDialog dlg = new JDialog(this, editando ? "Editar materia" : "Agregar materia", true);

        JSpinner spnNum = new JSpinner(new SpinnerNumberModel(
                editando ? original.num : siguienteNum(), 1, 999, 1));
        JTextField txtCodigo = new JTextField(editando ? original.codigo : "", 12);
        JTextField txtNombre = new JTextField(editando ? original.nombre : "", 28);
        JSpinner spnUV = new JSpinner(new SpinnerNumberModel(editando ? original.uv : 4, 1, 30, 1));

        // --- Prerrequisitos ---
        JCheckBox chkBach = new JCheckBox("Bachillerato (sin prerrequisitos)");
        JCheckBox chkUV = new JCheckBox("UV previas aprobadas:");
        JSpinner spnUVPrev = new JSpinner(new SpinnerNumberModel(40, 1, 1000, 1));

        Map<String, JCheckBox> checks = new LinkedHashMap<>();
        JPanel listaMaterias = new JPanel();
        listaMaterias.setLayout(new BoxLayout(listaMaterias, BoxLayout.Y_AXIS));
        for (Registro r : registros) { // ya están ordenadas por Nº
            if (editando && r == original) {
                continue;
            }
            JCheckBox c = new JCheckBox("Nº " + r.num + " · " + r.codigo + " - " + r.nombre);
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            checks.put(r.codigo, c);
            listaMaterias.add(c);
        }
        if (checks.isEmpty()) {
            listaMaterias.add(new JLabel("  Aún no hay otras materias agregadas."));
        }
        JScrollPane scrollMaterias = new JScrollPane(listaMaterias);
        scrollMaterias.setPreferredSize(new Dimension(430, 170));

        if (editando) {
            for (String p : original.prereqs) {
                JCheckBox c = checks.get(p);
                if (c != null) {
                    c.setSelected(true);
                }
            }
            if (original.uvPrevias > 0) {
                chkUV.setSelected(true);
                spnUVPrev.setValue(original.uvPrevias);
            }
            chkBach.setSelected(original.prereqs.isEmpty() && original.uvPrevias == 0);
        } else {
            chkBach.setSelected(registros.isEmpty()); // la primera materia suele ser de Bachillerato
        }

        Runnable actualizar = () -> {
            boolean bach = chkBach.isSelected();
            if (bach) {
                chkUV.setSelected(false);
                for (JCheckBox c : checks.values()) {
                    c.setSelected(false);
                }
            }
            chkUV.setEnabled(!bach);
            spnUVPrev.setEnabled(!bach && chkUV.isSelected());
            for (JCheckBox c : checks.values()) {
                c.setEnabled(!bach);
            }
        };
        chkBach.addActionListener(e -> actualizar.run());
        chkUV.addActionListener(e -> actualizar.run());
        actualizar.run();

        // --- Diseño ---
        JPanel campos = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;
        fila(campos, gc, 0, "Nº correlativo:", spnNum);
        fila(campos, gc, 1, "Código:", txtCodigo);
        fila(campos, gc, 2, "Nombre:", txtNombre);
        fila(campos, gc, 3, "Unidades valorativas:", spnUV);

        chkBach.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel filaUV = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filaUV.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaUV.add(chkUV);
        filaUV.add(spnUVPrev);
        JLabel lblMaterias = new JLabel("Materias previas (usa el Nº que aparece en la columna Prerrequisito de la guía):");
        lblMaterias.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollMaterias.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel prereq = new JPanel();
        prereq.setLayout(new BoxLayout(prereq, BoxLayout.Y_AXIS));
        prereq.setBorder(BorderFactory.createTitledBorder("Prerrequisitos"));
        prereq.add(chkBach);
        prereq.add(Box.createVerticalStrut(4));
        prereq.add(filaUV);
        prereq.add(Box.createVerticalStrut(6));
        prereq.add(lblMaterias);
        prereq.add(Box.createVerticalStrut(2));
        prereq.add(scrollMaterias);

        // --- Botones ---
        Registro[] resultado = new Registro[1];
        JButton btnOk = new JButton(editando ? "Guardar cambios" : "Agregar");
        JButton btnCancelar = new JButton("Cancelar");

        btnOk.addActionListener(e -> {
            String codigo = txtCodigo.getText().trim();
            String nombre = txtNombre.getText().trim();
            int num = valor(spnNum);

            if (codigo.isEmpty() || nombre.isEmpty()) {
                aviso(dlg, "El código y el nombre son obligatorios.");
                return;
            }
            if (codigo.contains(",") || codigo.contains(" ") || codigo.contains("|")) {
                aviso(dlg, "El código no puede contener espacios, comas ni barras.");
                return;
            }
            if (esNumero(codigo) || codigo.equals("Bachillerato")) {
                aviso(dlg, "El código no puede ser un número ni la palabra \"Bachillerato\".");
                return;
            }
            for (Registro r : registros) {
                if (r == original) {
                    continue;
                }
                if (r.codigo.equals(codigo)) {
                    aviso(dlg, "Ya existe una materia con el código " + codigo + ".");
                    return;
                }
                if (r.num == num) {
                    aviso(dlg, "El Nº " + num + " ya lo usa la materia " + r.codigo + ".");
                    return;
                }
            }

            List<String> prereqs = new ArrayList<>();
            for (Map.Entry<String, JCheckBox> en : checks.entrySet()) {
                if (en.getValue().isSelected()) {
                    prereqs.add(en.getKey());
                }
            }
            int uvPrev = chkUV.isSelected() ? valor(spnUVPrev) : 0;
            if (!chkBach.isSelected() && prereqs.isEmpty() && uvPrev == 0) {
                aviso(dlg, "Elige \"Bachillerato\", unas UV previas o al menos una materia previa.");
                return;
            }

            Registro nuevo = new Registro();
            nuevo.num = num;
            nuevo.codigo = codigo;
            nuevo.nombre = nombre;
            nuevo.uv = valor(spnUV);
            nuevo.prereqs = prereqs;
            nuevo.uvPrevias = uvPrev;
            resultado[0] = nuevo;
            dlg.dispose();
        });
        btnCancelar.addActionListener(e -> dlg.dispose());

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pie.add(btnCancelar);
        pie.add(btnOk);

        JPanel contenido = new JPanel(new BorderLayout(0, 6));
        contenido.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        contenido.add(campos, BorderLayout.NORTH);
        contenido.add(prereq, BorderLayout.CENTER);
        contenido.add(pie, BorderLayout.SOUTH);

        dlg.setContentPane(contenido);
        dlg.getRootPane().setDefaultButton(btnOk);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        txtCodigo.requestFocusInWindow();
        dlg.setVisible(true); // bloquea hasta que se cierre (es modal)
        return resultado[0];
    }

    private void fila(JPanel p, GridBagConstraints gc, int y, String etiqueta, JComponent campo) {
        gc.gridy = y;
        gc.gridx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        p.add(new JLabel(etiqueta), gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        p.add(campo, gc);
    }

    // =====================================================================
    //  Acciones sobre la lista de materias
    // =====================================================================

    private void agregarMaterias() {
        while (true) {
            Registro nuevo = pedirMateria(null);
            if (nuevo == null) {
                break;
            }
            registros.add(nuevo);
            refrescarTabla();
            estado.setText("Materia " + nuevo.codigo + " agregada. Total: " + registros.size());
            int r = JOptionPane.showConfirmDialog(this,
                    "Materia " + nuevo.codigo + " agregada.\n¿Quieres agregar otra?",
                    "Agregar materia", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) {
                break;
            }
        }
    }

    private void editarSeleccionada() {
        int fila = tablaRegistro.getSelectedRow();
        if (fila < 0) {
            aviso(this, "Selecciona una materia de la tabla.");
            return;
        }
        Registro viejo = registros.get(fila);
        String codigoViejo = viejo.codigo;
        Registro nuevo = pedirMateria(viejo);
        if (nuevo == null) {
            return;
        }
        registros.set(fila, nuevo);
        if (!codigoViejo.equals(nuevo.codigo)) { // si cambió el código, actualizar referencias
            for (Registro r : registros) {
                r.prereqs.replaceAll(c -> c.equals(codigoViejo) ? nuevo.codigo : c);
            }
        }
        refrescarTabla();
        estado.setText("Materia " + nuevo.codigo + " actualizada.");
    }

    private void eliminarSeleccionadas() {
        int[] filas = tablaRegistro.getSelectedRows();
        if (filas.length == 0) {
            aviso(this, "Selecciona una o más materias de la tabla.");
            return;
        }
        List<String> codigos = new ArrayList<>();
        for (int f : filas) {
            codigos.add(registros.get(f).codigo);
        }

        // ¿Alguna otra materia depende de las que se van a borrar?
        List<String> dependientes = new ArrayList<>();
        for (Registro r : registros) {
            if (codigos.contains(r.codigo)) {
                continue;
            }
            for (String p : r.prereqs) {
                if (codigos.contains(p)) {
                    dependientes.add(r.codigo);
                    break;
                }
            }
        }
        String msg = "¿Eliminar " + String.join(", ", codigos) + "?";
        if (!dependientes.isEmpty()) {
            msg += "\n\nLas materias " + String.join(", ", dependientes)
                    + " la usan como prerrequisito; se les quitará ese prerrequisito.";
        }
        if (JOptionPane.showConfirmDialog(this, msg, "Eliminar", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }

        for (int i = filas.length - 1; i >= 0; i--) {
            registros.remove(filas[i]);
        }
        for (Registro r : registros) {
            r.prereqs.removeAll(codigos);
        }
        refrescarTabla();
        estado.setText("Total de materias: " + registros.size());
    }

    private void limpiarTodo() {
        if (registros.isEmpty()) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Borrar todas las materias agregadas?",
                "Limpiar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            registros.clear();
            refrescarTabla();
            estado.setText("Pensum vacío.");
        }
    }

    // =====================================================================
    //  Archivos: el .txt conserva el formato de 4 líneas de LectorPorCaso;
    //  el .txt.meta guarda carrera y Nº (solo lo usa esta ventana).
    // =====================================================================

    private File carpetaCasos() {
        File f = new File("src/test/Casos_de_prueba");
        if (!f.exists()) {
            f = new File("pensum.de.analizador/src/test/Casos_de_prueba");
        }
        return f;
    }

    private String construirTexto() {
        StringBuilder sb = new StringBuilder();
        for (Registro r : registros) {
            List<String> partes = new ArrayList<>(r.prereqs);
            if (r.uvPrevias > 0) {
                partes.add(String.valueOf(r.uvPrevias));
            }
            if (partes.isEmpty()) {
                partes.add("Bachillerato");
            }
            sb.append(r.nombre).append("\n")
              .append(r.codigo).append("\n")
              .append(String.join(",", partes)).append("\n")
              .append(r.uv).append("\n\n");
        }
        return sb.toString();
    }

    private String construirMeta() {
        StringBuilder sb = new StringBuilder();
        sb.append("carrera=").append(txtCarrera.getText().trim()).append("\n");
        for (Registro r : registros) {
            sb.append(r.codigo).append("|").append(r.num).append("\n");
        }
        return sb.toString();
    }

    private void cargarArchivo() {
        if (!registros.isEmpty() && JOptionPane.showConfirmDialog(this,
                "Se reemplazará el pensum actual. ¿Continuar?",
                "Cargar archivo", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        JFileChooser chooser = new JFileChooser(carpetaCasos());
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = chooser.getSelectedFile();
        try {
            List<String> lineas = new ArrayList<>();
            for (String l : Files.readAllLines(archivo.toPath(), StandardCharsets.UTF_8)) {
                l = l.replace("\uFEFF", "").trim();
                if (!l.isEmpty()) {
                    lineas.add(l);
                }
            }

            registros.clear();
            int omitidas = 0;
            for (int i = 0; i + 3 < lineas.size(); i += 4) {
                Registro r = new Registro();
                r.nombre = lineas.get(i);
                r.codigo = lineas.get(i + 1);
                try {
                    r.uv = Integer.parseInt(lineas.get(i + 3));
                } catch (NumberFormatException e) {
                    omitidas++;
                    continue;
                }
                for (String p : lineas.get(i + 2).split(",")) {
                    p = p.trim();
                    if (p.isEmpty() || p.equals("Bachillerato")) {
                        continue;
                    }
                    if (esNumero(p)) {
                        r.uvPrevias = Integer.parseInt(p);
                    } else {
                        r.prereqs.add(p);
                    }
                }
                r.num = registros.size() + 1;
                registros.add(r);
            }

            boolean conMeta = aplicarMeta(new File(archivo.getPath() + ".meta"));
            refrescarTabla();

            String msg = "Se cargaron " + registros.size() + " materias"
                    + (conMeta ? " (con Nº guardados)." : ".");
            if (omitidas > 0 || lineas.size() % 4 != 0) {
                msg += " Algunas entradas incompletas se omitieron; revisa el archivo.";
            }
            estado.setText(msg);
        } catch (IOException ex) {
            aviso(this, "No se pudo leer el archivo: " + ex.getMessage());
        }
    }

    /** Lee el .meta si existe y es consistente con las materias cargadas. */
    private boolean aplicarMeta(File meta) {
        if (!meta.exists()) {
            return false;
        }
        try {
            Map<String, int[]> datos = new HashMap<>();
            String carrera = null;
            for (String l : Files.readAllLines(meta.toPath(), StandardCharsets.UTF_8)) {
                l = l.replace("\uFEFF", "").trim();
                if (l.startsWith("carrera=")) {
                    carrera = l.substring(8);
                } else {
                    String[] p = l.split("\\|");
                    if (p.length >= 2) { // acepta también archivos viejos con ciclo (se ignora)
                        try {
                            datos.put(p[0].trim(), new int[]{Integer.parseInt(p[1].trim())});
                        } catch (NumberFormatException ignorada) {
                            // línea inválida: se ignora
                        }
                    }
                }
            }
            if (carrera != null) {
                txtCarrera.setText(carrera);
            }

            Set<Integer> usados = new HashSet<>();
            for (Registro r : registros) {
                int[] d = datos.get(r.codigo);
                if (d == null || d[0] <= 0 || !usados.add(d[0])) {
                    return false; // inconsistente: se conservan los Nº automáticos
                }
            }
            for (Registro r : registros) {
                r.num = datos.get(r.codigo)[0];
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private void guardarArchivo() {
        if (registros.isEmpty()) {
            aviso(this, "No hay materias para guardar.");
            return;
        }
        JFileChooser chooser = new JFileChooser(carpetaCasos());
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".txt")) {
            f = new File(f.getParentFile(), f.getName() + ".txt");
        }
        try {
            Files.writeString(f.toPath(), construirTexto(), StandardCharsets.UTF_8);
            Files.writeString(new File(f.getPath() + ".meta").toPath(), construirMeta(), StandardCharsets.UTF_8);
            estado.setText("Pensum guardado en " + f.getName() + " (y " + f.getName() + ".meta).");
        } catch (IOException ex) {
            aviso(this, "No se pudo guardar el archivo: " + ex.getMessage());
        }
    }

    // =====================================================================
    //  Análisis y ventanas de resultado
    // =====================================================================

    private String formatearPrerrequisitos(Materia m) {
        List<String> partes = new ArrayList<>();
        for (String p : m.getCodigoPrerequisitos()) {
            try {
                partes.add(Integer.parseInt(p) + " UV");
            } catch (NumberFormatException e) {
                partes.add(p);
            }
        }
        return String.join(", ", partes);
    }

    private void analizar() {
        if (registros.isEmpty()) {
            aviso(this, "Agrega al menos una materia (o carga un archivo) antes de analizar.");
            return;
        }

        // Capturamos System.out / System.err: la lógica del equipo escribe ahí sus mensajes
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream captura = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        System.setOut(captura);
        System.setErr(captura);

        List<Materia> orden = null;
        try {
            LectorPorCaso lector = new LectorPorCaso(new Scanner(construirTexto()));
            Grafo pensum = lector.crearGrafo();
            lector.close();

            if (pensum == null) {
                System.out.println("No se pudo crear el grafo. Revisa los errores indicados.");
            } else if (pensum.hayCiclo()) {
                System.err.println("No se puede cursar la carrera. Materias bloqueadas:");
                for (Materia m : pensum.getMateriasBloqueadas()) {
                    System.err.println("  - " + m.getCodigo());
                }
                if (pensum.bloqueoPorUV()) {
                    System.err.println("\nCausa: UV insuficientes para al menos una materia.");
                } else {
                    System.err.println("\nCausa: dependencias cíclicas entre materias.");
                }
            } else {
                System.out.println("Éxito, grafo leído:\n\n" + pensum);
                orden = pensum.ordenTopologico();
            }
        } catch (RuntimeException ex) {
            System.err.println("Error: " + ex.getMessage());
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        String salida = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        if (orden == null) {
            mostrarError(salida);
            estado.setText("El análisis terminó con errores.");
        } else {
            estado.setText("Análisis completado: el pensum se puede cursar (" + orden.size() + " materias).");
            mostrarResultados(salida, orden);
        }
    }

    private void mostrarError(String detalle) {
        JTextArea area = new JTextArea(detalle);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(520, 240));
        JOptionPane.showMessageDialog(this, scroll,
                "No se puede cursar este pensum", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarResultados(String detalle, List<Materia> orden) {
        JDialog dlg = new JDialog(this, "Manera idónea de cursar el pensum", true);

        // --- Pestaña: orden de cursado ---
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Paso", "Código", "Nombre", "UV", "UV acumuladas", "Prerrequisitos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        StringBuilder ruta = new StringBuilder("Ruta: ");
        int paso = 1;
        int uvAcumuladas = 0;
        for (Materia m : orden) {
            uvAcumuladas += m.getUnidadesValorativas();
            modelo.addRow(new Object[]{paso++, m.getCodigo(), m.getNombre(),
                    m.getUnidadesValorativas(), uvAcumuladas, formatearPrerrequisitos(m)});
            ruta.append(m.getCodigo()).append(" → ");
        }
        ruta.append("Graduado");

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        int[] anchos = {50, 90, 320, 50, 110, 300};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col : new int[]{0, 1, 3, 4}) {
            tabla.getColumnModel().getColumn(col).setCellRenderer(centrado);
        }

        JTextArea txtRuta = new JTextArea(ruta.toString());
        txtRuta.setEditable(false);
        txtRuta.setLineWrap(true);
        txtRuta.setWrapStyleWord(true);
        txtRuta.setBackground(dlg.getBackground());
        txtRuta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        txtRuta.setFont(txtRuta.getFont().deriveFont(Font.BOLD, 13f));

        JPanel panelOrden = new JPanel(new BorderLayout());
        panelOrden.add(txtRuta, BorderLayout.NORTH);
        panelOrden.add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- Pestaña: grafo ---
        GrafoPanel panelGrafo = new GrafoPanel();
        panelGrafo.setMaterias(orden);

        // --- Pestaña: detalle en texto ---
        JTextArea areaDetalle = new JTextArea(detalle);
        areaDetalle.setEditable(false);
        areaDetalle.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaDetalle.setCaretPosition(0);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Mi pensum", new PensumPanel(orden, txtCarrera.getText()));
        tabs.addTab("Orden de cursado", panelOrden);
        tabs.addTab("Grafo", new JScrollPane(panelGrafo));
        tabs.addTab("Detalle", new JScrollPane(areaDetalle));

        JLabel resumen = new JLabel("El pensum se puede cursar: " + orden.size()
                + " materias, " + uvAcumuladas + " UV en total.");
        resumen.setFont(resumen.getFont().deriveFont(Font.BOLD, 14f));
        resumen.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dlg.dispose());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pie.add(btnCerrar);

        dlg.add(resumen, BorderLayout.NORTH);
        dlg.add(tabs, BorderLayout.CENTER);
        dlg.add(pie, BorderLayout.SOUTH);
        dlg.setSize(1150, 700);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}