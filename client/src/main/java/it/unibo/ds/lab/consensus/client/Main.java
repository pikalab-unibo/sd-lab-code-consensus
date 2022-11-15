package it.unibo.ds.lab.consensus.client;

import java.io.ByteArrayOutputStream;

public class Main {

    public static void main(String...args){

        String[] servers = new String[]{"http://172.18.0.101:2379", "http://172.18.0.102:2379", "http://172.18.0.103:2379"};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        ChatClient client = new ChatClient(System.in, outputStream, "Matteo", servers);
    }

}
