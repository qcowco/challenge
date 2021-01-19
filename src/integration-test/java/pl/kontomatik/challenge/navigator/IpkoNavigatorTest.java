package pl.kontomatik.challenge.navigator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.mapper.IpkoMapper;
import pl.kontomatik.challenge.mapper.IpkoMapperImpl;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class IpkoNavigatorTest {
    private static IpkoNavigatorProperties ipkoNavigatorProperties;

    @BeforeAll
    public static void setup() throws IOException {
        ipkoNavigatorProperties = new IpkoNavigatorProperties();
    }

    @Test
    public void givenLoggingIn_whenCorrectCredentials_thenDoesntThrow() {
        // given
        IpkoMapper ipkoMapper = new IpkoMapperImpl();
        BankNavigator bankNavigator = new IpkoNavigator(ipkoMapper);

        String username = ipkoNavigatorProperties.getUsername();
        String password = ipkoNavigatorProperties.getPassword();

        // when/then
        assertDoesNotThrow(() -> bankNavigator.login(username, password));
    }

    @Test
    public void givenGettingAccounts_whenCorrectCredentials_thenDoesntThrow() {
        // given
        IpkoMapper ipkoMapper = new IpkoMapperImpl();
        BankNavigator bankNavigator = new IpkoNavigator(ipkoMapper);

        String username = ipkoNavigatorProperties.getUsername();
        String password = ipkoNavigatorProperties.getPassword();

        bankNavigator.login(username, password);

        // when/then
        assertDoesNotThrow(bankNavigator::getAccounts);
    }

}
