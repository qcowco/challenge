package pl.kontomatik.challenge.navigator;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import pl.kontomatik.challenge.exception.LoginFailedException;
import pl.kontomatik.challenge.exception.NotAuthenticatedException;
import pl.kontomatik.challenge.mapper.IpkoMapper;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;

import java.io.IOException;
import java.util.Map;

public class IpkoNavigator implements BankNavigator {
    public static final String LOGIN_URL = "https://www.ipko.pl/ipko3/login";
    public static final String NDCD_URL = "https://www.ipko.pl/nudatasecurity/2.2/w/w-573441/init/js/?q=%7B%22e%22%3A653560%2C%22fvq%22%3A%2263605qs6-1964-4721-n2n8-4p9n6027743p%22%2C%22oq%22%3A%22901%3A948%3A909%3A1033%3A1848%3A1053%22%2C%22wfi%22%3A%22flap-148694%22%2C%22yf%22%3A%7B%7D%2C%22jc%22%3A%22YbtvaCXB%22%2C%22jcc%22%3A1%2C%22ov%22%3A%22o2%7C1920k1080%201848k1053%2024%2024%7C-60%7Cra-HF%7Coc1-s649n1rr70p77oo7%7Csnyfr%7C%7CZbmvyyn%2F5.0%20(Jvaqbjf%20AG%2010.0%3B%20Jva64%3B%20k64)%20NccyrJroXvg%2F537.36%20(XUGZY%2C%20yvxr%20Trpxb)%20Puebzr%2F87.0.4280.88%20Fnsnev%2F537.36%7Cjt1-753633n7q242q4n9%22%7D";
    public static final String INIT_URL = "https://www.ipko.pl/ipko3/init";
    public static final String FINGERPRINT = "6d95628f9a2a967148e1bce995e5b98a";

    private IpkoMapper ipkoMapper = new IpkoMapper();

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

        AuthResponse authResponse = ipkoMapper.getAuthResponseFrom(response.body());

        isSuccessful(authResponse);

        assignSessionToken(response.headers());
        assignFlowTokens(authResponse);
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

    private void assignFlowTokens(AuthResponse authResponse) {
        authFlowId = authResponse.getFlowId();
        authFlowToken = authResponse.getToken();
    }

    private boolean isSuccessful(AuthResponse authResponse) {
        if (authResponse.hasErrors())
            throw new LoginFailedException("Couldn't login, response has errors.");

        return true;
    }

    private String getAuthenticationBody(String username) throws JsonProcessingException {
        return ipkoMapper.getAuthRequestBodyFor(FINGERPRINT, username, getAndIncrementSequence());
    }

    public int getAndIncrementSequence() {
        return requestSequenceNumber++;
    }

    private void authorizeSessionToken(String password) throws IOException {
        String jsonBody = sendAuthorizeSessionRequest(password)
                .body();

        AuthResponse authResponse = ipkoMapper.getAuthResponseFrom(jsonBody);

        sessionTokenAuthorized = isSuccessful(authResponse);
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

    private String getAuthorizeSessionBody(String password) throws JsonProcessingException {
        return ipkoMapper.getSessionAuthRequestBodyFor(authFlowId, authFlowToken, password,
                getAndIncrementSequence());
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

        return ipkoMapper.getAccountsFromJson(jsonResponse);
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
        return ipkoMapper.getAccountsRequestBodyFor(getAndIncrementSequence());
    }

}
