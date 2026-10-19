package visualiser.ui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import visualiser.model.ArrayModel;
import visualiser.model.Snapshot;

/** Draws one bar per element. Only ever reads an immutable Snapshot, so it never blocks the sorter. */
final class BarPanel extends JPanel {
    static final Color BASE = new Color(0x4A90E2), COMPARE = new Color(0xF2C230), WRITE = new Color(0xE24E4E),
                       PIVOT = new Color(0xAA6EE6), SORTED = new Color(0x4CAF50);

    private volatile Snapshot snap;

    BarPanel() {
        setBackground(new Color(0x1E1E1E));
        setPreferredSize(new java.awt.Dimension(400, 300));
    }

    void setSnapshot(Snapshot s) { this.snap = s; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Snapshot s = snap;
        if (s == null || s.data().length == 0) return;
        int n = s.data().length, w = getWidth(), h = getHeight() - 4;
        double bw = (double) w / n;
        for (int i = 0; i < n; i++) {
            int x0 = (int) Math.round(i * bw), x1 = (int) Math.round((i + 1) * bw);
            int width = Math.max(1, x1 - x0 - (bw >= 4 ? 1 : 0));
            int height = Math.max(1, (int) (h * (double) s.data()[i] / s.maxValue()));
            g.setColor(colorFor(s, i));
            g.fillRect(x0, getHeight() - height, width, height);
        }
    }

    private Color colorFor(Snapshot s, int i) {
        if (s.marks()[i] == ArrayModel.WRITE) return WRITE;
        if (s.marks()[i] == ArrayModel.COMPARE) return COMPARE;
        if (s.pivot() == i) return PIVOT;
        if (s.sorted()[i]) return SORTED;
        return BASE;
    }
}
