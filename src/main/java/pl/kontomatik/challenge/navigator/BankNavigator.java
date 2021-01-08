package pl.kontomatik.challenge.navigator;

import java.io.IOException;
import java.util.Map;

public interface BankNavigator {
    void login(String username, String password) throws IOException;

    boolean isAuthenticated();

    Map<String, Double> getAccounts() throws IOException;
}
