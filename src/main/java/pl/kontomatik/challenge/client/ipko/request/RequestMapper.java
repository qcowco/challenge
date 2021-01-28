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

  public static String createLoginRequestBody(String username) {
    Request authRequest = new Request("login", Map.of("login", username));
    return writeString(authRequest);
  }

  public static String createPasswordRequestBody(String flowId, String token,
                                                 String password) {
    Request authRequest = new Request("password", flowId, token, Map.of("password", password));
    return writeString(authRequest);
  }

  public static String accountsRequestBody() {
    return writeString(accountsRequest());
  }

  private static Request accountsRequest() {
    return new Request(Map.of("accounts", Map.of()));
  }

  private static String writeString(Request accountsRequest) {
    try {
      return OBJECT_MAPPER.writeValueAsString(accountsRequest);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't parse given node as String", e);
    }
  }

}
