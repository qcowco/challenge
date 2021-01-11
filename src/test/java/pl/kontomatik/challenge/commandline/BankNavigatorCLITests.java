package pl.kontomatik.challenge.commandline;

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

    @Test
    public void givenCliRuns_thenDisplaysBankNavigators() throws Exception {
        // given
        BankNavigatorCLI cli = new BankNavigatorCLI(Map.of(NAVIGATOR_NAME, bankNavigator));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        cli.setOut(out);

        // when
        cli.run();

        String output = out.toString();

        // then
        assertTrue(output.contains(NAVIGATOR_NAME));
    }

    @Test
    public void givenCliRuns_whenReceivesExitCommand_thenThrows_ForcedExitException() {
        // given
        BankNavigatorCLI cli = new BankNavigatorCLI(Map.of(NAVIGATOR_NAME, bankNavigator));

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
