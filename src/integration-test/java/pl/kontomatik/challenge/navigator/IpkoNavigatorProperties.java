package pl.kontomatik.challenge.navigator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class IpkoNavigatorProperties {
    private final String RESOURCE_NAME = "application.properties";
    private Map<String, String> credentials;
    private Map<String, Double> accounts;

    public IpkoNavigatorProperties() throws IOException {
        setUserData();
    }

    public String getUsername() {
        return credentials.get("username");
    }

    public String getPassword() {
        return credentials.get("password");
    }

    public Map<String, Double> getAccounts() {
        return accounts;
    }

    private void setUserData() throws IOException {
        String path = getTestResourcePath();

        String[] userData = Files.readString(Path.of(path))
                .split("\n\n");

        String credentials = userData[0];
        this.credentials = getCredentials(credentials);

        String accounts = userData[1];
        this.accounts = getAccounts(accounts);
    }

    private String getTestResourcePath() {
        ClassLoader classLoader = getClass().getClassLoader();

        return classLoader.getResource(RESOURCE_NAME).getFile();
    }

    private Map<String, Double> getAccounts(String accounts) {
        String[] bankAccounts = accounts.split("\n");

        Map<String, Double> accountMap = new HashMap<>();

        for (String accountValuePair : bankAccounts) {
            String[] values = accountValuePair.split("=");
            accountMap.put(values[0], Double.valueOf(values[1]));
        }

        return accountMap;
    }

    private Map<String, String> getCredentials(String credentials) {
        String[] accountCredentials = credentials.split("=");

        return Map.of("username", accountCredentials[0],
                "password", accountCredentials[1]);
    }
}
