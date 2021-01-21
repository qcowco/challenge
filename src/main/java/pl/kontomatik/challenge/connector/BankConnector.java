package pl.kontomatik.challenge.connector;

import java.util.Map;

public interface BankConnector {
  void login(String username, String password);
  Map<String, Double> getAccounts();
}
