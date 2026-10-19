package visualiser.model;

/** Unchecked: thrown inside the sorting thread when the user presses Stop/Reset. Unwinds the algorithm cleanly. */
public class SortCancelledException extends RuntimeException {
    public SortCancelledException() { super("Sort cancelled"); }
}
