package pl.kontomatik.challenge.connector.exception;

public class ConnectionFailed extends RuntimeException {
  public ConnectionFailed(Throwable cause) {
    super(cause);
  }
}
