import elementales.Materia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Vista "Mi pensum": materias agrupadas por ciclo, con estado (pendiente / bloqueada / aprobada). */
public class PensumPanel extends JPanel {

    /** Máximo de materias por ciclo. 0 = sin límite (cada materia va en el primer ciclo posible). */
    private static final int MAX_POR_CICLO = 0;
    /** Máximo de tarjetas por línea dentro de un ciclo. */
    private static final int POR_FILA = 5;

    private enum Estado { APROBADA, PENDIENTE, BLOQUEADA }

    private static final Color AZUL = new Color(52, 152, 219);
    private static final Color GRIS = new Color(149, 165, 166);
    private static final Color VERDE = new Color(39, 174, 96);

    private final String carrera;
    private final List<List<Materia>> ciclos = new ArrayList<>();
    private final Set<Materia> aprobadas = new HashSet<>();
    private final List<Tarjeta> tarjetas = new ArrayList<>();
    private final int totalMaterias;
    private final int totalUV;
    private final JLabel lblResumen = new JLabel();
    private final JProgressBar barra = new JProgressBar();

    public PensumPanel(List<Materia> orden, String carrera) {
        super(new BorderLayout());
        this.carrera = carrera == null ? "" : carrera.trim();
        calcularCiclos(orden);

        int uv = 0;
        for (Materia m : orden) {
            uv += m.getUnidadesValorativas();
        }
        totalMaterias = orden.size();
        totalUV = uv;
        barra.setMinimum(0);
        barra.setMaximum(Math.max(totalUV, 1));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearLeyenda(), BorderLayout.SOUTH);
        actualizar();
    }

    // =====================================================================
    //  Ciclos: cada materia va en el primer ciclo donde ya se cumplen sus
    //  prerrequisitos (de ciclos anteriores) y las UV previas requeridas.
    // =====================================================================

    private void calcularCiclos(List<Materia> orden) {
        List<Materia> pendientes = new ArrayList<>(orden);
        Set<Materia> colocadas = new HashSet<>();
        int uvPrevias = 0;

        while (!pendientes.isEmpty()) {
            List<Materia> ciclo = new ArrayList<>();
            for (Materia m : pendientes) {
                if (MAX_POR_CICLO > 0 && ciclo.size() >= MAX_POR_CICLO) {
                    break;
                }
                if (colocadas.containsAll(m.getPrerequisitos()) && uvPrevias >= m.getPrerequisitoUV()) {
                    ciclo.add(m);
                }
            }
            if (ciclo.isEmpty()) { // no debería pasar si el pensum es válido
                ciclos.add(new ArrayList<>(pendientes));
                break;
            }
            ciclos.add(ciclo);
            pendientes.removeAll(ciclo);
            colocadas.addAll(ciclo);
            for (Materia m : ciclo) {
                uvPrevias += m.getUnidadesValorativas();
            }
        }
    }

    // =====================================================================
    //  Estado de las materias
    // =====================================================================

    private int uvAprobadas(Materia excluir) {
        int suma = 0;
        for (Materia m : aprobadas) {
            if (m != excluir) {
                suma += m.getUnidadesValorativas();
            }
        }
        return suma;
    }

    private boolean cumple(Materia m) {
        return aprobadas.containsAll(m.getPrerequisitos())
                && uvAprobadas(m) >= m.getPrerequisitoUV();
    }

    private Estado estado(Materia m) {
        if (aprobadas.contains(m)) {
            return Estado.APROBADA;
        }
        return cumple(m) ? Estado.PENDIENTE : Estado.BLOQUEADA;
    }

    /** Si se desmarca una materia, quita las aprobadas que dejaron de cumplir sus requisitos. */
    private void normalizar() {
        boolean cambio = true;
        while (cambio) {
            cambio = aprobadas.removeIf(m -> !cumple(m));
        }
    }

    private void alHacerClic(Materia m) {
        switch (estado(m)) {
            case APROBADA -> {
                aprobadas.remove(m);
                normalizar();
            }
            case PENDIENTE -> aprobadas.add(m);
            case BLOQUEADA -> mostrarFaltantes(m);
        }
        actualizar();
    }

    private void mostrarFaltantes(Materia m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getCodigo()).append(" - ").append(m.getNombre()).append(" está bloqueada.\n");

        List<Materia> faltan = new ArrayList<>();
        for (Materia p : m.getPrerequisitos()) {
            if (!aprobadas.contains(p)) {
                faltan.add(p);
            }
        }
        if (!faltan.isEmpty()) {
            sb.append("\nTe falta aprobar:\n");
            for (Materia p : faltan) {
                sb.append("  · ").append(p.getCodigo()).append(" - ").append(p.getNombre()).append("\n");
            }
        }
        int uv = uvAprobadas(null);
        if (uv < m.getPrerequisitoUV()) {
            sb.append("\nUV aprobadas: ").append(uv).append(" de ")
              .append(m.getPrerequisitoUV()).append(" requeridas.");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Materia bloqueada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizar() {
        int uv = uvAprobadas(null);
        lblResumen.setText(aprobadas.size() + " de " + totalMaterias + " materias aprobadas  ·  "
                + uv + " / " + totalUV + " UV");
        barra.setValue(uv);
        for (Tarjeta t : tarjetas) {
            t.repaint();
        }
    }

    // =====================================================================
    //  Construcción de la interfaz
    // =====================================================================

    private JPanel crearEncabezado() {
        JLabel titulo = new JLabel("Mi pensum");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 26f));
        JLabel sub = new JLabel("Haz clic en una materia disponible para marcarla como aprobada; "
                + "en una bloqueada verás qué le falta.");
        sub.setForeground(Color.GRAY);

        JPanel izq = new JPanel();
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        izq.add(titulo);
        if (!carrera.isEmpty()) {
            JLabel lblCarrera = new JLabel(carrera);
            lblCarrera.setFont(lblCarrera.getFont().deriveFont(Font.BOLD, 15f));
            lblCarrera.setForeground(AZUL);
            izq.add(Box.createVerticalStrut(2));
            izq.add(lblCarrera);
        }
        izq.add(Box.createVerticalStrut(4));
        izq.add(sub);

        JButton reiniciar = new JButton("Reiniciar avance");
        reiniciar.addActionListener(e -> {
            aprobadas.clear();
            actualizar();
        });
        JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        filaBoton.add(reiniciar);

        lblResumen.setHorizontalAlignment(SwingConstants.RIGHT);
        barra.setPreferredSize(new Dimension(260, 16));

        JPanel der = new JPanel(new GridLayout(0, 1, 0, 6));
        der.add(lblResumen);
        der.add(barra);
        der.add(filaBoton);

        JPanel p = new JPanel(new BorderLayout(20, 0));
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        p.add(izq, BorderLayout.WEST);
        p.add(der, BorderLayout.EAST);
        return p;
    }

    private JScrollPane crearCuerpo() {
        Contenido contenido = new Contenido();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));

        int numero = 1;
        for (List<Materia> ciclo : ciclos) {
            int filas = (int) Math.ceil(ciclo.size() / (double) POR_FILA);
            int cols = (int) Math.ceil(ciclo.size() / (double) filas);

            JPanel grid = new JPanel(new GridLayout(filas, cols, 8, 8));
            for (Materia m : ciclo) {
                Tarjeta t = new Tarjeta(m);
                tarjetas.add(t);
                grid.add(t);
            }
            for (int i = ciclo.size(); i < filas * cols; i++) { // celdas vacías para conservar el ancho
                JPanel vacia = new JPanel();
                vacia.setOpaque(false);
                grid.add(vacia);
            }

            JLabel lbl = new JLabel("Ciclo " + numero++, SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
            lbl.setPreferredSize(new Dimension(80, 10));

            JPanel fila = new JPanel(new BorderLayout(10, 0));
            fila.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            fila.add(lbl, BorderLayout.WEST);
            fila.add(grid, BorderLayout.CENTER);
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
            contenido.add(fila);
        }

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel crearLeyenda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.add(chip("Disponible (pendiente)", AZUL));
        p.add(chip("Bloqueada", GRIS));
        p.add(chip("Aprobada", VERDE));
        return p;
    }

    private JLabel chip(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setOpaque(true);
        l.setBackground(color);
        l.setForeground(Color.WHITE);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    /** Panel que se ajusta al ancho de la ventana dentro del JScrollPane. */
    private static class Contenido extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return Math.max(r.height - 16, 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // =====================================================================
    //  Tarjeta de una materia
    // =====================================================================

    private class Tarjeta extends JPanel {
        private final Materia materia;
        private boolean hover;

        Tarjeta(Materia m) {
            this.materia = m;
            setOpaque(false);
            setPreferredSize(new Dimension(150, 80));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(m.getCodigo() + " - " + m.getNombre());
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    alHacerClic(materia);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Estado e = estado(materia);
            Color base = switch (e) {
                case APROBADA -> VERDE;
                case PENDIENTE -> AZUL;
                case BLOQUEADA -> GRIS;
            };
            if (hover) {
                base = base.darker();
            }
            int w = getWidth();
            int h = getHeight();

            g2.setColor(base);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            g2.setColor(Color.WHITE);

            // Código y UV
            g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
            g2.drawString(materia.getCodigo() + " · " + materia.getUnidadesValorativas() + " UV", 10, 18);

            // Nombre (hasta 2 líneas)
            g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
            FontMetrics fm = g2.getFontMetrics();
            int y = 38;
            for (String linea : partir(materia.getNombre(), fm, w - 20, 2)) {
                g2.drawString(linea, 10, y);
                y += fm.getHeight() - 1;
            }

            // Estado
            g2.setFont(getFont().deriveFont(Font.BOLD, 10f));
            int yb = h - 10;
            g2.setStroke(new BasicStroke(1.5f));
            switch (e) {
                case BLOQUEADA -> {
                    dibujarCandado(g2, 10, yb - 9);
                    g2.drawString("BLOQUEADA", 26, yb);
                }
                case PENDIENTE -> g2.drawString("PENDIENTE", 10, yb);
                case APROBADA -> {
                    g2.drawLine(11, yb - 4, 14, yb - 1);
                    g2.drawLine(14, yb - 1, 20, yb - 9);
                    g2.drawString("APROBADA", 26, yb);
                }
            }
            g2.dispose();
        }

        private void dibujarCandado(Graphics2D g2, int x, int y) {
            g2.drawArc(x + 2, y - 3, 6, 8, 0, 180);
            g2.fillRoundRect(x, y + 2, 10, 7, 2, 2);
        }

        /** Parte el texto en líneas que quepan en el ancho; la última se recorta con "…". */
        private List<String> partir(String texto, FontMetrics fm, int ancho, int maxLineas) {
            List<String> lineas = new ArrayList<>();
            StringBuilder actual = new StringBuilder();
            for (String palabra : texto.split(" ")) {
                String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
                if (fm.stringWidth(prueba) <= ancho || actual.length() == 0) {
                    actual = new StringBuilder(prueba);
                } else {
                    lineas.add(actual.toString());
                    actual = new StringBuilder(palabra);
                }
            }
            if (actual.length() > 0) {
                lineas.add(actual.toString());
            }
            if (lineas.size() > maxLineas) {
                lineas = new ArrayList<>(lineas.subList(0, maxLineas));
                lineas.set(maxLineas - 1, lineas.get(maxLineas - 1) + "…");
            }
            List<String> resultado = new ArrayList<>();
            for (String l : lineas) {
                resultado.add(ajustar(l, fm, ancho));
            }
            return resultado;
        }

        private String ajustar(String t, FontMetrics fm, int ancho) {
            if (fm.stringWidth(t) <= ancho) {
                return t;
            }
            String base = t.endsWith("…") ? t.substring(0, t.length() - 1) : t;
            while (base.length() > 1 && fm.stringWidth(base + "…") > ancho) {
                base = base.substring(0, base.length() - 1);
            }
            return base + "…";
        }
    }
}