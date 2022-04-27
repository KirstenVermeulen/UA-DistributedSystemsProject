package ua.node;

import ua.util.MulticastPublisher;
import ua.util.MulticastReceiver;

public class Node {

    // --- FIELDS --- //

    private String previousNode;
    private String nextNode;

    private String nodeName;

    private MulticastPublisher publisher;


    // --- CONSTRUCTOR --- //
    public Node() {
        // TODO: Generate unique name
        nodeName = "Node 1";

        publisher = new MulticastPublisher();

        MulticastReceiver receiver = new MulticastReceiver();
        receiver.start();
    }

    // --- METHODS --- //
    public void run() {
        discovery();
    }

    private void discovery() {
        System.out.println("Publish");
        publisher.publishName(nodeName);
    }
}
