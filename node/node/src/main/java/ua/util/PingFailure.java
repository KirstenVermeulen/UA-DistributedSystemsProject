package ua.util;

import ua.node.Node;

import java.net.InetAddress;

public class PingFailure extends Thread {

    int countunreachable = 0;

    public void start() {
        while(true) {
            try {
                String previousNode = Node.getInstance().getPreviousNode();
                String nextNode = Node.getInstance().getNextNode();

                if (previousNode != null) {
                    try {
                        Node.getInstance().sendTCP(previousNode, "PING", null);
                    } catch (Exception e) {
                        System.out.println("wow deze node bestaat niet xddd");
                        e.printStackTrace();
                        Node.getInstance().failure(previousNode);
                    }
                }
                if (nextNode != null) {
                    try {
                        Node.getInstance().sendTCP(previousNode, "PING", null);
                    } catch (Exception e) {
                        System.out.println("wow deze node bestaat niet xddd");
                        e.printStackTrace();
                        Node.getInstance().failure(previousNode);
                    }

                }
                System.out.println("previousNode " + previousNode);
                System.out.println("nextNode " + nextNode);
                System.out.println("own ip " + InetAddress.getLocalHost().getHostAddress());
                System.out.println("Isbiggest: " + Node.getInstance().isBiggesthash() + "\n IsSmallest" + Node.getInstance().isSmallesthash());

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