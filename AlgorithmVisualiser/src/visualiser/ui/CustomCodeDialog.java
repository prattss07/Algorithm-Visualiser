package visualiser.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

final class CustomCodeDialog extends JDialog {
    public record CustomCodeResult(String name, String code) {}

    private static final String SCRATCH_TEMPLATE = """
            int n = m.size();
            // Write your sorting algorithm from scratch here:
            
            """;

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

    private static final Color BACKGROUND = new Color(0x0B1220);
    private static final Color SURFACE = new Color(0x111C2E);
    private static final Color BORDER = new Color(0x263750);
    private static final Color MUTED = new Color(0x93A6C1);
    private static final Color ACCENT = new Color(0x42D6C5);
    private static final Color TEXT = new Color(0xEAF1FC);

    private final JTextField nameField = new JTextField("My Custom Sort", 22);
    private final JTextArea editor = new JTextArea(SCRATCH_TEMPLATE, 18, 76);
    private CustomCodeResult result;

    private CustomCodeDialog(Component parent) {
        super(javax.swing.SwingUtilities.getWindowAncestor(parent),
                "Write a Java sorting method", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 4, 16));

        JLabel instructions = new JLabel(
                "<html>Enter the body of <code>sort(ArrayModel m)</code>. Use "
                        + "<code>m.less(i,j)</code>, <code>m.swap(i,j)</code>, "
                        + "<code>m.set(i,value)</code>, and <code>m.get(i)</code> "
                        + "for animated array operations.</html>");
        instructions.setForeground(new Color(0xDCE6F5));
        instructions.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(instructions);
        topPanel.add(Box.createVerticalStrut(10));

        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controlRow.setOpaque(false);
        controlRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel("Algorithm Name:");
        nameLabel.setForeground(MUTED);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        controlRow.add(nameLabel);

        nameField.setBackground(SURFACE);
        nameField.setForeground(TEXT);
        nameField.setCaretColor(ACCENT);
        nameField.setFont(nameField.getFont().deriveFont(13f));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
        controlRow.add(nameField);

        JButton scratchButton = new JButton("Start from Scratch");
        styleButton(scratchButton, false);
        scratchButton.addActionListener(e -> {
            editor.setText(SCRATCH_TEMPLATE);
            editor.setCaretPosition(editor.getText().length());
            editor.requestFocusInWindow();
        });
        controlRow.add(scratchButton);

        JButton exampleButton = new JButton("Load Example");
        styleButton(exampleButton, false);
        exampleButton.addActionListener(e -> {
            editor.setText(EXAMPLE);
            editor.setCaretPosition(0);
            editor.requestFocusInWindow();
            if (nameField.getText().trim().isEmpty() || "My Custom Sort".equals(nameField.getText().trim())) {
                nameField.setText("Custom Java Sort");
            }
        });
        controlRow.add(exampleButton);

        JButton clearButton = new JButton("Clear");
        styleButton(clearButton, false);
        clearButton.addActionListener(e -> {
            editor.setText("");
            editor.requestFocusInWindow();
        });
        controlRow.add(clearButton);

        topPanel.add(controlRow);
        add(topPanel, BorderLayout.NORTH);

        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        editor.setTabSize(4);
        editor.setBackground(SURFACE);
        editor.setForeground(TEXT);
        editor.setCaretColor(ACCENT);
        editor.setSelectionColor(new Color(0x233754));
        editor.setSelectedTextColor(Color.WHITE);
        editor.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane scrollPane = new JScrollPane(editor);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 16, 0, 16),
                BorderFactory.createLineBorder(BORDER, 1)));
        add(scrollPane, BorderLayout.CENTER);

        JLabel warning = new JLabel(
                "Only run code you trust. Custom Java code runs with your user permissions and is not sandboxed.");
        warning.setForeground(new Color(0xF6C85F));
        warning.setBorder(BorderFactory.createEmptyBorder(6, 16, 4, 16));

        JButton cancel = new JButton("Cancel");
        styleButton(cancel, false);
        JButton run = new JButton("Compile & Run");
        styleButton(run, true);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
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
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                name = "Custom Java Sort";
            }
            result = new CustomCodeResult(name, editor.getText());
            dispose();
        });

        getRootPane().setDefaultButton(run);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(880, 600);
        setLocationRelativeTo(parent);
    }

    private static void styleButton(JButton button, boolean primary) {
        button.setForeground(primary ? new Color(0x071B20) : new Color(0xDCE6F5));
        button.setBackground(primary ? ACCENT : new Color(0x1B2940));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    static CustomCodeResult prompt(Component parent) {
        CustomCodeDialog dialog = new CustomCodeDialog(parent);
        dialog.setVisible(true);
        return dialog.result;
    }

    static String promptCode(Component parent) {
        CustomCodeResult r = prompt(parent);
        return r != null ? r.code() : null;
    }
}
