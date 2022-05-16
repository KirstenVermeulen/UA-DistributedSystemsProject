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
//            Node.getInstance().failure(ip);
            e.printStackTrace();
        }
    }

    public void sendMessage(String type, String content) {
        String msg = String.format("%s:%s", type, content);
        out.println(msg);
    }

    public void sendFile(String receiverip, String filename) {
        // type:receiverip:senderip:filename
        String msg = String.format("%s:%s:%s:%s", "FILETRANSFER", receiverip, Node.getInstance().getCurrentNode(), filename);
        out.println(msg);
    }

    public void sendFileData(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            //Get socket's output stream
            OutputStream os = clientSocket.getOutputStream();
            //Read File Contents into contents array
            byte[] contents;
            long fileLength = file.length();
            long current = 0;
            long start = System.nanoTime();
            while (current != fileLength) {
                int size = 10000;
                if (fileLength - current >= size)
                    current += size;
                else {
                    size = (int) (fileLength - current);
                    current = fileLength;
                }
                contents = new byte[size];
                bis.read(contents, 0, size);
                os.write(contents);
                System.out.print("Sending file " + file.getName() + " ... " + (current * 100) / fileLength + "% complete!");
            }
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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