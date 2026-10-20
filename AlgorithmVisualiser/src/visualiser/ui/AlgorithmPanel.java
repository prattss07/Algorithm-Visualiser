package visualiser.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import javax.swing.*;
import visualiser.algorithms.AlgorithmRegistry;
import visualiser.algorithms.SortingAlgorithm;
import visualiser.engine.RunState;
import visualiser.engine.SortRunner;
import visualiser.model.ArrayModel;
import visualiser.model.OperationListener;
import visualiser.model.PlaybackControl;
import visualiser.model.Snapshot;

/**
 * One visualisation lane: algorithm picker + bars + stats. Owns its ArrayModel, PlaybackControl and
 * state machine. All methods are called on the EDT; the sort itself runs on a worker thread.
 */
final class AlgorithmPanel extends JPanel {
    private static final Color SURFACE = new Color(0x111C2E);
    private static final Color BORDER = new Color(0x263750);
    private static final Color MUTED = new Color(0x93A6C1);

    interface StateListener { void stateChanged(AlgorithmPanel panel, RunState state); }

    private final JComboBox<SortingAlgorithm> combo =
            new JComboBox<>(AlgorithmRegistry.all().toArray(new SortingAlgorithm[0]));
    private final JLabel info = new JLabel(" ");
    private final JLabel stats = new JLabel(" ");
    private final JLabel status = new JLabel(" ");
    private final BarPanel bars = new BarPanel();
    private final StateListener listener;

    private int[] baseData = new int[0];
    private ArrayModel model;
    private PlaybackControl control;
    private RunState state = RunState.IDLE;
    private String failure = "";
    private int place;
    private int delayMillis = 20;
    private OperationListener opListener;

    AlgorithmPanel(int defaultAlgorithm, StateListener listener) {
        super(new BorderLayout(10, 10));
        this.listener = listener;
        setBackground(SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        combo.setSelectedIndex(defaultAlgorithm);
        combo.setBackground(new Color(0x1B2940));
        combo.setForeground(new Color(0xEAF1FC));
        combo.setFont(combo.getFont().deriveFont(Font.BOLD, 14f));
        combo.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
        combo.addActionListener(e -> updateInfo());

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(combo, BorderLayout.NORTH);
        info.setForeground(MUTED);
        info.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 2));
        header.add(info, BorderLayout.CENTER);
        JPanel footer = new JPanel(new GridLayout(2, 1));
        footer.setOpaque(false);
        stats.setForeground(new Color(0xDCE6F5));
        stats.setFont(stats.getFont().deriveFont(Font.BOLD, 12f));
        status.setForeground(MUTED);
        footer.add(stats);
        footer.add(status);
        add(header, BorderLayout.NORTH);
        add(bars, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        updateInfo();
        new Timer(30, e -> refreshView()).start();     // GUI refresh is decoupled from the sorting thread
    }

    // ---------------------------------------------------------------- public API (EDT)
    RunState state() { return state; }
    boolean isLive() { return state == RunState.RUNNING || state == RunState.PAUSED; }

    void loadData(int[] base) { baseData = base.clone(); reset(); }

    void reset() {
        if (control != null) control.cancel();          // stops any old worker thread
        control = new PlaybackControl();
        control.setDelayMillis(delayMillis);
        model = new ArrayModel(baseData, control);
        model.setListener(opListener);
        place = 0; failure = "";
        setState(RunState.IDLE);
        refreshView();
    }

    void start(ExecutorService executor, boolean startPaused) {
        if (state != RunState.IDLE) reset();
        SortingAlgorithm algo = (SortingAlgorithm) combo.getSelectedItem();
        ArrayModel m = model;
        control.setPaused(startPaused);
        SortRunner runner = new SortRunner(algo, m,
                outcome -> SwingUtilities.invokeLater(() -> finished(m, outcome)));
        setState(startPaused ? RunState.PAUSED : RunState.RUNNING);
        executor.execute(runner);
    }

    void pause() {
        if (state == RunState.RUNNING) { control.setPaused(true); setState(RunState.PAUSED); }
    }

    void resume() {
        if (state == RunState.PAUSED) { control.setPaused(false); setState(RunState.RUNNING); }
    }

    void step() {
        if (state == RunState.RUNNING) pause();
        if (state == RunState.PAUSED) control.requestStep();
    }

    void stop() {
        if (isLive()) { control.cancel(); setState(RunState.STOPPED); }
    }

    void setDelay(int ms) {
        delayMillis = ms;
        if (control != null) control.setDelayMillis(ms);
    }

    void setOperationListener(OperationListener l) {
        opListener = l;
        if (model != null) model.setListener(l);
    }

    void setCustomAlgorithm(SortingAlgorithm algorithm) {
        ArrayList<SortingAlgorithm> choices = new ArrayList<>(AlgorithmRegistry.all());
        choices.add(algorithm);
        combo.setModel(new DefaultComboBoxModel<>(choices.toArray(new SortingAlgorithm[0])));
        combo.setSelectedItem(algorithm);
        updateInfo();
    }

    void setPlace(int rank) { place = rank; updateStatus(); }

    // ---------------------------------------------------------------- internals
    private void finished(ArrayModel m, SortRunner.Outcome outcome) {
        if (m != model) return;                          // stale result from a run that was reset
        if (outcome.state() == RunState.FAILED) failure = outcome.message();
        setState(outcome.state());
    }

    private void setState(RunState s) {
        state = s;
        combo.setEnabled(!isLive());
        updateStatus();
        listener.stateChanged(this, s);
    }

    private void updateStatus() {
        Color statusColor = switch (state) {
            case RUNNING -> new Color(0x42D6C5);
            case PAUSED -> new Color(0xF6C85F);
            case DONE -> new Color(0x66D19E);
            case FAILED -> new Color(0xFF6B7A);
            default -> MUTED;
        };
        status.setForeground(statusColor);
        status.setText(switch (state) {
            case IDLE -> "Ready";
            case RUNNING -> "Running...";
            case PAUSED -> "Paused";
            case DONE -> place > 0 ? "Sorted!  Finished #" + place : "Sorted!";
            case STOPPED -> "Stopped";
            case FAILED -> "Failed: " + failure;
        });
    }

    private void updateInfo() {
        SortingAlgorithm a = (SortingAlgorithm) combo.getSelectedItem();
        if (a == null) return;
        info.setText("<html><body style='width:320px'>" + a.getComplexitySummary()
                + "<br>" + a.getDescription() + "</body></html>");
    }

    private void refreshView() {
        if (model == null) return;
        Snapshot s = model.snapshot();
        bars.setSnapshot(s);
        stats.setText("Comparisons: " + s.comparisons() + "    Writes: " + s.writes());
    }
}
