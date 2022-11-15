package it.unibo.ds.lab.consensus.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.common.exception.EtcdException;
import io.etcd.jetcd.watch.WatchEvent;
import it.unibo.ds.lab.consensus.presentation.GsonUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;


public class ChatClient {

    private static final Gson gson = GsonUtils.createGson();
    private static final String CHAT_NAME = "chat1";
    private static final int BUFFER_SIZE = 1024;
    private static final byte[] buffer = new byte[BUFFER_SIZE];

    public static void main(String[] args){
        try {
            String username = args[0];
            String[] servers = Arrays.copyOfRange(args, 1, args.length);
            chatter(username, servers);
        } catch (EtcdException e) {
            System.out.println("Cannot initialise chat client.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot use IO.");
            System.exit(1);
        } catch (InterruptedException e) {
            System.out.println("Client interruption.");
            System.exit(1);
        }
    }

    private static void chatter(String username, String ... servers) throws IOException, InterruptedException {
        try {
            System.out.printf("Contacting host(s) %s...\n", Arrays.toString(servers));
            Client client = Client.builder().endpoints(servers).build();
            System.out.println("Connection established");
            chatImpl(username, client);
            System.out.println("Goodbye!");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException | ExecutionException e) {
            // Silently ignores
        }
    }

    private static void chatImpl(String username, Client client) throws IOException, ExecutionException, InterruptedException {
        propagateServerToStdout(client);
        propagateStdinToServer(username, client);
    }

    private static void propagateStdinToServer(String username, Client client) throws IOException, ExecutionException, InterruptedException {
        var inputStream = System.in;
        KV kv = client.getKVClient();
        while (true) {
            int readBytes = inputStream.read(buffer);
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
    }

    private static void propagateServerToStdout(Client client) throws InterruptedException {
        var outputStream = System.out;
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
        });
        Watch watch = client.getWatchClient();
        Watch.Watcher watcher = watch.watch(key, listener);
    }
}
