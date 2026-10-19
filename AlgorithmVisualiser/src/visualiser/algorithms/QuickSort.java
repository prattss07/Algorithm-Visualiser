package visualiser.algorithms;

import visualiser.model.ArrayModel;

/** Lomuto partition; pivot is moved to the end (shown in purple) so it is easy to follow. */
public final class QuickSort extends SortingAlgorithm {
    public QuickSort() {
        super("Quick Sort", "O(n log n)", "O(n log n)", "O(n^2)", "O(log n)",
              "Picks a pivot, moves smaller values before it and larger after, then recurses on both sides.");
    }

    @Override public void sort(ArrayModel m) { quick(m, 0, m.size() - 1); }

    private void quick(ArrayModel m, int lo, int hi) {
        if (lo >= hi) {
            if (lo == hi) m.markSorted(lo);
            return;
        }
        int mid = lo + (hi - lo) / 2;
        if (mid != hi) m.swap(mid, hi);
        m.setPivot(hi);
        int store = lo;
        for (int j = lo; j < hi; j++) {
            if (m.less(j, hi)) {
                if (store != j) m.swap(store, j);
                store++;
            }
        }
        if (store != hi) m.swap(store, hi);
        m.clearPivot();
        m.markSorted(store);
        quick(m, lo, store - 1);
        quick(m, store + 1, hi);
    }
}
