package mod.swinegraphics.exception;

public class BackgroundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BackgroundException(String message) {
        super(message);
    }

    public BackgroundException(String message, Throwable cause) {
        super(message, cause);
    }
}