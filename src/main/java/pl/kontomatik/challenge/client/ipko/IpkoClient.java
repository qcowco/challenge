package pl.kontomatik.challenge.client.ipko;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.dto.AuthResponse;
import pl.kontomatik.challenge.client.ipko.mapper.HttpBodyMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.util.Map;

public class IpkoClient implements BankClient {

  private static final String LOGIN_URL = "https://www.ipko.pl/ipko3/login";
  private static final String SESSION_HEADER = "X-Session-Id";
  private static final HttpBodyMapper MAPPER = new HttpBodyMapper();
  private final Proxy proxy;

  public IpkoClient() {
    this(null);
  }

  public IpkoClient(Proxy proxy) {
    this.proxy = proxy;
  }

  @Override
  public AuthorizedSession signIn(String username, String password) {
    AuthResponse loginResponse = submitLogin(username);
    return submitPassword(loginResponse, password);
  }

  private AuthResponse submitLogin(String username) {
    Connection request = createLoginRequest(username);
    Connection.Response response = send(request);
    AuthResponse authResponse = MAPPER.createAuthResponse(response.headers(), response.body());
    assertCredentialAccepted(authResponse);
    return authResponse;
  }

  private static Connection createLoginRequest(String username) {
    return Jsoup.connect(LOGIN_URL)
      .ignoreContentType(true)
      .requestBody(createLoginRequestBody(username))
      .method(Connection.Method.POST);
  }

  private static String createLoginRequestBody(String username) {
    return MAPPER.createLoginRequestBody(username);
  }

  private AuthorizedSession submitPassword(AuthResponse loginResponse, String password) {
    Connection request = createPasswordRequest(loginResponse, password);
    Connection.Response response = send(request);
    AuthResponse sessionResponse = MAPPER.createAuthResponse(response.headers(), response.body());
    assertCredentialAccepted(sessionResponse);
    return new IpkoSession(sessionResponse.sessionToken);
  }

  private static Connection createPasswordRequest(AuthResponse authResponse, String password) {
    return Jsoup.connect(LOGIN_URL)
      .ignoreContentType(true)
      .requestBody(createPasswordRequestBody(authResponse, password))
      .header(SESSION_HEADER, authResponse.sessionToken)
      .method(Connection.Method.POST);
  }

  private static String createPasswordRequestBody(AuthResponse authResponse, String password) {
    return MAPPER.createPasswordRequestBody(authResponse.flowId, authResponse.flowToken, password);
  }

  private Connection.Response send(Connection request) {
    try {
      return request
        .proxy(proxy)
        .header("Content-Type", "application/json")
        .execute();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void assertCredentialAccepted(AuthResponse authResponse) {
    if (authResponse.wrongCredentials)
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
      return MAPPER.getAccountsFromJson(jsonResponse);
    }

    private Connection.Response sendAccountsRequest() {
      Connection request = accountsRequest();
      return send(request);
    }

    private Connection accountsRequest() {
      return Jsoup.connect("https://www.ipko.pl/ipko3/init")
        .ignoreContentType(true)
        .requestBody(MAPPER.accountsRequestBody())
        .header(SESSION_HEADER, sessionToken)
        .method(Connection.Method.POST);
    }

  }

}
