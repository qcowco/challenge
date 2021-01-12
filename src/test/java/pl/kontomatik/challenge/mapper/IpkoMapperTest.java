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

    private int version = 3;
    private String location = "";
    private String action = "submit";
    private int sequenceNumber = 0;

    private String authStateId = "login";

    private String sessionStateId = "password";

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
        String fingerprint = "fingerprint";
        String username = "username";

        AuthRequest expectedRequest = getBaseRequest()
                .setStateId(authStateId)
                .putData("login", username)
                .putData("fingerprint", fingerprint)
                .build();

        String expectedJsonBody = objectMapper.writeValueAsString(expectedRequest);

        // when
        String actualJsonBody = ipkoMapper.getAuthRequestBodyFor(fingerprint, username, sequenceNumber);

        // then
        assertEquals(expectedJsonBody, actualJsonBody);
    }

    private AuthRequest.Builder getBaseRequest() {
        return AuthRequest.authBuilder()
                .setVersion(version)
                .setSeq(sequenceNumber)
                .setLocation(location)
                .setAction(action);
    }

    @Test
    public void givenMapsSessionAuthRequest_thenReturnsAuthRequestJson() throws JsonProcessingException {
        // given
        String flowId = "password";
        String token = "token";
        String password = "password";
        String placement = "LoginPKO";
        int placement_page_no = 0;

        AuthRequest expectedRequest = getBaseRequest()
                .setStateId(sessionStateId)
                .setFlowId(flowId)
                .setToken(token)
                .putData("password", password)
                .putData("placement", placement)
                .putData("placement_page_no", placement_page_no)
                .build();

        String expectedJsonBody = objectMapper.writeValueAsString(expectedRequest);

        // when
        String actualJsonBody = ipkoMapper.getSessionAuthRequestBodyFor(flowId, token, password, sequenceNumber);

        // then
        assertEquals(expectedJsonBody, actualJsonBody);
    }

}