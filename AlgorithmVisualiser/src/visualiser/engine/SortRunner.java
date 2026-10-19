package visualiser.engine;

import java.util.function.Consumer;
import visualiser.algorithms.SortingAlgorithm;
import visualiser.model.ArrayModel;
import visualiser.model.SortCancelledException;

/** The task executed on a worker thread: runs one algorithm on one model and reports how it ended. */
public final class SortRunner implements Runnable {
    public record Outcome(RunState state, String message) { }

    private final SortingAlgorithm algorithm;
    private final ArrayModel model;
    private final Consumer<Outcome> onFinished; // called on the worker thread; the GUI hops to the EDT itself

    public SortRunner(SortingAlgorithm algorithm, ArrayModel model, Consumer<Outcome> onFinished) {
        this.algorithm = algorithm; this.model = model; this.onFinished = onFinished;
    }

    @Override public void run() {
        Outcome outcome;
        try {
            algorithm.sort(model);
            model.sweepSorted();
            if (!model.isSorted())
                throw new IllegalStateException(algorithm.getName() + " left the array unsorted");
            outcome = new Outcome(RunState.DONE, "");
        } catch (SortCancelledException e) {
            outcome = new Outcome(RunState.STOPPED, "");
        } catch (RuntimeException e) {                      // bug in an algorithm: report, never crash the app
            outcome = new Outcome(RunState.FAILED, e.toString());
        }
        onFinished.accept(outcome);
    }
}
