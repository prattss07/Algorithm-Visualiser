package visualiser.algorithms;

import visualiser.model.ArrayModel;

public final class InsertionSort extends SortingAlgorithm {
    public InsertionSort() {
        super("Insertion Sort", "O(n)", "O(n^2)", "O(n^2)", "O(1)",
              "Takes each element and slides it left into its place among the already-sorted prefix.");
    }

    @Override public void sort(ArrayModel m) {
        for (int i = 1; i < m.size(); i++) {
            int key = m.get(i);
            int j = i - 1;
            while (j >= 0 && m.lessThan(key, m.get(j), j + 1, j)) {
                m.set(j + 1, m.get(j));
                j--;
            }
            if (j + 1 != i) m.set(j + 1, key);
        }
    }
}
