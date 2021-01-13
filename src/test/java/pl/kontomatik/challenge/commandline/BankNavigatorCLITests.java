package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kontomatik.challenge.navigator.BankNavigator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class BankNavigatorCLITests {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static final String ACCOUNT_NUMBER = "0 1234 2345 3456 4567";
    private static final Double ACCOUNT_VALUE = 500.15;
    private static final Map<String, Double> BANK_ACCOUNTS = Map.of(ACCOUNT_NUMBER, ACCOUNT_VALUE);

    @Mock
    private BankNavigator bankNavigator;

    private BankNavigatorCLI cli;

    @BeforeEach
    public void setup() {
        cli = new BankNavigatorCLI(bankNavigator);

        cli.setOut(new ByteArrayOutputStream());
    }


    @Test
    public void givenLogIn_whenFails_thenDisplaysLoginFailed() throws Exception {
        // given
        cli.setIn(getLoginInputStream());

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
        cli.setIn(getLoginInputStream());

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
        cli.setIn(getLoginInputStream());

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
        cli.setIn(getLoginInputStream());

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

    private ByteArrayInputStream getLoginInputStream() {
        return new ByteArrayInputStream(prepareInput(USERNAME, PASSWORD).getBytes());
    }

    private String prepareInput(String ... inputs) {
        return Stream.of(inputs).collect(StringBuilder::new,
                (stringBuilder, s1) -> stringBuilder.append(s1).append('\n'), StringBuilder::append)
                .toString();
    }
}
