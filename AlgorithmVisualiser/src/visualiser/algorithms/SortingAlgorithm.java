package visualiser.algorithms;

import visualiser.model.ArrayModel;

/** Abstract base: an algorithm only describes itself and implements sort(model) via model operations. */
public abstract class SortingAlgorithm {
    private final String name, best, average, worst, space, description;

    protected SortingAlgorithm(String name, String best, String average, String worst,
                               String space, String description) {
        this.name = name; this.best = best; this.average = average;
        this.worst = worst; this.space = space; this.description = description;
    }

    public abstract void sort(ArrayModel m);

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getComplexitySummary() {
        return "Best " + best + " | Average " + average + " | Worst " + worst + " | Space " + space;
    }
    @Override public String toString() { return name; }
}
