package pl.kontomatik.challenge.navigator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.exception.InvalidCredentials;
import pl.kontomatik.challenge.exception.NotAuthenticated;
import pl.kontomatik.challenge.mockserver.MockNavigatorServer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class IpkoNavigatorTest extends MockNavigatorServer {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static final String ACCOUNT_NUMBER = "123456789";
    private static final double ACCOUNT_BALANCE = 0.5;

    @Test
    public void signInSucceedsOnValidCredentials(MockServerClient mockServerClient) {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        assertDoesNotThrow(() -> bankNavigator.login(USERNAME, PASSWORD));
    }

    @Test
    public void signInFailsOnInvalidCredentials(MockServerClient mockServerClient) {
        // given
        mockFailedLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        // when/then
        assertThrows(InvalidCredentials.class, () -> bankNavigator.login(USERNAME, PASSWORD));
    }

    @Test
    public void afterSignInCanFetchAccounts(MockServerClient mockServerClient) {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        mockAccountsRequest(mockServerClient);

        Map<String, Double> expectedAccounts = Map.of(ACCOUNT_NUMBER, ACCOUNT_BALANCE);

        bankNavigator.login(USERNAME, PASSWORD);

        // when
        Map<String, Double> accounts = bankNavigator.getAccounts();

        // then
        assertEquals(expectedAccounts, accounts);
    }

    @Test
    public void accountFetchingFailsWhenNotAuthenticated() {
        // when/then
        assertThrows(NotAuthenticated.class, bankNavigator::getAccounts);
    }

}
