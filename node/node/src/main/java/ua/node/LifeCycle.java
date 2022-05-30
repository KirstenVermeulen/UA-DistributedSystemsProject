package ua.node;

import ua.util.Constants;
import ua.util.Hashing;
import ua.util.MulticastPublisher;
import ua.util.TCPSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class LifeCycle {

    // --- FIELDS --- //
    private MulticastPublisher publisher;


    // --- CONSTRUCTOR --- //
    public LifeCycle() {
        publisher = new MulticastPublisher();
    }


    // --- METHODS --- //
    public void discovery(String nodeName) {
        publisher.publishName(nodeName);
    }

    public void checkIfAlone(int numberOfNodes) {
        if (numberOfNodes < 2) {
            try {
                Node node = Node.getInstance();
                node.setPreviousNode(InetAddress.getLocalHost().getHostAddress());
                node.setNextNode(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void shutdown() {

        // Build URL to exit the network
        URL urlShutdown;

        try {
            urlShutdown = new URL("http://" + Node.getInstance().getNameserver() + ":8080/NameServer/ExitNetwork");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // Send request and receive response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlShutdown.openStream(), StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null; ) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Node removed from nameserver");

        TCPSender tcpSender = new TCPSender();
        String previousNode = Node.getInstance().getPreviousNode();
        String nextNode = Node.getInstance().getNextNode();

        // Update next node with value of previous node
        tcpSender.sendTCP(nextNode, "PREVIOUS", previousNode);
        System.out.println("Next node updated with previous node value");

        //update previous node with value of next node
        tcpSender.sendTCP(previousNode, "NEXT", nextNode);
        System.out.println("Previous node updated with next node value");
    }

    public void nodeJoined(String ipAddress) {
        ipAddress = ipAddress.replace("/", "");

        Node node = Node.getInstance();
        TCPSender sender = new TCPSender();

        String currentNode = node.getCurrentNode();
        String previousNode = node.getPreviousNode();
        String nextNode = node.getNextNode();

        int nodesInNetwork = 1;

        int myNodeHash = Hashing.hash(currentNode);
        int nextNodeHash = Hashing.hash(nextNode);
        int previousNodeHash = Hashing.hash(previousNode);
        int newNodeHash = Hashing.hash(ipAddress);

        // Request the number of nodes in the network from the name server
        URL amountofnodes;

        try {
            amountofnodes = new URL("http://" + node.getNameserver() + ":8080/NameServer/AmountOfNodes");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(amountofnodes.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null; ) {
                nodesInNetwork = Integer.parseInt(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("check if nameserver is running ;) ");
        }

        /*
         * We keep track if the node is the smallest/ biggest in the network.
         * This is to handle edge cases later
         */

        /*
         * In case there are 2 nodes in the network, they will
         * be each other's neighbouring nodes.
         */

        if (nodesInNetwork == 2) {

            if (newNodeHash < myNodeHash) {
                sender.sendTCP(ipAddress, "SETSMALLEST", null);
                node.setBiggest(true);
            } else {
                sender.sendTCP(ipAddress, "SETBIGGEST", null);
                node.setSmallest(true);
            }

            sender.sendTCP(ipAddress, "PREVIOUS", currentNode);
            sender.sendTCP(ipAddress, "NEXT", currentNode);
        }

        /*
         * In case there are 3 (or more) nodes in the network, the
         * neighbouring nodes are defined by the hash of the IP address.
         */

        else if (nodesInNetwork >= 3) {

            //
            if (newNodeHash < myNodeHash) {
                if (node.isSmallest()) {
                    sender.sendTCP(ipAddress, "SETSMALLEST", null);
                    sender.sendTCP(ipAddress, "NEXT", currentNode);

                    node.setSmallest(false);

                } else if (node.isBiggest() & (nextNodeHash > newNodeHash)) {
                    sender.sendTCP(ipAddress, "SETSMALLEST", null);
                    sender.sendTCP(ipAddress, "PREVIOUS", currentNode);

                } else if (previousNodeHash < newNodeHash) {
                    sender.sendTCP(ipAddress, "NEXT", currentNode);
                }
            }

            if (newNodeHash > myNodeHash) {
                if (node.isBiggest()) {
                    sender.sendTCP(ipAddress, "SETBIGGEST", null);
                    sender.sendTCP(ipAddress, "PREVIOUS", currentNode);

                    node.setBiggest(false);
                }
                if (node.isSmallest() & (previousNodeHash < newNodeHash)) {
                    sender.sendTCP(ipAddress, "SETBIGGEST", null);
                    sender.sendTCP(ipAddress, "NEXT", currentNode);

                } else if (newNodeHash < nextNodeHash) {
                    sender.sendTCP(ipAddress, "PREVIOUS", currentNode);
                }

            }
        }
    }

}
