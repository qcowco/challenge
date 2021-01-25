package pl.kontomatik.challenge.client.ipko;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.exception.NotAuthenticated;
import pl.kontomatik.challenge.client.ipko.dto.AuthResponse;
import pl.kontomatik.challenge.client.ipko.mapper.HttpBodyMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;

public class IpkoClient implements BankClient {
  private static final String LOGIN_URL = "https://www.ipko.pl/ipko3/login";
  private static final String INIT_URL = "https://www.ipko.pl/ipko3/init";
  private String sessionToken;
  private final HttpBodyMapper mapper = new HttpBodyMapper();
  private final Proxy proxy;

  public IpkoClient() {
    this(null);
  }

  public IpkoClient(Proxy proxy) {
    this.proxy = proxy;
  }

  @Override
  public void login(String username, String password) {
    AuthResponse authResponse = submitLogin(username);
    authorizeSessionToken(authResponse, password);
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

  private Connection loginRequestFor(String username) {
    return Jsoup.connect(LOGIN_URL)
      .ignoreContentType(true)
      .requestBody(loginRequestBodyFor(username))
      .method(Connection.Method.POST)
      .header("Content-Type", "application/json");
  }

  private Connection.Response handleSend(Connection request) {
    try {
      return request
        .proxy(proxy)
        .execute();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void verifySuccessful(AuthResponse authResponse) {
    if (authResponse.wrongCredentials)
      throw new InvalidCredentials("Couldn't login with provided credentials.");
  }

  private String loginRequestBodyFor(String username) {
    return mapper.getAuthRequestBodyFor(username);
  }

  private void authorizeSessionToken(AuthResponse authResponse, String password) {
    Connection.Response authorizationResponse = sendAuthorizeSessionRequest(authResponse, password);
    AuthResponse sessionResponse = mapper.getAuthResponseFrom(authorizationResponse.headers(), authorizationResponse.body());
    verifySuccessful(sessionResponse);
    this.sessionToken = sessionResponse.sessionToken;
  }

  private Connection.Response sendAuthorizeSessionRequest(AuthResponse authResponse, String password) {
    Connection request = getAuthorizeSessionRequest(authResponse, password);
    return handleSend(request);
  }

  private Connection getAuthorizeSessionRequest(AuthResponse authResponse, String password) {
    return Jsoup.connect(LOGIN_URL)
      .ignoreContentType(true)
      .requestBody(sessionRequestBodyFor(authResponse, password))
      .headers(Map.of("X-Session-Id", authResponse.sessionToken, "Content-Type", "application/json"))
      .proxy(proxy)
      .method(Connection.Method.POST);
  }

  private String sessionRequestBodyFor(AuthResponse authResponse, String password) {
    return mapper.getSessionAuthRequestBodyFor(authResponse.flowId, authResponse.flowToken, password);
  }

  @Override
  public Map<String, Double> fetchAccounts() {
    verifyAuthenticated();
    String jsonResponse = sendAccountsRequest()
      .body();
    return mapper.getAccountsFromJson(jsonResponse);
  }

  private void verifyAuthenticated() {
    if (Objects.isNull(sessionToken))
      throw new NotAuthenticated("You're not authenticated. Log in first.");
  }

  private Connection.Response sendAccountsRequest() {
    Connection request = accountsRequest();
    return handleSend(request);
  }

  private Connection accountsRequest() {
    return Jsoup.connect(INIT_URL)
      .ignoreContentType(true)
      .requestBody(mapper.accountsRequestBody())
      .header("X-Session-Id", sessionToken)
      .method(Connection.Method.POST);
  }

}
