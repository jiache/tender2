package io.jiache.main;

import io.jiache.core.Address;
import io.jiache.raft.RaftNode;
import io.jiache.raft.RaftServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiacheng on 17-8-28.
 */
public class ServerMain {
    public static void main(String[] args) throws InterruptedException {
        // args is [host:port] [secretaryHost0,secretaryPort0,...] [host0:port0,host1:port1,...,host:port]
        if(args.length != 3) {
            System.out.println("args is [host:port] [secretaryHost0:secretaryPort0,...] [host0:port0,host1:port1,...,host:port]");
            System.exit(-1);
        }
        String[] args1 = args[0].split(":");
        Address local = new Address(args1[0], Integer.parseInt(args1[1]));
        List<Address> cluster = new ArrayList<>();
        Arrays.stream(args[2].split(",")).forEach((s -> {
            String[] address = s.split(":");
            cluster.add(new Address(address[0], Integer.parseInt(address[1])));
        }));
        List<Address> secretaries = new ArrayList<>();
        Arrays.stream(args[1].split(",")).forEach((s)->{
            String[] address = s.split(":");
            secretaries.add(new Address(address[0], Integer.parseInt(address[1])));
        });
        RaftServer server = new RaftNode(local,cluster.get(0),cluster.subList(1,cluster.size()-1),secretaries);
        new Thread(()->{
            try {
                server.start();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        TimeUnit.SECONDS.sleep(10);
        server.initStub();
        System.out.println("server started");
    }
}
