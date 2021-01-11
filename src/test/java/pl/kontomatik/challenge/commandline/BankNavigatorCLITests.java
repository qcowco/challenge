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
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class BankNavigatorCLITests {
    private static final String NAVIGATOR_NAME = "ipko";
    private static final String EXIT_COMMAND = "/exit";

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

    private String prepareInput(String ... inputs) {
        return Stream.of(inputs).collect(StringBuilder::new,
                (stringBuilder, s1) -> stringBuilder.append(s1).append('\n'), StringBuilder::append)
                .toString();
    }


}
