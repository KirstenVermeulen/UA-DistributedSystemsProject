package ua.node;

import ua.util.MulticastPublisher;
import ua.util.MulticastReceiver;
import ua.util.TCPClient;
import ua.util.TCPServer;

public class Main {
    public static void main(String[] args)  {

        MulticastReceiver receiver = new MulticastReceiver();
        TCPServer server = new TCPServer();
        TCPClient client = new TCPClient();

        receiver.start();
        server.run();
        client.run();
    }
}
