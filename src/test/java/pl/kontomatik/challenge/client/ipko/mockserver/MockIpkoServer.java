package pl.kontomatik.challenge.client.ipko.mockserver;

import org.junit.jupiter.api.BeforeAll;
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

@MockServerSettings(ports = 1090)
public abstract class MockIpkoServer {
  private static final String LOGIN_JSON_TEMPLATE = "{\"data\":{\"login\":\"%s\"}}";
  private static final String PASSWORD_JSON_TEMPLATE = "{\"data\":{\"password\":\"%s\"}}";
  protected static final String USERNAME = "USERNAME";
  protected static final String WRONG_USERNAME = "WRONG_USERNAME";
  protected static final String PASSWORD = "PASSWORD";
  protected static final String WRONG_PASSWORD = "WRONG_PASSWORD";
  protected static final String ACCOUNT_NUMBER = "123456789";
  protected static final double ACCOUNT_BALANCE = 0.5;
  private static final String LOGIN_PATH = "/ipko3/login";
  private static final String NDCD_PATH = "/nudatasecurity/2.2/w/w-573441/init/js";
  private static final String INIT_PATH = "/ipko3/init";
  private static final String SESSION_HEADER = "X-Session-Id";
  private static final String SESSION_TOKEN = "TOKEN";
  private static final String LOGIN_RESPONSE_BODY = "{\"response\":{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}}";
  private static final String BAD_AUTH_RESPONSE_BODY = "{\"response\":{\"flow_id\":\"flow_id\",\"token\":\"token\",\"fields\":{\"errors\":{\"description\":\"An error!\"}}}}";
  private static final String ACCOUNT_RESPONSE_TEMPLATE = "{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"%s\"},\"balance\":%f}}}";

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
    mockSuccessfulAuthentication(mockServerClient);
    mockFailedAuthentication(mockServerClient);
    mockCookieRequest(mockServerClient);
    mockAccountsRequest(mockServerClient);
    mockNotFound(mockServerClient);
  }

  private static void mockSuccessfulAuthentication(MockServerClient mockServerClient) {
    mockSuccessfulLogin(mockServerClient);
    mockSuccessfulSessionAuthorization(mockServerClient);
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

  private static void mockSuccessfulSessionAuthorization(MockServerClient mockServerClient) {
    mockServerClient
      .when(request()
        .withMethod("POST")
        .withPath(LOGIN_PATH)
        .withBody(JsonBody.json(String.format(PASSWORD_JSON_TEMPLATE, PASSWORD),
          MatchType.ONLY_MATCHING_FIELDS)))
      .respond(response()
        .withStatusCode(200)
        .withHeader(SESSION_HEADER, SESSION_TOKEN)
        .withBody(LOGIN_RESPONSE_BODY)
      );
  }

  private static void mockFailedAuthentication(MockServerClient mockServerClient) {
    mockFailedLogin(mockServerClient);
    mockFailedSessionAuthorization(mockServerClient);
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
        .withBody(BAD_AUTH_RESPONSE_BODY)
      );
  }

  private static void mockFailedSessionAuthorization(MockServerClient mockServerClient) {
    mockServerClient
      .when(request()
        .withMethod("POST")
        .withPath(LOGIN_PATH)
        .withBody(JsonBody.json(String.format(PASSWORD_JSON_TEMPLATE, WRONG_PASSWORD),
          MatchType.ONLY_MATCHING_FIELDS)))
      .respond(response()
        .withStatusCode(200)
        .withHeader(SESSION_HEADER, SESSION_TOKEN)
        .withBody(BAD_AUTH_RESPONSE_BODY)
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
        .withBody(String.format(ACCOUNT_RESPONSE_TEMPLATE, ACCOUNT_NUMBER, ACCOUNT_BALANCE)));
  }

  private static void mockNotFound(MockServerClient mockServerClient) {
    mockServerClient.when(request())
      .respond(response()
        .withStatusCode(404));
  }
}
