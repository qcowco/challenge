package pl.kontomatik.challenge.commandline;

import pl.kontomatik.challenge.navigator.BankNavigator;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BankNavigatorCLI {
    private BankNavigator bankNavigator;
    private Supplier<String> supplier;
    private Consumer<String> consumer;

    public BankNavigatorCLI(BankNavigator bankNavigator, Supplier<String> supplier, Consumer<String> consumer) {
        this.bankNavigator = bankNavigator;
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

        bankNavigator.login(username, password);
    }

    private String askForInput(String message) {
        consumer.accept(message);

        return supplier.get();
    }

    private void displayAccounts() {
        Map<String, Double> accounts = bankNavigator.getAccounts();

        consumer.accept(stringFrom(accounts));
    }

    private String stringFrom(Map<String, Double> accounts) {
        StringBuilder accountsListed = new StringBuilder();

        accountsListed.append("Accounts:\n");

        accounts.forEach((s, aDouble) -> accountsListed.append(String.format("Account number: %s, Value %f\n", s, aDouble)));

        return accountsListed.toString();
    }
}
