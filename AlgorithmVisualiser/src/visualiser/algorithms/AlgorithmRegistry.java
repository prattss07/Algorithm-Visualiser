package visualiser.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** One line here = a new algorithm appears in both dropdowns. */
public final class AlgorithmRegistry {
    private static final List<SortingAlgorithm> BUILT_IN = List.of(
            new BubbleSort(), new SelectionSort(), new InsertionSort(), new ShellSort(),
            new MergeSort(), new QuickSort(), new HeapSort());

    private static final List<SortingAlgorithm> CUSTOM = new CopyOnWriteArrayList<>();

    private AlgorithmRegistry() {}

    public static List<SortingAlgorithm> all() {
        if (CUSTOM.isEmpty()) {
            return BUILT_IN;
        }
        List<SortingAlgorithm> list = new ArrayList<>(BUILT_IN);
        list.addAll(CUSTOM);
        return Collections.unmodifiableList(list);
    }

    public static void register(SortingAlgorithm algorithm) {
        if (algorithm != null && !CUSTOM.contains(algorithm)) {
            CUSTOM.add(algorithm);
        }
    }

    public static List<SortingAlgorithm> customAlgorithms() {
        return Collections.unmodifiableList(CUSTOM);
    }
}
