package pl.kontomatik.challenge.exception;

public class NotAuthenticated extends RuntimeException {
    public NotAuthenticated(String message) {
        super(message);
    }
}
