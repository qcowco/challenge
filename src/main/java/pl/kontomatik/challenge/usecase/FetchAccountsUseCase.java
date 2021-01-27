package pl.kontomatik.challenge.usecase;

import pl.kontomatik.challenge.client.BankClient;

import java.util.Map;
import java.util.function.Consumer;

public class FetchAccountsUseCase {

  private final BankClient bankClient;
  private final Consumer<String> consumer;

  public FetchAccountsUseCase(BankClient bankClient, Consumer<String> consumer) {
    this.bankClient = bankClient;
    this.consumer = consumer;
  }

  public void execute(String username, String password) {
    BankClient.AuthorizedSession authorizedSession = bankClient.login(username, password);
    Map<String, Double> accounts = authorizedSession.fetchAccounts();
    display(accounts);
  }

  private void display(Map<String, Double> accounts) {
    String formattedAccounts = format(accounts);
    consumer.accept(formattedAccounts);
  }

  private static String format(Map<String, Double> accounts) {
    StringBuilder accountsListed = new StringBuilder();
    accountsListed.append("Accounts:\n");
    accounts.forEach((number, value) ->
      accountsListed.append(String.format("Account number: %s, Value %f\n", number, value))
    );
    return accountsListed.toString();
  }

}
