package pl.kontomatik.challenge.connector.ipko.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequest extends BaseRequest {
  @JsonProperty("state_id")
  private final String stateId;
  @JsonProperty("flow_id")
  private final String flowId;
  private final String token;
  private final String action;

  public AuthRequest(int version, int seq, String location, Map<String, Object> data, String stateId,
                     String flowId, String token, String action) {
    super(version, seq, location, data);
    this.stateId = stateId;
    this.flowId = flowId;
    this.token = token;
    this.action = action;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AuthRequest that = (AuthRequest) o;
    return stateId.equals(that.stateId) && Objects.equals(flowId, that.flowId)
            && Objects.equals(token, that.token) && action.equals(that.action);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stateId, flowId, token, action);
  }

  public static Builder authBuilder() {
    return new Builder();
  }

  public static class Builder {
    private int version;
    private int seq;
    private String location;
    private final Map<String, Object> data;
    private String stateId;
    private String flowId;
    private String token;
    private String action;

    public Builder() {
      data = new HashMap<>();
    }

    public Builder setVersion(int version) {
      this.version = version;
      return this;
    }

    public Builder setSeq(int seq) {
      this.seq = seq;
      return this;
    }

    public Builder setLocation(String location) {
      this.location = location;
      return this;
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

    public Builder setAction(String action) {
      this.action = action;
      return this;
    }

    public AuthRequest build() {
      return new AuthRequest(version, seq, location, data, stateId, flowId, token, action);
    }
  }
}
