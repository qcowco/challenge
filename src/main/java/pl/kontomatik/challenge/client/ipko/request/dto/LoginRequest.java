package pl.kontomatik.challenge.client.ipko.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class LoginRequest {

  @JsonProperty("state_id")
  private final String stateId;
  private final String action = "submit";
  private final Map<String, Object> data;

  public LoginRequest(String stateId, Map<String, Object> data) {
    this.stateId = stateId;
    this.data = data;
  }

}
