package ua.util;

import ua.node.Node;

import java.io.*;
import java.net.*;

// ClientHandler class
public class TCPHandler implements Runnable {
    private final Socket clientSocket;

    // Constructor
    public TCPHandler(Socket socket)
    {
        this.clientSocket = socket;
    }

    public void run()
    {
        PrintWriter out = null;
        BufferedReader in = null;

        try {

            in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String line;
            while ((line = in.readLine()) != null) {

                String[] msg = line.split(":");

                if (msg[0].equals("END")) {
                    break;
                } else if (msg[0].equals("PREVIOUS")) {
                    Node.getInstance().setPreviousNode(msg[1]);
                } else if (msg[0].equals("NEXT")) {
                    Node.getInstance().setNextNode(msg[1]);
                } else if (msg[0].equals("NUMBEROFNODES")) {
                    Node.getInstance().setNameserver(clientSocket.getInetAddress().getHostAddress());
                    Node.getInstance().checkIfAlone(Integer.parseInt(msg[1]));
                } else if (msg[0].equals("SHUTDOWN")) {
                    Node.getInstance().shutdown();
                } else if (msg[0].equals("FILETRANSFER")) {
                    // check if sender ip equals our own ip
                    if (msg[2] == Node.getInstance().getCurrentNode()){
                        // we got our own file back ...
                    }
                    else if (msg[1] == Node.getInstance().getCurrentNode()){
                        // file was meant for this node
                    }
                    else{
                        // transmit to next_node
                        tcpSender.startConnection(ipAddress, Constants.PORT);
                        tcpSender.sendFile(msg[2], msg[2]);
                        tcpSender.stopConnection();

                    }
                }

                else {
                    System.out.println("Not a valid packet type");
                }

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}