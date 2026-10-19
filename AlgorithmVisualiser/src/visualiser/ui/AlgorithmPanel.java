package visualiser.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
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
        super(new BorderLayout(4, 4));
        this.listener = listener;
        combo.setSelectedIndex(defaultAlgorithm);
        combo.addActionListener(e -> updateInfo());

        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.add(combo, BorderLayout.NORTH);
        header.add(info, BorderLayout.CENTER);
        JPanel footer = new JPanel(new GridLayout(2, 1));
        footer.add(stats);
        footer.add(status);
        add(header, BorderLayout.NORTH);
        add(bars, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

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
        status.setForeground(state == RunState.FAILED ? Color.RED : UIManager.getColor("Label.foreground"));
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
