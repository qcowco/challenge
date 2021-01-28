package pl.kontomatik.challenge.client.ipko.http;

import org.jsoup.Connection;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;

public class JSoupHttpClient {

  private Proxy proxy;

  public JSoupHttpClient() {
    this(null);
  }

  public JSoupHttpClient(Proxy proxy) {
    this.proxy = proxy;
  }

  public Connection.Response send(Connection request) {
    try {
      return request
        .proxy(proxy)
        .header("Content-Type", "application/json")
        .execute();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
