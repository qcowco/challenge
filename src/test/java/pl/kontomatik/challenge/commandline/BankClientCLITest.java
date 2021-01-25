package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.IpkoClient;
import pl.kontomatik.challenge.client.ipko.mockserver.MockIpkoServer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BankClientCLITest extends MockIpkoServer {

  @BeforeAll
  public static void setupMocks(MockServerClient mockServerClient) {
    setupMockedServer(mockServerClient);
  }

  @Test
  public void signInFailsOnInvalidCredentials() {
    Iterator<String> input = iterate(WRONG_USERNAME, WRONG_PASSWORD);
    List<String> output = new LinkedList<>();
    BankClientCLI bankClientCLI = proxiedCliFor(input, output);
    assertThrows(InvalidCredentials.class, bankClientCLI::run);
  }

  @Test
  public void signInSucceedsOnValidCredentials() {
    Iterator<String> input = iterate(USERNAME, PASSWORD);
    List<String> output = new LinkedList<>();
    BankClientCLI bankClientCLI = proxiedCliFor(input, output);
    assertDoesNotThrow(bankClientCLI::run);
  }

  @Test
  public void afterSigningInDisplaysAccounts() {
    Iterator<String> input = iterate(USERNAME, PASSWORD);
    List<String> output = new LinkedList<>();
    proxiedCliFor(input, output).run();
    assertContainsEveryElement(output, ACCOUNT_NUMBER, String.valueOf(ACCOUNT_BALANCE));
  }

  private BankClientCLI proxiedCliFor(Iterator<String> input, List<String> output) {
    BankClient proxiedClient = new IpkoClient(proxy);
    return new BankClientCLI(proxiedClient, input::next, output::add);
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
