package pl.kontomatik.challenge;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankNavigatorTests {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String SESSION_TOKEN = "TOKEN";
    private static final String LOGIN_RESPONSE_BODY = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}";
    private static final String BAD_LOGIN_RESPONSE_BODY = "{\"flow_id\":\"flow_id\",\"token\":\"token\"}";
    private static final String ACCOUNT_RESPONSE_BODY = "{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"123456789\"},\"balance\":0.5}}}";
    private static final String ACCOUNT_NUMBER = "123456789";
    private static final double ACCOUNT_BALANCE = 0.5;


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

    @Nested
    @DisplayName("Given a login is being requested")
    class Login {

        @Nested
        @DisplayName("When login is correct")
        class Correct {

            @Test
            @DisplayName("Then is authenticated")
            public void shouldReturnAuthenticated() throws IOException {
                // given
                BankNavigator bankNavigator = new BankNavigator();

                try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
                    jsoup.when(() -> Jsoup.connect("https://www.ipko.pl/ipko3/login"))
                            .thenReturn(loginConnection);

                    given(loginConnection.execute())
                            .willReturn(loginResponse);

                    given(loginResponse.headers())
                            .willReturn(Map.of("X-Session-Id", SESSION_TOKEN));

                    jsoup.when(() -> Jsoup.connect(startsWith("https://www.ipko.pl/nudatasecurity/2.2/w/w-573441/init/js/")))
                            .thenReturn(cookieConnection);

                    given(cookieConnection.execute())
                            .willReturn(cookieResponse);

                    given(cookieResponse.cookies())
                            .willReturn(new HashMap<>());

                    given(loginResponse.body())
                            .willReturn(LOGIN_RESPONSE_BODY);

                    bankNavigator.login(USERNAME, PASSWORD);
                }

                // when
                boolean authenticated = bankNavigator.isAuthenticated();

                // then
                assertTrue(authenticated);
            }
        }

        @Nested
        @DisplayName("When login is incorrect")
        class Incorrect {

            @Test
            @DisplayName("Then isn't authenticated")
            public void shouldReturnNotAuthenticated() throws IOException {
                // given
                BankNavigator bankNavigator = new BankNavigator();

                try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
                    jsoup.when(() -> Jsoup.connect("https://www.ipko.pl/ipko3/login"))
                            .thenReturn(loginConnection);

                    given(loginConnection.execute())
                            .willReturn(loginResponse);

                    given(loginResponse.headers())
                            .willReturn(Map.of("X-Session-Id", SESSION_TOKEN));

                    jsoup.when(() -> Jsoup.connect(startsWith("https://www.ipko.pl/nudatasecurity/2.2/w/w-573441/init/js/")))
                            .thenReturn(cookieConnection);

                    given(cookieConnection.execute())
                            .willReturn(cookieResponse);

                    given(cookieResponse.cookies())
                            .willReturn(new HashMap<>());

                    given(loginResponse.body())
                            .willReturn(BAD_LOGIN_RESPONSE_BODY);

                    bankNavigator.login(USERNAME, PASSWORD);
                }

                // when
                boolean authenticated = bankNavigator.isAuthenticated();

                // then
                assertFalse(authenticated);
            }
        }
    }

    @Test
    public void givenRequestingAccounts_whenLoggedIn_thenReturnsAccounts() throws IOException {
        // given
        BankNavigator bankNavigator = new BankNavigator();

        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect("https://www.ipko.pl/ipko3/login"))
                    .thenReturn(loginConnection);

            given(loginConnection.execute())
                    .willReturn(loginResponse);

            given(loginResponse.headers())
                    .willReturn(Map.of("X-Session-Id", SESSION_TOKEN));

            jsoup.when(() -> Jsoup.connect(startsWith("https://www.ipko.pl/nudatasecurity/2.2/w/w-573441/init/js/")))
                    .thenReturn(cookieConnection);

            given(cookieConnection.execute())
                    .willReturn(cookieResponse);

            given(cookieResponse.cookies())
                    .willReturn(new HashMap<>());

            given(loginResponse.body())
                    .willReturn(BAD_LOGIN_RESPONSE_BODY);

            jsoup.when(() -> Jsoup.connect("https://www.ipko.pl/ipko3/init"))
                    .thenReturn(accountConnection);

            given(accountConnection.execute())
                    .willReturn(accountResponse);

            given(accountResponse.body())
                    .willReturn(ACCOUNT_RESPONSE_BODY);

            Map<String, Double> expectedAccounts = Map.of(ACCOUNT_NUMBER, ACCOUNT_BALANCE);

            bankNavigator.login(USERNAME, PASSWORD);

            //when
            Map<String, Double> accounts = bankNavigator.getAccounts();

            //then
            assertEquals(expectedAccounts, accounts);
        }
    }

}
