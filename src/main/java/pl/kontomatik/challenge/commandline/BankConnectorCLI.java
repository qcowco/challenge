package pl.kontomatik.challenge.commandline;

import pl.kontomatik.challenge.connector.BankConnector;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BankConnectorCLI {
  private final BankConnector bankConnector;
  private final Supplier<String> supplier;
  private final Consumer<String> consumer;

  public BankConnectorCLI(BankConnector bankConnector, Supplier<String> supplier, Consumer<String> consumer) {
    this.bankConnector = bankConnector;
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
    bankConnector.login(username, password);
  }

  private String askForInput(String message) {
    consumer.accept(message);
    return supplier.get();
  }

  private void displayAccounts() {
    Map<String, Double> accounts = bankConnector.fetchAccounts();
    consumer.accept(stringFrom(accounts));
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
