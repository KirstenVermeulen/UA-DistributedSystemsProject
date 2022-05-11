package ua.util;

import ua.node.Node;

import java.io.*;
import java.net.*;

public class TCPSender {

    // --- FIELDS --- //

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    // --- CONSTRUCTOR --- //

    public TCPSender() {

    }


    // --- METHODS --- //

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            Node.getInstance().failure(ip);
            e.printStackTrace();
        }
    }

    public void sendMessage(String type, String content) {
        String msg = String.format("%s:%s", type, content);
        out.println(msg);
    }

    public void sendFile(String receiverip, File content) {
        String msg = String.format("%s:%s:%s", "FILETRANSFER", receiverip, Node.getInstance().getCurrentNode(), content);
        out.println(msg);
    }

    public void stopConnection() {
        try {
            sendMessage("END", "");
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}