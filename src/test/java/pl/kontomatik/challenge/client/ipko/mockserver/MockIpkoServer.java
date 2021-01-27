package pl.kontomatik.challenge.client.ipko.mockserver;

import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.JsonBody;
import org.mockserver.socket.tls.KeyStoreFactory;

import javax.net.ssl.HttpsURLConnection;
import java.net.Proxy;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.Not.not;
import static pl.kontomatik.challenge.client.ipko.mockserver.MockIpkoServer.MockData.*;

public class MockIpkoServer {

  public static class MockData {

    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String ACCOUNT_NUMBER = "123456789";
    public static final double ACCOUNT_BALANCE = 0.5;

  }

  private static final String LOGIN_JSON_TEMPLATE = "{\"data\":{\"login\":\"%s\"}}";
  private static final String PASSWORD_JSON_TEMPLATE = "{\"data\":{\"password\":\"%s\"}}";
  private static final String LOGIN_PATH = "/ipko3/login";
  private static final String NDCD_PATH = "/nudatasecurity/2.2/w/w-573441/init/js";
  private static final String INIT_PATH = "/ipko3/init";
  private static final String SESSION_HEADER = "X-Session-Id";
  private static final String SESSION_TOKEN = "TOKEN";
  private static final String LOGIN_RESPONSE_BODY = "{\"response\":{\"flow_id\":\"flow_id\",\"token\":\"token\",\"finished\":true}}";
  private static final String BAD_AUTH_RESPONSE_BODY = "{\"response\":{\"flow_id\":\"flow_id\",\"token\":\"token\",\"fields\":{\"errors\":{\"description\":\"An error!\"}}}}";
  private static final String ACCOUNT_RESPONSE_TEMPLATE = "{\"accounts\":{\"acc1\":{\"number\":{\"value\":\"%s\"},\"balance\":%f}}}";

  private ClientAndServer mockServer;

  private MockIpkoServer() {
    mockServer = ClientAndServer.startClientAndServer(1090);
    setupHttps();
    setupMocks();
  }

  public static MockIpkoServer startMockIpkoServer() {
    return new MockIpkoServer();
  }

  private static void setupHttps() {
    HttpsURLConnection.setDefaultSSLSocketFactory(
      new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory()
    );
  }

  public Proxy getProxy() {
    return new Proxy(Proxy.Type.HTTP, mockServer.remoteAddress());
  }

  public void stop() {
    mockServer.stop();
  }

  private void setupMocks() {
    mockSuccessfulAuthentication();
    mockFailedAuthentication();
    mockAccountsRequest();
    mockNotFound();
  }

  private void mockSuccessfulAuthentication() {
    mockSuccessfulLogin();
    mockSuccessfulSessionAuthorization();
  }

  private void mockSuccessfulLogin() {
    mockServer
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

  private void mockSuccessfulSessionAuthorization() {
    mockServer
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

  private void mockFailedAuthentication() {
    mockFailedLogin();
    mockFailedSessionAuthorization();
  }

  private void mockFailedLogin() {
    mockServer
      .when(request()
        .withMethod("POST")
        .withPath(LOGIN_PATH)
        .withBody(not(JsonBody.json(String.format(LOGIN_JSON_TEMPLATE, USERNAME),
          MatchType.ONLY_MATCHING_FIELDS))))
      .respond(response()
        .withStatusCode(200)
        .withBody(BAD_AUTH_RESPONSE_BODY)
      );
  }

  private void mockFailedSessionAuthorization() {
    mockServer
      .when(request()
        .withMethod("POST")
        .withPath(LOGIN_PATH)
        .withBody(not(JsonBody.json(String.format(PASSWORD_JSON_TEMPLATE, PASSWORD),
          MatchType.ONLY_MATCHING_FIELDS))))
      .respond(response()
        .withStatusCode(200)
        .withHeader(SESSION_HEADER, SESSION_TOKEN)
        .withBody(BAD_AUTH_RESPONSE_BODY)
      );
  }

  private void mockAccountsRequest() {
    mockServer
      .when(request()
        .withMethod("POST")
        .withPath(INIT_PATH))
      .respond(response()
        .withStatusCode(200)
        .withBody(String.format(ACCOUNT_RESPONSE_TEMPLATE, ACCOUNT_NUMBER, ACCOUNT_BALANCE)));
  }

  private void mockNotFound() {
    mockServer.when(request())
      .respond(response()
        .withStatusCode(404));
  }

}
