package visualiser.model;

import java.util.ArrayList;
import java.util.List;

/** Parses a user-typed array such as "5 3, 8; 1 9". All problems become InvalidArrayException. */
public final class ArrayParser {
    public static final int MIN_SIZE = 2, MAX_SIZE = 300, MAX_VALUE = 1000;

    private ArrayParser() {}

    public static int[] parse(String text) throws InvalidArrayException {
        if (text == null || text.isBlank())
            throw new InvalidArrayException("Type some numbers first, e.g. 5 3 8 1 9 2");
        List<Integer> values = new ArrayList<>();
        for (String tok : text.trim().split("[,;\\s]+")) {
            if (tok.isEmpty()) continue;
            int v;
            try {
                v = Integer.parseInt(tok);
            } catch (NumberFormatException e) {
                throw new InvalidArrayException("'" + tok + "' is not a whole number.", e);
            }
            if (v < 1 || v > MAX_VALUE)
                throw new InvalidArrayException("Values must be between 1 and " + MAX_VALUE + " (got " + v + ").");
            values.add(v);
        }
        if (values.size() < MIN_SIZE || values.size() > MAX_SIZE)
            throw new InvalidArrayException("Use between " + MIN_SIZE + " and " + MAX_SIZE
                    + " numbers (got " + values.size() + ").");
        return values.stream().mapToInt(Integer::intValue).toArray();
    }
}
