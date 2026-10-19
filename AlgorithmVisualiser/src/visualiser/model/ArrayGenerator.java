package visualiser.model;

import java.util.Random;

public final class ArrayGenerator {
    private ArrayGenerator() {}

    public enum Pattern {
        RANDOM("Random"), NEARLY_SORTED("Nearly sorted"), REVERSED("Reversed"),
        FEW_UNIQUE("Few unique values"), SORTED("Already sorted");

        private final String label;
        Pattern(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public static int[] generate(int n, Pattern pattern) {
        Random rnd = new Random();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i + 1;
        switch (pattern) {
            case SORTED -> { }
            case REVERSED -> { for (int i = 0; i < n; i++) a[i] = n - i; }
            case RANDOM -> shuffle(a, rnd);
            case NEARLY_SORTED -> {
                int swaps = Math.max(1, n / 10);
                for (int s = 0; s < swaps; s++) swap(a, rnd.nextInt(n), rnd.nextInt(n));
            }
            case FEW_UNIQUE -> { for (int i = 0; i < n; i++) a[i] = (rnd.nextInt(5) + 1) * Math.max(1, n / 5); }
        }
        return a;
    }

    private static void shuffle(int[] a, Random rnd) {
        for (int i = a.length - 1; i > 0; i--) swap(a, i, rnd.nextInt(i + 1));
    }

    private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
}
