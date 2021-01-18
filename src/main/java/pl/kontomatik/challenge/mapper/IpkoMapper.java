package pl.kontomatik.challenge.mapper;

import pl.kontomatik.challenge.navigator.dto.AuthResponse;

import java.util.Map;

public interface IpkoMapper {
    AuthResponse getAuthResponseFrom(String responseBody);

    String getAuthRequestBodyFor(String fingerprint,
                                 String username, int sequenceNumber);

    String getSessionAuthRequestBodyFor(String flowId, String token,
                                        String password, int sequenceNumber);

    String getAccountsRequestBodyFor(int sequenceNumber);

    Map<String, Double> getAccountsFromJson(String jsonAccounts);
}
