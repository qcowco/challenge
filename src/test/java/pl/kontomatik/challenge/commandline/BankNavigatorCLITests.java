package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kontomatik.challenge.exception.ForcedExitException;
import pl.kontomatik.challenge.navigator.BankNavigator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class BankNavigatorCLITests {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static final String NAVIGATOR_NAME = "ipko";
    private static final String EXIT_COMMAND = "/exit";

    private static final String ACCOUNT_NUMBER = "0 1234 2345 3456 4567";
    private static final Double ACCOUNT_VALUE = 500.15;
    private static final Map<String, Double> BANK_ACCOUNTS = Map.of(ACCOUNT_NUMBER, ACCOUNT_VALUE);

    @Mock
    private BankNavigator bankNavigator;

    private BankNavigatorCLI cli;

    @BeforeEach
    public void setup() {
        cli = new BankNavigatorCLI(Map.of(NAVIGATOR_NAME, bankNavigator));

        cli.setOut(new ByteArrayOutputStream());
    }


    @Test
    public void givenCliRuns_thenDisplaysBankNavigators() throws Exception {
        // when
        cli.setIn(new ByteArrayInputStream(prepareInput(EXIT_COMMAND).getBytes()));

        try {
            cli.run();
        } catch (RuntimeException exception) {

        }

        String output = cli.getOut().toString();

        // then
        assertTrue(output.contains(NAVIGATOR_NAME));
    }

    @Test
    public void givenCliRuns_whenReceivesExitCommand_thenThrows_ForcedExitException() {
        // given
        String textInput = prepareInput(EXIT_COMMAND);

        cli.setIn(new ByteArrayInputStream(textInput.getBytes()));

        // when/then
        assertThrows(ForcedExitException.class, cli::run);
    }

    @Test
    public void givenLogIn_whenFails_thenDisplaysLoginFailed() throws Exception {
        // given
        cli.setIn(new ByteArrayInputStream(prepareInput(NAVIGATOR_NAME, USERNAME, PASSWORD).getBytes()));

        String expectedOutput = "login failed";

        doThrow(RuntimeException.class)
                .when(bankNavigator).login(USERNAME, PASSWORD);

        // when
        cli.run();

        String actualOutput = cli.getOut().toString();

        // then
        assertTrue(actualOutput.toLowerCase().contains(expectedOutput));
    }

    @Test
    public void givenLogIn_whenFails_thenDisplaysErrorMessage() throws Exception {
        // given
        cli.setIn(new ByteArrayInputStream(prepareInput(NAVIGATOR_NAME, USERNAME, PASSWORD).getBytes()));

        String expectedOutput = "encountered exception";

        doThrow(RuntimeException.class)
                .when(bankNavigator).login(USERNAME, PASSWORD);

        // when
        cli.run();

        String actualOutput = cli.getOut().toString();

        // then
        assertTrue(actualOutput.toLowerCase().contains(expectedOutput));
    }

    @Test
    public void givenLogin_whenSuccessful_thenDisplaysLoginSuccessful() throws Exception {
        // given
        cli.setIn(new ByteArrayInputStream(prepareInput(NAVIGATOR_NAME, USERNAME, PASSWORD).getBytes()));

        String expectedOutput = "login successful";

        given(bankNavigator.isAuthenticated())
                .willReturn(true);

        // when
        cli.run();

        String actualOutput = cli.getOut().toString();

        // then
        assertTrue(actualOutput.toLowerCase().contains(expectedOutput));
    }

    @Test
    public void givenGettingAccounts_whenSuccessful_thenDisplaysBankAccounts() throws Exception {
        // given
        cli.setIn(new ByteArrayInputStream(prepareInput(NAVIGATOR_NAME, USERNAME, PASSWORD).getBytes()));

        given(bankNavigator.isAuthenticated())
                .willReturn(true);

        given(bankNavigator.getAccounts())
                .willReturn(BANK_ACCOUNTS);

        // when
        cli.run();

        String actualOutput = cli.getOut().toString();

        // then
        assertTrue(actualOutput.contains(ACCOUNT_NUMBER));
        assertTrue(actualOutput.contains(String.valueOf(ACCOUNT_VALUE)));
    }

    private String prepareInput(String ... inputs) {
        return Stream.of(inputs).collect(StringBuilder::new,
                (stringBuilder, s1) -> stringBuilder.append(s1).append('\n'), StringBuilder::append)
                .toString();
    }
}
