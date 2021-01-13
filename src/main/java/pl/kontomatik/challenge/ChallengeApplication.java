package pl.kontomatik.challenge;

import pl.kontomatik.challenge.commandline.BankNavigatorCLI;
import pl.kontomatik.challenge.mapper.IpkoMapperImpl;
import pl.kontomatik.challenge.navigator.IpkoNavigator;

public class ChallengeApplication {

    public static void main(String[] args) throws Exception {
        IpkoMapperImpl ipkoMapper = new IpkoMapperImpl();
        IpkoNavigator ipkoNavigator = new IpkoNavigator(ipkoMapper);
        BankNavigatorCLI cli = new BankNavigatorCLI(ipkoNavigator);
        cli.run();
    }

}
