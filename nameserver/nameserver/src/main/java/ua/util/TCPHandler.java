package ua.util;

import java.io.*;
import java.net.Socket;

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

            // Setup file to share
            byte b[] = new byte[2000];  // needs to be bigger than filesize
            FileInputStream fileInputStream = new FileInputStream("D:\\Send.txt");
            fileInputStream.read(b, 0, b.length);

            // Write the file to the output stream
            clientSocket.getOutputStream().write(b, 0, b.length);

            // get the outputstream of client
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // TODO: Handle message received from client

            // get the inputstream of client
            in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {

                // writing the received message from client
                System.out.printf("Sent from the client: %s\n", line);
                out.println(line);
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