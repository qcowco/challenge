package pl.kontomatik.challenge.connector.exception;

public class NotAuthenticated extends RuntimeException {
  public NotAuthenticated(String message) {
    super(message);
  }
}
