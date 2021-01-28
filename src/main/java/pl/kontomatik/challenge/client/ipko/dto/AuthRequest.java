package pl.kontomatik.challenge.client.ipko.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequest extends BaseRequest {

  @JsonProperty("state_id")
  public final String stateId;
  @JsonProperty("flow_id")
  public final String flowId;
  public final String token;
  public final String action = "submit";

  AuthRequest(Map<String, Object> data, String stateId,
              String flowId, String token) {
    super(data);
    this.stateId = stateId;
    this.flowId = flowId;
    this.token = token;
  }

  public static Builder authBuilder() {
    return new Builder();
  }

  public static class Builder {

    private final Map<String, Object> data;
    private String stateId;
    private String flowId;
    private String token;

    public Builder() {
      data = new HashMap<>();
    }

    public Builder putData(String key, Object value) {
      this.data.put(key, value);
      return this;
    }

    public Builder setStateId(String stateId) {
      this.stateId = stateId;
      return this;
    }

    public Builder setFlowId(String flowId) {
      this.flowId = flowId;
      return this;
    }

    public Builder setToken(String token) {
      this.token = token;
      return this;
    }

    public AuthRequest build() {
      return new AuthRequest(data, stateId, flowId, token);
    }

  }

}
