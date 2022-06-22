package ua.node;

import ua.util.DetectNewFilesThread;
import ua.util.MulticastReceiver;
import ua.util.PingFailure;
import ua.util.TCPReceiver;

public class Main {
    public static void main(String[] args) {

        // Create a Node object
        Node node = Node.getInstance();

        // Start listening for incoming TCP requests
        TCPReceiver tcpReceiver = new TCPReceiver();
        tcpReceiver.start();

        node.initNode();
        //node.discovery();

        // Start listening for incoming multicast messages
        MulticastReceiver multicastReceiver = new MulticastReceiver();
        multicastReceiver.start();

        // Start checking of other nodes are still alive
        PingFailure pingFailure = new PingFailure();
        pingFailure.start();

        // Start checking for new files
        DetectNewFilesThread detectNewFilesThread = new DetectNewFilesThread();
        detectNewFilesThread.start();
    }
}
