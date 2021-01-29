package pl.kontomatik.challenge.client.ipko.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.client.ipko.request.dto.Request;

import java.util.Map;

public class RequestMapper {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  public static String loginRequestJson(String username) {
    return writeAsJson(createLoginRequest(username));
  }

  private static Request createLoginRequest(String username) {
    return new Request("login", Map.of("login", username));
  }

  public static String passwordRequestJson(String flowId, String token,
                                           String password) {
    return writeAsJson(createPasswordRequest(flowId, token, password));
  }

  private static Request createPasswordRequest(String flowId, String token, String password) {
    return new Request("password", flowId, token, Map.of("password", password));
  }

  public static String accountsRequestJson() {
    return writeAsJson(createAccountsRequest());
  }

  private static Request createAccountsRequest() {
    return new Request(Map.of("accounts", Map.of()));
  }

  private static String writeAsJson(Request accountsRequest) {
    try {
      return OBJECT_MAPPER.writeValueAsString(accountsRequest);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't parse given node as Json", e);
    }
  }

}
