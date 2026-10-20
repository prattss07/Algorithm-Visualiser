package visualiser.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import visualiser.model.ArrayModel;
import visualiser.model.Snapshot;

/** Draws one bar per element. Only ever reads an immutable Snapshot, so it never blocks the sorter. */
final class BarPanel extends JPanel {
    static final Color BASE = new Color(0x42D6C5), COMPARE = new Color(0xF6C85F), WRITE = new Color(0xFF6B7A),
                       PIVOT = new Color(0xB78BFA), SORTED = new Color(0x66D19E);

    private volatile Snapshot snap;

    BarPanel() {
        setBackground(new Color(0x0B1423));
        setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0x263750)));
        setPreferredSize(new java.awt.Dimension(400, 300));
    }

    void setSnapshot(Snapshot s) { this.snap = s; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Snapshot s = snap;
        if (s == null || s.data().length == 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int n = s.data().length, w = getWidth(), top = 18, bottom = getHeight() - 14;
        int h = bottom - top;

        g2.setColor(new Color(0x17243A));
        for (int i = 1; i <= 4; i++) {
            int y = top + h * i / 5;
            g2.drawLine(0, y, w, y);
        }
        g2.setColor(new Color(0x32445F));
        g2.drawLine(0, bottom, w, bottom);

        double bw = (double) w / n;
        for (int i = 0; i < n; i++) {
            int x0 = (int) Math.round(i * bw), x1 = (int) Math.round((i + 1) * bw);
            int width = Math.max(1, x1 - x0 - (bw >= 4 ? 1 : 0));
            int height = Math.max(1, (int) (h * (double) s.data()[i] / s.maxValue()));
            g2.setColor(colorFor(s, i));
            if (width >= 5) {
                g2.fillRoundRect(x0, bottom - height, width, height, 4, 4);
            } else {
                g2.fillRect(x0, bottom - height, width, height);
            }
        }
        g2.dispose();
    }

    private Color colorFor(Snapshot s, int i) {
        if (s.marks()[i] == ArrayModel.WRITE) return WRITE;
        if (s.marks()[i] == ArrayModel.COMPARE) return COMPARE;
        if (s.pivot() == i) return PIVOT;
        if (s.sorted()[i]) return SORTED;
        return BASE;
    }
}
