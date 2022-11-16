package it.unibo.ds.lab.consensus.client;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestChatClient extends BaseTest{

    @Test
    public void emptyInputStreamIsNotTransmitted() throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("etcd");
        pr.waitFor(3, TimeUnit.SECONDS);
        String etcdServers = "http://localhost:2379";
        try (TestableProcess client = startJavaProcess(ChatClient.class, "client", "chat0", etcdServers)) {
            client.stdin().close();
            assertTrue(client.process().waitFor(3, TimeUnit.SECONDS));
            assertEquals(0, client.process().exitValue());
            client.printDebugInfo("client");
            assertTrue(client.stderrAsText().isBlank());
            assertRelativeOrderOfLines(
                    client.stdoutAsText(),
                    "Contacting host(s) [http://localhost:2379]...",
                    "Connection established",
                    "client: exited!"
            );
        }
        pr.destroy();
    }

    @Test
    public void SingleMessageTransmission() throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("etcd");
        pr.waitFor(3, TimeUnit.SECONDS);
        String etcdServers = "http://localhost:2379";
        String chat = "chat1";
        try (TestableProcess clientMatteo = startJavaProcess(ChatClient.class, "Matteo", chat, etcdServers);
             TestableProcess clientGiovanni = startJavaProcess(ChatClient.class, "Giovanni", chat, etcdServers);
             TestableProcess clientAndrea = startJavaProcess(ChatClient.class, "Andrea", chat, etcdServers)) {
            try (var clientMatteoStdin = clientMatteo.stdin()) {
                clientMatteoStdin.write("Hello there!\n");
                Thread.sleep(1000);
                clientMatteo.stdin().close();
                Thread.sleep(1000);
                assertTrue(clientMatteo.process().waitFor(3, TimeUnit.SECONDS));
                clientGiovanni.stdin().close();
                Thread.sleep(1000);
                assertTrue(clientGiovanni.process().waitFor(3, TimeUnit.SECONDS));
                clientAndrea.stdin().close();
                Thread.sleep(1000);
                assertTrue(clientAndrea.process().waitFor(3, TimeUnit.SECONDS));
                assertTrue(clientMatteo.stderrAsText().isBlank());
                assertTrue(clientGiovanni.stderrAsText().isBlank());
                assertTrue(clientAndrea.stderrAsText().isBlank());
                clientMatteo.printDebugInfo("clientMatteo");
                assertRelativeOrderOfLines(
                        clientMatteo.stdoutAsText(),
                        "Contacting host(s) [http://localhost:2379]...",
                        "Connection established",
                        "Matteo: Hello there!"
                );
                clientGiovanni.printDebugInfo("clientGiovanni");
                assertRelativeOrderOfLines(
                        clientGiovanni.stdoutAsText(),
                        "Contacting host(s) [http://localhost:2379]...",
                        "Connection established",
                        "Matteo: Hello there!",
                        "Matteo: exited!"
                );

                clientGiovanni.printDebugInfo("clientAndrea");
                assertRelativeOrderOfLines(
                        clientGiovanni.stdoutAsText(),
                        "Contacting host(s) [http://localhost:2379]...",
                        "Connection established",
                        "Matteo: Hello there!",
                        "Matteo: exited!",
                        "Giovanni: exited!"
                );
            }
        }
        pr.destroy();
    }

    @Test
    public void notTrivialMessageExchange() throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("etcd");
        pr.waitFor(3, TimeUnit.SECONDS);
        String etcdServers = "http://localhost:2379";
        String chat = "chat2";
        try (TestableProcess clientMatteo = startJavaProcess(ChatClient.class, "Matteo", chat, etcdServers);
             TestableProcess clientGiovanni = startJavaProcess(ChatClient.class, "Giovanni", chat, etcdServers);
             TestableProcess clientAndrea = startJavaProcess(ChatClient.class, "Andrea", chat, etcdServers)) {
            try (var clientMatteoStdin = clientMatteo.stdin();
                 var clientGiovanniStdin = clientGiovanni.stdin();
                 var clientAndreaStdin = clientAndrea.stdin()) {
                clientMatteoStdin.write("Hello there!\n");
                clientAndrea.process().waitFor(2, TimeUnit.SECONDS);
                clientGiovanni.process().waitFor(2, TimeUnit.SECONDS);
                clientGiovanniStdin.write("I've finished my coffee capsules\n");
                clientAndrea.process().waitFor(2, TimeUnit.SECONDS);
                clientMatteo.process().waitFor(2, TimeUnit.SECONDS);
                clientAndreaStdin.write("I'm leaving\n");
                clientMatteo.process().waitFor(2, TimeUnit.SECONDS);
                clientGiovanni.process().waitFor(2, TimeUnit.SECONDS);
                clientAndrea.stdin().close();
                assertTrue(clientAndrea.process().waitFor(5, TimeUnit.SECONDS));
                clientMatteoStdin.write("You can take one of mine\n");
                clientGiovanni.process().waitFor(2, TimeUnit.SECONDS);
                clientMatteo.stdin().close();
                assertTrue(clientMatteo.process().waitFor(5, TimeUnit.SECONDS));
                clientGiovanni.stdin().close();
                assertTrue(clientGiovanni.process().waitFor(5, TimeUnit.SECONDS));
                assertTrue(clientMatteo.stderrAsText().isBlank());
                assertTrue(clientGiovanni.stderrAsText().isBlank());
                assertTrue(clientAndrea.stderrAsText().isBlank());
                clientMatteo.printDebugInfo("clientMatteo");
                assertRelativeOrderOfLines(
                        clientMatteo.stdoutAsText(),
                        "Contacting host(s) [http://localhost:2379]...",
                        "Connection established",
                        "Matteo: Hello there!",
                        "Giovanni: I've finished my coffee capsules",
                        "Andrea: I'm leaving",
                        "Andrea: exited!",
                        "Matteo: You can take one of mine"
                );
                clientGiovanni.printDebugInfo("clientGiovanni");
                assertRelativeOrderOfLines(
                        clientGiovanni.stdoutAsText(),
                        "Contacting host(s) [http://localhost:2379]...",
                        "Connection established",
                        "Matteo: Hello there!",
                        "Giovanni: I've finished my coffee capsules",
                        "Andrea: I'm leaving",
                        "Andrea: exited!",
                        "Matteo: You can take one of mine",
                        "Matteo: exited!"
                );
                clientGiovanni.printDebugInfo("clientAndrea");
                assertRelativeOrderOfLines(
                        clientGiovanni.stdoutAsText(),
                        "Contacting host(s) [http://localhost:2379]...",
                        "Connection established",
                        "Matteo: Hello there!",
                        "Giovanni: I've finished my coffee capsules",
                        "Andrea: I'm leaving"
                );
            }
        }
        pr.destroy();
    }
}
