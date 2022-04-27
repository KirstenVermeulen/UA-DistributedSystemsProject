package ua.util;

import java.net.*;
import java.io.*;

public class TCPServer {
    public void run() {
        try {

            // Setup server
            int serverPort = 4020;
            ServerSocket server = new ServerSocket(serverPort);

            while(true) {
                System.out.println("Waiting for client on port " + server.getLocalPort() + "...");

                // Wait for client to connect
                Socket client = server.accept();
                // serverSocket.setSoTimeout(10000);

                System.out.println("New client connected: " + client.getInetAddress().getHostAddress());

                // create a new thread object
                TCPClientHandler clientSock = new TCPClientHandler(client);

                // This thread will handle the client separately
                new Thread(clientSock).start();
            }
        }
        catch(UnknownHostException ex) {
            ex.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


}