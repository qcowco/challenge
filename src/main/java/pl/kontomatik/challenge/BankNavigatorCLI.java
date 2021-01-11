package pl.kontomatik.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.kontomatik.challenge.exception.ForcedExitException;
import pl.kontomatik.challenge.navigator.BankNavigator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

@Component
public class BankNavigatorCLI implements CommandLineRunner {
    private Map<String, BankNavigator> bankNavigators;
    private BankNavigator bankNavigator;

    private InputStream in;
    private OutputStream out;
    private Scanner scanner;

    private Map<String, Runnable> commands;

    public BankNavigatorCLI() {
        this.in = System.in;
        this.out = System.out;
        updateScanner();
        initCommands();
    }

    private void initCommands() {
        commands = new HashMap<>();

        commands.put("/exit", () -> {
            throw new ForcedExitException("The exit command has been entered. Exitting..");
        });
    }

    @Autowired
    public BankNavigatorCLI(Map<String, BankNavigator> bankNavigators) {
        this();
        this.bankNavigators = bankNavigators;
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
        String input = tryHandleInput();

        handleCommand(input);

        return input;
    }

    private void handleCommand(String input) {
        if (commands.containsKey(input))
            commands.get(input).run();
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

    @Override
    public void run(String... args) throws Exception {
        writeOutput("Welcome to the BankNavigator app.");

        String scrapers = String.join(", ", bankNavigators.keySet());
        writeOutput("Available scrapers: " + scrapers);
        String navigatorChoice = handleInput();

        bankNavigator = bankNavigators.get(navigatorChoice);

        writeOutput("Type in Your username:");
        String username = handleInput();
        writeOutput("Type in Your password:");
        String password = handleInput();

        writeOutput("Logging in...");

        boolean authenticated = true;

        try {
            bankNavigator.login(username, password);
        } catch (RuntimeException exception) {
            writeOutput(String.format("Encountered exception: %s", exception.getMessage()));
            authenticated = false;
        }

        if (authenticated && bankNavigator.isAuthenticated()) {
            writeOutput("Login successful.");
        } else {
            writeOutput("Login failed.");
        }
    }
}
