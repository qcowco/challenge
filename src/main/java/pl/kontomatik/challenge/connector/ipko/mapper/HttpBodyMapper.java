package pl.kontomatik.challenge.connector.ipko.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.connector.ipko.dto.AuthRequest;
import pl.kontomatik.challenge.connector.ipko.dto.AuthResponse;
import pl.kontomatik.challenge.connector.ipko.dto.BaseRequest;

import java.util.HashMap;
import java.util.Map;

public class HttpBodyMapper {
  private static final int VERSION = 3;
  private static final String LOCATION = "";
  private static final String ACTION = "submit";
  private static final String AUTH_STATE_ID = "login";
  private static final String SESSION_STATE_ID = "password";
  private static final String PLACEMENT = "LoginPKO";
  private static final int PLACEMENT_PAGE_NO = 0;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public HttpBodyMapper() {
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  public AuthResponse getAuthResponseFrom(String responseBody) {
    JsonNode responseNode = tryGetJsonNodeFrom(responseBody);
    String flowId = responseNode.findPath("flow_id").asText();
    String token = responseNode.findPath("token").asText();
    boolean wrongCredential = containsLoginErrors(responseNode);
    return new AuthResponse(flowId, token, wrongCredential);
  }

  private JsonNode tryGetJsonNodeFrom(String responseBody) {
    try {
      return objectMapper.readTree(responseBody);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Cannot parse json in response", e);
    }
  }

  private boolean containsLoginErrors(JsonNode responseNode) {
    JsonNode fields = responseNode.with("response").with("fields");
    boolean generalError = hasGeneralError(fields);
    boolean wrongCredential = hasCredentialError(fields);
    return generalError || wrongCredential;
  }

  private boolean hasGeneralError(JsonNode fields) {
    return fields.hasNonNull("errors");
  }

  private boolean hasCredentialError(JsonNode fields) {
    boolean wrongLogin = fields.with(AUTH_STATE_ID).hasNonNull("errors");
    boolean wrongPassword = fields.with(SESSION_STATE_ID).hasNonNull("errors");
    return wrongLogin || wrongPassword;
  }

  public String getAuthRequestBodyFor(String fingerprint,
                                      String username, int sequenceNumber) {
    AuthRequest authRequest = authRequestFor(fingerprint, username, sequenceNumber);
    return tryWriteAsString(authRequest);
  }

  private AuthRequest authRequestFor(String fingerprint, String username, int sequenceNumber) {
    return getBaseRequest()
      .setSeq(sequenceNumber)
      .setStateId(AUTH_STATE_ID)
      .putData("login", username)
      .putData("fingerprint", fingerprint)
      .build();
  }

  private AuthRequest.Builder getBaseRequest() {
    return AuthRequest.authBuilder()
      .setVersion(VERSION)
      .setLocation(LOCATION)
      .setAction(ACTION);
  }

  public String getSessionAuthRequestBodyFor(String flowId, String token,
                                             String password, int sequenceNumber) {
    AuthRequest authRequest = sessionAuthRequestFor(flowId, token, password, sequenceNumber);
    return tryWriteAsString(authRequest);
  }

  private AuthRequest sessionAuthRequestFor(String flowId, String token, String password, int sequenceNumber) {
    return getBaseRequest()
      .setSeq(sequenceNumber)
      .setStateId(SESSION_STATE_ID)
      .setFlowId(flowId)
      .setToken(token)
      .putData("password", password)
      .putData("placement", PLACEMENT)
      .putData("placement_page_no", PLACEMENT_PAGE_NO)
      .build();
  }

  public String getAccountsRequestBodyFor(int sequenceNumber) {
    BaseRequest accountsRequest = accountsRequestFor(sequenceNumber);
    return tryWriteAsString(accountsRequest);
  }

  private BaseRequest accountsRequestFor(int sequenceNumber) {
    return BaseRequest.builder()
      .setVersion(VERSION)
      .setLocation(LOCATION)
      .setSeq(sequenceNumber)
      .putData("accounts", Map.of())
      .build();
  }

  private String tryWriteAsString(BaseRequest accountsRequest) {
    String value;
    try {
      value = objectMapper.writeValueAsString(accountsRequest);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't parse given node as String", e);
    }
    return value;
  }

  public Map<String, Double> getAccountsFromJson(String jsonAccounts) {
    JsonNode accountsNode = findAccountsNode(jsonAccounts);
    return getAccountsFrom(accountsNode);
  }

  private JsonNode findAccountsNode(String jsonAccounts) {
    JsonNode accountsTree = tryGetJsonNodeFrom(jsonAccounts);
    return accountsTree.findPath("accounts");
  }

  private Map<String, Double> getAccountsFrom(JsonNode accountsNode) {
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
