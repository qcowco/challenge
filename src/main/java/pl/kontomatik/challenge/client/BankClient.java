package pl.kontomatik.challenge.client;

import java.util.Map;

public interface BankClient {
  void login(String username, String password);
  Map<String, Double> fetchAccounts();
}
