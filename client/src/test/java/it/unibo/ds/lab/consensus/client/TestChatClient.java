package it.unibo.ds.lab.consensus.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestChatClient extends BaseTest {

    @BeforeEach
    public void startEndpoints() throws IOException, InterruptedException {
        try (var dockerComposeUp = startProcessInDir("..", "docker-compose", "up", "-d")) {
            dockerComposeUp.process().waitFor();
            dockerComposeUp.printDebugInfo("docker-compose-up");
        }
    }

    @AfterEach
    public void stopEndpoints() throws IOException, InterruptedException {
        try (var dockerComposeDown = startProcessInDir("..", "docker-compose", "down")) {
            dockerComposeDown.process().waitFor();
            dockerComposeDown.printDebugInfo("docker-compose-down");
        }
    }

    private String endpoint(int i) {
        return "http://localhost:" + (10000 + i);
    }

    private final String[] defaultEndpoints = new String[]{endpoint(0), endpoint(1), endpoint(2)};

    private TestableProcess startClient(String name, String chat, String... endpoints) throws IOException {
        var args = Stream.concat(Stream.of(name, chat), Stream.of(endpoints)).toArray();
        return startJavaProcess(ChatClient.class, args);
    }

    private void awaitClientsAreConnected(TestableProcess... clients) {
        Arrays.stream(clients).forEach(c -> c.awaitOutputContains("Connection established"));
    }

    private void awaitClientsAreListening(TestableProcess... clients) {
        Arrays.stream(clients).forEach(c -> c.awaitOutputContains("Listening to new messages on chat"));
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
        try (TestableProcess matteo = startClient("Matteo", chat, defaultEndpoints);
             TestableProcess giovanni = startClient("Giovanni", chat, defaultEndpoints);
             TestableProcess andrea = startClient("Andrea", chat, defaultEndpoints)
        ) {
            awaitClientsAreConnected(matteo, giovanni, andrea);
            awaitClientsAreListening(matteo, giovanni, andrea);
            matteo.feedStdin("Hello there!\n");
            matteo.stdin().close();
            assertEquals(0, matteo.process().waitFor());

            giovanni.awaitOutputContains("Matteo: Hello there!");
            giovanni.stdin().close();
            assertEquals(0, giovanni.process().waitFor());

            andrea.awaitOutputContains("Matteo: Hello there!");
            andrea.stdin().close();
            assertEquals(0, andrea.process().waitFor());

            matteo.printDebugInfo("matteo");
            giovanni.printDebugInfo("giovanni");
            andrea.printDebugInfo("andrea");
            assertTrue(matteo.stderrAsText().isBlank());
            assertTrue(giovanni.stderrAsText().isBlank());
            assertTrue(andrea.stderrAsText().isBlank());

            assertRelativeOrderOfLines(
                    matteo.stdoutAsText(),
                    "Contacting host(s) [http://localhost:1000",
                    "Connection established",
                    "Matteo: Hello there!"
            );
            assertRelativeOrderOfLines(
                    giovanni.stdoutAsText(),
                    "Contacting host(s) [http://localhost:1000",
                    "Connection established",
                    "Matteo: Hello there!",
                    "Matteo: exited!"
            );
            assertRelativeOrderOfLines(
                    andrea.stdoutAsText(),
                    "Contacting host(s) [http://localhost:1000",
                    "Connection established",
                    "Matteo: Hello there!",
                    "Matteo: exited!",
                    "Giovanni: exited!"
            );
        }
    }

    @Test
    public void allClientsSeeMessagesInTheSameOrder() throws IOException, InterruptedException {
        String chat = "chat2";
        try (TestableProcess matteo = startClient("Matteo", chat, defaultEndpoints);
             TestableProcess giovanni = startClient("Giovanni", chat, defaultEndpoints);
             TestableProcess andrea = startClient("Andrea", chat, defaultEndpoints)
        ) {
            awaitClientsAreConnected(matteo, giovanni, andrea);
            awaitClientsAreListening(matteo, giovanni, andrea);
            matteo.feedStdin("Hello there!\n");
            matteo.awaitOutputContains("Matteo: Hello there!");

            giovanni.awaitOutputContains("Matteo: Hello there!");
            giovanni.feedStdin("Hi there...\n");
            giovanni.awaitOutputContains("Giovanni: Hi there...");

            andrea.awaitOutputContains("Giovanni: Hi there...");
            andrea.feedStdin("Hello guys.\n");
            andrea.awaitOutputContains("Andrea: Hello guys.");

            matteo.awaitOutputContains("Andrea: Hello guys.");
            matteo.stdin().close();
            giovanni.awaitOutputContains("Matteo: exited!");
            giovanni.stdin().close();
            andrea.awaitOutputContains("Giovanni: exited!");
            andrea.stdin().close();

            assertEquals(0, matteo.process().waitFor());
            assertEquals(0, giovanni.process().waitFor());
            assertEquals(0, andrea.process().waitFor());
            matteo.printDebugInfo("matteo");
            giovanni.printDebugInfo("giovanni");
            andrea.printDebugInfo("andrea");
            assertTrue(matteo.stderrAsText().isBlank());
            assertTrue(giovanni.stderrAsText().isBlank());
            assertTrue(andrea.stderrAsText().isBlank());

            for (var client : List.of(matteo, giovanni, andrea)) {
                assertRelativeOrderOfLines(
                        client.stdoutAsText(),
                        "Contacting host(s) [http://localhost:1000",
                        "Connection established",
                        "Matteo: Hello there!",
                        "Giovanni: Hi there...",
                        "Andrea: Hello guys."
                );
            }
        }
    }
}
