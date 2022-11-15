package it.unibo.ds.lab.consensus.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchEvent;
import it.unibo.ds.lab.consensus.presentation.GsonUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class ClientSideConsumerAgent extends Thread {

    private final Gson gson = GsonUtils.createGson();
    private static final String CHAT_NAME = "chat1";
    private final Client client;
    private final OutputStream outputStream;

    public ClientSideConsumerAgent(Client client) {
        this.outputStream = System.out;
        this.client = client;
    }

    @Override
    public void run() {
        var outputStream = this.outputStream;
        CountDownLatch latch = new CountDownLatch(1);
        ByteSequence key = ByteSequence.from(CHAT_NAME.getBytes());
        Watch.Listener listener = Watch.listener(response -> {
            for (WatchEvent event : response.getEvents()) {
                var value = Optional.ofNullable(event.getKeyValue().getValue()).map(ByteSequence::toString).orElse("");
                try {
                    Message message = gson.fromJson(value, Message.class);
                    outputStream.write(message.toPrettyString().getBytes());
                } catch (JsonSyntaxException e) {
                    System.out.print("Watching Error " + e);
                    System.exit(1);
                } catch (IOException e) {
                    System.out.print("IO Error " + e);
                    System.exit(1);
                }
            }
            // latch.countDown();
        });
        try (Watch watch = client.getWatchClient();
             Watch.Watcher watcher = watch.watch(key, listener)) {
            latch.await(); // Blocking, prevent the thread to terminate.
        } catch (Exception e) {
            System.out.print("Watching Error " + e);
            System.exit(1);
        }
    }
}
