package visualiser.model;

/** Checked: the custom array typed by the user cannot be visualised. The GUI must handle it. */
public class InvalidArrayException extends Exception {
    public InvalidArrayException(String message) { super(message); }
    public InvalidArrayException(String message, Throwable cause) { super(message, cause); }
}
