package pl.kontomatik.challenge.client.ipko.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.client.ipko.request.dto.Request;

import java.util.HashMap;
import java.util.Map;

public class RequestMapper {

  private static final ObjectMapper objectMapper = new ObjectMapper();

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
      return objectMapper.writeValueAsString(accountsRequest);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't parse given node as String", e);
    }
  }

  public static Map<String, Double> getAccountsFromJson(String jsonAccounts) {
    JsonNode accountsNode = findAccountsNode(jsonAccounts);
    return parseAccounts(accountsNode);
  }

  private static JsonNode findAccountsNode(String jsonAccounts) {
    JsonNode accountsTree = mapJsonNode(jsonAccounts);
    return accountsTree.findPath("accounts");
  }

  private static JsonNode mapJsonNode(String responseBody) {
    try {
      return objectMapper.readTree(responseBody);
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
