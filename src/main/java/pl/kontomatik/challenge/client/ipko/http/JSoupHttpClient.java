package pl.kontomatik.challenge.client.ipko.http;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.util.Map;

public class JSoupHttpClient {

  private final Proxy proxy;

  public JSoupHttpClient() {
    this(null);
  }

  public JSoupHttpClient(Proxy proxy) {
    this.proxy = proxy;
  }

  public Response post(String URL, Map<String, String> headers, String body) {
    Connection request = createPost(URL, headers, body);
    Connection.Response response = send(request);
    return createResponse(response);
  }

  private static Connection createPost(String URL, Map<String, String> headers, String body) {
    return Jsoup.connect(URL)
      .method(Connection.Method.POST)
      .headers(headers)
      .requestBody(body)
      .ignoreContentType(true);
  }

  private Connection.Response send(Connection request) {
    try {
      return request
        .header("Content-Type", "application/json")
        .proxy(proxy)
        .execute();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Response createResponse(Connection.Response response) {
    return new Response(response.headers(), response.body());
  }

  public static class Response {

    public final Map<String, String> headers;
    public final String body;

    private Response(Map<String, String> headers, String body) {
      this.headers = headers;
      this.body = body;
    }

  }

}
