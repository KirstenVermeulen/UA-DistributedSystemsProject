package ua.util;

import java.net.*;
import java.io.*;

public class TCPReceiver extends Thread {
    public void run() {
        try {
            // Setup server
            ServerSocket server = new ServerSocket(Constants.PORT);

            while(true) {
                System.out.println("TCP listener started (" + server.getLocalPort() + ")");

                // Wait for client to connect
                Socket client = server.accept();
                // serverSocket.setSoTimeout(10000);

                System.out.println("New client connected: " + client.getInetAddress().getHostAddress());

                // create a new thread object
                TCPHandler clientSock = new TCPHandler(client);

                // This thread will handle the client separately
                new Thread(clientSock).start();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}