import elementales.Materia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GrafoPanel extends JPanel {

    private static final int ANCHO = 150;
    private static final int ALTO = 46;
    private static final int SEP_X = 90;
    private static final int SEP_Y = 24;
    private static final int MARGEN = 30;
    private static final int ENCABEZADO = 30;

    private List<Materia> materias = new ArrayList<>();
    private final Map<Materia, Rectangle> posiciones = new LinkedHashMap<>();
    private int niveles = 0;

    public GrafoPanel() {
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    public void setMaterias(List<Materia> materias) {
        this.materias = materias == null ? new ArrayList<>() : materias;
        calcularPosiciones();
        revalidate();
        repaint();
    }

    private int nivel(Materia m, Map<Materia, Integer> memo, Set<Materia> enProceso) {
        Integer guardado = memo.get(m);
        if (guardado != null) {
            return guardado;
        }
        if (!enProceso.add(m)) {
            return 0;
        }
        int max = -1;
        for (Materia p : m.getPrerequisitos()) {
            max = Math.max(max, nivel(p, memo, enProceso));
        }
        enProceso.remove(m);
        memo.put(m, max + 1);
        return max + 1;
    }

    private void calcularPosiciones() {
        posiciones.clear();
        niveles = 0;
        Map<Materia, Integer> memo = new HashMap<>();
        Map<Integer, Integer> filasPorNivel = new HashMap<>();
        int maxFilas = 0;

        for (Materia m : materias) {
            int n = nivel(m, memo, new HashSet<>());
            int fila = filasPorNivel.merge(n, 1, Integer::sum) - 1;
            int x = MARGEN + n * (ANCHO + SEP_X);
            int y = MARGEN + ENCABEZADO + fila * (ALTO + SEP_Y);
            posiciones.put(m, new Rectangle(x, y, ANCHO, ALTO));
            niveles = Math.max(niveles, n + 1);
            maxFilas = Math.max(maxFilas, fila + 1);
        }

        int w = MARGEN * 2 + Math.max(niveles, 1) * (ANCHO + SEP_X) - SEP_X;
        int h = MARGEN * 2 + ENCABEZADO + Math.max(maxFilas, 1) * (ALTO + SEP_Y);
        setPreferredSize(new Dimension(w, h));
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        for (Map.Entry<Materia, Rectangle> en : posiciones.entrySet()) {
            if (en.getValue().contains(e.getPoint())) {
                Materia m = en.getKey();
                return m.getCodigo() + " - " + m.getNombre();
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (posiciones.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString("El grafo aparecerá aquí después de analizar un pensum válido.", 20, 30);
            g2.dispose();
            return;
        }

        g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g2.setColor(Color.DARK_GRAY);
        for (int n = 0; n < niveles; n++) {
            int x = MARGEN + n * (ANCHO + SEP_X);
            g2.drawString("Nivel " + (n + 1), x, MARGEN + 8);
        }

        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(new Color(130, 130, 140));
        for (Materia m : materias) {
            Rectangle destino = posiciones.get(m);
            for (Materia p : m.getPrerequisitos()) {
                Rectangle origen = posiciones.get(p);
                if (origen != null && destino != null) {
                    flecha(g2,
                            origen.x + origen.width, origen.y + origen.height / 2,
                            destino.x, destino.y + destino.height / 2);
                }
            }
        }

        Font normal = getFont().deriveFont(Font.PLAIN, 11f);
        Font negrita = getFont().deriveFont(Font.BOLD, 13f);
        for (Map.Entry<Materia, Rectangle> en : posiciones.entrySet()) {
            Materia m = en.getKey();
            Rectangle r = en.getValue();

            g2.setColor(new Color(214, 232, 252));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.setColor(new Color(70, 120, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

            g2.setColor(new Color(30, 30, 40));
            g2.setFont(negrita);
            g2.drawString(m.getCodigo(), r.x + 10, r.y + 19);

            String detalle = m.getUnidadesValorativas() + " UV";
            if (m.getPrerequisitoUV() > 0) {
                detalle += "  ·  req. " + m.getPrerequisitoUV() + " UV";
            }
            g2.setFont(normal);
            g2.drawString(detalle, r.x + 10, r.y + 36);
        }
        g2.dispose();
    }

    private void flecha(Graphics2D g2, double x1, double y1, double x2, double y2) {
        g2.draw(new Line2D.Double(x1, y1, x2, y2));
        double ang = Math.atan2(y2 - y1, x2 - x1);
        int punta = 8;
        Polygon p = new Polygon();
        p.addPoint((int) x2, (int) y2);
        p.addPoint((int) (x2 - punta * Math.cos(ang - Math.PI / 7)),
                   (int) (y2 - punta * Math.sin(ang - Math.PI / 7)));
        p.addPoint((int) (x2 - punta * Math.cos(ang + Math.PI / 7)),
                   (int) (y2 - punta * Math.sin(ang + Math.PI / 7)));
        g2.fill(p);
    }
}