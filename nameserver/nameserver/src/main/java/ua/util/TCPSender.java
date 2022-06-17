package ua.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPSender {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTCP(String ipToConnect, String type, String message) {
        startConnection(ipToConnect, Constants.PORT);
        sendMessage(type, message);
        stopConnection();
    }

    public void sendMessage(String type, String content) {
        String msg = String.format("%s:%s", type, content);
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