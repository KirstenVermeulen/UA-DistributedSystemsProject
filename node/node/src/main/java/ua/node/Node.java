package ua.node;

import ua.HTTP.ExitNetwork;
import ua.HTTP.GetNeighbors;
import ua.HTTP.JsonBodyHandler;
import ua.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

public class Node {

    // --- FIELDS --- //

    private static Node instance = null;

    private String previousNode;
    private String nextNode;
    private String currentNode;

    private String nameserver;

    private String nodeName;

    private MulticastPublisher publisher;
    private TCPSender tcpSender;
    private HttpClient httpClient;

    // --- CONSTRUCTOR --- //
    private Node() {
        try {
            currentNode = InetAddress.getLocalHost().getHostAddress();
            nodeName = InetAddress.getLocalHost().getHostName();

            nextNode = currentNode;
            previousNode = currentNode;
        } catch (UnknownHostException e) {
            nodeName = "BadNodeName";
            e.printStackTrace();
        }



        publisher = new MulticastPublisher();
        tcpSender = new TCPSender();
        httpClient = HttpClient.newHttpClient();

        discovery();
    }

    public static Node getInstance() {
        if (Node.instance == null) {
            Node.instance = new Node();
        }

        return Node.instance;
    }

    // --- SETTERS --- //

    public void setPreviousNode(String previousNode) {
        this.previousNode = previousNode;
    }

    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }

    public void setNameserver(String nameserver) {
        this.nameserver = nameserver;
    }

    // --- METHODS --- //

    private void discovery() {
        publisher.publishName(nodeName);
    }

    public void nodeJoined(String ipAddress) {
        ipAddress = ipAddress.replace("/", "");

        int myHash = Hashing.hash(currentNode);
        int nextHash = Hashing.hash(nextNode);
        int previousHash = Hashing.hash(previousNode);
        int newHash = Hashing.hash(ipAddress);

        if (myHash == nextHash || (newHash < nextHash && newHash > myHash)) {
            nextNode = ipAddress;

            // Respond to the node
            try {
                tcpSender.startConnection(ipAddress, Constants.PORT);
                tcpSender.sendMessage("PREVIOUS", InetAddress.getLocalHost().getHostAddress());
                tcpSender.stopConnection();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        if (previousHash == myHash || (newHash > previousHash && newHash < myHash)) {
            previousNode = ipAddress;

            // Respond to the node
            try {
                tcpSender.startConnection(ipAddress, Constants.PORT);
                tcpSender.sendMessage("NEXT", InetAddress.getLocalHost().getHostAddress());
                tcpSender.stopConnection();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkIfAlone(int numberOfNodes){
        if (numberOfNodes < 2) {
            try {
                previousNode = InetAddress.getLocalHost().getHostAddress();
                nextNode = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void failure(String failedNode) {
        // Remove Node from network
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://" + nameserver + "/NameServer/ExitNetwork/" + failedNode))
                .header("accept", "application/json")
                .build();
        try {
            HttpResponse<Supplier<ExitNetwork>> response = httpClient.send(request, new JsonBodyHandler<>(ExitNetwork.class));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // Get my new neighbors
        request = HttpRequest.newBuilder(URI.create("http://" + nameserver + "/NameServer/GetNeighbors"))
                .header("accept", "application/json")
                .build();

        try {
            HttpResponse<Supplier<GetNeighbors>> response = httpClient.send(request, new JsonBodyHandler<>(GetNeighbors.class));

            System.out.println(response.body().get().next_node);
            System.out.println(response.body().get().previous_node);

            this.previousNode = response.body().get().previous_node;
            this.nextNode = response.body().get().next_node;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        // Notify my next neighbor
        try {
            tcpSender.startConnection(nextNode, Constants.PORT);
            tcpSender.sendMessage("PREVIOUS", InetAddress.getLocalHost().getHostAddress());
            tcpSender.stopConnection();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws MalformedURLException {
        // Remove itself from nameserver
        URL url = new URL("http://" + nameserver + ":8080/NameServer/ExitNetwork");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("node removed from nameserver");
        //update next node with value of prev node
        tcpSender.startConnection(nextNode, Constants.PORT);
        tcpSender.sendMessage("PREVIOUS", previousNode);
        tcpSender.stopConnection();
        System.out.println("nextnode updated with previousnode value");
        //update previous node with value of next node
        tcpSender.startConnection(previousNode, Constants.PORT);
        tcpSender.sendMessage("NEXT", nextNode);
        tcpSender.stopConnection();
        System.out.println("previousnode updated with nextnode value");
    }
}
