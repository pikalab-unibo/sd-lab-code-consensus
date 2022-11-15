package it.unibo.ds.lab.consensus.client;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.common.exception.EtcdException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ChatClient {

    public ChatClient(InputStream inputStream, OutputStream outputStream, String username, String ... servers){
        try {
            chatter(inputStream, outputStream, username, servers);
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

    private static void chatter(InputStream inputStream, OutputStream outputStream, String username, String ... servers) throws IOException, InterruptedException {
        try {
            Client client = Client.builder().endpoints(servers).build();
            // Read messages form servers
            var reader = new ClientSideConsumerAgent(outputStream, client);
            reader.start();
            // Write messages to servers
            var writer = new ConsoleConsumerAgent(inputStream, username, client);
            writer.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
