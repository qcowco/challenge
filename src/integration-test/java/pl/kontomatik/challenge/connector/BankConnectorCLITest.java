package pl.kontomatik.challenge.connector;

import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.commandline.BankConnectorCLI;
import pl.kontomatik.challenge.connector.ipko.IpkoConnector;
import pl.kontomatik.challenge.connector.ipko.mapper.HttpBodyMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class BankConnectorCLITest {
  private final String RESOURCE_NAME = "application.properties";

  @Test
  public void afterSignInCanFetchAccounts() throws IOException {
    HttpBodyMapper mapper = new HttpBodyMapper();
    BankConnector bankConnector = new IpkoConnector(mapper);
    Properties credentials = credentialsFromProperties();
    Iterator<String> input = inputFrom(credentials);
    List<String> output = new LinkedList<>();
    BankConnectorCLI bankConnectorCLI = new BankConnectorCLI(bankConnector, input::next, output::add);
    assertDoesNotThrow(bankConnectorCLI::run);
  }

  private Iterator<String> inputFrom(Properties credentials) {
    String username = credentials.getProperty("username");
    String password = credentials.getProperty("password");
    return List.of(username, password).iterator();
  }

  private Properties credentialsFromProperties() throws IOException {
    Properties properties = new Properties();
    InputStream inputStream = Files.newInputStream(getTestResourcePath());
    properties.load(inputStream);
    return properties;
  }

  private Path getTestResourcePath() {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource(RESOURCE_NAME).getFile();
    return Path.of(path);
  }
}
