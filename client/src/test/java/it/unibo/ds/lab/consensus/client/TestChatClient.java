package it.unibo.ds.lab.consensus.client;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestChatClient extends BaseTest{

    @Test
    public void emptyInputStreamIsEchoedAsWell() throws IOException, InterruptedException {
        String etcdServers = "http://localhost:2379";
        try (TestableProcess client = startJavaProcess(ChatClient.class, "Matteo", etcdServers)) {
            client.stdin().close();
            assertTrue(client.process().waitFor(3, TimeUnit.SECONDS));
            assertEquals(0, client.process().exitValue());
            client.printDebugInfo("client");
            assertTrue(client.stderrAsText().isBlank());
            assertRelativeOrderOfLines(
                    client.stdoutAsText(),
                    "Contacting host(s) [http://localhost:2379]...",
                    "Connection established",
                    "Reached end of input",
                    "Goodbye!"
            );
        }
    }

}
