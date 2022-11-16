package it.unibo.ds.lab.consensus.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestChatClient extends BaseTest {

    @BeforeEach
    public void startEndpoints() throws IOException, InterruptedException {
        var dockerComposeUp = startProcessInDir("..", "docker-compose", "up", "-d");
        dockerComposeUp.process().waitFor();
        dockerComposeUp.printDebugInfo("docker-compose-up");
    }

    @AfterEach
    public void stopEndpoints() throws IOException, InterruptedException {
        var dockerComposeDown = startProcessInDir("..", "docker-compose", "down");
        dockerComposeDown.process().waitFor();
        dockerComposeDown.printDebugInfo("docker-compose-down");
    }

    private String endpoint(int i) {
        return "http://localhost:" + (10000 + i);
    }

    private final String[] defaultEndpoints = new String[]{endpoint(0), endpoint(1), endpoint(2)};

    private TestableProcess startClient(String name, String chat, String... endpoints) throws IOException {
        var args = Stream.concat(Stream.of(name, chat), Stream.of(endpoints)).toArray();
        return startJavaProcess(ChatClient.class, args);
    }

    @Test
    public void emptyInputStreamIsNotTransmitted() throws IOException, InterruptedException {
        try (TestableProcess client = startClient("client", "chat0", defaultEndpoints)) {
            client.stdin().close();
            assertEquals(0, client.process().waitFor());
            client.printDebugInfo("client");
            assertTrue(client.stderrAsText().isBlank());
            assertRelativeOrderOfLines(
                    client.stdoutAsText(),
                    "Contacting host(s) [http://localhost:1000",
                    "Connection established",
                    "client: exited!"
            );
        }
    }

    @Test
    public void singleMessageTransmission() throws IOException, InterruptedException {
        String chat = "chat1";
        try (TestableProcess clientMatteo = startClient("Matteo", chat, defaultEndpoints);
             TestableProcess clientGiovanni = startClient("Giovanni", chat, defaultEndpoints);
             TestableProcess clientAndrea = startClient("Andrea", chat, defaultEndpoints)) {
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
                        "Contacting host(s) [http://localhost:1000",
                        "Connection established",
                        "Matteo: Hello there!"
                );
                clientGiovanni.printDebugInfo("clientGiovanni");
                assertRelativeOrderOfLines(
                        clientGiovanni.stdoutAsText(),
                        "Contacting host(s) [http://localhost:1000",
                        "Connection established",
                        "Matteo: Hello there!",
                        "Matteo: exited!"
                );

                clientGiovanni.printDebugInfo("clientAndrea");
                assertRelativeOrderOfLines(
                        clientGiovanni.stdoutAsText(),
                        "Contacting host(s) [http://localhost:1000",
                        "Connection established",
                        "Matteo: Hello there!",
                        "Matteo: exited!",
                        "Giovanni: exited!"
                );
            }
        }
    }

    @Test
    public void notTrivialMessageExchange() throws IOException, InterruptedException {
        String chat = "chat2";
        try (TestableProcess clientMatteo = startClient("Matteo", chat, defaultEndpoints);
             TestableProcess clientGiovanni = startClient("Giovanni", chat, defaultEndpoints);
             TestableProcess clientAndrea = startClient("Andrea", chat, defaultEndpoints)) {
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
                        "Contacting host(s) [http://localhost:1000",
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
                        "Contacting host(s) [http://localhost:1000",
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
                        "Contacting host(s) [http://localhost:1000",
                        "Connection established",
                        "Matteo: Hello there!",
                        "Giovanni: I've finished my coffee capsules",
                        "Andrea: I'm leaving"
                );
            }
        }
    }
}
