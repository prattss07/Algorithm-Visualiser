package visualiser.algorithms;

import visualiser.model.ArrayModel;

public final class ShellSort extends SortingAlgorithm {
    public ShellSort() {
        super("Shell Sort", "O(n log n)", "~O(n^1.5)", "O(n^2)", "O(1)",
              "Insertion sort over shrinking gaps, so far-apart elements move early.");
    }

    @Override public void sort(ArrayModel m) {
        int n = m.size();
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                int tmp = m.get(i);
                int j = i;
                while (j >= gap && m.lessThan(tmp, m.get(j - gap), j, j - gap)) {
                    m.set(j, m.get(j - gap));
                    j -= gap;
                }
                if (j != i) m.set(j, tmp);
            }
        }
    }
}
