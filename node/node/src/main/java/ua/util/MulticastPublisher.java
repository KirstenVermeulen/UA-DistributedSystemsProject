package ua.util;

import java.io.IOException;
import java.net.*;

public class MulticastPublisher {

    // --- FIELDS --- //

    private DatagramSocket socket;
    private InetAddress group;
    private byte[] buffer;

    // --- CONSTRUCTOR --- //

    public MulticastPublisher() {
    }


    // --- METHODS --- //

    public void publishName(String name) {
        multicast("JOIN", name);
    }

    public void multicast(String type, String message) {
        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName("230.0.0.0");

            String multicastMessage = String.format("%s:%s", type, message);
            buffer = multicastMessage.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4446);
            socket.send(packet);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
