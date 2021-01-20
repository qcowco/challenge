package pl.kontomatik.challenge.navigator;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import pl.kontomatik.challenge.exception.ConnectionFailed;
import pl.kontomatik.challenge.exception.InvalidCredentials;
import pl.kontomatik.challenge.exception.NotAuthenticated;
import pl.kontomatik.challenge.mapper.IpkoMapper;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;

import java.io.IOException;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;

public class IpkoNavigator implements BankNavigator {
    public static final String FINGERPRINT = "6d95628f9a2a967148e1bce995e5b98a";
    private String loginUrl = "https://www.ipko.pl/ipko3/login";
    private String ndcdUrl = "https://www.ipko.pl/nudatasecurity/2.2/w/w-573441/init/js/?q=%7B%22e%22%3A653560%2C%22fvq%22%3A%2263605qs6-1964-4721-n2n8-4p9n6027743p%22%2C%22oq%22%3A%22901%3A948%3A909%3A1033%3A1848%3A1053%22%2C%22wfi%22%3A%22flap-148694%22%2C%22yf%22%3A%7B%7D%2C%22jc%22%3A%22YbtvaCXB%22%2C%22jcc%22%3A1%2C%22ov%22%3A%22o2%7C1920k1080%201848k1053%2024%2024%7C-60%7Cra-HF%7Coc1-s649n1rr70p77oo7%7Csnyfr%7C%7CZbmvyyn%2F5.0%20(Jvaqbjf%20AG%2010.0%3B%20Jva64%3B%20k64)%20NccyrJroXvg%2F537.36%20(XUGZY%2C%20yvxr%20Trpxb)%20Puebzr%2F87.0.4280.88%20Fnsnev%2F537.36%7Cjt1-753633n7q242q4n9%22%7D";
    private String initUrl = "https://www.ipko.pl/ipko3/init";

    private IpkoMapper ipkoMapper;
    private Proxy proxy;

    private Map<String, String> cookies;

    private String sessionToken;

    private String authFlowId;
    private String authFlowToken;

    private int requestSequenceNumber;

    public IpkoNavigator(IpkoMapper ipkoMapper) {
        this(ipkoMapper, null);
    }

    public IpkoNavigator(IpkoMapper ipkoMapper, Proxy proxy) {
        this.ipkoMapper = ipkoMapper;
        this.proxy = proxy;
    }

    @Override
    public void login(String username, String password) {
        String sessionToken = beginAuthentication(username);
        authorizeSessionToken(sessionToken, password);
    }

    private String beginAuthentication(String username) {
        Connection.Response response = sendAuthenticationRequest(username);

        AuthResponse authResponse = ipkoMapper.getAuthResponseFrom(response.body());

        verifySuccessful(authResponse);

        assignFlowTokens(authResponse);
        return getSessionToken(response.headers());
    }

    private Connection.Response sendAuthenticationRequest(String username) {
        Connection request = getAuthenticationRequest(username);

        applyProxy(request);

        return trySendRequest(request);
    }

    private void applyProxy(Connection request) {
        request.proxy(proxy);
    }

    private Connection getAuthenticationRequest(String username) {
        return Jsoup.connect(loginUrl)
                .ignoreContentType(true)
                .requestBody(getAuthenticationBody(username))
                .cookies(getCookies())
                .method(Connection.Method.POST);
    }

    private Connection.Response trySendRequest(Connection request) {
        try {
            return request.execute();
        } catch (IOException e) {
            throw new ConnectionFailed(e);
        }
    }

    private String getSessionToken(Map<String, String> headers) {
        return headers.get("X-Session-Id");
    }

    private void assignFlowTokens(AuthResponse authResponse) {
        authFlowId = authResponse.getFlowId();
        authFlowToken = authResponse.getToken();
    }

    private void verifySuccessful(AuthResponse authResponse) {
        if (authResponse.isWrongCredentials())
            throw new InvalidCredentials("Couldn't login with provided credentials.");
    }

    private String getAuthenticationBody(String username) {
        return ipkoMapper.getAuthRequestBodyFor(FINGERPRINT, username, getAndIncrementSequence());
    }

    public int getAndIncrementSequence() {
        return requestSequenceNumber++;
    }

    private void authorizeSessionToken(String sessionToken, String password) {
        String jsonBody = sendAuthorizeSessionRequest(sessionToken, password)
                .body();

        AuthResponse authResponse = ipkoMapper.getAuthResponseFrom(jsonBody);

        verifySuccessful(authResponse);

        this.sessionToken = sessionToken;
    }

    private Connection.Response sendAuthorizeSessionRequest(String sessionToken, String password) {
        Connection request = getAuthorizeSessionRequest(sessionToken, password);

        applyProxy(request);

        return trySendRequest(request);
    }

    private Connection getAuthorizeSessionRequest(String sessionToken, String password) {
        return Jsoup.connect(loginUrl)
                    .ignoreContentType(true)
                    .requestBody(getAuthorizeSessionBody(password))
                    .cookies(getCookies())
                    .header("X-Session-Id", sessionToken)
                    .method(Connection.Method.POST);
    }

    private String getAuthorizeSessionBody(String password) {
        return ipkoMapper.getSessionAuthRequestBodyFor(authFlowId, authFlowToken, password,
                getAndIncrementSequence());
    }

    private Map<String, String> getCookies() {
        if (cookies == null)
            cookies = sendCookieRequest()
                    .cookies();

        return cookies;
    }

    private Connection.Response sendCookieRequest() {
        Connection request = getCookieRequest();

        applyProxy(request);

        return trySendRequest(request);
    }

    private Connection getCookieRequest() {
        return Jsoup.connect(ndcdUrl)
                .ignoreContentType(true);
    }

    @Override
    public Map<String, Double> getAccounts() {
        verifyAuthenticated();

        String jsonResponse = sendAccountsRequest()
                .body();

        return ipkoMapper.getAccountsFromJson(jsonResponse);
    }

    private void verifyAuthenticated() {
        if (Objects.isNull(sessionToken))
            throw new NotAuthenticated("You're not authenticated. Log in first.");
    }

    private Connection.Response sendAccountsRequest() {
        Connection request = getAccountsRequest();

        applyProxy(request);

        return trySendRequest(request);
    }

    private Connection getAccountsRequest() {
        return Jsoup.connect(initUrl)
                .ignoreContentType(true)
                .requestBody(getAccountsBody())
                .cookies(getCookies())
                .header("X-Session-Id", sessionToken)
                .method(Connection.Method.POST);
    }

    private String getAccountsBody() {
        return ipkoMapper.getAccountsRequestBodyFor(getAndIncrementSequence());
    }

}
