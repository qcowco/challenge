package pl.kontomatik.challenge.connector;

import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.commandline.BankConnectorCLI;
import pl.kontomatik.challenge.connector.ipko.IpkoConnector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class BankConnectorCLITest {

  @Test
  public void afterSignInCanFetchAccounts() throws IOException {
    BankConnector bankConnector = new IpkoConnector();
    Iterator<String> input = loadCredentials();
    List<String> output = new LinkedList<>();
    BankConnectorCLI bankConnectorCLI = new BankConnectorCLI(bankConnector, input::next, output::add);
    assertDoesNotThrow(bankConnectorCLI::run);
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
    return BankConnectorCLITest.class.getResourceAsStream("application.properties");
  }

}
