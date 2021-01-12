package pl.kontomatik.challenge;

import pl.kontomatik.challenge.commandline.BankNavigatorCLI;
import pl.kontomatik.challenge.navigator.IpkoNavigator;

import java.util.Map;

public class ChallengeApplication {

    public static void main(String[] args) throws Exception {
        BankNavigatorCLI bankNavigatorCLI = new BankNavigatorCLI(Map.of("ipko", new IpkoNavigator()));
        bankNavigatorCLI.run();
    }

}
