package ua.util;

import java.io.IOException;
import java.net.*;

public class MulticastPublisher {

    // --- CONSTRUCTOR --- //
    public MulticastPublisher() {
    }

    // --- METHODS --- //

    // Multicast messages //
    public void publishName(String name) {
        multicast("JOIN", name);
    }

    // Send any type of message via multicast //
    public void multicast(String type, String message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName("230.0.0.0");

            String multicastMessage = String.format("%s:%s", type, message);
            byte[] buffer = multicastMessage.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4446);
            socket.send(packet);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
