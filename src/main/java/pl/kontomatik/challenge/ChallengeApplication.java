package pl.kontomatik.challenge;

import pl.kontomatik.challenge.commandline.BankNavigatorCLI;
import pl.kontomatik.challenge.mapper.IpkoMapperImpl;
import pl.kontomatik.challenge.navigator.IpkoNavigator;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChallengeApplication {

    public static void main(String[] args) {
        IpkoMapperImpl ipkoMapper = new IpkoMapperImpl();
        IpkoNavigator ipkoNavigator = new IpkoNavigator(ipkoMapper);

        Supplier<String> supplier = () -> new Scanner(System.in).nextLine();
        Consumer<String> consumer = System.out::println;

        BankNavigatorCLI cli = new BankNavigatorCLI(ipkoNavigator, supplier, consumer);
        cli.run();
    }

}
