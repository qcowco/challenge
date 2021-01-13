package pl.kontomatik.challenge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import pl.kontomatik.challenge.navigator.dto.AuthResponse;

import java.util.Map;

public interface IpkoMapper {
    AuthResponse getAuthResponseFrom(String responseBody) throws JsonProcessingException;

    String getAuthRequestBodyFor(String fingerprint,
                                 String username, int sequenceNumber) throws JsonProcessingException;

    String getSessionAuthRequestBodyFor(String flowId, String token,
                                        String password, int sequenceNumber) throws JsonProcessingException;

    String getAccountsRequestBodyFor(int sequenceNumber) throws JsonProcessingException;

    Map<String, Double> getAccountsFromJson(String jsonAccounts) throws JsonProcessingException;
}
