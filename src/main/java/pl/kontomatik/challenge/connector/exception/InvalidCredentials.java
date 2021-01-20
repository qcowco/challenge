package pl.kontomatik.challenge.connector.exception;

public class InvalidCredentials extends RuntimeException {
  public InvalidCredentials(String message) {
    super(message);
  }
}
