package ua.util;// Client Side

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPClient {
    public void run() {

        PrintWriter out = null;
        BufferedReader in = null;

        try (Socket socket = new Socket("localhost", 4020)){

            System.out.println("Just connected to " + socket.getRemoteSocketAddress());

            // Receive the file
            byte[] b = new byte[2000];
            InputStream is = socket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream("D:\\received.txt");
            is.read(b, 0, b.length);
            fileOutputStream.write(b, 0, b.length);

            // get the outputstream of client
            out = new PrintWriter(socket.getOutputStream(), true);

            // get the inputstream of client
            in = new BufferedReader( new InputStreamReader(socket.getInputStream()));

            // object of scanner class
            Scanner sc = new Scanner(System.in);
            String line = null;

            while (!"exit".equalsIgnoreCase(line)) {

                // reading from user
                line = sc.nextLine();

                // sending the user input to server
                out.println(line);
                out.flush();

                // displaying server reply
                System.out.println("Server replied: " + in.readLine());
            }

            // closing the scanner object
            sc.close();
        }
        catch(UnknownHostException ex) {
            ex.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

}