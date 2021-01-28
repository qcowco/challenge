package pl.kontomatik.challenge.client.ipko;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class JsonResponseParser {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static String extractSessionId(Map<String, String> headers) {
    return headers.get("X-Session-Id");
  }

  public static String extractFlowId(String body) {
    return findPath(body, "flow_id");
  }

  public static String extractFlowToken(String body) {
    return findPath(body, "token");
  }

  private static String findPath(String body, String flow_id) {
    return mapJsonNode(body).findPath(flow_id).asText();
  }

  public static boolean containsCredentialErrors(String body) {
    return containsCredentialErrors(mapJsonNode(body));
  }

  private static boolean containsCredentialErrors(JsonNode responseNode) {
    JsonNode fields = responseNode.with("response").with("fields");
    boolean generalError = hasGeneralError(fields);
    boolean wrongCredential = hasCredentialError(fields);
    return generalError || wrongCredential;
  }

  private static boolean hasGeneralError(JsonNode fields) {
    return fields.hasNonNull("errors");
  }

  private static boolean hasCredentialError(JsonNode fields) {
    boolean wrongLogin = fields.with("login").hasNonNull("errors");
    boolean wrongPassword = fields.with("password").hasNonNull("errors");
    return wrongLogin || wrongPassword;
  }

  private static JsonNode mapJsonNode(String responseBody) {
    try {
      return OBJECT_MAPPER.readTree(responseBody);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Cannot parse json in response", e);
    }
  }

}
