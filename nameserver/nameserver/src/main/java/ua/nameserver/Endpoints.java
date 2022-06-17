package ua.nameserver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.util.Constants;
import ua.util.TCPSender;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;


@RestController
@RequestMapping("/NameServer")
public class Endpoints {

    private NameServer namingServer = NameServer.getInstance();
    private  TCPSender tcpSender = new TCPSender();
    @PutMapping("/UpdateServerFiles")
    @ResponseStatus(code = HttpStatus.OK, reason = "OK")
    public ResponseEntity<String> updateServerFiles() {
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    // Returns the hashed ip (int) of owner of the file
    @GetMapping("/ReplicateHashFile/{hash}")
    public String replicateHashFile(@PathVariable("hash") int filehash) {
        System.out.println("Checking replication ip of file: " + filehash);
        return namingServer.CheckIfReplicatedNode(filehash);
    }

    @GetMapping("/JoinNetwork")
//    @ResponseStatus(code = HttpStatus.OK, reason = "OK")
    public ResponseEntity<String> joinNetwork(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        System.out.println(ip + " has joined the network");
        namingServer.addIp(ip);
        return new ResponseEntity<>("Successfully joined the network", HttpStatus.ACCEPTED);
    }

    @GetMapping("/ExitNetwork")
    public ResponseEntity<String> exitNetworkByIP(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (namingServer.ipCheck(ip)) {
            namingServer.removeIp(ip);
            System.out.println(ip + " has quit the network");
            return new ResponseEntity<>("Successfully exited the network", HttpStatus.ACCEPTED);
        }
        System.out.println(ip + " is not in the network");
        return new ResponseEntity<>("Found nothing to remove", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/ExitNetwork/{ip}")
    public ResponseEntity<String> exitNetworkByIPInput(@PathVariable("ip") String ip) {
        if (namingServer.ipCheck(ip)) {
            namingServer.removeIp(ip);
            System.out.println(ip + " has quit the network");
            return new ResponseEntity<>("Successfully exited the network", HttpStatus.ACCEPTED);
        }
        System.out.println(ip + " is not in the network");
        return new ResponseEntity<>("Found nothing to remove", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/GetFileIp/{filename}")
    public String getfileIp(@PathVariable("filename") String fileName) {
        return namingServer.getFileIp(fileName);
    }

    @GetMapping("/GetNeighbors")
    public String getNeighbors(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        System.out.println("Getting neighbors for: " + ip);
        if (namingServer.getMapLength() < 1) {
            return "Not enough nodes in network, wait\n";
        } else {
            return namingServer.getNeighbors(ip);
        }
    }

    @GetMapping("/AmountOfNodes")
    public Integer AmountOfNodes() {
        System.out.println("Getting the amount of nodes");
        return namingServer.getMapLength();
    }

    @GetMapping("/GetIpMap")
    public String getIpMap() {
        System.out.println("Getting the ipmap");
        return namingServer.getIpMapData();
    }

    @GetMapping("/shutdown/{ip}")
    public String shutdown(@PathVariable("ip") String ip) {
        System.out.println("shutdown to " + ip);
        try {
            NameServer.getInstance().getTcpSender().sendTCP(ip,"SHUTDOWN", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "error";

        }
        return "shutdown send";
    }
}
