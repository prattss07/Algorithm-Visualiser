package visualiser.algorithms;

import visualiser.model.ArrayModel;

public final class SelectionSort extends SortingAlgorithm {
    public SelectionSort() {
        super("Selection Sort", "O(n^2)", "O(n^2)", "O(n^2)", "O(1)",
              "Finds the minimum of the unsorted part and swaps it into place.");
    }

    @Override public void sort(ArrayModel m) {
        int n = m.size();
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) if (m.less(j, min)) min = j;
            if (min != i) m.swap(i, min);
            m.markSorted(i);
        }
    }
}
