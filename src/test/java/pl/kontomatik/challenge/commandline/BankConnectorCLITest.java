package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.connector.BankConnector;
import pl.kontomatik.challenge.connector.exception.InvalidCredentials;
import pl.kontomatik.challenge.connector.ipko.IpkoConnector;
import pl.kontomatik.challenge.connector.ipko.mapper.HttpBodyMapper;
import pl.kontomatik.challenge.connector.ipko.mockserver.MockIpkoServer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BankConnectorCLITest extends MockIpkoServer {
  private static final String USERNAME = "USERNAME";
  private static final String PASSWORD = "PASSWORD";
  private static final String WRONG_USERNAME = "WRONG_USERNAME";
  private static final String WRONG_PASSWORD = "WRONG_PASSWORD";
  private static final String ACCOUNT_NUMBER = "123456789";
  private static final Double ACCOUNT_VALUE = 0.5;

  @BeforeAll
  public static void setupMocks(MockServerClient mockServerClient) {
    setupMockedServer(mockServerClient);
  }

  @Test
  public void signInFailsOnInvalidCredentials() {
    Iterator<String> input = iterate(WRONG_USERNAME, WRONG_PASSWORD);
    List<String> output = new LinkedList<>();
    BankConnectorCLI bankConnectorCLI = getProxiedCli(input, output);
    assertThrows(InvalidCredentials.class, bankConnectorCLI::run);
  }

  @Test
  public void signInSucceedsOnValidCredentials() {
    Iterator<String> input = iterate(USERNAME, PASSWORD);
    List<String> output = new LinkedList<>();
    BankConnectorCLI bankConnectorCLI = getProxiedCli(input, output);
    assertDoesNotThrow(bankConnectorCLI::run);
  }

  @Test
  public void afterSigningInDisplaysAccounts() {
    Iterator<String> input = iterate(USERNAME, PASSWORD);
    List<String> output = new LinkedList<>();
    getProxiedCli(input, output).run();
    assertContainsEveryElement(output, ACCOUNT_NUMBER, String.valueOf(ACCOUNT_VALUE));
  }

  private BankConnectorCLI getProxiedCli(Iterator<String> input, List<String> output) {
    HttpBodyMapper mapper = new HttpBodyMapper();
    BankConnector bankConnector = new IpkoConnector(mapper, proxy);
    return new BankConnectorCLI(bankConnector, input::next, output::add);
  }

  private void assertContainsEveryElement(List<String> output, String... elements) {
    assertTrue(containsEveryElement(output, elements));
  }

  private boolean containsEveryElement(List<String> output, String[] values) {
    return Arrays.stream(values)
      .allMatch(value -> output.stream().anyMatch(outputValue -> outputValue.contains(value)));
  }

  private Iterator<String> iterate(String... inputs) {
    return Arrays.asList(inputs).iterator();
  }
}
