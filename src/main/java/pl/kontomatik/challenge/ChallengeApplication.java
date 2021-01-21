package pl.kontomatik.challenge;

import pl.kontomatik.challenge.commandline.BankConnectorCLI;
import pl.kontomatik.challenge.connector.ipko.IpkoConnector;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChallengeApplication {

  public static void main(String[] args) {
    IpkoConnector ipkoConnector = new IpkoConnector();
    Supplier<String> supplier = () -> new Scanner(System.in).nextLine();
    Consumer<String> consumer = System.out::println;
    BankConnectorCLI cli = new BankConnectorCLI(ipkoConnector, supplier, consumer);
    cli.run();
  }

}
