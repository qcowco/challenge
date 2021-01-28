package pl.kontomatik.challenge.client.usecase;

import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.IpkoClient;
import pl.kontomatik.challenge.client.ipko.http.JSoupHttpClient;
import pl.kontomatik.challenge.usecase.FetchAccountsUseCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FetchAccountsUseCaseTest {

  @Test
  public void afterSignInCanFetchAccounts() throws IOException {
    BankClient bankClient = new IpkoClient(new JSoupHttpClient());
    List<String> output = new ArrayList<>();
    FetchAccountsUseCase useCase = new FetchAccountsUseCase(bankClient, output::add);
    Properties credentials = loadCredentials();
    useCase.execute(credentials.getProperty("username"), credentials.getProperty("password"));
    assertContains(output, "Account number:");
  }

  private static Properties loadCredentials() throws IOException {
    Properties credentials = new Properties();
    credentials.load(credentialStream());
    return credentials;
  }

  private static InputStream credentialStream() {
    return FetchAccountsUseCaseTest.class.getResourceAsStream("/application.properties");
  }

  private static void assertContains(List<String> output, String message) {
    assertTrue(anyLineContains(output, message));
  }

  private static boolean anyLineContains(List<String> output, String message) {
    return output.stream()
      .anyMatch(outputLine -> outputLine.contains(message));
  }

  @Test
  public void failsOnInvalidCredentials() {
    BankClient bankClient = new IpkoClient(new JSoupHttpClient());
    List<String> output = new ArrayList<>();
    FetchAccountsUseCase useCase = new FetchAccountsUseCase(bankClient, output::add);
    assertThrows(InvalidCredentials.class, () -> useCase.execute("qwerty", "azerty"));
  }

}
