package ua.nameserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ua.util.Hashing;
import ua.util.TCPSender;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import static ua.util.Hashing.hash;

public class NameServer {

    /* FIELDS */

    private static NameServer instance = null;

    private HashMap<Integer, String> ipMap = new HashMap<>();
    private TCPSender tcpSender;
    /* CONSTRUCTOR (SINGLETON) */


    private NameServer() {
        /* Testing values */
        ipMap.put(100, "testipadress");
        tcpSender = new TCPSender();
    }

    public TCPSender getTcpSender() {
        return tcpSender;
    }

    public static NameServer getInstance() {
        if (NameServer.instance == null) {
            NameServer.instance = new NameServer();
        }
        return NameServer.instance;
    }


    /* METHODS */
    public String getFileIp(String filePath) {
        int fileHash = hash((filePath));
        // int requiredIpHash = ipMap.keySet().stream().map(s -> abs(s - fileHash)).min(Integer::compare).get();
        int min_diff = 32768;
        String ip = "";
        int diff;
        for (int key : ipMap.keySet()) {
            diff = Math.abs(key - fileHash);
            if (diff < min_diff) {
                min_diff = diff;
                ip = ipMap.get(key);
            }
        }
        return ip;
    }

    public void addIp(String ip) {
        ipMap.put(hash(ip), ip);
    }

    public void removeIp(String ip) {
        ipMap.remove(hash((ip)));
    }

    public boolean ipCheck(String ip) {
        return ipMap.containsKey(hash((ip)));
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
                ipMap = mapper.readValue(json, new TypeReference<>() {
                });
            } else {
                ipMap = new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMapLength() {
        return ipMap.size();
    }

    public String getNeighbors(String ip) {
        int hash = hash(ip);
        String prev_node = null;
        String next_node = null;
        Object[] keys = ipMap.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            if (hash == (int) keys[i]) {
                if (i == 0) {
                    prev_node = ipMap.get((int) keys[keys.length - 1]);
                } else {
                    prev_node = ipMap.get((int) keys[i - 1]);
                }
                if (i == keys.length - 1) {
                    next_node = ipMap.get((int) keys[0]);
                } else {
                    next_node = ipMap.get((int) keys[i + 1]);
                }
            }
        }
        return "{" +
                "\"previous_node\": \"" + prev_node + "\"," +
                "\"next_node\": \"" + next_node + "\"" +
                "}";
    }
}
