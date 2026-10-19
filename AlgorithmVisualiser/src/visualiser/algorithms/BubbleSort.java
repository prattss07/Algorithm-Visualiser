package visualiser.algorithms;

import visualiser.model.ArrayModel;

public final class BubbleSort extends SortingAlgorithm {
    public BubbleSort() {
        super("Bubble Sort", "O(n)", "O(n^2)", "O(n^2)", "O(1)",
              "Repeatedly swaps adjacent out-of-order pairs; the largest value bubbles to the end each pass.");
    }

    @Override public void sort(ArrayModel m) {
        int n = m.size();
        for (int end = n - 1; end > 0; end--) {
            boolean swapped = false;
            for (int j = 0; j < end; j++) {
                if (m.less(j + 1, j)) { m.swap(j, j + 1); swapped = true; }
            }
            m.markSorted(end);
            if (!swapped) break;
        }
    }
}
