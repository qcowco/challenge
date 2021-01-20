package pl.kontomatik.challenge.connector.ipko;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.connector.BankConnector;
import pl.kontomatik.challenge.connector.exception.InvalidCredentials;
import pl.kontomatik.challenge.connector.exception.NotAuthenticated;
import pl.kontomatik.challenge.connector.ipko.mapper.HttpBodyMapper;
import pl.kontomatik.challenge.connector.ipko.mockserver.MockConnectorServer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class IpkoConnectorTest extends MockConnectorServer {
  private static final String USERNAME = "USERNAME";
  private static final String PASSWORD = "PASSWORD";
  private static final String WRONG_USERNAME = "WRONG_USERNAME";
  private static final String WRONG_PASSWORD = "WRONG_PASSWORD";
  private static final String ACCOUNT_NUMBER = "123456789";
  private static final double ACCOUNT_BALANCE = 0.5;

  @BeforeAll
  public static void setupMocks(MockServerClient mockServerClient) {
    setupMockedServer(mockServerClient);
  }

  @Test
  public void signInSucceedsOnValidCredentials() {
    BankConnector bankConnector = getProxiedConnector();
    assertDoesNotThrow(() -> bankConnector.login(USERNAME, PASSWORD));
  }

  @Test
  public void signInFailsOnInvalidCredentials() {
    BankConnector bankConnector = getProxiedConnector();
    assertThrows(InvalidCredentials.class, () -> bankConnector.login(WRONG_USERNAME, WRONG_PASSWORD));
  }

  @Test
  public void afterSignInCanFetchAccounts() {
    BankConnector bankConnector = getProxiedConnector();
    bankConnector.login(USERNAME, PASSWORD);
    Map<String, Double> expectedAccounts = Map.of(ACCOUNT_NUMBER, ACCOUNT_BALANCE);
    Map<String, Double> actualAccounts = bankConnector.getAccounts();
    assertEquals(expectedAccounts, actualAccounts);
  }

  @Test
  public void accountFetchingFailsWhenNotAuthenticated() {
    BankConnector bankConnector = getProxiedConnector();
    assertThrows(NotAuthenticated.class, bankConnector::getAccounts);
  }

  private BankConnector getProxiedConnector() {
    return new IpkoConnector(new HttpBodyMapper(), proxy);
  }

}
