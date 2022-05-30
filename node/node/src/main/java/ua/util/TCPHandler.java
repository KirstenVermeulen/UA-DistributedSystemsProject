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

            Node node = Node.getInstance();

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String line;
            while ((line = in.readLine()) != null) {

                String[] msg = line.split(":");

                if (msg[0].equals("END")) {
                    break;
                } else if (msg[0].equals("PREVIOUS")) {
                    node.setPreviousNode(msg[1]);
                } else if (msg[0].equals("NEXT")) {
                    node.setNextNode(msg[1]);
                } else if (msg[0].equals("NUMBEROFNODES")) {
                    node.setNameserver(clientSocket.getInetAddress().getHostAddress());
                    node.getLifeCycle().checkIfAlone(Integer.parseInt(msg[1]));
                } else if (msg[0].equals("SETSMALLEST")) {
                    node.setSmallest(true);
                    node.setBiggest(false);
                } else if (msg[0].equals("PING")) {
                    //so it does not throw error
                } else if (msg[0].equals("SETBIGGEST")) {
                    node.setSmallest(false);
                    node.setBiggest(true);
                } else if (msg[0].equals("SHUTDOWN")) {
                    node.getLifeCycle().shutdown();
                } else if (msg[0].equals("FILETRANSFER")) {
                    // format = type, ReceiverNodeIP, SenderNodeIP, FileName
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

                    node.FileTransfer(msg);
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

