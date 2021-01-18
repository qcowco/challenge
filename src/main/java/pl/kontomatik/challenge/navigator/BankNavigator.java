package pl.kontomatik.challenge.navigator;

import java.util.Map;

public interface BankNavigator {
    void login(String username, String password);

    boolean isAuthenticated();

    Map<String, Double> getAccounts();
}
