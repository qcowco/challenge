package pl.kontomatik.challenge.client.ipko;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.mockserver.MockIpkoServer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class IpkoClientTest extends MockIpkoServer {

  @BeforeAll
  public static void setupMocks(MockServerClient mockServerClient) {
    setupMockedServer(mockServerClient);
  }

  @Test
  public void signInSucceedsOnValidCredentials() {
    BankClient bankClient = new IpkoClient(proxy);
    assertDoesNotThrow(() -> bankClient.login(USERNAME, PASSWORD));
  }

  @Test
  public void signInFailsOnInvalidCredentials() {
    BankClient bankClient = new IpkoClient(proxy);
    assertThrows(InvalidCredentials.class, () -> bankClient.login(WRONG_USERNAME, WRONG_PASSWORD));
  }

  @Test
  public void afterSignInCanFetchAccounts() {
    BankClient bankClient = new IpkoClient(proxy);
    BankClient.AuthorizedSession authorizedSession = bankClient.login(USERNAME, PASSWORD);
    Map<String, Double> expectedAccounts = Map.of(ACCOUNT_NUMBER, ACCOUNT_BALANCE);
    Map<String, Double> actualAccounts = authorizedSession.fetchAccounts();
    assertEquals(expectedAccounts, actualAccounts);
  }


}
