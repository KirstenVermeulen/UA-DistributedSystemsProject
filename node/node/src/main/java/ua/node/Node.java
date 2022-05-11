package ua.node;

import ua.HTTP.ExitNetwork;
import ua.HTTP.GetNeighbors;
import ua.HTTP.JsonBodyHandler;
import ua.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class Node {

    // --- FIELDS --- //

    private static Node instance = null;

    private String previousNode;
    private String nextNode;
    private String nameserver;

    private int currentHash;
    private int previousHash = -1;
    private int nextHash = -1;

    private String nodeName;

    private MulticastPublisher publisher;
    private TCPSender tcpSender;
    private HttpClient httpClient;

    // --- CONSTRUCTOR --- //
    private Node() throws UnknownHostException {
        try {
            nodeName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            nodeName = "BadNodeName";
            e.printStackTrace();
        }

        currentHash = Hashing.hash(nodeName);

        publisher = new MulticastPublisher();
        tcpSender = new TCPSender();
        httpClient = HttpClient.newHttpClient();

        discovery();
        starting();
    }

    public static Node getInstance() {
        if (Node.instance == null) {
            try {
                Node.instance = new Node();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
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

    public void nodeJoined(String name, String ipAddress) {
        int hash = Hashing.hash(name);
        ipAddress = ipAddress.replace("/", "");
        if (nextHash < 0 || (hash < nextHash && hash > currentHash)) {
            nextHash = hash;

            // Respond to the node
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

    public void checkIfAlone(int numberOfNodes) {
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
        URL url = new URL("http://" + nameserver + ":8080/NameServer/ExitNetwork/");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null; ) {
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

    public void starting() {
        String path = "/root/FilesToReplicate";
        File files = new File(path);
        try {
            if (!files.exists()) {
                // folder is empty so create folder
                Files.createDirectories(Paths.get(path));
            } else {
                // loop through all files
                if (files.listFiles() != null) {
                    for (File file : files.listFiles()) {
                        // share file
                        System.out.println(file.getAbsolutePath());


                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void ReplicateFile(File file) {
        try {
            URL url = new URL("http://" + nameserver + ":8080/NameServer/ExitNetwork/");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8")))
            for (String line; (line = reader.readLine()) != null; ) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}