package pl.kontomatik.challenge.usecase;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.IpkoClient;
import pl.kontomatik.challenge.client.ipko.mockserver.MockIpkoServer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static pl.kontomatik.challenge.client.ipko.mockserver.MockIpkoServer.MockData.*;

public class FetchAccountsUseCaseTest {

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
  public void signInFailsOnInvalidCredentials() {
    Iterator<String> input = iterate("WRONG_USERNAME", "WRONG_PASSWORD");
    List<String> output = new LinkedList<>();
    FetchAccountsUseCase useCase = proxiedCliFor(input, output);
    assertThrows(InvalidCredentials.class, useCase::execute);
  }

  private Iterator<String> iterate(String... inputs) {
    return Arrays.asList(inputs).iterator();
  }

  private FetchAccountsUseCase proxiedCliFor(Iterator<String> input, List<String> output) {
    BankClient proxiedClient = new IpkoClient(server.getProxy());
    return new FetchAccountsUseCase(proxiedClient, input::next, output::add);
  }

  @Test
  public void signInSucceedsOnValidCredentials() {
    Iterator<String> input = iterate(USERNAME, PASSWORD);
    List<String> output = new LinkedList<>();
    FetchAccountsUseCase useCase = proxiedCliFor(input, output);
    assertDoesNotThrow(useCase::execute);
  }

  @Test
  public void afterSigningInDisplaysAccounts() {
    Iterator<String> input = iterate(USERNAME, PASSWORD);
    List<String> output = new LinkedList<>();
    proxiedCliFor(input, output).execute();
    assertContainsEveryElement(output, ACCOUNT_NUMBER, String.valueOf(ACCOUNT_BALANCE));
  }

  private void assertContainsEveryElement(List<String> output, String... elements) {
    assertTrue(containsEveryElement(output, elements));
  }

  private boolean containsEveryElement(List<String> output, String[] values) {
    return Arrays.stream(values)
      .allMatch(value -> output.stream().anyMatch(outputValue -> outputValue.contains(value)));
  }

}
