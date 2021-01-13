package pl.kontomatik.challenge.navigator;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kontomatik.challenge.exception.InvalidCredentialsException;
import pl.kontomatik.challenge.exception.NotAuthenticatedException;
import pl.kontomatik.challenge.mapper.IpkoMapper;
import pl.kontomatik.challenge.mapper.IpkoMapperImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IpkoNavigatorTests {
    private static final String LOGIN_URL = "https://www.ipko.pl/ipko3/login";
    private static final String NDCD_URL = "https://www.ipko.pl/nudatasecurity/2.2/w/w-573441/init/js";
    private static final String INIT_URL = "https://www.ipko.pl/ipko3/init";

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static final String SESSION_HEADER = "X-Session-Id";
    private static final String SESSION_TOKEN = "TOKEN";

    private static final String LOGIN_RESPONSE_BODY = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}";
    private static final String BAD_LOGIN_RESPONSE_BODY = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"fields\":{\"errors\":{\"description\":\"An error!\"}}}";
    private static final String ACCOUNT_RESPONSE_BODY = "{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"123456789\"},\"balance\":0.5}}}";

    private static final String ACCOUNT_NUMBER = "123456789";
    private static final double ACCOUNT_BALANCE = 0.5;

    private static final Map<String, String> COOKIES = new HashMap<>();

    private static IpkoNavigator bankNavigator;
    private static IpkoMapper ipkoMapper = new IpkoMapperImpl();

    @Mock(answer = Answers.RETURNS_SELF)
    private Connection loginConnection;

    @Mock(answer = Answers.RETURNS_SELF)
    private Connection cookieConnection;

    @Mock(answer = Answers.RETURNS_SELF)
    private Connection accountConnection;

    @Mock
    private Connection.Response loginResponse;

    @Mock
    private Connection.Response cookieResponse;

    @Mock
    private Connection.Response accountResponse;


    @BeforeEach
    public void setupEach() {
        bankNavigator = new IpkoNavigator(ipkoMapper);
    }

    @Test
    public void givenAuthenticationCheck_whenLoggedIn_thenReturnsIsAuthenticated() throws IOException {
        // given
        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect(LOGIN_URL))
                    .thenReturn(loginConnection);

            given(loginConnection.execute())
                    .willReturn(loginResponse);

            given(loginResponse.headers())
                    .willReturn(Map.of(SESSION_HEADER, SESSION_TOKEN));

            given(loginResponse.body())
                    .willReturn(LOGIN_RESPONSE_BODY);

            jsoup.when(() -> Jsoup.connect(startsWith(NDCD_URL)))
                    .thenReturn(cookieConnection);

            given(cookieConnection.execute())
                    .willReturn(cookieResponse);

            given(cookieResponse.cookies())
                    .willReturn(COOKIES);

            bankNavigator.login(USERNAME, PASSWORD);
        }

        // when
        boolean authenticated = bankNavigator.isAuthenticated();

        // then
        assertTrue(authenticated);
    }

    @Test
    public void givenAuthenticationCheck_whenNotLoggedIn_thenReturnsNotAuthenticated() throws IOException {
        // given
        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect(LOGIN_URL))
                    .thenReturn(loginConnection);

            given(loginConnection.execute())
                    .willReturn(loginResponse);

            given(loginResponse.body())
                    .willReturn(BAD_LOGIN_RESPONSE_BODY);

            jsoup.when(() -> Jsoup.connect(startsWith(NDCD_URL)))
                    .thenReturn(cookieConnection);

            given(cookieConnection.execute())
                    .willReturn(cookieResponse);

            given(cookieResponse.cookies())
                    .willReturn(COOKIES);

            try {
                bankNavigator.login(USERNAME, PASSWORD);
            } catch (InvalidCredentialsException exception) {

            }
        }

        // when
        boolean authenticated = bankNavigator.isAuthenticated();

        // then
        assertFalse(authenticated);
    }

    @Test
    public void givenLoggingIn_whenFails_thenThrows_LoginFailedException() throws IOException {
        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect(LOGIN_URL))
                    .thenReturn(loginConnection);

            given(loginConnection.execute())
                    .willReturn(loginResponse);

            given(loginResponse.body())
                    .willReturn(BAD_LOGIN_RESPONSE_BODY);

            jsoup.when(() -> Jsoup.connect(startsWith(NDCD_URL)))
                    .thenReturn(cookieConnection);

            given(cookieConnection.execute())
                    .willReturn(cookieResponse);

            given(cookieResponse.cookies())
                    .willReturn(COOKIES);

            assertThrows(InvalidCredentialsException.class, () -> bankNavigator.login(USERNAME, PASSWORD));
        }
    }

    @Test
    public void givenRequestingAccounts_whenLoggedIn_thenReturnsAccounts() throws IOException {
        // given
        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect(LOGIN_URL))
                    .thenReturn(loginConnection);

            given(loginConnection.execute())
                    .willReturn(loginResponse);

            given(loginResponse.headers())
                    .willReturn(Map.of(SESSION_HEADER, SESSION_TOKEN));

            given(loginResponse.body())
                    .willReturn(LOGIN_RESPONSE_BODY);

            jsoup.when(() -> Jsoup.connect(startsWith(NDCD_URL)))
                    .thenReturn(cookieConnection);

            given(cookieConnection.execute())
                    .willReturn(cookieResponse);

            given(cookieResponse.cookies())
                    .willReturn(COOKIES);

            jsoup.when(() -> Jsoup.connect(INIT_URL))
                    .thenReturn(accountConnection);

            given(accountConnection.execute())
                    .willReturn(accountResponse);

            given(accountResponse.body())
                    .willReturn(ACCOUNT_RESPONSE_BODY);

            Map<String, Double> expectedAccounts = Map.of(ACCOUNT_NUMBER, ACCOUNT_BALANCE);

            bankNavigator.login(USERNAME, PASSWORD);

            // when
            Map<String, Double> accounts = bankNavigator.getAccounts();

            // then
            assertEquals(expectedAccounts, accounts);
        }
    }

    @Test
    public void givenRequestingAccounts_whenNotLoggedIn_thenThrows_NotAuthenticatedException() {
        // when/then
        assertThrows(NotAuthenticatedException.class, bankNavigator::getAccounts);
    }

}
