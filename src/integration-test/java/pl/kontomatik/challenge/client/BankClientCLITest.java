package pl.kontomatik.challenge.client;

import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.client.ipko.IpkoClient;
import pl.kontomatik.challenge.commandline.BankClientCLI;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class BankClientCLITest {

  @Test
  public void afterSignInCanFetchAccounts() throws IOException {
    BankClient bankClient = new IpkoClient();
    Iterator<String> input = loadCredentials();
    List<String> output = new LinkedList<>();
    BankClientCLI bankClientCLI = new BankClientCLI(bankClient, input::next, output::add);
    assertDoesNotThrow(bankClientCLI::run);
  }

  private Iterator<String> loadCredentials() throws IOException {
    return inputFrom(credentialProperties());
  }

  private Iterator<String> inputFrom(Properties credentials) {
    String username = credentials.getProperty("username");
    String password = credentials.getProperty("password");
    return List.of(username, password).iterator();
  }

  private Properties credentialProperties() throws IOException {
    Properties properties = new Properties();
    properties.load(resourceStream());
    return properties;
  }

  private InputStream resourceStream() {
    return BankClientCLITest.class.getResourceAsStream("/application.properties");
  }

}
