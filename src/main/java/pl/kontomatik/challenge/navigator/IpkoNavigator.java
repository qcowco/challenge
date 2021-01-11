package pl.kontomatik.challenge.navigator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import pl.kontomatik.challenge.exception.LoginFailedException;
import pl.kontomatik.challenge.exception.NotAuthenticatedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class IpkoNavigator implements BankNavigator {
    public static final String LOGIN_URL = "https://www.ipko.pl/ipko3/login";
    public static final String NDCD_URL = "https://www.ipko.pl/nudatasecurity/2.2/w/w-573441/init/js/?q=%7B%22e%22%3A653560%2C%22fvq%22%3A%2263605qs6-1964-4721-n2n8-4p9n6027743p%22%2C%22oq%22%3A%22901%3A948%3A909%3A1033%3A1848%3A1053%22%2C%22wfi%22%3A%22flap-148694%22%2C%22yf%22%3A%7B%7D%2C%22jc%22%3A%22YbtvaCXB%22%2C%22jcc%22%3A1%2C%22ov%22%3A%22o2%7C1920k1080%201848k1053%2024%2024%7C-60%7Cra-HF%7Coc1-s649n1rr70p77oo7%7Csnyfr%7C%7CZbmvyyn%2F5.0%20(Jvaqbjf%20AG%2010.0%3B%20Jva64%3B%20k64)%20NccyrJroXvg%2F537.36%20(XUGZY%2C%20yvxr%20Trpxb)%20Puebzr%2F87.0.4280.88%20Fnsnev%2F537.36%7Cjt1-753633n7q242q4n9%22%7D";
    public static final String INIT_URL = "https://www.ipko.pl/ipko3/init";
    public static final String FINGERPRINT = "6d95628f9a2a967148e1bce995e5b98a";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, String> cookies;

    private String sessionToken;
    private boolean sessionTokenAuthorized;

    private String authFlowId;
    private String authFlowToken;

    private int requestSequenceNumber;

    @Override
    public void login(String username, String password) throws IOException {
        beginAuthentication(username);
        authorizeSessionToken(password);
    }

    private void beginAuthentication(String username) throws IOException {
        Connection.Response response = sendAuthenticationRequest(username);

        isSuccessful(response.body());

        assignSessionToken(response.headers());
        assignFlowTokens(response.body());
    }

    private Connection.Response sendAuthenticationRequest(String username) throws IOException {
        return Jsoup.connect(LOGIN_URL)
                .ignoreContentType(true)
                .requestBody(getAuthenticationBody(username))
                .cookies(getCookies())
                .method(Connection.Method.POST)
                .execute();
    }

    private void assignSessionToken(Map<String, String> headers) {
        sessionToken = headers.get("X-Session-Id");
    }

    private void assignFlowTokens(String body) throws JsonProcessingException {
        JsonNode responseNode = objectMapper.readTree(body);

        authFlowId = responseNode.get("flow_id").asText();
        authFlowToken = responseNode.get("token").asText();
    }

    private void isSuccessful(String body) throws JsonProcessingException {
        if (hasErrors(body))
            throw new LoginFailedException("Couldn't login, response has errors.");
    }

    private boolean hasErrors(String responseBody) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        return jsonNode.findPath("fields").hasNonNull("errors");
    }

    private String getAuthenticationBody(String username) throws JsonProcessingException {
        ObjectNode body = getBaseNode();

        body.put("state_id", "login");

        ObjectNode dataInner = body.with("data");
        dataInner.put("login", username);
        dataInner.put("fingerprint", FINGERPRINT);

        body.put("action", "submit");

        return objectMapper.writeValueAsString(body);
    }


    private ObjectNode getBaseNode() {
        ObjectNode baseNode = objectMapper.createObjectNode();

        baseNode.put("version", 3);
        baseNode.put("seq", requestSequenceNumber++);
        baseNode.put("location", "");

        ObjectNode dataNode = objectMapper.createObjectNode();

        baseNode.set("data", dataNode);

        return baseNode;
    }

    private void authorizeSessionToken(String password) throws IOException {
        String jsonBody = sendAuthorizeSessionRequest(password)
                .body();

        isSuccessful(jsonBody);

        verifySuccessful(jsonBody);
    }

    private Connection.Response sendAuthorizeSessionRequest(String password) throws IOException {
        return Jsoup.connect(LOGIN_URL)
                .ignoreContentType(true)
                .requestBody(getAuthorizeSessionBody(password))
                .cookies(getCookies())
                .header("X-Session-Id", sessionToken)
                .method(Connection.Method.POST)
                .execute();
    }

    private void verifySuccessful(String jsonBody) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonBody);

        sessionTokenAuthorized = jsonNode.findPath("finished").asBoolean();
    }

    private String getAuthorizeSessionBody(String password) throws JsonProcessingException {
        ObjectNode body = getBaseNode();

        body.put("state_id", "password");
        body.put("flow_id", authFlowId);
        body.put("token", authFlowToken);

        ObjectNode dataInner = body.with("data");
        dataInner.put("password", password);
        dataInner.put("placement", "LoginPKO");
        dataInner.put("placement_page_no", 1);

        body.put("action", "submit");

        return objectMapper.writeValueAsString(body);
    }

    @Override
    public boolean isAuthenticated() {
        return sessionTokenAuthorized;
    }

    private Map<String, String> getCookies() throws IOException {
        if (cookies == null)
            cookies = sendCookieRequest()
                    .cookies();

        return cookies;
    }

    private Connection.Response sendCookieRequest() throws IOException {
        return Jsoup.connect(NDCD_URL)
                .ignoreContentType(true)
                .execute();
    }

    @Override
    public Map<String, Double> getAccounts() throws IOException {
        if (!isAuthenticated())
            throw new NotAuthenticatedException("You're not authenticated. Log in first.");

        String jsonResponse = sendAccountsRequest()
                .body();

        return getAccountsFrom(jsonResponse);
    }

    private Connection.Response sendAccountsRequest() throws IOException {
        return Jsoup.connect(INIT_URL)
                .ignoreContentType(true)
                .requestBody(getAccountsBody())
                .cookies(getCookies())
                .header("X-Session-Id", sessionToken)
                .method(Connection.Method.POST)
                .execute();
    }

    private String getAccountsBody() throws JsonProcessingException {
        ObjectNode body = getBaseNode();

        ObjectNode dataInner = body.with("data");
        dataInner.set("accounts", objectMapper.createObjectNode());

        body.set("data", dataInner);

        return objectMapper.writeValueAsString(body);
    }

    private Map<String, Double> getAccountsFrom(String jsonAccounts) throws JsonProcessingException {
        JsonNode accountsNode = jsonNodeFrom(jsonAccounts);
        return accountsFrom(accountsNode);
    }

    private JsonNode jsonNodeFrom(String jsonAccounts) throws JsonProcessingException {
        JsonNode accountsTree = objectMapper.readTree(jsonAccounts);

        return accountsTree.findPath("accounts");
    }

    private Map<String, Double> accountsFrom(JsonNode accountsNode) {
        Map<String, Double> accountMap = new HashMap<>();

        accountsNode.forEach(accountNode -> {
            String account = accountNode.with("number")
                    .get("value").asText();
            Double balance = accountNode.get("balance")
                    .asDouble();

            accountMap.put(account, balance);
        });

        return accountMap;
    }

}
