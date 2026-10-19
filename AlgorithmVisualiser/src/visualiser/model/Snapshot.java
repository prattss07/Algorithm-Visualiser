package visualiser.model;

/** Immutable copy of the model's state so the GUI thread can paint without blocking the sorter. */
public record Snapshot(int[] data, byte[] marks, boolean[] sorted, int pivot,
                       long comparisons, long writes, int maxValue) { }
