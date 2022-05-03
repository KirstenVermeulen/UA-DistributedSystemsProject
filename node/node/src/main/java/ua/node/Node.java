package ua.node;

import ua.util.Constants;
import ua.util.Hashing;
import ua.util.MulticastPublisher;
import ua.util.TCPSender;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Node {

    // --- FIELDS --- //

    private static Node instance = null;

    private String previousNode;
    private String nextNode;

    private int currentHash;
    private int previousHash = -1;
    private int nextHash = -1;

    private String nodeName;

    private MulticastPublisher publisher;
    private TCPSender tcpSender;

    // --- CONSTRUCTOR --- //
    private Node() {
        // TODO: Generate unique name
        nodeName = "Node 1";
        currentHash = Hashing.hash(nodeName);

        publisher = new MulticastPublisher();
        tcpSender = new TCPSender();

        discovery();
    }

    public static Node getInstance() {
        if (Node.instance == null) {
            Node.instance = new Node();
        }

        return Node.instance;
    }

    // --- METHODS --- //

    private void discovery() {
        System.out.println("Publish");
        publisher.publishName(nodeName);
    }

    public void nodeJoined(String name, String ipAddress) {
        int hash = Hashing.hash(name);

        if (nextHash < 0 || (hash < nextHash && hash > currentHash)) {
            nextHash = hash;

            try {
                tcpSender.startConnection(ipAddress, Constants.PORT);
                tcpSender.sendMessage("PREVIOUS", InetAddress.getLocalHost().getHostAddress());
                tcpSender.stopConnection();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        if (previousHash < 0 || (hash > previousHash && hash < currentHash)) {
            previousHash = hash;

            try {
                tcpSender.startConnection(ipAddress, Constants.PORT);
                tcpSender.sendMessage("NEXT", InetAddress.getLocalHost().getHostAddress());
                tcpSender.stopConnection();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }


    // Shutdown logica
    public void redButton(){

    }
}
