package pl.kontomatik.challenge.client.ipko;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.http.JSoupHttpClient;
import pl.kontomatik.challenge.client.ipko.mapper.HttpBodyMapper;

import java.util.Map;

import static pl.kontomatik.challenge.client.ipko.JsonResponseParser.extractFlowToken;

public class IpkoClient implements BankClient {

  private static final String LOGIN_URL = "https://www.ipko.pl/ipko3/login";
  private static final String SESSION_HEADER = "X-Session-Id";
  private final JSoupHttpClient httpClient;

  public IpkoClient(JSoupHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public AuthorizedSession signIn(String username, String password) {
    Connection.Response loginResponse = submitLogin(username);
    return submitPassword(loginResponse, password);
  }

  private Connection.Response submitLogin(String username) {
    Connection request = createLoginRequest(username);
    Connection.Response response = httpClient.send(request);
    assertCredentialAccepted(response.body());
    return response;
  }

  private static Connection createLoginRequest(String username) {
    return Jsoup.connect(LOGIN_URL)
      .ignoreContentType(true)
      .requestBody(RequestMapper.createLoginRequestBody(username))
      .method(Connection.Method.POST);
  }

  private AuthorizedSession submitPassword(Connection.Response loginResponse, String password) {
    Connection request = createPasswordRequest(loginResponse, password);
    Connection.Response response = httpClient.send(request);
    assertCredentialAccepted(response.body());
    return new IpkoSession(JsonResponseParser.extractSessionId(response.headers()));
  }

  private static Connection createPasswordRequest(Connection.Response response, String password) {
    return Jsoup.connect(LOGIN_URL)
      .ignoreContentType(true)
      .requestBody(createPasswordRequestBody(response.body(), password))
      .header(SESSION_HEADER, JsonResponseParser.extractSessionId(response.headers()))
      .method(Connection.Method.POST);
  }

  private static String createPasswordRequestBody(String responseBody, String password) {
    return RequestMapper.createPasswordRequestBody(JsonResponseParser.extractFlowId(responseBody),
      JsonResponseParser.extractFlowToken(responseBody), password);
  }

  private static void assertCredentialAccepted(String body) {
    if (JsonResponseParser.containsCredentialErrors(body))
      throw new InvalidCredentials("Couldn't login with provided credentials.");
  }

  public class IpkoSession implements AuthorizedSession {

    private final String sessionToken;

    private IpkoSession(String sessionToken) {
      this.sessionToken = sessionToken;
    }

    @Override
    public Map<String, Double> fetchAccounts() {
      String jsonResponse = sendAccountsRequest()
        .body();
      return RequestMapper.getAccountsFromJson(jsonResponse);
    }

    private Connection.Response sendAccountsRequest() {
      Connection request = accountsRequest();
      return httpClient.send(request);
    }

    private Connection accountsRequest() {
      return Jsoup.connect("https://www.ipko.pl/ipko3/init")
        .ignoreContentType(true)
        .requestBody(RequestMapper.accountsRequestBody())
        .header(SESSION_HEADER, sessionToken)
        .method(Connection.Method.POST);
    }

  }

}
