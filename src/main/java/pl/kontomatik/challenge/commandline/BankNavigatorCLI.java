package pl.kontomatik.challenge.commandline;

import pl.kontomatik.challenge.exception.InvalidCredentialsException;
import pl.kontomatik.challenge.navigator.BankNavigator;

import java.io.IOException;
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

    public void run() throws Exception {
        consumer.accept("Welcome to the BankNavigator app.");

        boolean tryAgain = true;
        while (tryAgain)
            tryAgain = performLogin();

        if (bankNavigator.isAuthenticated())
            displayAccounts();
    }

    private boolean performLogin() throws IOException {
        consumer.accept("Logging in to ipko...");

        String username = askForInput("Type in Your username:");
        String password = askForInput("Type in Your password:");

        tryLogin(username, password);

        return tryRepeatLoginIfFailed();
    }

    private String askForInput(String message) {
        consumer.accept(message);

        return supplier.get();
    }

    private void tryLogin(String username, String password) throws IOException {
        try {
            bankNavigator.login(username, password);
        } catch (InvalidCredentialsException exception) {
            consumer.accept(String.format("Encountered exception: %s", exception.getMessage()));
        }
    }

    private boolean tryRepeatLoginIfFailed() throws IOException {
        boolean tryAgain = false;

        if (bankNavigator.isAuthenticated()) {
            writeOutput("Login successful.");
        } else {
            tryAgain = askIfRepeat();
        }

        return tryAgain;
    }

    private boolean askIfRepeat() throws IOException {
        String answer = askForInput("Login failed. Try again? (y/n)");

        return notNegative(answer);
    }

    private boolean notNegative(String answer) {
        return !answer.equals("n");
    }

    private void displayAccounts() throws IOException {
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
