package ua.util;

import ua.nameserver.NameServer;

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

                String[] msg = new String(packet.getData(), packet.getOffset(), packet.getLength()).split(":");
                // [msg0]:[msg1]
                // type:message
                if (msg[0].equals("END")) {
                    break;
                } else if (msg[0].equals("JOIN")) {
                    NameServer.getInstance().addIp(msg[1]);
                    NameServer.getInstance().getTcpSender().sendMessage("NumberNodes", String.valueOf(NameServer.getInstance().getMapLength()));
                } else {
                    System.out.println("Not a valid packet type");
                }

            }
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
