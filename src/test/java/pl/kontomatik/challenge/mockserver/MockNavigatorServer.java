package pl.kontomatik.challenge.mockserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import pl.kontomatik.challenge.mapper.IpkoMapper;
import pl.kontomatik.challenge.mapper.IpkoMapperImpl;
import pl.kontomatik.challenge.navigator.IpkoNavigator;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
public class MockNavigatorServer {
    private static final String MOCK_URL_TEMPLATE = "http://localhost:%d%s";
    private static final String LOGIN_PATH = "/ipko3/login";
    private static final String NDCD_PATH = "/nudatasecurity/2.2/w/w-573441/init/js";
    private static final String INIT_PATH = "/ipko3/init";

    private static final String SESSION_HEADER = "X-Session-Id";
    private static final String SESSION_TOKEN = "TOKEN";

    private static final String LOGIN_RESPONSE_BODY = "{\"response\":{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}}";
    private static final String BAD_LOGIN_RESPONSE_BODY = "{\"response\":{\"flow_id\":\"flow_id\",\"token\":\"token\",\"fields\":{\"errors\":{\"description\":\"An error!\"}}}}";
    private static final String ACCOUNT_RESPONSE_BODY = "{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"123456789\"},\"balance\":0.5}}}";


    protected static IpkoNavigator bankNavigator;
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

    protected void mockSuccessfulLogin(MockServerClient mockServerClient) {
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

    protected void mockFailedLogin(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/ipko3/login"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(BAD_LOGIN_RESPONSE_BODY)
                );
    }

    protected void mockCookieRequest(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath(NDCD_PATH))
                .respond(response()
                        .withStatusCode(200));
    }

    protected void mockAccountsRequest(MockServerClient mockServerClient) {
        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath(INIT_PATH))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(ACCOUNT_RESPONSE_BODY));
    }
}
