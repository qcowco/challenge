package pl.kontomatik.challenge.connector.ipko.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.connector.ipko.dto.AuthRequest;
import pl.kontomatik.challenge.connector.ipko.dto.AuthResponse;
import pl.kontomatik.challenge.connector.ipko.dto.BaseRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpBodyMapperTest {
    private static final String FLOW_ID = "flow_id";
    private static final String TOKEN = "token";
    private static final String FINGERPRINT = "fingerprint";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final int SEQUENCE_NUMBER = 0;
    private static final int VERSION = 3;
    private static final String LOCATION = "";

    private final HttpBodyMapper mapper = new HttpBodyMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpBodyMapperTest() {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    @Test
    public void mapsAuthResponseFromJson() {
        boolean hasErrors = false;
        AuthResponse expectedResponse = new AuthResponse(FLOW_ID, TOKEN, hasErrors);
        String loginResponse = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}";
        AuthResponse actualResponse = mapper.getAuthResponseFrom(loginResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void mapsAuthResponseWithErrorOnGeneralError() {
        boolean hasErrors = true;
        AuthResponse expectedResponse = new AuthResponse(FLOW_ID, TOKEN, hasErrors);
        String errorResponse = "{\"response\":{\"flow_id\":\"flow_id\",\"token\":\"token\",\"fields\":{\"errors\":{}}}}";
        AuthResponse actualResponse = mapper.getAuthResponseFrom(errorResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void mapsAuthResponseWithErrorOnCredentialError() {
        boolean hasErrors = true;
        AuthResponse expectedResponse = new AuthResponse(FLOW_ID, TOKEN, hasErrors);
        String errorResponse = "{\"response\":" +
                "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"fields\":" +
                "{\"login\":{\"errors\":{}},\"password\":{\"errors\":{}}}" +
                "}" +
                "}";
        AuthResponse actualResponse = mapper.getAuthResponseFrom(errorResponse);
        assertEquals(expectedResponse, actualResponse);
    }


    @Test
    public void mapsJsonFromAuthRequest() throws JsonProcessingException {
        AuthRequest expectedRequest = getBaseRequest()
                .setStateId("login")
                .putData("login", USERNAME)
                .putData(FINGERPRINT, FINGERPRINT)
                .build();
        String expectedRequestJson = objectMapper.writeValueAsString(expectedRequest);
        String actualRequestJson = mapper.getAuthRequestBodyFor(FINGERPRINT, USERNAME, SEQUENCE_NUMBER);
        assertEquals(expectedRequestJson, actualRequestJson);
    }

    private AuthRequest.Builder getBaseRequest() {
        return AuthRequest.authBuilder()
                .setVersion(VERSION)
                .setSeq(SEQUENCE_NUMBER)
                .setLocation(LOCATION)
                .setAction("submit");
    }

    @Test
    public void mapsJsonFromSessionAuthRequest() throws JsonProcessingException {
        String sessionStateId = "password";
        AuthRequest expectedRequest = getBaseRequest()
                .setStateId(sessionStateId)
                .setFlowId(FLOW_ID)
                .setToken(TOKEN)
                .putData(PASSWORD, PASSWORD)
                .putData("placement", "LoginPKO")
                .putData("placement_page_no", 0)
                .build();
        String expectedJsonBody = objectMapper.writeValueAsString(expectedRequest);
        String actualJsonBody = mapper.getSessionAuthRequestBodyFor(FLOW_ID, TOKEN, PASSWORD, SEQUENCE_NUMBER);
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
        Double balance = 0.5;
        String accountNumber = "123456789";
        Map<String, Double> expectedAccounts = Map.of(accountNumber, balance);
        String jsonAccounts = "{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"123456789\"},\"balance\":0.5}}}";
        Map<String, Double> actualAccounts = mapper.getAccountsFromJson(jsonAccounts);
        assertEquals(expectedAccounts, actualAccounts);
    }
}