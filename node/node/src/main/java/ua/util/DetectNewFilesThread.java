package ua.util;

import ua.node.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DetectNewFilesThread extends Thread {
    public void run() {
        System.out.println("Detecting new files thread is running...");
        Node nodereference = Node.getInstance();

        // This will get the files that were first captured when initializing a node

        while (true) {
            File files = new File(Constants.path);
            try {
                if (!files.exists()) {
                    // folder is empty so create folder
                    Files.createDirectories(Paths.get(Constants.path));
                } else {
                    // loop through all files
                    if (files.listFiles() != null) {
                        for (File file : Objects.requireNonNull(files.listFiles())) {
                            boolean replicate = true;
                            for (String sfile : nodereference.getStartFiles()) {
                                if (file.getName().equals(sfile)) {
                                    replicate = false;
                                }
                            }
                            if (replicate){
                                // Needs to be replicated
                                //System.out.println("New file detected: " + file.getName());
                                nodereference.ReplicateFile(file);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Regular interval every 0,5 seconds
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
