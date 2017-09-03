package io.jiache.main;

import io.jiache.core.Address;
import io.jiache.raft.SecretaryNode;
import io.jiache.raft.SecretaryServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jiacheng on 17-8-28.
 */
public class SecretaryMain {
    public static void main(String[] args) {
        // args is [host:port] [followerHost1:followerPort1,...]
        if(args.length != 2) {
            System.out.println("args is [host:port] [followerHost1:followerPort1,...]");
            System.exit(-1);
        }
        String[] addressS = args[0].split(":");
        Address address = new Address(addressS[0], Integer.parseInt(addressS[1]));
        List<Address> followers = new ArrayList<>();
        Arrays.stream(args[1].split(",")).forEach((s)->{
            String[] ss = s.split(":");
            followers.add(new Address(ss[0], Integer.parseInt(ss[1])));
        });
        SecretaryServer secretary = new SecretaryNode(address,followers);
        new Thread(()->{
            try {
                secretary.start();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(secretary::bootstrap).start();
    }
}
