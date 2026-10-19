package visualiser.model;

import java.util.Arrays;

/**
 * The array being sorted. Algorithms may ONLY touch data through this class, which
 *  - records what is happening (marks, counters) so the GUI can draw it,
 *  - paces the algorithm (PlaybackControl.checkpoint) so it is watchable,
 *  - is thread-safe: sorter thread mutates, GUI thread takes snapshots.
 * Never sleeps while holding the model lock.
 */
public final class ArrayModel {
    public static final byte NONE = 0, COMPARE = 1, WRITE = 2;

    private final int[] data;
    private final byte[] marks;
    private final boolean[] sorted;
    private final int maxValue;
    private final PlaybackControl control;
    private int pivot = -1;
    private long comparisons, writes;
    private volatile OperationListener listener;

    public ArrayModel(int[] initial, PlaybackControl control) {
        this.data = initial.clone();
        this.marks = new byte[data.length];
        this.sorted = new boolean[data.length];
        this.control = control;
        this.maxValue = Math.max(1, Arrays.stream(data).max().orElse(1));
    }

    public void setListener(OperationListener l) { this.listener = l; }

    public int size() { return data.length; }

    /** Silent read (no animation step). */
    public synchronized int get(int i) { return data[i]; }

    /** Visible comparison of data[i] &lt; data[j]. */
    public boolean less(int i, int j) { return lessThan(get(i), get(j), i, j); }

    /** Visible comparison of two values, highlighting positions hi and hj (use -1 for none). */
    public boolean lessThan(int a, int b, int hi, int hj) {
        synchronized (this) { comparisons++; mark(hi, COMPARE); mark(hj, COMPARE); }
        notifyAccess(a);
        tick();
        return a < b;
    }

    public void swap(int i, int j) {
        int v;
        synchronized (this) {
            int t = data[i]; data[i] = data[j]; data[j] = t;
            writes += 2; mark(i, WRITE); mark(j, WRITE);
            v = data[i];
        }
        notifyAccess(v);
        tick();
    }

    public void set(int i, int value) {
        synchronized (this) { data[i] = value; writes++; mark(i, WRITE); }
        notifyAccess(value);
        tick();
    }

    public synchronized void markSorted(int i) { if (i >= 0 && i < sorted.length) sorted[i] = true; }
    public synchronized void setPivot(int i) { pivot = i; }
    public synchronized void clearPivot() { pivot = -1; }

    /** Green sweep shown after the algorithm finishes. */
    public void sweepSorted() {
        int n = data.length;
        int pause = Math.max(1, Math.min(15, 600 / Math.max(1, n)));
        for (int i = 0; i < n; i++) {
            int v;
            synchronized (this) { sorted[i] = true; v = data[i]; }
            notifyAccess(v);
            control.sleep(pause);
        }
    }

    public synchronized boolean isSorted() {
        for (int i = 1; i < data.length; i++) if (data[i - 1] > data[i]) return false;
        return true;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(data.clone(), marks.clone(), sorted.clone(), pivot, comparisons, writes, maxValue);
    }

    // ---- internals
    private void mark(int i, byte m) { if (i >= 0 && i < marks.length) marks[i] = m; }

    private void notifyAccess(int value) {
        OperationListener l = listener;
        if (l != null) l.onAccess(value, maxValue);
    }

    private void tick() {
        control.checkpoint();                       // may block (pause) or sleep (speed)
        synchronized (this) { Arrays.fill(marks, NONE); }
    }
}
