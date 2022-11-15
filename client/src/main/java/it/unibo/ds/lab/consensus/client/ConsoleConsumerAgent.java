package it.unibo.ds.lab.consensus.client;
import com.google.gson.Gson;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import it.unibo.ds.lab.consensus.presentation.GsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class ConsoleConsumerAgent extends Thread {

    private static final int BUFFER_SIZE = 1024;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private final Gson gson = GsonUtils.createGson();
    private final String username;
    private final InputStream inputStream;
    private final Client client;
    private static final String CHAT_NAME = "chat1";

    public ConsoleConsumerAgent(InputStream inputStream, String username, Client client) {
        this.inputStream = System.in;
        this.username = username;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            KV kv = client.getKVClient();
            while (true) {
                int readBytes = this.inputStream.read(buffer);
                if (readBytes < 0) {
                    System.out.println("Reached end of input");
                    break;
                } else {
                    var key = ByteSequence.from(CHAT_NAME.getBytes());
                    var message = new Message(username, Arrays.copyOfRange(buffer, 0, readBytes));
                    var serializedMessage = gson.toJson(message);
                    var value = ByteSequence.from(serializedMessage.getBytes());
                    kv.put(key, value).get();
                    // System.out.printf("Sent message of %d bytes\n", readBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
