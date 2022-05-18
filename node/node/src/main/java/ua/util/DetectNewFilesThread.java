package ua.util;

import ua.node.Node;

public class DetectNewFilesThread extends Thread {
    public void run(){
        System.out.println("thread is running...");
        Node nodereference = Node.getInstance();
        while (true){
            // Every x time check if there is a new file added
            //nodereference.starting();
        }
    }
}
