package pl.kontomatik.challenge.client.ipko.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

  @JsonProperty("state_id")
  private String stateId;
  @JsonProperty("flow_id")
  private String flowId;
  @JsonProperty("token")
  private String flowToken;
  private String action = "submit";
  private Map<String, Object> data;

  public Request(Map<String, Object> data) {
    this.data = data;
  }

  public Request(String stateId, Map<String, Object> data) {
    this.stateId = stateId;
    this.data = data;
  }

  public Request(String stateId, String flowId, String flowToken, Map<String, Object> data) {
    this.stateId = stateId;
    this.flowId = flowId;
    this.flowToken = flowToken;
    this.data = data;
  }

}
