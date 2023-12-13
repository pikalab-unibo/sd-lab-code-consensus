package it.unibo.ds.lab.consensus.client;

import com.google.gson.Gson;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.common.exception.EtcdException;
import it.unibo.ds.lab.consensus.presentation.GsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;


public class ChatClient {

    private static final Gson gson = GsonUtils.createGson();
    private static final int BUFFER_SIZE = 1024;
    private static final byte[] buffer = new byte[BUFFER_SIZE];

    public static void main(String[] args){
        try {
            String username = args[0];
            String chat = args[1];
            String[] servers = Arrays.copyOfRange(args, 2, args.length);
            chatter(username, chat, servers);
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

    private static void chatter(String username, String chat, String ... servers) throws IOException, InterruptedException {
        try {
            System.out.printf("Contacting host(s) %s...\n", Arrays.toString(servers));
            Client client = Client.builder().endpoints(servers).build();
            System.out.println("Connection established");
            CountDownLatch latch = new CountDownLatch(1);
            chatImpl(username, chat, client, latch);
            latch.await();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException | ExecutionException e) {
            // Silently ignores
        }
    }

    private static void chatImpl(String username, String chat, Client client, CountDownLatch latch) throws IOException, ExecutionException, InterruptedException {
        propagateServerToStdout(username, chat, client, latch);
        propagateStdinToServer(username, chat, client);
    }

    private static void propagateStdinToServer(String username, String chat, Client client) throws IOException, ExecutionException, InterruptedException {
        InputStream inputStream = System.in;
        KV kv = client.getKVClient();
        var key = ByteSequence.from(chat.getBytes());
        while (true) {
            // TODO: put the message as a new value on the kv store
        }
    }

    private static void propagateServerToStdout(String username, String chat, Client client, CountDownLatch latch) {
        OutputStream outputStream = System.out;
        ByteSequence key = ByteSequence.from(chat.getBytes());
        Watch.Listener listener = Watch.listener(response -> {
            // TODO: implement the callback in such a way to output the message in the output stream
        });
        Watch watch = client.getWatchClient();
        watch.watch(key, listener);
        System.out.println("Listening to new messages on chat \"" + chat + "\"");
    }
}
