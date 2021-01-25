package pl.kontomatik.challenge.commandline;

import pl.kontomatik.challenge.client.BankClient;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BankClientCLI {
  private final BankClient bankClient;
  private final Supplier<String> supplier;
  private final Consumer<String> consumer;

  public BankClientCLI(BankClient bankClient, Supplier<String> supplier, Consumer<String> consumer) {
    this.bankClient = bankClient;
    this.supplier = supplier;
    this.consumer = consumer;
  }

  public void run() {
    performLogin();
    displayAccounts();
  }

  private void performLogin() {
    String username = askForInput("Type in Your username:");
    String password = askForInput("Type in Your password:");
    bankClient.login(username, password);
  }

  private String askForInput(String message) {
    consumer.accept(message);
    return supplier.get();
  }

  private void displayAccounts() {
    Map<String, Double> accounts = bankClient.fetchAccounts();
    consumer.accept(format(accounts));
  }

  private String stringFrom(Map<String, Double> accounts) {
    StringBuilder accountsListed = new StringBuilder();
    accountsListed.append("Accounts:\n");
    accounts.forEach((number, value) ->
      accountsListed.append(String.format("Account number: %s, Value %f\n", number, value))
    );
    return accountsListed.toString();
  }
}
