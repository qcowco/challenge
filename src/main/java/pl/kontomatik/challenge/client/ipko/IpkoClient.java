package pl.kontomatik.challenge.client.ipko;

import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.http.JSoupHttpClient;
import pl.kontomatik.challenge.client.ipko.request.RequestMapper;
import pl.kontomatik.challenge.client.ipko.response.ResponseParser;

import java.util.Map;

public class IpkoClient implements BankClient {

  private static final String SESSION_HEADER = "X-Session-Id";
  private final JSoupHttpClient httpClient;

  public IpkoClient(JSoupHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public AuthorizedSession signIn(String username, String password) {
    JSoupHttpClient.Response response = submitLogin(username);
    return submitPassword(response, password);
  }

  private JSoupHttpClient.Response submitLogin(String username) {
    JSoupHttpClient.Response response = sendLoginRequest(username);
    assertCredentialAccepted(response.body);
    return response;
  }

  private JSoupHttpClient.Response sendLoginRequest(String username) {
    return sendSignInRequest(Map.of(), RequestMapper.loginRequestJson(username));
  }

  private AuthorizedSession submitPassword(JSoupHttpClient.Response loginResponse, String password) {
    JSoupHttpClient.Response response = sendPasswordRequest(loginResponse, password);
    assertCredentialAccepted(response.body);
    return createSession(response.headers);
  }

  private JSoupHttpClient.Response sendPasswordRequest(JSoupHttpClient.Response loginResponse, String password) {
    return sendSignInRequest(
      Map.of(SESSION_HEADER, ResponseParser.extractSessionId(loginResponse.headers)),
      RequestMapper.passwordRequestJson(
        ResponseParser.extractFlowId(loginResponse.body),
        ResponseParser.extractFlowToken(loginResponse.body),
        password
      )
    );
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

    private final String sessionToken;

    private IpkoSession(String sessionToken) {
      this.sessionToken = sessionToken;
    }

    @Override
    public Map<String, Double> fetchAccounts() {
      JSoupHttpClient.Response response = sendAccountsRequest();
      return ResponseParser.getAccountsFromJson(response.body);
    }

    private JSoupHttpClient.Response sendAccountsRequest() {
      return httpClient.post("https://www.ipko.pl/ipko3/init",
        Map.of(SESSION_HEADER, sessionToken), RequestMapper.accountsRequestJson());
    }

  }

}
