package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.navigator.dto.AuthRequest;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;

public class IpkoMapper {
    private int version = 3;
    private String location = "";
    private String authStateId = "state_id";
    private String action = "submit";

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
        AuthRequest authRequest = AuthRequest.builder()
                .setVersion(version)
                .setSeq(sequenceNumber)
                .setLocation(location)
                .setStateId(authStateId)
                .putData("login", username)
                .putData("fingerprint", fingerprint)
                .setAction(action)
                .build();

        return objectMapper.writeValueAsString(authRequest);
    }
}
