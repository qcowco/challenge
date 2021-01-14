package pl.kontomatik.challenge.navigator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import pl.kontomatik.challenge.exception.InvalidCredentialsException;
import pl.kontomatik.challenge.exception.NotAuthenticatedException;
import pl.kontomatik.challenge.mapper.IpkoMapper;
import pl.kontomatik.challenge.mapper.IpkoMapperImpl;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith({MockitoExtension.class, MockServerExtension.class})
public class IpkoNavigatorTests {
    private static final String MOCK_URL_TEMPLATE = "http://localhost:%d%s";
    private static final String LOGIN_PATH = "/ipko3/login";
    private static final String NDCD_PATH = "/nudatasecurity/2.2/w/w-573441/init/js";
    private static final String INIT_PATH = "/ipko3/init";

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static final String SESSION_HEADER = "X-Session-Id";
    private static final String SESSION_TOKEN = "TOKEN";

    private static final String LOGIN_RESPONSE_BODY = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}";
    private static final String BAD_LOGIN_RESPONSE_BODY = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"fields\":{\"errors\":{\"description\":\"An error!\"}}}";
    private static final String ACCOUNT_RESPONSE_BODY = "{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"123456789\"},\"balance\":0.5}}}";

    private static final String ACCOUNT_NUMBER = "123456789";
    private static final double ACCOUNT_BALANCE = 0.5;

    private static IpkoNavigator bankNavigator;
    private static IpkoMapper ipkoMapper = new IpkoMapperImpl();

    @BeforeEach
    public void setupEach(MockServerClient client, ClientAndServer clientAndServer) {
        setupBankNavigator(clientAndServer);

        client.clear(request());
    }

    private void setupBankNavigator(ClientAndServer clientAndServer) {
        bankNavigator = new IpkoNavigator(ipkoMapper);

        setupMockUrls(clientAndServer.getPort());
    }

    private void setupMockUrls(Integer port) {
        bankNavigator.setLoginUrl(getMockUrl(port, LOGIN_PATH));
        bankNavigator.setNdcdUrl(getMockUrl(port, NDCD_PATH));
        bankNavigator.setInitUrl(getMockUrl(port, INIT_PATH));
    }

    private static String getMockUrl(Integer port, String path) {
        return String.format(MOCK_URL_TEMPLATE, port, path);
    }

    @Test
    public void givenAuthenticationCheck_whenLoggedIn_thenReturnsIsAuthenticated(MockServerClient mockServerClient) throws IOException {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        bankNavigator.login(USERNAME, PASSWORD);

        // when
        boolean authenticated = bankNavigator.isAuthenticated();

        // then
        assertTrue(authenticated);
    }

    private void mockSuccessfulLogin(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/ipko3/login"))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader(SESSION_HEADER, SESSION_TOKEN)
                        .withBody(LOGIN_RESPONSE_BODY)
                );
    }

    @Test
    public void givenAuthenticationCheck_whenNotLoggedIn_thenReturnsNotAuthenticated(MockServerClient mockServerClient) throws IOException {
        // given
        mockFailedLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        try {
            bankNavigator.login(USERNAME, PASSWORD);
        } catch (InvalidCredentialsException exception) {

        }

        // when
        boolean authenticated = bankNavigator.isAuthenticated();

        // then
        assertFalse(authenticated);
    }

    private void mockFailedLogin(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/ipko3/login"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(BAD_LOGIN_RESPONSE_BODY)
                );
    }

    @Test
    public void givenLoggingIn_whenFails_thenThrows_LoginFailedException(MockServerClient mockServerClient) {
        // given
        mockFailedLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        // when/then
        assertThrows(InvalidCredentialsException.class, () -> bankNavigator.login(USERNAME, PASSWORD));
    }

    private void mockCookieRequest(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath(NDCD_PATH))
                .respond(response()
                        .withStatusCode(200));
    }

    @Test
    public void givenRequestingAccounts_whenLoggedIn_thenReturnsAccounts(MockServerClient mockServerClient) throws IOException {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        mockAccountsRequest(mockServerClient);

        Map<String, Double> expectedAccounts = Map.of(ACCOUNT_NUMBER, ACCOUNT_BALANCE);

        bankNavigator.login(USERNAME, PASSWORD);

        // when
        Map<String, Double> accounts = bankNavigator.getAccounts();

        // then
        assertEquals(expectedAccounts, accounts);
    }

    private void mockAccountsRequest(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath(INIT_PATH))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(ACCOUNT_RESPONSE_BODY));
    }

    @Test
    public void givenRequestingAccounts_whenNotLoggedIn_thenThrows_NotAuthenticatedException() {
        // when/then
        assertThrows(NotAuthenticatedException.class, bankNavigator::getAccounts);
    }

}
