package pl.kontomatik.challenge.client.ipko.request.dto;

import java.util.Map;

public class BaseRequest {

  public final Map<String, Object> data;

  protected BaseRequest(Map<String, Object> data) {
    this.data = data;
  }

  public static BaseRequest accountsRequest() {
    return new BaseRequest(Map.of("accounts", Map.of()));
  }

}
