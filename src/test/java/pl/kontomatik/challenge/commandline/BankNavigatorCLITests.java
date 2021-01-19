package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.exception.InvalidCredentialsException;
import pl.kontomatik.challenge.mockserver.MockNavigatorServer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    public void signInFailsOnInvalidCredentials(MockServerClient mockServerClient) {
        // given
        mockFailedLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        setInput(USERNAME, PASSWORD);

        // when/then
        assertThrows(InvalidCredentialsException.class, cli::run);
    }

    @Test
    public void signInSucceedsOnValidCredentials(MockServerClient mockServerClient) {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        mockAccountsRequest(mockServerClient);

        setInput(USERNAME, PASSWORD);

        // when/then
        assertDoesNotThrow(cli::run);
    }

    @Test
    public void afterSigningInDisplaysAccounts(MockServerClient mockServerClient) {
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
