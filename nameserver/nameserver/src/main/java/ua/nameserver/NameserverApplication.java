package ua.nameserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ua.util.MulticastReceiver;

@SpringBootApplication
public class NameserverApplication {

    public static void main(String[] args) {

        // --- Start REST application --- /
        SpringApplication.run(NameserverApplication.class, args);

        // --- Start multicast receiver --- //
        MulticastReceiver receiver = new MulticastReceiver();
        receiver.start();
    }
}
