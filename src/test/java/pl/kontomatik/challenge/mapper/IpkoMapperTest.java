package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.navigator.dto.AuthRequest;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;
import pl.kontomatik.challenge.navigator.dto.BaseRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpkoMapperTest {
    private static final String LOGIN_RESPONSE_BODY = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}";

    private final int VERSION = 3;
    private final String LOCATION = "";
    private final String ACTION = "submit";
    private final int SEQUENCE_NUMBER = 0;

    private final String AUTH_STATE_ID = "login";

    private final String SESSION_STATE_ID = "password";

    private final String FLOW_ID = "flow_id";
    private final String TOKEN = "token";

    private IpkoMapper ipkoMapper = new IpkoMapperImpl();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenMapsAuthResponse_thenReturnsAuthResponse() {
        // given
        boolean hasErrors = false;

        AuthResponse expectedResponse = new AuthResponse(FLOW_ID, TOKEN, hasErrors);

        // when
        AuthResponse actualResponse = ipkoMapper.getAuthResponseFrom(LOGIN_RESPONSE_BODY);

        // then
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void givenMapsAuthResponse_whenContainsGeneralError_thenSetsLoginErrorTrue() {
        // given
        String generalErrorTemplate = "{\"response\":{\"flow_id\":\"%s\",\"token\":\"%s\",\"fields\":{\"errors\":{}}}}";
        String generalErrorResponse = String.format(generalErrorTemplate, FLOW_ID, TOKEN);

        boolean hasErrors = true;

        AuthResponse expectedResponse = new AuthResponse(FLOW_ID, TOKEN, hasErrors);

        // when
        AuthResponse actualResponse = ipkoMapper.getAuthResponseFrom(generalErrorResponse);

        // then
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void givenMapsAuthResponse_whenContainsCredentialError_thenSetsLoginErrorTrue() {
        // given
        String credentialErrorTemplate = "{\"response\":{\"flow_id\":\"%s\",\"token\":\"%s\",\"fields\":{\"login\":{\"errors\":{}},\"password\":{\"errors\":{}}}}}";
        String credentialErrorResponse = String.format(credentialErrorTemplate, FLOW_ID, TOKEN);

        boolean hasErrors = true;

        AuthResponse expectedResponse = new AuthResponse(FLOW_ID, TOKEN, hasErrors);

        // when
        AuthResponse actualResponse = ipkoMapper.getAuthResponseFrom(credentialErrorResponse);

        // then
        assertEquals(expectedResponse, actualResponse);
    }


    @Test
    public void givenMapsStartAuthRequest_thenReturnsAuthRequestJson() throws JsonProcessingException {
        // given
        String fingerprint = "fingerprint";
        String username = "username";

        AuthRequest expectedRequest = getBaseRequest()
                .setStateId(AUTH_STATE_ID)
                .putData("login", username)
                .putData("fingerprint", fingerprint)
                .build();

        String expectedJsonBody = objectMapper.writeValueAsString(expectedRequest);

        // when
        String actualJsonBody = ipkoMapper.getAuthRequestBodyFor(fingerprint, username, SEQUENCE_NUMBER);

        // then
        assertEquals(expectedJsonBody, actualJsonBody);
    }

    private AuthRequest.Builder getBaseRequest() {
        return AuthRequest.authBuilder()
                .setVersion(VERSION)
                .setSeq(SEQUENCE_NUMBER)
                .setLocation(LOCATION)
                .setAction(ACTION);
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
                .setStateId(SESSION_STATE_ID)
                .setFlowId(flowId)
                .setToken(token)
                .putData("password", password)
                .putData("placement", placement)
                .putData("placement_page_no", placement_page_no)
                .build();

        String expectedJsonBody = objectMapper.writeValueAsString(expectedRequest);

        // when
        String actualJsonBody = ipkoMapper.getSessionAuthRequestBodyFor(flowId, token, password, SEQUENCE_NUMBER);

        // then
        assertEquals(expectedJsonBody, actualJsonBody);
    }

    @Test
    public void givenMapsAccountRequest_thenReturnsBaseRequestJson() throws JsonProcessingException {
        // given
        BaseRequest accountsRequest = BaseRequest.builder()
                .setVersion(VERSION)
                .setSeq(SEQUENCE_NUMBER)
                .setLocation(LOCATION)
                .putData("accounts", objectMapper.createObjectNode())
                .build();

        String expectedJsonBody = objectMapper.writeValueAsString(accountsRequest);

        // when
        String actualJsonBody = ipkoMapper.getAccountsRequestBodyFor(SEQUENCE_NUMBER);

        // then
        assertEquals(expectedJsonBody, actualJsonBody);
    }

    @Test
    public void givenMapsAccountsResponse_thenReturnsAccountMap() {
        // given
        String accountNumber = "123456789";
        double balance = 0.5;
        String jsonAccounts = String.format("{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"%s\"},\"balance\":%f}}}",
                accountNumber, balance);

        Map<String, Double> expectedAccounts = Map.of(accountNumber, balance);

        // when
        Map<String, Double> actualAccounts = ipkoMapper.getAccountsFromJson(jsonAccounts);

        // then
        assertEquals(expectedAccounts, actualAccounts);
    }
}