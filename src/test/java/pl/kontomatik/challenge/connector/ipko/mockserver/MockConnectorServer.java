package pl.kontomatik.challenge.connector.ipko.mockserver;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.JsonBody;
import org.mockserver.socket.tls.KeyStoreFactory;

import javax.net.ssl.HttpsURLConnection;
import java.net.Proxy;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockitoExtension.class)
@MockServerSettings(ports = 1090)
public abstract class MockConnectorServer {
    private static final String LOGIN_JSON_TEMPLATE = "{\"data\":{\"login\":\"%s\"}}";
    private static final String USERNAME = "USERNAME";
    private static final String WRONG_USERNAME = "WRONG_USERNAME";
    private static final String LOGIN_PATH = "/ipko3/login";
    private static final String NDCD_PATH = "/nudatasecurity/2.2/w/w-573441/init/js";
    private static final String INIT_PATH = "/ipko3/init";
    private static final String SESSION_HEADER = "X-Session-Id";
    private static final String SESSION_TOKEN = "TOKEN";
    private static final String LOGIN_RESPONSE_BODY = "{\"response\":{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}}";
    private static final String BAD_LOGIN_RESPONSE_BODY = "{\"response\":{\"flow_id\":\"flow_id\",\"token\":\"token\",\"fields\":{\"errors\":{\"description\":\"An error!\"}}}}";
    private static final String ACCOUNT_RESPONSE_BODY = "{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"123456789\"},\"balance\":0.5}}}";

    protected static Proxy proxy;

    @BeforeAll
    public static void setupHttps() {
        HttpsURLConnection.setDefaultSSLSocketFactory(
                new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory()
        );
    }

    @BeforeAll
    public static void setupProxy(ClientAndServer clientAndServer) {
        proxy = new Proxy(Proxy.Type.HTTP, clientAndServer.remoteAddress());
    }

    protected static void setupMockedServer(MockServerClient mockServerClient) {
        mockSuccessfulLogin(mockServerClient);
        mockFailedLogin(mockServerClient);
        mockCookieRequest(mockServerClient);
        mockAccountsRequest(mockServerClient);
    }

    private static void mockSuccessfulLogin(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath(LOGIN_PATH)
                        .withBody(JsonBody.json(String.format(LOGIN_JSON_TEMPLATE, USERNAME),
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader(SESSION_HEADER, SESSION_TOKEN)
                        .withBody(LOGIN_RESPONSE_BODY)
                );
    }

    private static void mockFailedLogin(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath(LOGIN_PATH)
                        .withBody(JsonBody.json(String.format(LOGIN_JSON_TEMPLATE, WRONG_USERNAME),
                                MatchType.ONLY_MATCHING_FIELDS)))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(BAD_LOGIN_RESPONSE_BODY)
                );
    }

    private static void mockCookieRequest(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath(NDCD_PATH))
                .respond(response()
                        .withStatusCode(200));
    }

    private static void mockAccountsRequest(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath(INIT_PATH))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(ACCOUNT_RESPONSE_BODY));
    }
}
