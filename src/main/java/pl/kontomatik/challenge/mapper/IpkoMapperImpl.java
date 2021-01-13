package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.navigator.dto.AuthRequest;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;
import pl.kontomatik.challenge.navigator.dto.BaseRequest;

import java.util.HashMap;
import java.util.Map;

public class IpkoMapperImpl implements IpkoMapper {
    private int version = 3;
    private String location = "";
    private String action = "submit";

    private String authStateId = "login";

    private String sessionStateId = "password";
    private String placement = "LoginPKO";
    private int placement_page_no = 0;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AuthResponse getAuthResponseFrom(String responseBody) throws JsonProcessingException {
        JsonNode responseNode = objectMapper.readTree(responseBody);

        String flowId = responseNode.findPath("flow_id").asText();
        String token = responseNode.findPath("token").asText();

        JsonNode fields = responseNode.with("response").with("fields");
        boolean wrongCredential = hasCredentialError(fields);

        return new AuthResponse(flowId, token, wrongCredential);
    }

    private boolean hasCredentialError(JsonNode fields) {
        boolean wrongLogin = fields.with(authStateId).hasNonNull("errors");
        boolean wrongPassword = fields.with(sessionStateId).hasNonNull("errors");

        return wrongLogin || wrongPassword;
    }

    @Override
    public String getAuthRequestBodyFor(String fingerprint,
                                        String username, int sequenceNumber) throws JsonProcessingException {
        AuthRequest authRequest = getBaseRequest()
                .setSeq(sequenceNumber)
                .setStateId(authStateId)
                .putData("login", username)
                .putData("fingerprint", fingerprint)
                .build();

        return objectMapper.writeValueAsString(authRequest);
    }

    private AuthRequest.Builder getBaseRequest() {
        return AuthRequest.authBuilder()
                .setVersion(version)
                .setLocation(location)
                .setAction(action);
    }

    @Override
    public String getSessionAuthRequestBodyFor(String flowId, String token,
                                               String password, int sequenceNumber) throws JsonProcessingException {
        AuthRequest authRequest = getBaseRequest()
                .setSeq(sequenceNumber)
                .setStateId(sessionStateId)
                .setFlowId(flowId)
                .setToken(token)
                .putData("password", password)
                .putData("placement", placement)
                .putData("placement_page_no", placement_page_no)
                .build();

        return objectMapper.writeValueAsString(authRequest);
    }

    @Override
    public String getAccountsRequestBodyFor(int sequenceNumber) throws JsonProcessingException {
        BaseRequest accountsRequest = BaseRequest.builder()
                .setVersion(version)
                .setLocation(location)
                .setSeq(sequenceNumber)
                .putData("accounts", Map.of())
                .build();

        return objectMapper.writeValueAsString(accountsRequest);
    }

    @Override
    public Map<String, Double> getAccountsFromJson(String jsonAccounts) throws JsonProcessingException {
        JsonNode accountsNode = findAccountsNode(jsonAccounts);

        return getAccountsFrom(accountsNode);
    }

    private JsonNode findAccountsNode(String jsonAccounts) throws JsonProcessingException {
        JsonNode accountsTree = objectMapper.readTree(jsonAccounts);

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
