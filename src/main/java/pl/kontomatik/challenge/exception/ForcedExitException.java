package pl.kontomatik.challenge.exception;

public class ForcedExitException extends RuntimeException {
    public ForcedExitException(String message) {
        super(message);
    }
}
