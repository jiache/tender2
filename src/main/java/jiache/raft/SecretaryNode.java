package jiache.raft;

import com.alibaba.fastjson.JSON;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jiache.grpc.*;
import jiache.core.Address;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiacheng on 17-7-30.
 */
public class SecretaryNode implements SecretaryServer{
    private Integer committedIndex;
    private List<Entry> log;
    private List<Integer> nextIndex;
    private Server secretaryServer;

    @Override
    public void start(Address address) throws IOException, InterruptedException {
        committedIndex = -1;
        log = new ArrayList<>();
        secretaryServer = ServerBuilder.forPort(address.getPort())
                .addService(new SecretaryServiceImpl())
                .build()
                .start();
//        Runtime.getRuntime().addShutdownHook(new Thread(()->{
//            System.out.println("JVM shutdown");
//            SecretaryNode.this.secretaryServer.shutdown();
//            System.out.println("SecretaryServer shutdown");
//        }));
        secretaryServer.awaitTermination();
    }

    @Override
    public void bootstrap(List<Address> followerAddresses) {
        nextIndex = new ArrayList<>();
        for(int i=0; i<followerAddresses.size(); ++i) {
            nextIndex.add(0);
        }
        for(int i=0; i<followerAddresses.size(); ++i) {
            Integer followerIndex = i;
            Address address = followerAddresses.get(i);
            new Thread(()->{
                ManagedChannel channel = ManagedChannelBuilder.forAddress(address.getIp(), address.getPort())
                        .usePlaintext(true)
                        .build();
                RaftServiceGrpc.RaftServiceBlockingStub blockingStub = RaftServiceGrpc.newBlockingStub(channel);
                while(true) {
                    Entry entry = nextEntry(followerIndex);
                    AppendEntriesResponce responce = blockingStub.appendEntries(AppendEntriesRequest.newBuilder()
                            .setCommittedIndex(committedIndex)
                            .setEntryJson(JSON.toJSONString(entry))
                            .build());
                    if(entry!=null && responce.getSuccess() == true){
                        addNextIndex(followerIndex);
                    }
                    Thread.interrupted();
                }
            }).start();
        }
    }

    private class SecretaryServiceImpl extends SecretaryServiceGrpc.SecretaryServiceImplBase{
        @Override
        public void addEntries(AddEntriesRequest request, StreamObserver<AddEntriesResponce> responseObserver) {
            Entry entry = JSON.parseObject(request.getEntryJson(), Entry.class);
            addEntry(entry);
            committedIndex = request.getCommittedIndex();
            responseObserver.onNext(AddEntriesResponce.newBuilder()
                    .setSuccess(true)
                    .build());
            responseObserver.onCompleted();
        }
    }

    // 增加entry
    private synchronized void addEntry(Entry entry) {
        log.add(entry);
    }

    // 得到下一个follower的Entry
    private Entry nextEntry(int followerIndex){
        int entryIndex = nextIndex.get(followerIndex);
        if(entryIndex<log.size()){
            return log.get(entryIndex);
        }
        return null;
    }

    // nextIndex指向下一个log
    private void addNextIndex(int followerIndex) {
        nextIndex.set(followerIndex,nextIndex.get(followerIndex)+1);
    }
}
