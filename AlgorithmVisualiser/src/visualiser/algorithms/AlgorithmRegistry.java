package visualiser.algorithms;

import java.util.List;

/** One line here = a new algorithm appears in both dropdowns. */
public final class AlgorithmRegistry {
    private AlgorithmRegistry() {}

    public static List<SortingAlgorithm> all() {
        return List.of(new BubbleSort(), new SelectionSort(), new InsertionSort(), new ShellSort(),
                       new MergeSort(), new QuickSort(), new HeapSort());
    }
}
