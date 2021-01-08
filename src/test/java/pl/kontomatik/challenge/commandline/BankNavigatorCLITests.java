package pl.kontomatik.challenge.commandline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kontomatik.challenge.BankNavigatorCLI;
import pl.kontomatik.challenge.navigator.BankNavigator;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class BankNavigatorCLITests {
    private static final String NAVIGATOR_NAME = "ipko";

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

}
