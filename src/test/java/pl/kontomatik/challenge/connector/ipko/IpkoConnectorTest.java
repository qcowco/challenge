package pl.kontomatik.challenge.connector.ipko;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.connector.BankConnector;
import pl.kontomatik.challenge.connector.exception.InvalidCredentials;
import pl.kontomatik.challenge.connector.exception.NotAuthenticated;
import pl.kontomatik.challenge.connector.ipko.mockserver.MockIpkoServer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class IpkoConnectorTest extends MockIpkoServer {

  @BeforeAll
  public static void setupMocks(MockServerClient mockServerClient) {
    setupMockedServer(mockServerClient);
  }

  @Test
  public void signInSucceedsOnValidCredentials() {
    BankConnector bankConnector = new IpkoConnector(proxy);
    assertDoesNotThrow(() -> bankConnector.login(USERNAME, PASSWORD));
  }

  @Test
  public void signInFailsOnInvalidCredentials() {
    BankConnector bankConnector = new IpkoConnector(proxy);
    assertThrows(InvalidCredentials.class, () -> bankConnector.login(WRONG_USERNAME, WRONG_PASSWORD));
  }

  @Test
  public void afterSignInCanFetchAccounts() {
    BankConnector bankConnector = new IpkoConnector(proxy);
    bankConnector.login(USERNAME, PASSWORD);
    Map<String, Double> expectedAccounts = Map.of(ACCOUNT_NUMBER, ACCOUNT_BALANCE);
    Map<String, Double> actualAccounts = bankConnector.fetchAccounts();
    assertEquals(expectedAccounts, actualAccounts);
  }

  @Test
  public void accountFetchingFailsWhenNotAuthenticated() {
    BankConnector bankConnector = new IpkoConnector(proxy);
    assertThrows(NotAuthenticated.class, bankConnector::fetchAccounts);
  }

}
