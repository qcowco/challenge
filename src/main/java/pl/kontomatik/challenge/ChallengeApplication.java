package pl.kontomatik.challenge;

import pl.kontomatik.challenge.client.ipko.IpkoClient;
import pl.kontomatik.challenge.usecase.FetchAccountsUseCase;

import java.util.Scanner;
import java.util.function.Consumer;

public class ChallengeApplication {
  private static Scanner scanner = new Scanner(System.in);

  public static void main(String[] args) {
    IpkoClient ipkoClient = new IpkoClient();
    Consumer<String> consumer = System.out::println;
    FetchAccountsUseCase useCase = new FetchAccountsUseCase(ipkoClient, consumer);
    String username = askForInput("Input Your username");
    String password = askForInput("Input Your password");
    useCase.execute(username, password);
  }

  private static String askForInput(String message) {
    System.out.println(message);
    return scanner.nextLine();
  }

}
