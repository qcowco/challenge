package pl.kontomatik.challenge.client.ipko;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.mockserver.MockIpkoServer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static pl.kontomatik.challenge.client.ipko.mockserver.MockIpkoServer.MockData.*;

public class IpkoClientTest {

  private static MockIpkoServer server;

  @BeforeAll
  public static void setUp() {
    server = MockIpkoServer.startMockIpkoServer();
  }

  @AfterAll
  public static void tearDown() {
    server.stop();
  }

  @Test
  public void signInSucceedsOnValidCredentials() {
    BankClient bankClient = new IpkoClient(server.getProxy());
    assertDoesNotThrow(() -> bankClient.signIn(USERNAME, PASSWORD));
  }

  @Test
  public void signInFailsOnInvalidCredentials() {
    BankClient bankClient = new IpkoClient(server.getProxy());
    assertThrows(InvalidCredentials.class, () -> bankClient.signIn("WRONG_USERNAME", "WRONG_PASSWORD"));
  }

  @Test
  public void afterSignInCanFetchAccounts() {
    BankClient bankClient = new IpkoClient(server.getProxy());
    BankClient.AuthorizedSession authorizedSession = bankClient.signIn(USERNAME, PASSWORD);
    Map<String, Double> expectedAccounts = Map.of(ACCOUNT_NUMBER, ACCOUNT_BALANCE);
    Map<String, Double> actualAccounts = authorizedSession.fetchAccounts();
    assertEquals(expectedAccounts, actualAccounts);
  }

}
