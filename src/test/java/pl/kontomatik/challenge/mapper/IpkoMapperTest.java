package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
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

    private final HttpBodyMapper mapper = new HttpBodyMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IpkoMapperTest() {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    @Test
    public void mapsAuthResponseFromJson() {
        boolean hasErrors = false;
        AuthResponse expectedResponse = new AuthResponse(FLOW_ID, TOKEN, hasErrors);
        AuthResponse actualResponse = mapper.getAuthResponseFrom(LOGIN_RESPONSE_BODY);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void mapsAuthResponseWithErrorOnGeneralError() {
        String generalErrorTemplate = "{\"response\":{\"flow_id\":\"%s\",\"token\":\"%s\",\"fields\":{\"errors\":{}}}}";
        String generalErrorResponse = String.format(generalErrorTemplate, FLOW_ID, TOKEN);
        boolean hasErrors = true;
        AuthResponse expectedResponse = new AuthResponse(FLOW_ID, TOKEN, hasErrors);
        AuthResponse actualResponse = mapper.getAuthResponseFrom(generalErrorResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void mapsAuthResponseWithErrorOnCredentialError() {
        String credentialErrorTemplate = "{\"response\":{\"flow_id\":\"%s\",\"token\":\"%s\",\"fields\":{\"login\":{\"errors\":{}},\"password\":{\"errors\":{}}}}}";
        String credentialErrorResponse = String.format(credentialErrorTemplate, FLOW_ID, TOKEN);
        boolean hasErrors = true;
        AuthResponse expectedResponse = new AuthResponse(FLOW_ID, TOKEN, hasErrors);
        AuthResponse actualResponse = mapper.getAuthResponseFrom(credentialErrorResponse);
        assertEquals(expectedResponse, actualResponse);
    }


    @Test
    public void mapsJsonFromAuthRequest() throws JsonProcessingException {
        String fingerprint = "fingerprint";
        String username = "username";
        AuthRequest expectedRequest = getBaseRequest()
                .setStateId(AUTH_STATE_ID)
                .putData("login", username)
                .putData("fingerprint", fingerprint)
                .build();
        String expectedJsonBody = objectMapper.writeValueAsString(expectedRequest);
        String actualJsonBody = mapper.getAuthRequestBodyFor(fingerprint, username, SEQUENCE_NUMBER);
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
    public void mapsJsonFromSessionAuthRequest() throws JsonProcessingException {
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
        String actualJsonBody = mapper.getSessionAuthRequestBodyFor(flowId, token, password, SEQUENCE_NUMBER);
        assertEquals(expectedJsonBody, actualJsonBody);
    }

    @Test
    public void mapsJsonFromBaseRequest() throws JsonProcessingException {
        BaseRequest accountsRequest = BaseRequest.builder()
                .setVersion(VERSION)
                .setSeq(SEQUENCE_NUMBER)
                .setLocation(LOCATION)
                .putData("accounts", objectMapper.createObjectNode())
                .build();
        String expectedJsonBody = objectMapper.writeValueAsString(accountsRequest);
        String actualJsonBody = mapper.getAccountsRequestBodyFor(SEQUENCE_NUMBER);
        assertEquals(expectedJsonBody, actualJsonBody);
    }

    @Test
    public void mapsAccountsFromJson() {
        String accountNumber = "123456789";
        double balance = 0.5;
        String jsonAccounts = String.format("{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"%s\"},\"balance\":%f}}}",
                accountNumber, balance);
        Map<String, Double> expectedAccounts = Map.of(accountNumber, balance);
        Map<String, Double> actualAccounts = mapper.getAccountsFromJson(jsonAccounts);
        assertEquals(expectedAccounts, actualAccounts);
    }
}