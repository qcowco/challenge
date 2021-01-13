package pl.kontomatik.challenge.commandline;

import pl.kontomatik.challenge.exception.InvalidCredentialsException;
import pl.kontomatik.challenge.navigator.BankNavigator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class BankNavigatorCLI {
    private BankNavigator bankNavigator;

    private InputStream in;
    private OutputStream out;
    private Scanner scanner;

    private boolean authenticated;

    public BankNavigatorCLI() {
        this.in = System.in;
        this.out = System.out;
        updateScanner();
    }

    public BankNavigatorCLI(BankNavigator bankNavigator) {
        this();
        this.bankNavigator = bankNavigator;
    }

    public InputStream getIn() {
        return in;
    }

    public void setIn(InputStream in) {
        this.in = in;
        updateScanner();
    }

    private void updateScanner() {
        scanner = new Scanner(in);
    }

    public OutputStream getOut() {
        return out;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    private String handleInput() {
        return tryHandleInput();
    }

    private String tryHandleInput() {
        String input = "";

        try {
            input = scanner.nextLine();
        } catch (NoSuchElementException exception) {

        }

        return input;
    }

    private void writeOutput(String output) throws IOException {
        out.write((output + '\n').getBytes());
    }

    public void run(String... args) throws Exception {
        writeOutput("Welcome to the BankNavigator app.");

        performLogin();

        if (authenticated)
            displayAccounts();

        handleInput();
    }

    private void performLogin() throws IOException {
        writeOutput("Type in Your username:");
        String username = handleInput();
        writeOutput("Type in Your password:");
        String password = handleInput();

        writeOutput("Logging in...");

        try {
            bankNavigator.login(username, password);
        } catch (InvalidCredentialsException exception) {
            writeOutput(String.format("Encountered exception: %s", exception.getMessage()));
        }

        if (bankNavigator.isAuthenticated()) {
            authenticated = true;
            writeOutput("Login successful.");
        } else
            writeOutput("Login failed.");
    }

    private void displayAccounts() throws IOException {
        Map<String, Double> accounts = bankNavigator.getAccounts();

        writeOutput(stringFrom(accounts));
    }

    private String stringFrom(Map<String, Double> accounts) {
        StringBuilder accountsListed = new StringBuilder();

        accountsListed.append("Accounts:\n");

        accounts.forEach((s, aDouble) -> accountsListed.append(String.format("Account number: %s, Value %f\n", s, aDouble)));

        return accountsListed.toString();
    }
}
