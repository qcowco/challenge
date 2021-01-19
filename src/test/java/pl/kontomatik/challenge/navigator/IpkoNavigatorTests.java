package pl.kontomatik.challenge.navigator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import pl.kontomatik.challenge.exception.InvalidCredentialsException;
import pl.kontomatik.challenge.exception.NotAuthenticatedException;
import pl.kontomatik.challenge.mockserver.MockNavigatorServer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class IpkoNavigatorTests extends MockNavigatorServer {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static final String ACCOUNT_NUMBER = "123456789";
    private static final double ACCOUNT_BALANCE = 0.5;

    @Test
    public void givenLoggingIn_whenSuccessful_thenDoesNotThrow(MockServerClient mockServerClient) {
        // given
        mockSuccessfulLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        assertDoesNotThrow(() -> bankNavigator.login(USERNAME, PASSWORD));
    }

    @Test
    public void givenLoggingIn_whenFails_thenThrows_LoginFailedException(MockServerClient mockServerClient) {
        // given
        mockFailedLogin(mockServerClient);

        mockCookieRequest(mockServerClient);

        // when/then
        assertThrows(InvalidCredentialsException.class, () -> bankNavigator.login(USERNAME, PASSWORD));
    }

    @Test
    public void givenGettingAccounts_whenLoggedIn_thenReturnsAccounts(MockServerClient mockServerClient) {
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
    public void givenGettingAccounts_whenNotLoggedIn_thenThrows_NotAuthenticatedException() {
        // when/then
        assertThrows(NotAuthenticatedException.class, bankNavigator::getAccounts);
    }

}
