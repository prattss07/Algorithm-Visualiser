package visualiser.algorithms;

import visualiser.model.ArrayModel;

public final class HeapSort extends SortingAlgorithm {
    public HeapSort() {
        super("Heap Sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(1)",
              "Builds a max-heap, then repeatedly swaps the root to the end and restores the heap.");
    }

    @Override public void sort(ArrayModel m) {
        int n = m.size();
        for (int i = n / 2 - 1; i >= 0; i--) siftDown(m, i, n);
        for (int end = n - 1; end > 0; end--) {
            m.swap(0, end);
            m.markSorted(end);
            siftDown(m, 0, end);
        }
    }

    private void siftDown(ArrayModel m, int i, int n) {
        while (true) {
            int l = 2 * i + 1;
            if (l >= n) return;
            int c = l;
            if (l + 1 < n && m.less(l, l + 1)) c = l + 1;
            if (!m.less(i, c)) return;
            m.swap(i, c);
            i = c;
        }
    }
}
