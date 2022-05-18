package ua.util;

import ua.node.Node;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

// ClientHandler class
public class TCPHandler implements Runnable {
    private final Socket clientSocket;

    // Constructor
    public TCPHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;

        try {

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
                } else if (msg[0].equals("SETSMALLEST")) {
                    Node.getInstance().setSmallesthash(true);
                } else if (msg[0].equals("PING")) {
                    //so it does not throw error
                } else if (msg[0].equals("SETBIGGEST")) {
                    Node.getInstance().setBiggesthash(true);
                } else if (msg[0].equals("SHUTDOWN")) {
                    Node.getInstance().shutdown();
                } else if (msg[0].equals("FILETRANSFER")) {
                    // file wegschrijven naar temp folder
                    // controleren of we temp file kunnen wegschrijven
                    checkTempFolder();
                    OutputStream output = new FileOutputStream("/root/tempTransferFiles/" + msg[3]);
                    // bytes lezen
                    InputStream is = clientSocket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    // Closing the FileOutputStream and inputstream handles
                    output.close();
                    is.close();

                    Node.getInstance().FileTransfer(msg);
                } else {
                    System.out.println("Not a valid packet type");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkTempFolder() {
        String path = "/root/tempTransferFiles";
        File files = new File(path);
        try {
            if (!files.exists()) {
                // folder is empty so create folder
                Files.createDirectories(Paths.get(path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

