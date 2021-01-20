package pl.kontomatik.challenge.connector.ipko.dto;

import java.util.Objects;

public class AuthResponse {
  public final String flowId;
  public final String token;
  public final boolean wrongCredentials;

  public AuthResponse(String flowId, String token, boolean wrongCredentials) {
    this.flowId = flowId;
    this.token = token;
    this.wrongCredentials = wrongCredentials;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AuthResponse that = (AuthResponse) o;
    return wrongCredentials == that.wrongCredentials && flowId.equals(that.flowId) && token.equals(that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(flowId, token, wrongCredentials);
  }
}
