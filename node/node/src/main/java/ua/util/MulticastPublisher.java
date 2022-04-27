package ua.util;

import java.io.IOException;
import java.net.*;

public class MulticastPublisher {
    private DatagramSocket socket;
    private InetAddress group;
    private byte[] buffer;

    public void publishName(String name) {
        multicast(name);
    }

    public void multicast(String multicastMessage) {
        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName("230.0.0.0");
            buffer = multicastMessage.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4446);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
