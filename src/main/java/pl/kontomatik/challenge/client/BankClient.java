package pl.kontomatik.challenge.client;

import java.util.Map;

public interface BankClient {

  AuthorizedSession signIn(String username, String password);

  interface AuthorizedSession {

    Map<String, Double> fetchAccounts();

  }

}
