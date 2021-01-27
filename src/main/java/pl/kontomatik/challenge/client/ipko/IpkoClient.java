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
  private static final HttpBodyMapper mapper = new HttpBodyMapper();
  private final Proxy proxy;

  public IpkoClient() {
    this(null);
  }

  public IpkoClient(Proxy proxy) {
    this.proxy = proxy;
  }

  @Override
  public AuthorizedSession login(String username, String password) {
    AuthResponse authResponse = submitLogin(username);
    return authorizeSessionToken(authResponse, password);
  }

  private AuthResponse submitLogin(String username) {
    Connection.Response response = submitLoginRequest(username);
    AuthResponse authResponse = mapper.getAuthResponseFrom(response.headers(), response.body());
    verifySuccessful(authResponse);
    return authResponse;
  }

  private Connection.Response submitLoginRequest(String username) {
    Connection request = loginRequestFor(username);
    return handleSend(request);
  }

  private static Connection loginRequestFor(String username) {
    return Jsoup.connect(LOGIN_URL)
      .ignoreContentType(true)
      .requestBody(loginRequestBodyFor(username))
      .method(Connection.Method.POST);
  }

  private static String loginRequestBodyFor(String username) {
    return mapper.getAuthRequestBodyFor(username);
  }

  private Connection.Response handleSend(Connection request) {
    try {
      return request
        .proxy(proxy)
        .header("Content-Type", "application/json")
        .execute();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void verifySuccessful(AuthResponse authResponse) {
    if (authResponse.wrongCredentials)
      throw new InvalidCredentials("Couldn't login with provided credentials.");
  }

  private AuthorizedSession authorizeSessionToken(AuthResponse authResponse, String password) {
    Connection.Response authorizationResponse = sendAuthorizeSessionRequest(authResponse, password);
    AuthResponse sessionResponse = mapper.getAuthResponseFrom(authorizationResponse.headers(), authorizationResponse.body());
    verifySuccessful(sessionResponse);
    return new IpkoSession(sessionResponse.sessionToken);
  }

  private Connection.Response sendAuthorizeSessionRequest(AuthResponse authResponse, String password) {
    Connection request = getAuthorizeSessionRequest(authResponse, password);
    return handleSend(request);
  }

  private static Connection getAuthorizeSessionRequest(AuthResponse authResponse, String password) {
    return Jsoup.connect(LOGIN_URL)
      .ignoreContentType(true)
      .requestBody(sessionRequestBodyFor(authResponse, password))
      .header(SESSION_HEADER, authResponse.sessionToken)
      .method(Connection.Method.POST);
  }

  private static String sessionRequestBodyFor(AuthResponse authResponse, String password) {
    return mapper.getSessionAuthRequestBodyFor(authResponse.flowId, authResponse.flowToken, password);
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
      return mapper.getAccountsFromJson(jsonResponse);
    }

    private Connection.Response sendAccountsRequest() {
      Connection request = accountsRequest();
      return handleSend(request);
    }

    private Connection accountsRequest() {
      return Jsoup.connect("https://www.ipko.pl/ipko3/init")
        .ignoreContentType(true)
        .requestBody(mapper.accountsRequestBody())
        .header(SESSION_HEADER, sessionToken)
        .method(Connection.Method.POST);
    }

  }

}
