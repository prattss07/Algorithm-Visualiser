package visualiser.model;

/** Observer: told about every visible array access (used for sound). Called from the sorting thread. */
@FunctionalInterface
public interface OperationListener {
    void onAccess(int value, int maxValue);
}
