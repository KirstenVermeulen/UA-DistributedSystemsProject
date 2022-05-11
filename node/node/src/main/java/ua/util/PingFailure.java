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
                    boolean prevreachable = addressprev.isReachable(10000);
                    System.out.println("prevreachable " + prevreachable);
                    System.out.println("addressprev " + addressprev);

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
