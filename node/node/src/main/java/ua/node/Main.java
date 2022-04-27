package ua.node;

import ua.util.MulticastReceiver;
import ua.util.TCPReceiver;

public class Main {
    public static void main(String[] args)  {

        // --- Multicast listener thread --- //
        MulticastReceiver receiver = new MulticastReceiver();
        receiver.start();

        // --- TCP listener thread --- //
        TCPReceiver server = new TCPReceiver();
        server.start();

    }
}
