package pl.kontomatik.challenge.client.exception;

public class NotAuthenticated extends RuntimeException {
  public NotAuthenticated(String message) {
    super(message);
  }
}
