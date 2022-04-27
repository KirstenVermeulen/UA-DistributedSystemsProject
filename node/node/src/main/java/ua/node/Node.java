package ua.node;

import ua.util.MulticastPublisher;
import ua.util.MulticastReceiver;

public class Node {

    // --- FIELDS --- //

    private static Node instance = null;

    private String previousNode;
    private String nextNode;

    private String nodeName;

    private MulticastPublisher publisher;

    // --- CONSTRUCTOR --- //
    private Node() {
        // TODO: Generate unique name
        nodeName = "Node 1";
        publisher = new MulticastPublisher();
        discovery();
    }

    public static Node getInstance() {
        if (Node.instance == null) {
            Node.instance = new Node();
        }

        return Node.instance;
    }

    // --- GETTERS & SETTERS --- //

    public String getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(String previousNode) {
        this.previousNode = previousNode;
    }

    public String getNextNode() {
        return nextNode;
    }

    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }


    // --- METHODS --- //

    private void discovery() {
        System.out.println("Publish");
        publisher.publishName(nodeName);
    }

    public void nodeJoined(String name, String ipAddress) {

    }

    public void redButton(){

    }
}
