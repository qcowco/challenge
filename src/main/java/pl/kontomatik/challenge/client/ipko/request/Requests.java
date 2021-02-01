package pl.kontomatik.challenge.client.ipko.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.client.ipko.request.dto.AuthorizedRequest;
import pl.kontomatik.challenge.client.ipko.request.dto.LoginRequest;
import pl.kontomatik.challenge.client.ipko.request.dto.PasswordRequest;

import java.util.Map;

public class Requests {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  public static String createLoginRequestBody(String username) {
    var body = new LoginRequest("login", Map.of("login", username));
    return writeAsJson(body);
  }

  public static String createPasswordRequestBody(String flowId, String token, String password) {
    var body = new PasswordRequest("password", flowId, token, Map.of("password", password));
    return writeAsJson(body);
  }

  public static String createAccountsRequestBody() {
    var body = new AuthorizedRequest(Map.of("accounts", Map.of()));
    return writeAsJson(body);
  }

  private static String writeAsJson(Object requestBody) {
    try {
      return OBJECT_MAPPER.writeValueAsString(requestBody);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't parse given node as Json", e);
    }
  }

}
