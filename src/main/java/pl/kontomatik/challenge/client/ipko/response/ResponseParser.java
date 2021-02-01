package pl.kontomatik.challenge.client.ipko.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class ResponseParser {

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

  private static String findPath(String body, String path) {
    return mapJsonNode(body).findPath(path).asText();
  }

  public static boolean containsCredentialErrors(String body) {
    JsonNode fields = findFieldsNode(body);
    return hasGeneralError(fields) || hasFieldError(fields);
  }

  private static JsonNode findFieldsNode(String body) {
    JsonNode responseNode = mapJsonNode(body);
    return responseNode.with("response").with("fields");
  }

  private static boolean hasGeneralError(JsonNode fields) {
    return fields.hasNonNull("errors");
  }

  private static boolean hasFieldError(JsonNode fields) {
    boolean wrongLogin = fields.with("login").hasNonNull("errors");
    boolean wrongPassword = fields.with("password").hasNonNull("errors");
    return wrongLogin || wrongPassword;
  }

  public static Map<String, Double> parseAccounts(String json) {
    JsonNode accountsNode = findAccountsNode(json);
    return parseAccounts(accountsNode);
  }

  private static JsonNode findAccountsNode(String jsonAccounts) {
    JsonNode accountsTree = mapJsonNode(jsonAccounts);
    return accountsTree.findPath("accounts");
  }

  private static JsonNode mapJsonNode(String responseBody) {
    try {
      return OBJECT_MAPPER.readTree(responseBody);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Cannot parse json in response", e);
    }
  }

  private static Map<String, Double> parseAccounts(JsonNode accountsNode) {
    Map<String, Double> accountMap = new HashMap<>();
    accountsNode.forEach(accountNode -> {
      String account = accountNode.with("number")
        .get("value").asText();
      Double balance = accountNode.get("balance")
        .asDouble();
      accountMap.put(account, balance);
    });
    return accountMap;
  }

}
