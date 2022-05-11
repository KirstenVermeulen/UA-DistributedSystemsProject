package ua.node;

import ua.util.MulticastReceiver;
import ua.util.PingFailure;
import ua.util.TCPReceiver;

import java.net.MalformedURLException;

public class Main {
    public static void main(String[] args) throws MalformedURLException {

        // --- TCP listener thread --- //
        TCPReceiver server = new TCPReceiver();
        server.start();

        Node.getInstance();


        // --- Multicast listener thread --- //
        MulticastReceiver receiver = new MulticastReceiver();
        receiver.start();

        //PingFailure start
        PingFailure pingFailure = new PingFailure();
        pingFailure.start();
    }
}
