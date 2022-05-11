package ua.util;

import ua.nameserver.NameServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver extends Thread {

    protected MulticastSocket socket = null;
    protected byte[] buffer = new byte[256];

    public void start() {
        System.out.println("Multicast listener started");

        try {
            socket = new MulticastSocket(4446);

            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group); // Deprecated (NOT for removal)

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String data = new String(packet.getData(), packet.getOffset(), packet.getLength());
                System.out.println(data);
                String[] msg = data.split(":");
                // [msg0]:[msg1]
                // type:message
                if (msg[0].equals("END")) {
                    break;
                } else if (msg[0].equals("JOIN")) {
                    String ip = packet.getAddress().getHostAddress().replace("/", "");
                    NameServer.getInstance().addIp(ip);
                    NameServer.getInstance().getTcpSender().startConnection(ip, Constants.PORT);
                    NameServer.getInstance().getTcpSender().sendMessage("NUMBEROFNODES",String.valueOf(NameServer.getInstance().getMapLength()));
                    NameServer.getInstance().getTcpSender().stopConnection();
                } else {
                    System.out.println("Not a valid packet type");
                }
            }
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            socket.close();
            e.printStackTrace();
        }
    }
}
