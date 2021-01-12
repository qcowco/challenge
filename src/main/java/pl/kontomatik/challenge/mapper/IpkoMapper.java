package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;

public class IpkoMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthResponse getAuthResponseFrom(String responseBody) throws JsonProcessingException {
        JsonNode responseNode = objectMapper.readTree(responseBody);

        String flowId = responseNode.get("flow_id").asText();
        String token = responseNode.get("token").asText();
        boolean hasErrors = responseNode.with("fields").has("errors");

        return new AuthResponse(flowId, token, hasErrors);
    }
}
