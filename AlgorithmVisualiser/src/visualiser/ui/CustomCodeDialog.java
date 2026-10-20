package visualiser.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

final class CustomCodeDialog extends JDialog {
    private static final String EXAMPLE = """
            int n = m.size();
            for (int end = n - 1; end > 0; end--) {
                for (int i = 0; i < end; i++) {
                    if (m.less(i + 1, i)) {
                        m.swap(i, i + 1);
                    }
                }
                m.markSorted(end);
            }
            """;

    private final JTextArea editor = new JTextArea(EXAMPLE, 18, 76);
    private String code;

    private CustomCodeDialog(Component parent) {
        super(javax.swing.SwingUtilities.getWindowAncestor(parent),
                "Write a Java sorting method", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(0x0B1220));
        JLabel instructions = new JLabel(
                "<html>Enter the body of <code>sort(ArrayModel m)</code>. Use "
                        + "<code>m.less(i,j)</code>, <code>m.swap(i,j)</code>, "
                        + "<code>m.set(i,value)</code>, and <code>m.get(i)</code> "
                        + "for animated array operations.</html>");
        instructions.setForeground(new Color(0xDCE6F5));
        instructions.setBorder(BorderFactory.createEmptyBorder(14, 16, 2, 16));
        add(instructions, BorderLayout.NORTH);

        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        editor.setTabSize(4);
        editor.setBackground(new Color(0x111C2E));
        editor.setForeground(new Color(0xEAF1FC));
        editor.setCaretColor(new Color(0xEAF1FC));
        editor.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(new JScrollPane(editor), BorderLayout.CENTER);

        JLabel warning = new JLabel(
                "Only run code you trust. Custom Java code runs with your user permissions and is not sandboxed.");
        warning.setForeground(new Color(0xF6C85F));
        warning.setBorder(BorderFactory.createEmptyBorder(0, 16, 4, 16));
        JButton cancel = new JButton("Cancel");
        JButton run = new JButton("Compile & Run");
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(run);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(warning, BorderLayout.NORTH);
        footer.add(buttons, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);

        cancel.addActionListener(event -> dispose());
        run.addActionListener(event -> {
            code = editor.getText();
            dispose();
        });
        getRootPane().setDefaultButton(run);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(850, 560);
        setLocationRelativeTo(parent);
    }

    static String prompt(Component parent) {
        CustomCodeDialog dialog = new CustomCodeDialog(parent);
        dialog.setVisible(true);
        return dialog.code;
    }
}
