package pl.kontomatik.challenge.http;

import java.util.Map;

public interface HttpClient {

  Response post(String URL, Map<String, String> headers, String body);

  interface Response {

    Map<String, String> getHeaders();

    String getBody();

  }

}
