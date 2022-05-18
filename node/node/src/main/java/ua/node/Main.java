package ua.node;

import ua.util.DetectNewFilesThread;
import ua.util.MulticastReceiver;
import ua.util.PingFailure;
import ua.util.TCPReceiver;

public class Main {
    public static void main(String[] args) {
        // --- TCP listener thread --- //
        TCPReceiver server = new TCPReceiver();
        server.start();

        Node.getInstance();
        Node.getInstance().discovery();
        Node.getInstance().starting();

        // --- Multicast listener thread --- //
        MulticastReceiver receiver = new MulticastReceiver();
        receiver.start();

        PingFailure pingFailure = new PingFailure();
        pingFailure.start();

        DetectNewFilesThread detectNewFilesThread= new DetectNewFilesThread();
        detectNewFilesThread.start();
    }
}
