package pl.kontomatik.challenge;

import pl.kontomatik.challenge.client.ipko.IpkoClient;
import pl.kontomatik.challenge.http.jsoup.JSoupHttpClient;
import pl.kontomatik.challenge.usecase.FetchAccountsUseCase;

import java.util.Scanner;

public class ChallengeApplication {

  public static void main(String[] args) {
    IpkoClient ipkoClient = new IpkoClient(new JSoupHttpClient());
    FetchAccountsUseCase useCase = new FetchAccountsUseCase(ipkoClient, System.out::println);
    String username = askForInput("Input Your username");
    String password = askForInput("Input Your password");
    useCase.execute(username, password);
  }

  private static String askForInput(String message) {
    System.out.println(message);
    return new Scanner(System.in).nextLine();
  }

}
