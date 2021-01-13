package pl.kontomatik.challenge.navigator.dto;

import java.util.Objects;

public class AuthResponse {
    private String flowId;
    private String token;
    private boolean wrongCredentials;

    public AuthResponse(String flowId, String token, boolean wrongCredentials) {
        this.flowId = flowId;
        this.token = token;
        this.wrongCredentials = wrongCredentials;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isWrongCredentials() {
        return wrongCredentials;
    }

    public void setWrongCredentials(boolean wrongCredentials) {
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
