package pl.kontomatik.challenge.client.ipko.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizedRequest {

  private final Map<String, Object> data;

  public AuthorizedRequest(Map<String, Object> data) {
    this.data = data;
  }

}
