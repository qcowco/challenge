package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kontomatik.challenge.BankNavigatorCLI;
import pl.kontomatik.challenge.exception.ForcedExitException;
import pl.kontomatik.challenge.navigator.BankNavigator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    @Nested
    @DisplayName("Given CLI runs")
    class CliRuns {
        private BankNavigatorCLI cli;
        private ByteArrayOutputStream out;

        @BeforeEach
        public void setup() {
            cli = new BankNavigatorCLI(Map.of(NAVIGATOR_NAME, bankNavigator));

            out = new ByteArrayOutputStream();

            cli.setOut(out);

        }

        @Test
        @DisplayName("Then displays bank navigators")
        public void shouldDisplayBankNavigators() throws Exception {
            // when
            cli.run();

            String output = out.toString();

            // then
            assertTrue(output.contains(NAVIGATOR_NAME));
        }

        @Nested
        @DisplayName("When receives exit command")
        class ExitCommand {

            @Test
            @DisplayName("Then throws ForcedExitException")
            public void shouldThrow_ForcedExitException() {
                // given
                String textInput = prepareInput(EXIT_COMMAND);

                cli.setIn(new ByteArrayInputStream(textInput.getBytes()));

                // when/then
                assertThrows(ForcedExitException.class, cli::run);
            }

        }
    }

    @Nested
    @DisplayName("Given user logs in")
    class LogIn {
        private BankNavigatorCLI cli;
        private String loginInput;

        @BeforeEach
        public void setup() {
            cli = new BankNavigatorCLI(Map.of(NAVIGATOR_NAME, bankNavigator));

            loginInput = prepareInput(NAVIGATOR_NAME, USERNAME, PASSWORD);

            cli.setOut(new ByteArrayOutputStream());
            cli.setIn(new ByteArrayInputStream(loginInput.getBytes()));
        }

        @Nested
        @DisplayName("When login is unsuccessful")
        class Unsuccessful {

            @BeforeEach
            public void setup() throws IOException {
                doThrow(RuntimeException.class)
                        .when(bankNavigator).login(USERNAME, PASSWORD);
            }

            @Test
            @DisplayName("Then displays that the login has failed")
            public void shouldDisplayLoginFailed() throws Exception {
                // given
                String expectedOutput = "login failed";

                // when
                cli.run();

                String actualOutput = cli.getOut().toString();

                // then
                assertTrue(actualOutput.toLowerCase().contains(expectedOutput));
            }

            @Test
            @DisplayName("Then displays an error message")
            public void shouldDisplayExceptionMessage() throws Exception {
                // given
                String expectedOutput = "encountered exception";

                // when
                cli.run();

                String actualOutput = cli.getOut().toString();

                // then
                assertTrue(actualOutput.toLowerCase().contains(expectedOutput));
            }

        }

        @Nested
        @DisplayName("When login was successful")
        class Successful {

            @BeforeEach
            public void setup() {
                given(bankNavigator.isAuthenticated())
                        .willReturn(true);
            }

            @Test
            @DisplayName("Then displays that the login was successful")
            public void shouldDisplayLoginSuccessful() throws Exception {
                // given
                String expectedOutput = "login successful";

                // when
                cli.run();

                String actualOutput = cli.getOut().toString();

                // then
                assertTrue(actualOutput.toLowerCase().contains(expectedOutput));
            }

            @Test
            @DisplayName("Then lists users bank accounts")
            public void shouldDisplayBankAccounts() throws Exception {
                // given
                given(bankNavigator.getAccounts())
                        .willReturn(BANK_ACCOUNTS);

                // when
                cli.run();

                String actualOutput = cli.getOut().toString();

                // then
                assertTrue(actualOutput.contains(ACCOUNT_NUMBER));
                assertTrue(actualOutput.contains(String.valueOf(ACCOUNT_VALUE)));
            }
        }
    }

    private String prepareInput(String ... inputs) {
        return Stream.of(inputs).collect(StringBuilder::new,
                (stringBuilder, s1) -> stringBuilder.append(s1).append('\n'), StringBuilder::append)
                .toString();
    }


}
