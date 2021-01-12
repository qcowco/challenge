package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.navigator.dto.AuthRequest;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpkoMapperTest {
    private static final String LOGIN_RESPONSE_BODY = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}";

    private IpkoMapper ipkoMapper = new IpkoMapper();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenMapsAuthResponse_thenReturnsAuthResponse() throws JsonProcessingException {
        // given
        String flowId = "flow_id";
        String token = "token";
        boolean hasErrors = false;

        AuthResponse expectedResponse = new AuthResponse(flowId, token, hasErrors);

        // when
        AuthResponse actualResponse = ipkoMapper.getAuthResponseFrom(LOGIN_RESPONSE_BODY);

        // then
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void givenMapsStartAuthRequest_thenReturnsAuthRequestJson() throws JsonProcessingException {
        // given
        int version = 3;
        int sequenceNumber = 0;
        String location = "";
        String stateId = "state_id";
        String fingerprint = "fingerprint";
        String username = "username";
        String action = "submit";

        AuthRequest expectedRequest = AuthRequest.builder()
                .setVersion(version)
                .setSeq(sequenceNumber)
                .setLocation(location)
                .setStateId(stateId)
                .putData("login", username)
                .putData("fingerprint", fingerprint)
                .setAction(action)
                .build();

        String expectedJsonBody = objectMapper.writeValueAsString(expectedRequest);

        // when
        String actualJsonBody = ipkoMapper.getAuthRequestBodyFor(fingerprint, username, sequenceNumber);

        // then
        assertEquals(expectedJsonBody, actualJsonBody);
    }
}