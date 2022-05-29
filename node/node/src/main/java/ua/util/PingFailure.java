package ua.util;

import ua.node.Node;

import java.net.InetAddress;

public class PingFailure extends Thread {

    int countunreachable = 0;
    TCPSender tcpSender = new TCPSender();

    public void start() {
        while(true) {
            try {
                String previousNode = Node.getInstance().getPreviousNode();
                String nextNode = Node.getInstance().getNextNode();

                if (previousNode != null) {
                    try {
                        tcpSender.sendTCP(previousNode, "PING", null);
                    } catch (Exception e) {
                        System.out.println("wow deze prev node bestaat niet xddd");
                        e.printStackTrace();
                        Node.getInstance().failure(previousNode);
                    }
                }
                if (nextNode != null) {
                    try {
                        tcpSender.sendTCP(nextNode, "PING", null);
                    } catch (Exception e) {
                        System.out.println("wow deze next node bestaat niet xddd");
                        e.printStackTrace();
                        Node.getInstance().failure(nextNode);
                    }

                }
                System.out.println("previousNode " + previousNode);
                System.out.println("nextNode " + nextNode);
                System.out.println("own ip " + InetAddress.getLocalHost().getHostAddress());
                //System.out.println("Isbiggest: " + Node.getInstance().isBiggesthash() + "\n IsSmallest" + Node.getInstance().isSmallesthash());

                System.out.println("countunreachable " + countunreachable);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }}