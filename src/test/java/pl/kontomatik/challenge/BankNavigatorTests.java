package pl.kontomatik.challenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankNavigatorTests {
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String SESSION_TOKEN = "TOKEN";

    @Mock(answer = Answers.RETURNS_SELF)
    private Connection loginConnection;

    @Mock(answer = Answers.RETURNS_SELF)
    private Connection cookieConnection;

    @Mock
    private Connection.Response loginResponse;

    @Mock
    private Connection.Response cookieResponse;

    private String loginResponseBody = "{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenLoggingIn_whenCorrectCredentials_thenIsAuthenticated() throws IOException {
        // given
        BankNavigator bankNavigator = new BankNavigator();

        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect("https://www.ipko.pl/ipko3/login"))
                    .thenReturn(loginConnection);

            given(loginConnection.execute())
                    .willReturn(loginResponse);

            given(loginResponse.headers())
                    .willReturn(Map.of("X-Session-Id", SESSION_TOKEN));

            given(loginResponse.body())
                    .willReturn(loginResponseBody);

            jsoup.when(() -> Jsoup.connect(startsWith("https://www.ipko.pl/nudatasecurity/2.2/w/w-573441/init/js/")))
                    .thenReturn(cookieConnection);

            given(cookieConnection.execute())
                    .willReturn(cookieResponse);

            given(cookieResponse.cookies())
                    .willReturn(new HashMap<>());

            bankNavigator.login(USERNAME, PASSWORD);
        }

        // when
        boolean authenticated = bankNavigator.isAuthenticated();

        // then
        assertTrue(authenticated);
    }

}
