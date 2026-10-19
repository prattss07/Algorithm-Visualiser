package visualiser.audio;

/** Checked: no audio device / format. The GUI catches it and simply disables the sound option. */
public class SoundUnavailableException extends Exception {
    public SoundUnavailableException(String message, Throwable cause) { super(message, cause); }
}
