package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.connector.dto.AuthRequest;
import pl.kontomatik.challenge.connector.dto.AuthResponse;
import pl.kontomatik.challenge.connector.dto.BaseRequest;

import java.util.HashMap;
import java.util.Map;

public class HttpBodyMapper {
    private int version = 3;
    private String location = "";
    private String action = "submit";

    private String authStateId = "login";
    private String sessionStateId = "password";

    private String placement = "LoginPKO";
    private int placement_page_no = 0;

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
        boolean wrongLogin = fields.with(authStateId).hasNonNull("errors");
        boolean wrongPassword = fields.with(sessionStateId).hasNonNull("errors");

        return wrongLogin || wrongPassword;
    }

    public String getAuthRequestBodyFor(String fingerprint,
                                        String username, int sequenceNumber) {
        AuthRequest authRequest = getBaseRequest()
                .setSeq(sequenceNumber)
                .setStateId(authStateId)
                .putData("login", username)
                .putData("fingerprint", fingerprint)
                .build();

        return tryWriteAsString(authRequest);
    }

    private AuthRequest.Builder getBaseRequest() {
        return AuthRequest.authBuilder()
                .setVersion(version)
                .setLocation(location)
                .setAction(action);
    }

    public String getSessionAuthRequestBodyFor(String flowId, String token,
                                               String password, int sequenceNumber) {
        AuthRequest authRequest = getBaseRequest()
                .setSeq(sequenceNumber)
                .setStateId(sessionStateId)
                .setFlowId(flowId)
                .setToken(token)
                .putData("password", password)
                .putData("placement", placement)
                .putData("placement_page_no", placement_page_no)
                .build();

        return tryWriteAsString(authRequest);
    }

    public String getAccountsRequestBodyFor(int sequenceNumber) {
        BaseRequest accountsRequest = BaseRequest.builder()
                .setVersion(version)
                .setLocation(location)
                .setSeq(sequenceNumber)
                .putData("accounts", Map.of())
                .build();

        return tryWriteAsString(accountsRequest);
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
