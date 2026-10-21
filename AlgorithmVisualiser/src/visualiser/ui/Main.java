package visualiser.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public final class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { /* default look and feel is fine */ }
        UIManager.put("Panel.background", new Color(0x0B1220));
        UIManager.put("Label.foreground", new Color(0xDCE6F5));
        UIManager.put("ComboBox.background", new Color(0x1B2940));
        UIManager.put("ComboBox.foreground", new Color(0xEAF1FC));
        UIManager.put("ComboBox.selectionBackground", new Color(0x203656));
        UIManager.put("ComboBox.selectionForeground", new Color(0x42D6C5));
        UIManager.put("ComboBox.disabledBackground", new Color(0x131D2D));
        UIManager.put("ComboBox.disabledForeground", new Color(0x5E728E));
        UIManager.put("TextField.background", new Color(0x111C2E));
        UIManager.put("TextField.foreground", new Color(0xEAF1FC));
        UIManager.put("TextField.caretForeground", new Color(0xEAF1FC));
        UIManager.put("CheckBox.background", new Color(0x111C2E));
        UIManager.put("CheckBox.foreground", new Color(0xDCE6F5));
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
