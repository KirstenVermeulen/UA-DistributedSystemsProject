package ua.node;

import ua.HTTP.ExitNetwork;
import ua.HTTP.GetNeighbors;
import ua.HTTP.JsonBodyHandler;
import ua.util.*;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class Node {

    // --- FIELDS --- //

    private static Node instance = null;

    private String previousNode;
    private String nextNode;
    private String currentNode;
    private String nameserver;
    private String nodeName;

    boolean biggesthash = false;
    boolean smallesthash = false;

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
        starting();
    }

    public static Node getInstance() {
        if (Node.instance == null) {
            Node.instance = new Node();
        }

        return Node.instance;
    }

    // --- SETTERS --- //

    public String getCurrentNode() {
        return currentNode;
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

    public void setBiggesthash(boolean biggesthash) {
        this.biggesthash = biggesthash;
    }

    public void setSmallesthash(boolean smallesthash) {
        this.smallesthash = smallesthash;
    }

    // --- GETTERS --- //

    public String getPreviousNode() {
        return previousNode;
    }

    public String getNextNode() {
        return nextNode;
    }

    public String getNameserver() {
        return nameserver;
    }

    public TCPSender getTcpSender() {
        return tcpSender;
    }

    public boolean isBiggesthash() {
        return biggesthash;
    }

    public boolean isSmallesthash() {
        return smallesthash;
    }

    // --- METHODS --- //

    private void discovery() {
        publisher.publishName(nodeName);
    }

    public void nodeJoined(String ipAddress) throws MalformedURLException {
        ipAddress = ipAddress.replace("/", "");

        int amountOfNodesInNetwork = 1;
     /*
        int myHash = Hashing.hash(currentNode);
        int nextHash = Hashing.hash(nextNode);
        int previousHash = Hashing.hash(previousNode);
     */

        int myHash = Hashing.hash(currentNode);
        int nextHash = Hashing.hash(nextNode);
        ;
        int previousHash = Hashing.hash(previousNode);
        int newHash = Hashing.hash(ipAddress);

        URL urlshutdown = new URL("http://" + nameserver + ":8080/NameServer/AmountOfNodes");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlshutdown.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null; ) {
                amountOfNodesInNetwork = Integer.parseInt(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("check if nameserver is running ;) ");
        }

        if (amountOfNodesInNetwork == 2) {
            previousNode = ipAddress;
            nextNode = ipAddress;
            nextHash = Hashing.hash(nextNode);
            previousHash = Hashing.hash(previousNode);
            if (newHash < myHash) {
                //todo send to other node it is the smallest now
                sendTCP(ipAddress, "SETSMALLEST", null);
                biggesthash = true;
            } else {
                smallesthash = true;
                sendTCP(ipAddress, "SETBIGGEST", null);
            }

            sendTCP(ipAddress, "PREVIOUS", currentNode);
            sendTCP(ipAddress, "NEXT", currentNode);
        }
/*
        if (amountOfNodesInNetwork == 3) {
            if (newHash < myHash) {
                if (smallesthash) {
                    //todo send to other node it is the smallest now
                    smallesthash = false;
                }
                if (biggesthash & (previousHash > newHash)) {
                    nextNode = ipAddress;
                    nextHash = Hashing.hash(previousNode);
                } else {
                    previousNode = ipAddress;
                    previousHash = Hashing.hash(previousNode);
                }
            }

            if (newHash > myHash) {
                if (biggesthash) {
                    //todo update new node with biggesthash
                    biggesthash = false;
                } if (smallesthash & (nextHash < newHash)) {
                    previousNode = ipAddress;
                    previousHash = Hashing.hash(previousNode);
                } else {
                    nextNode = ipAddress;
                    nextHash = Hashing.hash(nextNode);
                }
            }
        }

 */
        if (amountOfNodesInNetwork >= 3) {
            if (newHash < myHash) {
                if (smallesthash) {
                    //todo send to other node it is the smallest now
                    sendTCP(ipAddress, "SETSMALLEST", null);
                    sendTCP(ipAddress, "NEXT", currentNode);
                    smallesthash = false;
                    previousNode = ipAddress;
                    previousHash = Hashing.hash(previousNode);
                }
                if (biggesthash & (nextHash > newHash)) {
                    nextNode = ipAddress;
                    nextHash = Hashing.hash(nextNode);
                    sendTCP(ipAddress, "PREVIOUS", currentNode);
                    //todo update new node with prevnode and nextnode
                } else if (previousHash < newHash) {
                    previousNode = ipAddress;
                    previousHash = Hashing.hash(previousNode);
                    sendTCP(ipAddress, "NEXT", currentNode);
                }
            }

            if (newHash > myHash) {
                if (biggesthash) {
                    //todo update new node with biggesthash -> true && set own address as previousnode on newnode
                    biggesthash = false;
                    nextNode = ipAddress;
                    nextHash = Hashing.hash(nextNode);
                    sendTCP(ipAddress, "SETBIGGEST", null);
                    sendTCP(ipAddress, "PREVIOUS", currentNode);
                }
                if (smallesthash & (previousHash < newHash)) {
                    previousNode = ipAddress;
                    previousHash = Hashing.hash(previousNode);
                    //todo update new node with nextnode (and maybe biggesthash to true altho first if statement takes care of this)
                    sendTCP(ipAddress, "NEXT", currentNode);

                } else if (newHash < nextHash) {
                    nextNode = ipAddress;
                    nextHash = Hashing.hash(nextNode);
                    sendTCP(ipAddress, "PREVIOUS", currentNode);
                }

            }
        }


/*
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

 */

    }

    public void sendTCP(String ipAddressNewnode, String type, String ipAddresstogive) {
        tcpSender.startConnection(ipAddressNewnode, Constants.PORT);
        tcpSender.sendMessage(type, ipAddresstogive);
        tcpSender.stopConnection();
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
        URL urlshutdown = new URL("http://" + nameserver + ":8080/NameServer/ExitNetwork");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlshutdown.openStream(), "UTF-8"))) {
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
                        ReplicateFile(file);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void ReplicateFile(File file) {
        try {
            // ip voor file ophalen
            int nameHash = Hashing.hash(file.getName());
//            URL url = new URL("http://" + nameserver + ":8080/NameServer/ReplicateHashFile/" + nameHash);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
//            for (String line; (line = reader.readLine()) != null; ) {
//                System.out.println(line);
//                replicationIP += line;
//            }

            // extract ip from responds
            String replicationIP = "";

            URL url = new URL("http://" + nameserver + ":8080/NameServera/ReplicateHashFile/" + nameHash);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    System.out.println(line);
                    replicationIP += line;
                }

            tcpSender.startConnection(previousNode, Constants.PORT);
            tcpSender.sendFile(replicationIP, file.getName());
            tcpSender.sendFileData(file);
            tcpSender.stopConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void FileTransfer(String[] msg) {
        File file = new File("/root/tempTransferFiles/" + msg[3]);
        // check if sender ip equals our own ip
        if (msg[2].equals(Node.getInstance().getCurrentNode())) {
            // we got our own file back -> something went wrong, try again
            ReplicateFile(file);
            file.delete(); // remove  from temp folder
        } else if (msg[1].equals(Node.getInstance().getCurrentNode())) {
            // file was meant for this node -> write to disc -> move from temp folder to replicated folder
            try {
                Files.move(Paths.get(file.getAbsolutePath()), Paths.get("/root/FilesToReplicate/" + msg[3]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // file was not meant for this node -> transmit to next_node
            tcpSender.startConnection(nextNode, Constants.PORT);
            tcpSender.sendFile(msg[2], msg[3]);
            tcpSender.sendFileData(file);
            tcpSender.stopConnection();

            file.delete(); // remove  from temp folder
        }
    }
}