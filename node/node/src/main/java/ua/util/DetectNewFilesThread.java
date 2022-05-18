package ua.util;

import ua.node.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DetectNewFilesThread extends Thread {
    public void run(){
        System.out.println("Detecting new files thread is running...");
        Node nodereference = Node.getInstance();

        // This will get the files that were first captured when initializing a node
        ArrayList<String> starterfiles = nodereference.getStartFiles();
        ArrayList<String> currentfiles = nodereference.getStartFiles();
        Boolean firstTimeCheck = true;

        while (true){
            File files = new File(Constants.path);
            Collections.sort(starterfiles);

            // Regular interval every 0,5 seconds
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
