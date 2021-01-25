package pl.kontomatik.challenge.connector.ipko.dto;

import java.util.Map;

public class BaseRequest {

  private final Map<String, Object> data;

  public BaseRequest(Map<String, Object> data) {
    this.data = data;
  }

  public static BaseRequest accountsRequest() {
    return new BaseRequest(Map.of("accounts", Map.of()));
  }

}
