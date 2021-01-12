package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.navigator.dto.AuthRequest;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;

public class IpkoMapper {
    private int version = 3;
    private String location = "";
    private String action = "submit";

    private String authStateId = "login";

    private String sessionStateId = "password";
    private String placement = "LoginPKO";
    private int placement_page_no = 0;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthResponse getAuthResponseFrom(String responseBody) throws JsonProcessingException {
        JsonNode responseNode = objectMapper.readTree(responseBody);

        String flowId = responseNode.get("flow_id").asText();
        String token = responseNode.get("token").asText();
        boolean hasErrors = responseNode.with("fields").has("errors");

        return new AuthResponse(flowId, token, hasErrors);
    }

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
}
