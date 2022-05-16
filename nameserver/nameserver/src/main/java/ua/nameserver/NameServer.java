package ua.nameserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.util.TCPSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static ua.util.Hashing.hash;

public class NameServer {

    // --- FIELDS --- //

    private static NameServer instance = null;

    private HashMap<Integer, String> ipMap = new HashMap<>();

    private TCPSender tcpSender;

    // --- CONSTRUCTOR (SINGLETON) --- //

    private NameServer() {
        tcpSender = new TCPSender();
    }

    public static NameServer getInstance() {
        if (NameServer.instance == null) {
            NameServer.instance = new NameServer();
        }
        return NameServer.instance;
    }

    // --- GETTER --- //

    public TCPSender getTcpSender() {
        return tcpSender;
    }

    // --- METHODS --- //

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

    public String getIpMapData() {
        String json = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(ipMap);
        } catch (Exception e){
            System.out.println("JSON error");
        }
        return json;
    }

    private static HashMap sortValues(HashMap map)
    {
        List list = new LinkedList(map.entrySet());
        //Custom Comparator
        Collections.sort(list, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Comparable) ((Map.Entry) (o1)).getKey()).compareTo(((Map.Entry) (o2)).getKey());
            }
        });
        //copying the sorted list in HashMap to preserve the iteration order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public void addIp(String ip) {
        ipMap.put(hash(ip), ip);
        ipMap = sortValues(ipMap);
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

    public String CheckIfReplicatedNode(int filehash) {
                        //[IPHASH:IP ADDR]
        //private HashMap<Integer, String> ipMap = new HashMap<>();
        ArrayList<Integer> tempreplicatednodeslist = new ArrayList<>();
        // node.hash < file.hash
        for (Integer nodehash : ipMap.keySet()) {
            if (nodehash < filehash){
                tempreplicatednodeslist.add(nodehash);
            }
        }
        if (tempreplicatednodeslist.isEmpty()){

        }
        int max = Collections.max(tempreplicatednodeslist);
        return ipMap.get(max);
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
