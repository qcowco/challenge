package pl.kontomatik.challenge.navigator.dto;

import java.util.Objects;

public class AuthResponse {
    private String flowId;
    private String token;
    private boolean hasErrors;

    public AuthResponse(String flowId, String token, boolean hasErrors) {
        this.flowId = flowId;
        this.token = token;
        this.hasErrors = hasErrors;
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

    public boolean hasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResponse that = (AuthResponse) o;
        return hasErrors == that.hasErrors && flowId.equals(that.flowId) && token.equals(that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowId, token, hasErrors);
    }
}
