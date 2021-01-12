package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpkoMapperTest {
    private static final String LOGIN_RESPONSE_BODY = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}";

    private IpkoMapper ipkoMapper = new IpkoMapper();

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
}