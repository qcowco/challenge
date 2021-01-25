package pl.kontomatik.challenge;

import pl.kontomatik.challenge.client.ipko.IpkoClient;
import pl.kontomatik.challenge.commandline.BankClientCLI;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChallengeApplication {

  public static void main(String[] args) {
    IpkoClient ipkoClient = new IpkoClient();
    Supplier<String> supplier = () -> new Scanner(System.in).nextLine();
    Consumer<String> consumer = System.out::println;
    BankClientCLI cli = new BankClientCLI(ipkoClient, supplier, consumer);
    cli.run();
  }

}
