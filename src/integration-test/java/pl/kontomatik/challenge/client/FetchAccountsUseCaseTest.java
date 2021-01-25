package pl.kontomatik.challenge.client;

import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.IpkoClient;
import pl.kontomatik.challenge.usecase.FetchAccountsUseCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FetchAccountsUseCaseTest {

  @Test
  public void afterSignInCanFetchAccounts() throws IOException {
    BankClient bankClient = new IpkoClient();
    Iterator<String> input = loadCredentials();
    List<String> output = new LinkedList<>();
    FetchAccountsUseCase useCase = new FetchAccountsUseCase(bankClient, input::next, output::add);
    useCase.execute();
    assertContains(output, "Account number:");
  }

  private Iterator<String> loadCredentials() throws IOException {
    return inputFrom(credentialProperties());
  }

  private Properties credentialProperties() throws IOException {
    Properties properties = new Properties();
    properties.load(resourceStream());
    return properties;
  }

  private InputStream resourceStream() {
    return FetchAccountsUseCaseTest.class.getResourceAsStream("/application.properties");
  }

  private Iterator<String> inputFrom(Properties credentials) {
    String username = credentials.getProperty("username");
    String password = credentials.getProperty("password");
    return iterate(username, password);
  }

  private Iterator<String> iterate(String username, String password) {
    return List.of(username, password).iterator();
  }

  private void assertContains(List<String> output, String message) {
    assertTrue(anyLineContains(output, message));
  }

  private boolean anyLineContains(List<String> output, String message) {
    return output.stream()
      .anyMatch(outputLine -> outputLine.contains(message));
  }

  @Test
  public void failsOnInvalidCredentials() {
    BankClient bankClient = new IpkoClient();
    Iterator<String> input = iterate("qwerty", "azerty");
    List<String> output = new LinkedList<>();
    FetchAccountsUseCase useCase = new FetchAccountsUseCase(bankClient, input::next, output::add);
    assertThrows(InvalidCredentials.class, useCase::execute);
  }

}
