package pl.kontomatik.challenge.client.ipko.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class PasswordRequest {

  @JsonProperty("state_id")
  private final String stateId;
  @JsonProperty("flow_id")
  private final String flowId;
  @JsonProperty("token")
  private final String flowToken;
  private final String action = "submit";
  private final Map<String, Object> data;

  public PasswordRequest(String stateId, String flowId, String flowToken, Map<String, Object> data) {
    this.stateId = stateId;
    this.flowId = flowId;
    this.flowToken = flowToken;
    this.data = data;
  }

}
