package pl.kontomatik.challenge.client.ipko;

import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.request.RequestMapper;
import pl.kontomatik.challenge.client.ipko.response.ResponseParser;
import pl.kontomatik.challenge.http.HttpClient;
import pl.kontomatik.challenge.http.jsoup.JSoupHttpClient;

import java.util.HashMap;
import java.util.Map;

public class IpkoClient implements BankClient {

  private final HttpClient httpClient;

  public IpkoClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public AuthorizedSession signIn(String username, String password) {
    JSoupHttpClient.Response response = submitLogin(username);
    return submitPassword(response, password);
  }

  private JSoupHttpClient.Response submitLogin(String username) {
    JSoupHttpClient.Response response = sendLoginRequest(username);
    assertCredentialAccepted(response.getBody());
    return response;
  }

  private JSoupHttpClient.Response sendLoginRequest(String username) {
    return sendSignInRequest(baseHeaders(), RequestMapper.loginRequestJson(username));
  }

  private AuthorizedSession submitPassword(JSoupHttpClient.Response loginResponse, String password) {
    JSoupHttpClient.Response response = sendPasswordRequest(loginResponse, password);
    assertCredentialAccepted(response.getBody());
    return createSession(response.getHeaders());
  }

  private JSoupHttpClient.Response sendPasswordRequest(JSoupHttpClient.Response loginResponse, String password) {
    String sessionId = ResponseParser.extractSessionId(loginResponse.getHeaders());
    return sendSignInRequest(sessionHeaders(sessionId), passwordRequestJson(loginResponse, password));
  }

  private static Map<String, String> sessionHeaders(String sessionId) {
    Map<String, String> headers = baseHeaders();
    headers.put("X-Session-Id", sessionId);
    return headers;
  }

  private static Map<String, String> baseHeaders() {
    HashMap<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    return headers;
  }

  private static String passwordRequestJson(JSoupHttpClient.Response loginResponse, String password) {
    String flowId = ResponseParser.extractFlowId(loginResponse.getBody());
    String flowToken = ResponseParser.extractFlowToken(loginResponse.getBody());
    return RequestMapper.passwordRequestJson(flowId, flowToken, password);
  }

  private JSoupHttpClient.Response sendSignInRequest(Map<String, String> headers, String body) {
    return httpClient.post("https://www.ipko.pl/ipko3/login", headers, body);
  }

  private static void assertCredentialAccepted(String body) {
    if (ResponseParser.containsCredentialErrors(body))
      throw new InvalidCredentials("Couldn't login with provided credentials.");
  }

  private IpkoSession createSession(Map<String, String> headers) {
    return new IpkoSession(ResponseParser.extractSessionId(headers));
  }

  public class IpkoSession implements AuthorizedSession {

    private final String sessionId;

    private IpkoSession(String sessionId) {
      this.sessionId = sessionId;
    }

    @Override
    public Map<String, Double> fetchAccounts() {
      JSoupHttpClient.Response response = sendAccountsRequest();
      return ResponseParser.getAccountsFromJson(response.getBody());
    }

    private JSoupHttpClient.Response sendAccountsRequest() {
      Map<String, String> sessionHeaders = sessionHeaders(sessionId);
      String accountsRequestJson = RequestMapper.accountsRequestJson();
      return httpClient.post("https://www.ipko.pl/ipko3/init", sessionHeaders, accountsRequestJson);
    }

  }

}
