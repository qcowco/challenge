package pl.kontomatik.challenge.client.usecase;

import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.client.BankClient;
import pl.kontomatik.challenge.client.exception.InvalidCredentials;
import pl.kontomatik.challenge.client.ipko.IpkoClient;
import pl.kontomatik.challenge.http.jsoup.JSoupHttpClient;
import pl.kontomatik.challenge.usecase.FetchAccountsUseCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FetchAccountsUseCaseTest {

  @Test
  public void failsOnInvalidCredentials() {
    BankClient bankClient = createIpkoClient();
    FetchAccountsUseCase useCase = new FetchAccountsUseCase(bankClient, new ArrayList<String>()::add);
    assertThrows(InvalidCredentials.class, () -> useCase.execute("qwerty", "azerty"));
  }

  @Test
  public void afterSignInCanFetchAccounts() throws IOException {
    BankClient bankClient = createIpkoClient();
    List<String> output = new ArrayList<>();
    FetchAccountsUseCase useCase = new FetchAccountsUseCase(bankClient, output::add);
    Properties credentials = loadCredentials();
    useCase.execute(credentials.getProperty("username"), credentials.getProperty("password"));
    assertContains(output, "Account number:");
  }

  private static IpkoClient createIpkoClient() {
    return new IpkoClient(new JSoupHttpClient());
  }

  private static Properties loadCredentials() throws IOException {
    Properties credentials = new Properties();
    credentials.load(FetchAccountsUseCaseTest.class.getResourceAsStream("/application.properties"));
    return credentials;
  }

  private static void assertContains(List<String> output, String message) {
    assertTrue(output.stream().anyMatch(s -> s.contains(message)));
  }

}
