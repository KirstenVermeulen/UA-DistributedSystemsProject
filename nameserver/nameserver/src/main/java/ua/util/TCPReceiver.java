package ua.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPReceiver extends Thread {
    public void run() {
        try {

            // Setup server
            int serverPort = Constants.PORT;
            ServerSocket server = new ServerSocket(serverPort);

            while(true) {
                System.out.println("Waiting for client on port " + server.getLocalPort() + "...");

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