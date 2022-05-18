package ua.util;

import ua.node.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class PingFailure extends Thread {

    int countunreachable = 0;

    public void start() {
        while(true) {
            try {
                String previousNode = Node.getInstance().getPreviousNode();
                String nextNode = Node.getInstance().getNextNode();

                if (previousNode != null) {
                    InetAddress addressprev = InetAddress.getByName(previousNode);
                    InetAddress addressfalse = InetAddress.getByName("192.168.160.7");

                    boolean prevreachable = addressprev.isReachable(10000);
                    boolean addddreachable = addressfalse.isReachable(10000);
                    System.out.println("saysfalse " + addddreachable);
                    System.out.println("addressprev " + addressprev);
                    System.out.println("prevreachable " + prevreachable);
                    try {
                        Node.getInstance().sendTCP("192.168.160.7", "PING", null);
                    } catch (Exception e) {
                        System.out.println("wow deze node bestaat niet xddd");
                        e.printStackTrace();
                    }

                    //  if (!prevreachable) Node.getInstance().failure(previousNode);
                }
                if (nextNode != null) {
                    InetAddress addressnext = InetAddress.getByName(nextNode);
                    boolean nextreachable = addressnext.isReachable(10000);
                    System.out.println("nextreachable " + nextreachable);
                    System.out.println("addressnext " + addressnext);

                //    if (!nextreachable) Node.getInstance().failure(nextNode);

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
