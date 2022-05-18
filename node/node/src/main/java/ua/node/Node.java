package ua.node;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.util.Constants;
import ua.util.Hashing;
import ua.util.MulticastPublisher;
import ua.util.TCPSender;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

public class Node {

    // --- FIELDS --- //

    private static Node instance = null;

    private String previousNode;
    private String nextNode;
    private String currentNode;
    private volatile String nameserver;
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

    public void discovery() {
        publisher.publishName(nodeName);
        // wait for nameserver ip before continuing
        while (nameserver == null) {
            Thread.onSpinWait();
        }

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

    public void failure(String failedNode) throws MalformedURLException {
        // Remove Node from network
        // Remove itself from nameserver
        URL urlfailuredown = new URL("http://" + nameserver + ":8080/NameServer/ExitNetwork/" + failedNode);
        System.out.println("####################### we got into failure bois ############################");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlfailuredown.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;) {
                System.out.println("failuremethod: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get my new neighbors
        URL getneighbours = new URL("http://" + nameserver + ":8080/NameServer/GetNeighbors/");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getneighbours.openStream(), "UTF-8"))) {
            for (String json; (json = reader.readLine()) != null;) {
                ObjectMapper mapper = new ObjectMapper();
                HashMap<String, String> jsondata = new HashMap<>();
                try {
                    if (json != null) {
                        jsondata = mapper.readValue(json, new TypeReference<>() {
                        });
                    } else {
                        jsondata = new HashMap<>();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String nxtnode = jsondata.get("next_node");
                String prevnighbour = jsondata.get("previous_node");

                System.out.println("failure nextnode: " + nxtnode);
                System.out.println("failure prevnode: " + prevnighbour);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Update value of my next node with my value as previous
        try {
            tcpSender.startConnection(nextNode, Constants.PORT);
            tcpSender.sendMessage("PREVIOUS", InetAddress.getLocalHost().getHostAddress());
            tcpSender.stopConnection();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // update value of previous node with my value as next
        try {
            tcpSender.startConnection(previousNode, Constants.PORT);
            tcpSender.sendMessage("NEXT", InetAddress.getLocalHost().getHostAddress());
            tcpSender.stopConnection();
        } catch (UnknownHostException e) {
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
                    for (File file : Objects.requireNonNull(files.listFiles())) {
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
            if (currentNode == nextNode) return; //extra check to avoid loops

            // extract ip from responds
            String replicationIP = "";
            String getrequest = "http://" + nameserver + ":8080/NameServer/ReplicateHashFile/" + nameHash;
            System.out.println("Sending request: "+getrequest);
            URL url = new URL(getrequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            for (String line; (line = reader.readLine()) != null; ) {
                System.out.println(line);
                replicationIP += line;
            }
            // don't send file to ourselves
            if (!replicationIP.equals(currentNode)) {
                tcpSender.startConnection(nextNode, Constants.PORT);
                tcpSender.sendFile(replicationIP, file.getName());
                tcpSender.sendFileData(file);
                tcpSender.stopConnection();
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
                // Check if ReplicateFiles dir exists
                String path = "/root/ReplicateFiles/";
                File dir = new File(path);
                if (!dir.exists()) {
                    Files.createDirectories(Paths.get(path));
                }
                System.out.println("Done replicating file: "+file.getName());
                Files.move(Paths.get(file.getAbsolutePath()), Paths.get(path + msg[3]));

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