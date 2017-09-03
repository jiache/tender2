package io.jiache.main;

import java.util.concurrent.TimeUnit;

/**
 * Created by jiacheng on 17-8-28.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        new Thread(()->{
            try {
                ServerMain.main(new String[]{"localhost:8900","localhost:8800","localhost:8900,localhost:8901,localhost:8902"});
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(()->{
            try {
                ServerMain.main(new String[]{"localhost:8901","localhost:8800","localhost:8900,localhost:8901,localhost:8902"});
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(()->{
            try {
                ServerMain.main(new String[]{"localhost:8902","localhost:8800","localhost:8900,localhost:8901,localhost:8902"});
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(()->{
            SecretaryMain.main(new String[]{"localhost:8800", "localhost:8901,localhost:8902"});
        }).start();
        TimeUnit.SECONDS.sleep(15);
        new Thread(()->{
            try {
                ClientMain.main(new String[]{"localhost:8900","100","localhost:8900,localhost:8901,localhost:8902","100"});
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
