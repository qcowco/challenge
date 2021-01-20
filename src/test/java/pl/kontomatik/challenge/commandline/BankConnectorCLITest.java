package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.connector.BankConnector;
import pl.kontomatik.challenge.connector.IpkoConnector;
import pl.kontomatik.challenge.exception.InvalidCredentials;
import pl.kontomatik.challenge.mapper.HttpBodyMapper;
import pl.kontomatik.challenge.mockserver.MockConnectorServer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BankConnectorCLITest extends MockConnectorServer {
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
        BankConnectorCLI bankConnectorCLI = getProxiedCli(input, output);
        bankConnectorCLI.run();
        assertTrue(outputContains(ACCOUNT_NUMBER, output));
        assertTrue(outputContains(String.valueOf(ACCOUNT_VALUE), output));
    }

    private BankConnectorCLI getProxiedCli(Iterator<String> input, List<String> output) {
        HttpBodyMapper mapper = new HttpBodyMapper();
        BankConnector bankConnector = new IpkoConnector(mapper, proxy);
        return new BankConnectorCLI(bankConnector, input::next, output::add);
    }

    private Iterator<String> iterate(String... inputs) {
        return Arrays.asList(inputs).iterator();
    }

    private boolean outputContains(String expected, List<String> actual) {
        return actual.stream().anyMatch(s -> s.toLowerCase().contains(expected));
    }
}
