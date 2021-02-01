package pl.kontomatik.challenge.client.ipko;

import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.request.Requests;
import pl.kontomatik.challenge.client.ipko.response.ResponseParser;
import pl.kontomatik.challenge.http.HttpClient;

import java.util.HashMap;
import java.util.Map;

import static pl.kontomatik.challenge.client.ipko.request.Requests.createLoginRequestBody;
import static pl.kontomatik.challenge.client.ipko.request.Requests.createPasswordRequestBody;
import static pl.kontomatik.challenge.client.ipko.response.ResponseParser.*;

public class IpkoClient implements BankClient {

  private final HttpClient httpClient;

  public IpkoClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public AuthorizedSession signIn(String username, String password) {
    HttpClient.Response response = submitLogin(username);
    return submitPassword(response, password);
  }

  private HttpClient.Response submitLogin(String username) {
    HttpClient.Response response = sendLoginRequest(username);
    assertCredentialAccepted(response.getBody());
    return response;
  }

  private HttpClient.Response sendLoginRequest(String username) {
    return sendCredentialRequest(baseHeaders(), createLoginRequestBody(username));
  }

  private AuthorizedSession submitPassword(HttpClient.Response loginResponse, String password) {
    HttpClient.Response response = sendPasswordRequest(loginResponse, password);
    assertCredentialAccepted(response.getBody());
    return createSession(response.getHeaders());
  }

  private HttpClient.Response sendPasswordRequest(HttpClient.Response loginResponse, String password) {
    String sessionId = extractSessionId(loginResponse.getHeaders());
    String flowId = extractFlowId(loginResponse.getBody());
    String flowToken = extractFlowToken(loginResponse.getBody());
    return sendCredentialRequest(sessionHeaders(sessionId), createPasswordRequestBody(flowId, flowToken, password));
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

  private HttpClient.Response sendCredentialRequest(Map<String, String> headers, String body) {
    return httpClient.post("https://www.ipko.pl/ipko3/login", headers, body);
  }

  private static void assertCredentialAccepted(String body) {
    if (ResponseParser.containsCredentialErrors(body))
      throw new InvalidCredentials("Couldn't login with provided credentials.");
  }

  private IpkoSession createSession(Map<String, String> headers) {
    return new IpkoSession(extractSessionId(headers));
  }

  public class IpkoSession implements AuthorizedSession {

    private final String sessionId;

    private IpkoSession(String sessionId) {
      this.sessionId = sessionId;
    }

    @Override
    public Map<String, Double> fetchAccounts() {
      HttpClient.Response response = sendAccountsRequest();
      return ResponseParser.parseAccounts(response.getBody());
    }

    private HttpClient.Response sendAccountsRequest() {
      Map<String, String> sessionHeaders = sessionHeaders(sessionId);
      String accountsRequestJson = Requests.createAccountsRequestBody();
      return httpClient.post("https://www.ipko.pl/ipko3/init", sessionHeaders, accountsRequestJson);
    }

  }

}
