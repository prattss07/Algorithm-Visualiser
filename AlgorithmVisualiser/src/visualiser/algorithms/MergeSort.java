package visualiser.algorithms;

import visualiser.model.ArrayModel;

public final class MergeSort extends SortingAlgorithm {
    public MergeSort() {
        super("Merge Sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(n)",
              "Splits in halves, sorts each, then merges them back using a temporary copy.");
    }

    @Override public void sort(ArrayModel m) { sort(m, 0, m.size() - 1); }

    private void sort(ArrayModel m, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) >>> 1;
        sort(m, lo, mid);
        sort(m, mid + 1, hi);
        merge(m, lo, mid, hi);
    }

    private void merge(ArrayModel m, int lo, int mid, int hi) {
        int[] tmp = new int[hi - lo + 1];
        for (int k = 0; k < tmp.length; k++) tmp[k] = m.get(lo + k);
        int i = 0, j = mid - lo + 1;
        int iEnd = mid - lo, jEnd = hi - lo;
        for (int k = lo; k <= hi; k++) {
            int v;
            if (i > iEnd) v = tmp[j++];
            else if (j > jEnd) v = tmp[i++];
            else if (m.lessThan(tmp[j], tmp[i], lo + j, lo + i)) v = tmp[j++];
            else v = tmp[i++];
            m.set(k, v);
        }
    }
}
