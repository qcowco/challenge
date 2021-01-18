package pl.kontomatik.challenge.exception;

public class ConnectionFailed extends RuntimeException {
    public ConnectionFailed(Throwable cause) {
        super(cause);
    }
}
