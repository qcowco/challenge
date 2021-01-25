package pl.kontomatik.challenge.usecase;

import pl.kontomatik.challenge.client.BankClient;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FetchAccountsUseCase {

  private final BankClient bankClient;
  private final Supplier<String> supplier;
  private final Consumer<String> consumer;

  public FetchAccountsUseCase(BankClient bankClient, Supplier<String> supplier, Consumer<String> consumer) {
    this.bankClient = bankClient;
    this.supplier = supplier;
    this.consumer = consumer;
  }

  public void execute() {
    BankClient.AuthorizedSession authorizedSession = performLogin();
    Map<String, Double> accounts = authorizedSession.fetchAccounts();
    display(accounts);
  }

  private BankClient.AuthorizedSession performLogin() {
    String username = askForInput("Type in Your username:");
    String password = askForInput("Type in Your password:");
    return bankClient.login(username, password);
  }

  private String askForInput(String message) {
    consumer.accept(message);
    return supplier.get();
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
