package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.mockserver.MockNavigatorServer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({MockitoExtension.class})
public class BankNavigatorCLITests extends MockNavigatorServer {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private BankNavigatorCLI cli;
    private Iterator<String> input;
    private List<String> output;

    @BeforeEach
    public void setup() {
        output = new LinkedList<>();
        cli = new BankNavigatorCLI(bankNavigator, () -> input.next(), output::add);
    }

    @Test
    public void givenLogIn_whenFails_thenCanTryAgain(MockServerClient mockServerClient) throws Exception {
        // given
        mockFailedLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        setInput(USERNAME, PASSWORD, "y", USERNAME, PASSWORD, "n");

        String expectedOutput = "try again";

        // when
        cli.run();

        // then
        assertTrue(outputContains(expectedOutput));
    }

    @Test
    public void givenLogIn_whenFails_thenDisplaysErrorMessage(MockServerClient mockServerClient) throws Exception {
        // given
        mockFailedLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        setInput(USERNAME, PASSWORD, "n");

        String expectedOutput = "encountered exception";

        // when
        cli.run();

        // then
        assertTrue(outputContains(expectedOutput));
    }

    @Test
    public void givenLogin_whenSuccessful_thenDisplaysLoginSuccessful(MockServerClient mockServerClient) throws Exception {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        mockAccountsRequest(mockServerClient);

        setInput(USERNAME, PASSWORD);

        String expectedOutput = "login successful";

        // when
        cli.run();

        // then
        assertTrue(outputContains(expectedOutput));
    }

    @Test
    public void givenGettingAccounts_whenSuccessful_thenDisplaysBankAccounts(MockServerClient mockServerClient) throws Exception {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        mockAccountsRequest(mockServerClient);

        setInput(USERNAME, PASSWORD);

        // when
        cli.run();

        // then
        assertTrue(outputContains(ACCOUNT_NUMBER));
        assertTrue(outputContains(String.valueOf(ACCOUNT_VALUE)));
    }

    private void setInput(String... inputs) {
        input = Arrays.asList(inputs).iterator();
    }

    private boolean outputContains(String expectedOutput) {
        return output.stream().anyMatch(s -> s.toLowerCase().contains(expectedOutput));
    }
}
