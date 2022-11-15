package it.unibo.ds.lab.consensus.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;

public class TestChatClient {

    private static final int BUFFER_SIZE = 1024;

    private final String etcdServers = "http://localhost:2379";

    private final byte[] bufferMatteo = new byte[BUFFER_SIZE];
    private final InputStream inputMatteo = new BufferedInputStream(new ByteArrayInputStream(bufferMatteo));
    private final OutputStream outputMatteo = new ByteArrayOutputStream(BUFFER_SIZE);
    private final ChatClient clientMatteo = new ChatClient(inputMatteo, outputMatteo, "Matteo", etcdServers);

    private final byte[] bufferGiovanni = new byte[BUFFER_SIZE];
    private final InputStream inputGiovanni = new BufferedInputStream(new ByteArrayInputStream(bufferGiovanni));
    private final OutputStream outputGiovanni = new ByteArrayOutputStream(BUFFER_SIZE);
    private final ChatClient clientGiovanni = new ChatClient(inputGiovanni, outputGiovanni, "Giovanni", etcdServers);

    @Test
    public void testSingleMessageReceiving() throws IOException, InterruptedException {
        var message = "Hello there!";
        outputMatteo.write(message.getBytes());
        Thread.sleep(1000);
        var bytesFromMatteo = inputMatteo.read(bufferMatteo);
        var bytesFromGiovanni = inputGiovanni.read(bufferGiovanni);
        Assertions.assertEquals(bytesFromMatteo, bytesFromGiovanni);
        Assertions.assertArrayEquals(bufferMatteo, bufferGiovanni);
        System.out.print(Arrays.toString(bufferMatteo));
    }

}
