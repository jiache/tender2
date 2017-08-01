package jiache.raft;

import jiache.core.Address;

import java.io.IOException;
import java.util.List;

/**
 * Created by jiacheng on 17-7-31.
 */
public interface RaftServer {
    void start(Address address) throws IOException, InterruptedException;
    void bootstrap(List<Address> secretaryAddresses, List<Address> followerAddresses);
    void connectLeader(Address address);
}
