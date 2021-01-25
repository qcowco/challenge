package pl.kontomatik.challenge.connector.ipko.dto;

import java.util.Objects;

public class AuthResponse {
  public final String sessionToken;
  public final String flowId;
  public final String flowToken;
  public final boolean wrongCredentials;

  public AuthResponse(String sessionToken, String flowId, String flowToken, boolean wrongCredentials) {
    this.sessionToken = sessionToken;
    this.flowId = flowId;
    this.flowToken = flowToken;
    this.wrongCredentials = wrongCredentials;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AuthResponse that = (AuthResponse) o;
    return wrongCredentials == that.wrongCredentials && sessionToken.equals(that.sessionToken) &&
      flowId.equals(that.flowId) && flowToken.equals(that.flowToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionToken, flowId, flowToken, wrongCredentials);
  }
}
