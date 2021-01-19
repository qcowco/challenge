package pl.kontomatik.challenge.navigator;

import org.junit.jupiter.api.Test;
import pl.kontomatik.challenge.mapper.IpkoMapper;
import pl.kontomatik.challenge.mapper.IpkoMapperImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class IpkoNavigatorTest {
    private final String RESOURCE_NAME = "application.properties";

    @Test
    public void continuesOnCorrectCredentials() throws IOException {
        // given
        IpkoMapper ipkoMapper = new IpkoMapperImpl();
        BankNavigator bankNavigator = new IpkoNavigator(ipkoMapper);
        Properties testData = getTestData();

        String username = testData.getProperty("username");
        String password = testData.getProperty("password");

        // when/then
        assertDoesNotThrow(() -> bankNavigator.login(username, password));
    }

    @Test
    public void afterSignInCanFetchAccounts() throws IOException {
        // given
        IpkoMapper ipkoMapper = new IpkoMapperImpl();
        BankNavigator bankNavigator = new IpkoNavigator(ipkoMapper);
        Properties testData = getTestData();

        String username = testData.getProperty("username");
        String password = testData.getProperty("password");

        bankNavigator.login(username, password);

        // when/then
        assertDoesNotThrow(bankNavigator::getAccounts);
    }

    private Properties getTestData() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = Files.newInputStream(getTestResourcePath());
        properties.load(inputStream);
        return properties;
    }

    private Path getTestResourcePath() {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource(RESOURCE_NAME).getFile();
        return Path.of(path);
    }
}
