package visualiser.ui;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import visualiser.audio.SoundPlayer;
import visualiser.audio.SoundUnavailableException;
import visualiser.algorithms.SortingAlgorithm;
import visualiser.engine.RunState;
import visualiser.model.ArrayGenerator;
import visualiser.model.ArrayParser;
import visualiser.model.InvalidArrayException;
import visualiser.model.OperationListener;

public class MainFrame extends JFrame {
    private static final Color BACKGROUND = new Color(0x0B1220);
    private static final Color SURFACE = new Color(0x111C2E);
    private static final Color BORDER = new Color(0x263750);
    private static final Color MUTED = new Color(0x93A6C1);
    private static final Color ACCENT = new Color(0x42D6C5);

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "sorter");
        t.setDaemon(true);
        return t;
    });
    private SoundPlayer sound;

    private final AlgorithmPanel panelA = new AlgorithmPanel(0, this::onPanelState);
    private final AlgorithmPanel panelB = new AlgorithmPanel(5, this::onPanelState);
    private final JPanel lanes = new JPanel(new GridLayout(1, 0, 8, 0));

    private final JComboBox<ArrayGenerator.Pattern> patternBox = new JComboBox<>(ArrayGenerator.Pattern.values());
    private final JSlider sizeSlider = new JSlider(10, ArrayParser.MAX_SIZE, 60);
    private final JLabel sizeLabel = new JLabel("60 bars");
    private final JButton generateButton = new JButton("Generate");
    private final JTextField customField = new JTextField(16);
    private final JButton useButton = new JButton("Use array");
    private final JCheckBox raceBox = new JCheckBox("Race mode");
    private final JButton browseAlgorithmsButton = new JButton("View algorithms");
    private final JButton customCodeButton = new JButton("Write your own code");

    private final JButton startButton = new JButton("Start");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton stepButton = new JButton("Step");
    private final JButton stopButton = new JButton("Stop");
    private final JButton resetButton = new JButton("Reset");
    private final JSlider speedSlider = new JSlider(1, 100, 70);
    private final JCheckBox soundBox = new JCheckBox("Sound");

    private int[] currentData;
    private int finishCounter;
    private boolean compilingCustomCode;

    public MainFrame() {
        super("Algorithm Visualiser");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { shutdown(); }
        });
        setLayout(new BorderLayout(6, 6));
        getContentPane().setBackground(BACKGROUND);
        add(buildTop(), BorderLayout.NORTH);
        lanes.add(panelA);
        lanes.setOpaque(false);
        add(lanes, BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        initSound();
        wireActions();
        panelA.setDelay(delayFor(speedSlider.getValue()));
        panelB.setDelay(delayFor(speedSlider.getValue()));
        loadData(ArrayGenerator.generate(sizeSlider.getValue(), ArrayGenerator.Pattern.RANDOM));
        updateControls();

        setMinimumSize(new Dimension(850, 600));
        setSize(1180, 780);
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- construction
    private JComponent buildTop() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 2, 14, 2));
        JLabel title = new JLabel("Sorting Visualiser");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel subtitle = new JLabel("Explore how sorting algorithms move through your data");
        subtitle.setForeground(MUTED);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(subtitle);
        heading.add(titleBlock, BorderLayout.WEST);
        styleCheckBox(raceBox);
        styleButton(browseAlgorithmsButton, false);
        styleButton(customCodeButton, true);
        JPanel headingActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headingActions.setOpaque(false);
        headingActions.add(browseAlgorithmsButton);
        headingActions.add(customCodeButton);
        headingActions.add(raceBox);
        heading.add(headingActions, BorderLayout.EAST);
        p.add(heading);

        JPanel dataCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        dataCard.setBackground(SURFACE);
        dataCard.setBorder(cardBorder());
        JLabel dataLabel = new JLabel("DATA");
        dataLabel.setForeground(MUTED);
        dataLabel.setFont(dataLabel.getFont().deriveFont(Font.BOLD, 11f));
        dataCard.add(dataLabel);
        styleCombo(patternBox);
        dataCard.add(patternBox);
        styleSlider(sizeSlider);
        sizeSlider.setPreferredSize(new Dimension(150, 32));
        dataCard.add(sizeSlider);
        sizeLabel.setForeground(MUTED);
        sizeLabel.setPreferredSize(new Dimension(55, sizeLabel.getPreferredSize().height));
        dataCard.add(sizeLabel);
        styleButton(generateButton, true);
        dataCard.add(generateButton);
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 28));
        separator.setForeground(BORDER);
        dataCard.add(separator);
        JLabel customLabel = new JLabel("CUSTOM");
        customLabel.setForeground(MUTED);
        customLabel.setFont(customLabel.getFont().deriveFont(Font.BOLD, 11f));
        dataCard.add(customLabel);
        customField.setToolTipText("Numbers 1-" + ArrayParser.MAX_VALUE + ", e.g. 5 3 8 1 9 2");
        customField.setPreferredSize(new Dimension(210, 34));
        customField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(5, 9, 5, 9)));
        dataCard.add(customField);
        styleButton(useButton, false);
        dataCard.add(useButton);
        p.add(dataCard);
        return p;
    }

    private JComponent buildBottom() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8, 20, 16, 20));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 10));
        controls.setBackground(SURFACE);
        controls.setBorder(cardBorder());
        styleButton(startButton, true);
        styleButton(pauseButton, false);
        styleButton(stepButton, false);
        styleButton(stopButton, false);
        styleButton(resetButton, false);
        controls.add(startButton);
        controls.add(pauseButton);
        controls.add(stepButton);
        controls.add(stopButton);
        controls.add(resetButton);
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 26));
        separator.setForeground(BORDER);
        controls.add(separator);
        JLabel speedLabel = new JLabel("SPEED");
        speedLabel.setForeground(MUTED);
        speedLabel.setFont(speedLabel.getFont().deriveFont(Font.BOLD, 11f));
        controls.add(speedLabel);
        styleSlider(speedSlider);
        speedSlider.setPreferredSize(new Dimension(150, 32));
        controls.add(speedSlider);
        styleCheckBox(soundBox);
        controls.add(soundBox);

        JLabel legend = new JLabel("<html>"
                + "<font color='#42D6C5'>&#9632;</font> unsorted &nbsp; "
                + "<font color='#F6C85F'>&#9632;</font> comparing &nbsp; "
                + "<font color='#FF6B7A'>&#9632;</font> writing &nbsp; "
                + "<font color='#B78BFA'>&#9632;</font> pivot &nbsp; "
                + "<font color='#66D19E'>&#9632;</font> sorted</html>");
        legend.setForeground(MUTED);
        legend.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        p.add(controls, BorderLayout.NORTH);
        p.add(legend, BorderLayout.SOUTH);
        return p;
    }

    private static javax.swing.border.Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(1, 5, 1, 5));
    }

    private static void styleButton(JButton button, boolean primary) {
        button.setForeground(primary ? new Color(0x071B20) : new Color(0xDCE6F5));
        button.setBackground(primary ? ACCENT : new Color(0x1B2940));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    private static void styleCombo(JComboBox<?> combo) {
        combo.setBackground(new Color(0x1B2940));
        combo.setForeground(new Color(0xEAF1FC));
        combo.setPreferredSize(new Dimension(145, 34));
    }

    private static void styleSlider(JSlider slider) {
        slider.setOpaque(false);
        slider.setForeground(ACCENT);
        slider.setFocusable(false);
    }

    private static void styleCheckBox(JCheckBox checkBox) {
        checkBox.setOpaque(false);
        checkBox.setForeground(new Color(0xDCE6F5));
        checkBox.setFocusPainted(false);
    }

    private void initSound() {
        try {
            sound = new SoundPlayer();
            OperationListener l = (value, max) -> sound.beep(value, max);
            panelA.setOperationListener(l);
            panelB.setOperationListener(l);
        } catch (SoundUnavailableException e) {          // no audio device: degrade gracefully
            sound = null;
            soundBox.setEnabled(false);
            soundBox.setToolTipText(e.getMessage());
        }
    }

    private void wireActions() {
        sizeSlider.addChangeListener(e -> sizeLabel.setText(sizeSlider.getValue() + " bars"));
        generateButton.addActionListener(e -> loadData(ArrayGenerator.generate(
                sizeSlider.getValue(), (ArrayGenerator.Pattern) patternBox.getSelectedItem())));
        useButton.addActionListener(e -> useCustomArray());
        raceBox.addActionListener(e -> toggleRace());
        browseAlgorithmsButton.addActionListener(e -> AlgorithmBrowserDialog.show(this));
        customCodeButton.addActionListener(e -> compileCustomCode());

        startButton.addActionListener(e -> startAll(false));
        pauseButton.addActionListener(e -> togglePause());
        stepButton.addActionListener(e -> stepAll());
        stopButton.addActionListener(e -> active().forEach(AlgorithmPanel::stop));
        resetButton.addActionListener(e -> { panelA.reset(); panelB.reset(); });

        speedSlider.addChangeListener(e -> {
            int d = delayFor(speedSlider.getValue());
            panelA.setDelay(d);
            panelB.setDelay(d);
        });
        soundBox.addActionListener(e -> { if (sound != null) sound.setEnabled(soundBox.isSelected()); });
    }

    // ---------------------------------------------------------------- actions
    private List<AlgorithmPanel> active() {
        return raceBox.isSelected() ? List.of(panelA, panelB) : List.of(panelA);
    }

    private void loadData(int[] data) {
        currentData = data;
        panelA.loadData(data);
        panelB.loadData(data);
    }

    private void useCustomArray() {
        try {
            loadData(ArrayParser.parse(customField.getText()));
        } catch (InvalidArrayException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid array", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void compileCustomCode() {
        String body = CustomCodeDialog.prompt(this);
        if (body == null) return;
        compilingCustomCode = true;
        updateControls();
        new SwingWorker<SortingAlgorithm, Void>() {
            @Override protected SortingAlgorithm doInBackground() throws Exception {
                return CustomAlgorithmCompiler.compile(body);
            }

            @Override protected void done() {
                compilingCustomCode = false;
                updateControls();
                try {
                    panelA.setCustomAlgorithm(get());
                    startAll(false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Compiling custom code was interrupted.", "Custom algorithm",
                            JOptionPane.WARNING_MESSAGE);
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = cause == null ? e.getMessage() : cause.getMessage();
                    JOptionPane.showMessageDialog(MainFrame.this, message,
                            "Could not compile custom algorithm", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void toggleRace() {
        lanes.removeAll();
        lanes.add(panelA);
        if (raceBox.isSelected()) { panelB.loadData(currentData); lanes.add(panelB); }
        lanes.revalidate();
        lanes.repaint();
    }

    private void startAll(boolean paused) {
        finishCounter = 0;
        for (AlgorithmPanel p : active()) p.start(executor, paused);
        updateControls();
    }

    private void togglePause() {
        boolean anyRunning = active().stream().anyMatch(p -> p.state() == RunState.RUNNING);
        for (AlgorithmPanel p : active()) { if (anyRunning) p.pause(); else p.resume(); }
        updateControls();
    }

    private void stepAll() {
        if (active().stream().noneMatch(AlgorithmPanel::isLive)) startAll(true);
        active().forEach(AlgorithmPanel::step);
        updateControls();
    }

    private void onPanelState(AlgorithmPanel panel, RunState s) {
        if (s == RunState.DONE && raceBox.isSelected()) panel.setPlace(++finishCounter);
        updateControls();
    }

    private void updateControls() {
        boolean live = active().stream().anyMatch(AlgorithmPanel::isLive);
        boolean locked = live || compilingCustomCode;
        boolean anyRunning = active().stream().anyMatch(p -> p.state() == RunState.RUNNING);
        startButton.setEnabled(!locked);
        pauseButton.setEnabled(live);
        pauseButton.setText(anyRunning ? "Pause" : "Resume");
        stopButton.setEnabled(live);
        resetButton.setEnabled(!locked);
        for (JComponent c : new JComponent[]{patternBox, sizeSlider, generateButton, customField,
                useButton, raceBox, customCodeButton})
            c.setEnabled(!locked);
    }

    /** Speed 100 = no delay, speed 1 = 300 ms per step (quadratic so the slow end is fine-grained). */
    private static int delayFor(int speed) {
        double x = (100 - speed) / 99.0;
        return (int) Math.round(x * x * 300);
    }

    private void shutdown() {
        active().forEach(AlgorithmPanel::stop);
        panelA.reset();
        panelB.reset();
        executor.shutdownNow();
        if (sound != null) sound.close();
        dispose();
        System.exit(0);
    }
}
