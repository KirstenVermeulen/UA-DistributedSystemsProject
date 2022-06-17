package ua.node;

import org.json.JSONException;
import org.json.JSONObject;

import ua.util.Constants;
import ua.util.Hashing;
import ua.util.TCPSender;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

public class Node {

    // --- FIELDS --- //

    private static Node instance = null;

    private String nodeName;

    private String previousNode;
    private String nextNode;
    private String currentNode;

    private boolean isSmallest;
    private boolean isBiggest;

    private volatile String nameserver;
    private ArrayList startfilenames = new ArrayList<>();

    // Communication //
    private TCPSender tcpSender;
    private HttpClient httpClient;

    // Node features //
    private LifeCycle lifeCycle;

    // --- CONSTRUCTOR --- //

    private Node() {
        try {
            currentNode = InetAddress.getLocalHost().getHostAddress();
            nodeName = InetAddress.getLocalHost().getHostName();
            nextNode = currentNode;
            previousNode = currentNode;

        } catch (UnknownHostException e) {

            // Failure?
            nodeName = "BadNodeName";
            e.printStackTrace();
        }

        // Communication //
        tcpSender = new TCPSender();
        httpClient = HttpClient.newHttpClient();

        // Node features //
        lifeCycle = new LifeCycle();
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

    public void setSmallest(boolean smallest) {
        isSmallest = smallest;
    }

    public void setBiggest(boolean biggest) {
        isBiggest = biggest;
    }

    // --- GETTERS --- //

    public String getCurrentNode() {
        return currentNode;
    }

    public String getPreviousNode() {
        return previousNode;
    }

    public ArrayList<String> getStartFiles() {
        if (startfilenames != null) {
            return startfilenames;
        } else {
            return null;
        }
    }

    public String getNameserver() {
        return nameserver;
    }

    public LifeCycle getLifeCycle() {
        return lifeCycle;
    }

    public String getNextNode() {
        return nextNode;
    }

    public boolean isSmallest() {
        return isSmallest;
    }

    public boolean isBiggest() {
        return isBiggest;
    }

    // --- METHODS --- //

    public void initNode() {
        lifeCycle.discovery(nodeName);
    }

    public void failure(String failedNode) {
        // Remove Node from network

        URL urlExitNetwork = null;
        try {
            urlExitNetwork = new URL("http://" + nameserver + ":8080/NameServer/ExitNetwork/" + failedNode);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlExitNetwork.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null; ) {
                System.out.println("failure: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("check if nameserver is running ;) ");
        }

        // Get my new neighbors
        URL urlGetNeighbors = null;
        try {
            urlGetNeighbors = new URL("http://" + nameserver + ":8080/NameServer/GetNeighbors");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlGetNeighbors.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null; ) {
                System.out.println("GetNeighbors: " + line);
                JSONObject obj = new JSONObject(line);
                System.out.println("jsonobj = " + obj);
                System.out.println("prev = " + obj.getString("previous_node"));
                System.out.println("next = " + obj.getString("next_node"));
                this.previousNode = obj.getString("previous_node");
                this.nextNode = obj.getString("next_node");

            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("check if nameserver is running ;) ");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Notify my next neighbor
        try {
            tcpSender.sendTCP(nextNode, "PREVIOUS", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // Notify my previous neighbor
        try {
            tcpSender.sendTCP(previousNode, "NEXT", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }



    public void ReplicateFile(File file) {
        try {
            // get ip of file
            int nameHash = Hashing.hash(file.getName());
            // wait for nameserver and nextnode ip to be initialized
            while (nameserver == null) {
                Thread.onSpinWait();
            }
            if (!currentNode.equals(nextNode)) {
                // extract ip from responds
                String replicationIP = "";
                String getrequest = "http://" + nameserver + ":8080/NameServer/ReplicateHashFile/" + nameHash;
                //System.out.println(getrequest);
                URL url = new URL(getrequest);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                for (String line; (line = reader.readLine()) != null; ) {
                    System.out.println(line);
                    replicationIP += line;
                }
                // don't send file to ourselves
                if (!replicationIP.equals(currentNode)) {
                    System.out.println("Starting replication from: "+currentNode+" to: "+replicationIP+" via: "+nextNode);
                    tcpSender.startConnection(nextNode, Constants.PORT);
                    tcpSender.sendFile(replicationIP, file.getName());
                    tcpSender.sendFileData(file);
                    tcpSender.stopConnection();
                    startfilenames.add(file.getName());
                } else {
                    System.out.println("Skipping replication of: "+file.getName()+", received own IP");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void FileTransfer(String[] msg) {
        File file = new File("/root/tempTransferFiles/" + msg[3]);
        System.out.println("Created temp file /root/tempTransferFiles/" + msg[3]);
        // check if sender ip equals our own ip
        if (msg[2].equals(Node.getInstance().getCurrentNode())) {
            // we got our own file back -> something went wrong, try again
            ReplicateFile(file);
            file.delete(); // remove  from temp folder
        } else if (msg[1].equals(Node.getInstance().getCurrentNode())) {
            // file was meant for this node -> write to disc -> move from temp folder to replicated folder
            try {
                Files.move(Paths.get(file.getAbsolutePath()), Paths.get("/root/ReplicateFiles/" + msg[3]));
                file.delete(); // remove  from temp folder
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // file was not meant for this node -> transmit to next_node
            tcpSender.startConnection(nextNode, Constants.PORT);
            tcpSender.sendFile(msg[1], msg[3]);
            tcpSender.sendFileData(file);
            tcpSender.stopConnection();

            file.delete(); // remove  from temp folder
        }
    }
}
