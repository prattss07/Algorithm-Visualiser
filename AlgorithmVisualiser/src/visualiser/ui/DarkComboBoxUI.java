package visualiser.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;

/**
 * Custom dark combo box UI to match the application's dark theme and guarantee
 * sharp contrast and readability across all platforms and Look and Feels.
 */
public final class DarkComboBoxUI extends BasicComboBoxUI {
    public static final Color BG = new Color(0x1B2940);
    public static final Color BG_HOVER = new Color(0x223552);
    public static final Color BG_DISABLED = new Color(0x131D2D);
    public static final Color FG = new Color(0xEAF1FC);
    public static final Color FG_DISABLED = new Color(0x5E728E);
    public static final Color BORDER_COLOR = new Color(0x263750);
    public static final Color POPUP_BG = new Color(0x111C2E);
    public static final Color SELECTION_BG = new Color(0x203656);
    public static final Color SELECTION_FG = new Color(0x42D6C5);
    public static final Color ACCENT = new Color(0x42D6C5);
    public static final Color ARROW_COLOR = new Color(0x93A6C1);
    public static final Color ARROW_DISABLED = new Color(0x3B4C63);

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton() {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean enabled = comboBox != null && comboBox.isEnabled();

                // Button background
                g2.setColor(enabled ? BG : BG_DISABLED);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle vertical divider before the arrow
                g2.setColor(BORDER_COLOR);
                g2.drawLine(0, 6, 0, Math.max(6, getHeight() - 7));

                // Downward chevron arrow
                g2.setColor(enabled ? ACCENT : ARROW_DISABLED);
                int midX = getWidth() / 2;
                int midY = getHeight() / 2;
                int arrowWidth = 4;
                int[] xPoints = {midX - arrowWidth, midX, midX + arrowWidth};
                int[] yPoints = {midY - 2, midY + 3, midY - 2};
                g2.fillPolygon(xPoints, yPoints, 3);
                g2.dispose();
            }
        };
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(28, 34));
        return button;
    }

    @Override
    protected ComboPopup createPopup() {
        BasicComboPopup popup = new BasicComboPopup(comboBox) {
            @Override
            protected JScrollPane createScroller() {
                JScrollPane scroller = super.createScroller();
                scroller.setBorder(BorderFactory.createEmptyBorder());
                scroller.setBackground(POPUP_BG);
                scroller.getViewport().setBackground(POPUP_BG);
                scroller.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
                scroller.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                    @Override
                    protected void configureScrollBarColors() {
                        this.thumbColor = new Color(0x263750);
                        this.trackColor = POPUP_BG;
                    }

                    @Override
                    protected JButton createDecreaseButton(int orientation) {
                        return createZeroButton();
                    }

                    @Override
                    protected JButton createIncreaseButton(int orientation) {
                        return createZeroButton();
                    }

                    private JButton createZeroButton() {
                        JButton b = new JButton();
                        b.setPreferredSize(new Dimension(0, 0));
                        b.setMinimumSize(new Dimension(0, 0));
                        b.setMaximumSize(new Dimension(0, 0));
                        return b;
                    }

                    @Override
                    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(0x324765));
                        g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                                Math.max(2, thumbBounds.width - 2), Math.max(2, thumbBounds.height - 2), 4, 4);
                        g2.dispose();
                    }
                });
                return scroller;
            }
        };
        popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return popup;
    }

    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        Graphics2D g2 = (Graphics2D) g.create();
        boolean enabled = comboBox != null && comboBox.isEnabled();
        g2.setColor(enabled ? BG : BG_DISABLED);
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g2.dispose();
    }

    /**
     * Styles any JComboBox with the consistent dark theme and high visibility.
     */
    public static void apply(JComboBox<?> combo) {
        combo.setUI(new DarkComboBoxUI());
        combo.setBackground(BG);
        combo.setForeground(FG);
        combo.setOpaque(true);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 2)));

        ListCellRenderer<Object> customRenderer = new ListCellRenderer<>() {
            private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) defaultRenderer.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

                boolean enabled = combo.isEnabled();
                if (!enabled) {
                    label.setBackground(BG_DISABLED);
                    label.setForeground(FG_DISABLED);
                } else if (index < 0) {
                    // Closed combo box display
                    label.setBackground(BG);
                    label.setForeground(FG);
                } else {
                    // Dropdown popup list
                    if (isSelected) {
                        label.setBackground(SELECTION_BG);
                        label.setForeground(SELECTION_FG);
                    } else {
                        label.setBackground(POPUP_BG);
                        label.setForeground(FG);
                    }
                }

                if (value != null) {
                    String text = value.toString();
                    label.setText(text);
                    if (text.startsWith("+ Write your own code")) {
                        label.setForeground(isSelected ? Color.WHITE : ACCENT);
                        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
                    }
                }
                return label;
            }
        };
        combo.setRenderer(customRenderer);
    }
}
