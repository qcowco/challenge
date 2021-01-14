package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.mockserver.MockNavigatorServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({MockitoExtension.class})
public class BankNavigatorCLITests extends MockNavigatorServer {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private BankNavigatorCLI cli;

    @BeforeEach
    public void setup() {
        cli = new BankNavigatorCLI(bankNavigator);

        cli.setOut(new ByteArrayOutputStream());
    }

    @Test
    public void givenLogIn_whenFails_thenCanTryAgain(MockServerClient mockServerClient) throws Exception {
        // given
        mockFailedLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        String agreeTryAgain = "y";
        String refuseTryAgain = "n";

        cli.setIn(getInputStream(USERNAME, PASSWORD, agreeTryAgain, USERNAME, PASSWORD, refuseTryAgain));

        String expectedOutput = "try again";

        // when
        cli.run();

        String actualOutput = cli.getOut().toString();

        // then
        assertTrue(actualOutput.toLowerCase().contains(expectedOutput));
    }

    @Test
    public void givenLogIn_whenFails_thenDisplaysErrorMessage(MockServerClient mockServerClient) throws Exception {
        // given
        mockFailedLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        String refuseTryAgain = "n";

        cli.setIn(getInputStream(USERNAME, PASSWORD, refuseTryAgain));

        String expectedOutput = "encountered exception";

        // when
        cli.run();

        String actualOutput = cli.getOut().toString();

        // then
        assertTrue(actualOutput.toLowerCase().contains(expectedOutput));
    }

    @Test
    public void givenLogin_whenSuccessful_thenDisplaysLoginSuccessful(MockServerClient mockServerClient) throws Exception {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        mockAccountsRequest(mockServerClient);

        cli.setIn(getInputStream(USERNAME, PASSWORD));

        String expectedOutput = "login successful";

        // when
        cli.run();

        String actualOutput = cli.getOut().toString();

        // then
        assertTrue(actualOutput.toLowerCase().contains(expectedOutput));
    }

    @Test
    public void givenGettingAccounts_whenSuccessful_thenDisplaysBankAccounts(MockServerClient mockServerClient) throws Exception {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        mockAccountsRequest(mockServerClient);

        cli.setIn(getInputStream(USERNAME, PASSWORD));

        // when
        cli.run();

        String actualOutput = cli.getOut().toString();

        // then
        assertTrue(actualOutput.contains(ACCOUNT_NUMBER));
        assertTrue(actualOutput.contains(String.valueOf(ACCOUNT_VALUE)));

    }

    private ByteArrayInputStream getInputStream(String... inputs) {
        return new ByteArrayInputStream(prepareInput(inputs).getBytes());
    }

    private String prepareInput(String ... inputs) {
        return Stream.of(inputs).collect(StringBuilder::new,
                (stringBuilder, s1) -> stringBuilder.append(s1).append('\n'), StringBuilder::append)
                .toString();
    }
}
