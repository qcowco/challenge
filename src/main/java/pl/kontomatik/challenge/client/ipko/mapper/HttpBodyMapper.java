package pl.kontomatik.challenge.client.ipko.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.client.ipko.dto.AuthRequest;
import pl.kontomatik.challenge.client.ipko.dto.AuthResponse;
import pl.kontomatik.challenge.client.ipko.dto.BaseRequest;

import java.util.HashMap;
import java.util.Map;

public class HttpBodyMapper {

  private static final String AUTH_STATE_ID = "login";
  private static final String SESSION_STATE_ID = "password";
  private final ObjectMapper objectMapper = new ObjectMapper();

  public HttpBodyMapper() {
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  public AuthResponse getAuthResponseFrom(Map<String, String> headers, String body) {
    JsonNode responseNode = tryGetJsonNodeFrom(body);
    return authResponseFrom(headers, responseNode);
  }

  private JsonNode tryGetJsonNodeFrom(String responseBody) {
    try {
      return objectMapper.readTree(responseBody);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Cannot parse json in response", e);
    }
  }

  private static AuthResponse authResponseFrom(Map<String, String> headers, JsonNode responseNode) {
    String sessionToken = headers.get("X-Session-Id");
    String flowId = responseNode.findPath("flow_id").asText();
    String token = responseNode.findPath("token").asText();
    boolean wrongCredential = containsLoginErrors(responseNode);
    return new AuthResponse(sessionToken, flowId, token, wrongCredential);
  }

  private static boolean containsLoginErrors(JsonNode responseNode) {
    JsonNode fields = responseNode.with("response").with("fields");
    boolean generalError = hasGeneralError(fields);
    boolean wrongCredential = hasCredentialError(fields);
    return generalError || wrongCredential;
  }

  private static boolean hasGeneralError(JsonNode fields) {
    return fields.hasNonNull("errors");
  }

  private static boolean hasCredentialError(JsonNode fields) {
    boolean wrongLogin = fields.with(AUTH_STATE_ID).hasNonNull("errors");
    boolean wrongPassword = fields.with(SESSION_STATE_ID).hasNonNull("errors");
    return wrongLogin || wrongPassword;
  }

  public String getAuthRequestBodyFor(String username) {
    AuthRequest authRequest = authRequestFor(username);
    return tryWriteAsString(authRequest);
  }

  private static AuthRequest authRequestFor(String username) {
    return AuthRequest.authBuilder()
      .setStateId(AUTH_STATE_ID)
      .putData("login", username)
      .build();
  }

  public String getSessionAuthRequestBodyFor(String flowId, String token,
                                             String password) {
    AuthRequest authRequest = sessionAuthRequestFor(flowId, token, password);
    return tryWriteAsString(authRequest);
  }

  private static AuthRequest sessionAuthRequestFor(String flowId, String token, String password) {
    return AuthRequest.authBuilder()
      .setStateId(SESSION_STATE_ID)
      .setFlowId(flowId)
      .setToken(token)
      .putData("password", password)
      .build();
  }

  private String tryWriteAsString(BaseRequest accountsRequest) {
    try {
      return objectMapper.writeValueAsString(accountsRequest);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't parse given node as String", e);
    }
  }

  public String accountsRequestBody() {
    return tryWriteAsString(BaseRequest.accountsRequest());
  }

  public Map<String, Double> getAccountsFromJson(String jsonAccounts) {
    JsonNode accountsNode = findAccountsNode(jsonAccounts);
    return getAccountsFrom(accountsNode);
  }

  private JsonNode findAccountsNode(String jsonAccounts) {
    JsonNode accountsTree = tryGetJsonNodeFrom(jsonAccounts);
    return accountsTree.findPath("accounts");
  }

  private static Map<String, Double> getAccountsFrom(JsonNode accountsNode) {
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
