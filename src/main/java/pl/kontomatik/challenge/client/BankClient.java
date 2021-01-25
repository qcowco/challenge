package pl.kontomatik.challenge.client;

import java.util.Map;

public interface BankClient {

  AuthorizedSession login(String username, String password);

  interface AuthorizedSession {

    Map<String, Double> fetchAccounts();

  }

}
