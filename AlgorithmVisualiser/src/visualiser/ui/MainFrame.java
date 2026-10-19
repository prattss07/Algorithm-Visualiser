package visualiser.ui;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import visualiser.audio.SoundPlayer;
import visualiser.audio.SoundUnavailableException;
import visualiser.engine.RunState;
import visualiser.model.ArrayGenerator;
import visualiser.model.ArrayParser;
import visualiser.model.InvalidArrayException;
import visualiser.model.OperationListener;

public class MainFrame extends JFrame {
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

    private final JButton startButton = new JButton("Start");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton stepButton = new JButton("Step");
    private final JButton stopButton = new JButton("Stop");
    private final JButton resetButton = new JButton("Reset");
    private final JSlider speedSlider = new JSlider(1, 100, 70);
    private final JCheckBox soundBox = new JCheckBox("Sound");

    private int[] currentData;
    private int finishCounter;

    public MainFrame() {
        super("Algorithm Visualiser");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { shutdown(); }
        });
        setLayout(new BorderLayout(6, 6));
        add(buildTop(), BorderLayout.NORTH);
        lanes.add(panelA);
        add(lanes, BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        initSound();
        wireActions();
        panelA.setDelay(delayFor(speedSlider.getValue()));
        panelB.setDelay(delayFor(speedSlider.getValue()));
        loadData(ArrayGenerator.generate(sizeSlider.getValue(), ArrayGenerator.Pattern.RANDOM));
        updateControls();

        setSize(1100, 720);
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- construction
    private JComponent buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.add(new JLabel("Data:"));
        p.add(patternBox);
        p.add(sizeSlider);
        p.add(sizeLabel);
        p.add(generateButton);
        p.add(new JLabel("  Custom:"));
        customField.setToolTipText("Numbers 1-" + ArrayParser.MAX_VALUE + ", e.g. 5 3 8 1 9 2");
        p.add(customField);
        p.add(useButton);
        p.add(raceBox);
        return p;
    }

    private JComponent buildBottom() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        controls.add(startButton); controls.add(pauseButton); controls.add(stepButton);
        controls.add(stopButton); controls.add(resetButton);
        controls.add(new JLabel("   Speed:"));
        controls.add(speedSlider);
        controls.add(soundBox);

        JLabel legend = new JLabel("<html>"
                + "<font color='#4a90e2'>&#9632;</font> unsorted &nbsp; "
                + "<font color='#f2c230'>&#9632;</font> comparing &nbsp; "
                + "<font color='#e24e4e'>&#9632;</font> writing &nbsp; "
                + "<font color='#aa6ee6'>&#9632;</font> pivot &nbsp; "
                + "<font color='#4caf50'>&#9632;</font> sorted</html>");
        legend.setBorder(BorderFactory.createEmptyBorder(0, 10, 6, 10));

        JPanel p = new JPanel(new BorderLayout());
        p.add(controls, BorderLayout.NORTH);
        p.add(legend, BorderLayout.SOUTH);
        return p;
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
        boolean anyRunning = active().stream().anyMatch(p -> p.state() == RunState.RUNNING);
        startButton.setEnabled(!live);
        pauseButton.setEnabled(live);
        pauseButton.setText(anyRunning ? "Pause" : "Resume");
        stopButton.setEnabled(live);
        for (JComponent c : new JComponent[]{patternBox, sizeSlider, generateButton, customField, useButton, raceBox})
            c.setEnabled(!live);
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
