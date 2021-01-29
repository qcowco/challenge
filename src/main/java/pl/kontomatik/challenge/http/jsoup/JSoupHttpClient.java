package pl.kontomatik.challenge.http.jsoup;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import pl.kontomatik.challenge.http.HttpClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.util.Map;

public class JSoupHttpClient implements HttpClient {

  private final Proxy proxy;

  public JSoupHttpClient() {
    this(null);
  }

  public JSoupHttpClient(Proxy proxy) {
    this.proxy = proxy;
  }

  @Override
  public Response post(String URL, Map<String, String> headers, String body) {
    Connection request = createPostRequest(URL, headers, body);
    Connection.Response jsoupResponse = send(request);
    return new HttpResponse(jsoupResponse);
  }

  private static Connection createPostRequest(String URL, Map<String, String> headers, String body) {
    return Jsoup.connect(URL)
      .method(Connection.Method.POST)
      .headers(headers)
      .requestBody(body)
      .ignoreContentType(true);
  }

  private Connection.Response send(Connection request) {
    try {
      return request
        .proxy(proxy)
        .execute();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static class HttpResponse implements Response {

    public final Map<String, String> headers;
    public final String body;

    private HttpResponse(Connection.Response response) {
      this.headers = response.headers();
      this.body = response.body();
    }

    @Override
    public Map<String, String> getHeaders() {
      return headers;
    }

    @Override
    public String getBody() {
      return body;
    }

  }

}
