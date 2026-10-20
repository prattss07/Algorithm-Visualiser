package visualiser.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import visualiser.algorithms.AlgorithmRegistry;
import visualiser.algorithms.SortingAlgorithm;

final class AlgorithmBrowserDialog extends JDialog {
    private static final Color MUTED = new Color(0x93A6C1);

    private final JComboBox<SortingAlgorithm> algorithms =
            new JComboBox<>(AlgorithmRegistry.all().toArray(new SortingAlgorithm[0]));
    private final JLabel details = new JLabel(" ");
    private final JTextArea source = new JTextArea();

    private AlgorithmBrowserDialog(Component parent) {
        super(javax.swing.SwingUtilities.getWindowAncestor(parent),
                "Built-in sorting algorithms", ModalityType.MODELESS);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(0x0B1220));
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 14, 4, 14));
        algorithms.setBackground(new Color(0x1B2940));
        algorithms.setForeground(new Color(0xEAF1FC));
        header.add(algorithms, BorderLayout.NORTH);
        details.setForeground(MUTED);
        header.add(details, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        source.setEditable(false);
        source.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        source.setBackground(new Color(0x111C2E));
        source.setForeground(new Color(0xEAF1FC));
        source.setCaretColor(new Color(0xEAF1FC));
        source.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(new JScrollPane(source), BorderLayout.CENTER);
        algorithms.addActionListener(event -> showSelectedSource());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 650);
        setLocationRelativeTo(parent);
        showSelectedSource();
    }

    static void show(Component parent) {
        new AlgorithmBrowserDialog(parent).setVisible(true);
    }

    private void showSelectedSource() {
        SortingAlgorithm algorithm = (SortingAlgorithm) algorithms.getSelectedItem();
        if (algorithm == null) return;
        details.setText("<html><b>" + algorithm.getName() + "</b> &nbsp; "
                + algorithm.getComplexitySummary() + "<br>" + algorithm.getDescription() + "</html>");
        try {
            Path file = locateSource(algorithm);
            source.setText(Files.readString(file, StandardCharsets.UTF_8));
            source.setCaretPosition(0);
        } catch (IOException | URISyntaxException e) {
            source.setText("Unable to load source for " + algorithm.getName() + ".\n" + e.getMessage());
            source.setCaretPosition(0);
        }
    }

    private static Path locateSource(SortingAlgorithm algorithm) throws IOException, URISyntaxException {
        String fileName = algorithm.getClass().getSimpleName() + ".java";
        Path location = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path source = searchAncestors(location, fileName);
        if (source != null) return source;

        Path classes = Path.of(AlgorithmBrowserDialog.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        source = searchAncestors(Files.isDirectory(classes) ? classes : classes.getParent(), fileName);
        if (source != null) return source;
        throw new IOException("Run the app from its project folder to browse source files.");
    }

    private static Path searchAncestors(Path start, String fileName) {
        for (Path current = start; current != null; current = current.getParent()) {
            Path candidate = current.resolve(Path.of("src", "visualiser", "algorithms", fileName));
            if (Files.isRegularFile(candidate)) return candidate;
        }
        return null;
    }
}
