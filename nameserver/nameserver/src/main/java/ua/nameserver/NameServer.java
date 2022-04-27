package ua.nameserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.util.Functions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class NameServer {

    /* FIELDS */

    private static NameServer instance = null;

    private HashMap<Integer, String> ipMap = new HashMap<>();

    /* CONSTRUCTOR (SINGLETON) */

    private NameServer() {
        /* Testing values */
        ipMap.put(100, "testipadress");
    }

    public static NameServer getInstance() {
        if (NameServer.instance == null) {
            NameServer.instance = new NameServer();
        }
        return NameServer.instance;
    }

    /* METHODS */
    public String getFileIp(String filePath) {
        int fileHash = Functions.hashFunction((filePath));
        // int requiredIpHash = ipMap.keySet().stream().map(s -> abs(s - fileHash)).min(Integer::compare).get();
        int min_diff = 32768;
        String ip = "";
        int diff;
        for (int key : ipMap.keySet()){
            diff = Math.abs(key - fileHash);
            if (diff < min_diff){
                min_diff = diff;
                ip = ipMap.get(key);
            }
        }
        return ip;
    }

    public void addIp(String ip) {
        ipMap.put(Functions.hashFunction(ip), ip);
    }

    public void removeIp(String ip) {
        ipMap.remove(Functions.hashFunction((ip)));
    }

    public boolean ipCheck(String ip){
        return ipMap.containsKey(Functions.hashFunction((ip)));
    }

    private void saveHashMap() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            String json = mapper.writeValueAsString(ipMap);

            // Create the JSON-file
            File saveFile = new File("data.json");
            if (saveFile.createNewFile()) {
                System.out.println("File created: " + saveFile.getName());
            } else {
                System.out.println("File already exists.");
            }

            // Write to the JSON file
            FileWriter writer = new FileWriter("data.json");
            writer.write(json);
            writer.close();

            System.out.println("Successfully wrote to the file.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHashMap(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;

        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                json = scanner.nextLine();
            }

            if (json != null) {
                ipMap = mapper.readValue(json, new TypeReference<>() {});
            } else {
                ipMap = new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
