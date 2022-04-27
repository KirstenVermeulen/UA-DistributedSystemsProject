package ua.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver extends Thread {

    protected MulticastSocket socket = null;
    protected byte[] buffer = new byte[256];

    public void run() {
        System.out.println("Started Receiver");

        try {
            socket = new MulticastSocket(4446);

            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group); // Deprecated (NOT for removal)

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());

                String address = String.valueOf(packet.getAddress());
                String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());

                if ("end".equals(received)) {
                    break;
                }

                System.out.println("message address: " + address);
                System.out.println("message content: " + msg);
            }
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
