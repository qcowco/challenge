package pl.kontomatik.challenge.connector.ipko;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import pl.kontomatik.challenge.connector.BankConnector;
import pl.kontomatik.challenge.connector.exception.InvalidCredentials;
import pl.kontomatik.challenge.connector.exception.NotAuthenticated;
import pl.kontomatik.challenge.connector.ipko.dto.AuthResponse;
import pl.kontomatik.challenge.connector.ipko.mapper.HttpBodyMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;

public class IpkoConnector implements BankConnector {
  private static final String FINGERPRINT = "6d95628f9a2a967148e1bce995e5b98a";
  private static final String LOGIN_URL = "https://www.ipko.pl/ipko3/login";
  private static final String NDCD_URL = "https://www.ipko.pl/nudatasecurity/2.2/w/w-573441/init/js/?q=%7B%22e%22%3A653560%2C%22fvq%22%3A%2263605qs6-1964-4721-n2n8-4p9n6027743p%22%2C%22oq%22%3A%22901%3A948%3A909%3A1033%3A1848%3A1053%22%2C%22wfi%22%3A%22flap-148694%22%2C%22yf%22%3A%7B%7D%2C%22jc%22%3A%22YbtvaCXB%22%2C%22jcc%22%3A1%2C%22ov%22%3A%22o2%7C1920k1080%201848k1053%2024%2024%7C-60%7Cra-HF%7Coc1-s649n1rr70p77oo7%7Csnyfr%7C%7CZbmvyyn%2F5.0%20(Jvaqbjf%20AG%2010.0%3B%20Jva64%3B%20k64)%20NccyrJroXvg%2F537.36%20(XUGZY%2C%20yvxr%20Trpxb)%20Puebzr%2F87.0.4280.88%20Fnsnev%2F537.36%7Cjt1-753633n7q242q4n9%22%7D";
  private static final String INIT_URL = "https://www.ipko.pl/ipko3/init";
  private Map<String, String> cookies;
  private String sessionToken;
  private int requestSequenceNumber;
  private final HttpBodyMapper mapper = new HttpBodyMapper();
  private final Proxy proxy;

  public IpkoConnector() {
    this(null);
  }

  public IpkoConnector(Proxy proxy) {
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
      .cookies(getCookies())
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
    return mapper.getAuthRequestBodyFor(FINGERPRINT, username, getAndIncrementSequence());
  }

  private int getAndIncrementSequence() {
    return requestSequenceNumber++;
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
      .cookies(getCookies())
      .headers(Map.of("X-Session-Id", authResponse.sessionToken, "Content-Type", "application/json"))
      .proxy(proxy)
      .method(Connection.Method.POST);
  }

  private String sessionRequestBodyFor(AuthResponse authResponse, String password) {
    return mapper.getSessionAuthRequestBodyFor(authResponse.flowId, authResponse.flowToken, password,
      getAndIncrementSequence());
  }

  private Map<String, String> getCookies() {
    if (cookies == null)
      cookies = fetchCookies();
    return cookies;
  }

  private Map<String, String> fetchCookies() {
    Connection request = cookieRequest();
    return handleSend(request)
      .cookies();
  }

  private Connection cookieRequest() {
    return Jsoup.connect(NDCD_URL)
      .ignoreContentType(true)
      .ignoreHttpErrors(true);
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
      .requestBody(accountsBody())
      .cookies(getCookies())
      .header("X-Session-Id", sessionToken)
      .method(Connection.Method.POST);
  }

  private String accountsBody() {
    return mapper.getAccountsRequestBodyFor(getAndIncrementSequence());
  }

}
