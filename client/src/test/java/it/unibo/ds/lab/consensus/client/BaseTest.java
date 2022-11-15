package it.unibo.ds.lab.consensus.client;

import org.opentest4j.AssertionFailedError;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseTest {

    protected final int port;

    public BaseTest() {
        port = new Random().nextInt(10000, 20000);
    }

    protected static void assertMatches(String expectedRegex, String actual) {
        if (!actual.matches(expectedRegex)) {
            throw new AssertionFailedError(
                    "expected something matching: <%s> but was: <%s>".formatted(
                            escapeBlank(expectedRegex),
                            escapeBlank(actual)
                    ),
                    expectedRegex,
                    actual
            );
        }
    }

    protected static void assertRelativeOrderOfLines(String text, String... expectedLines) {
        var actualLines = List.of(text.split("\n"));
        var linesIndexes = Stream.of(expectedLines).map(line -> {
            for (int i = 0; i < actualLines.size(); i++) {
                if (actualLines.get(i).startsWith(line)) {
                    return i;
                }
            }
            return -1;
        }).toList();

        for (int i = 0; i < linesIndexes.size() - 1; i++) {
            var currentIndex = linesIndexes.get(i + 1);
            var previousIndex = linesIndexes.get(i);
            if (previousIndex < 0) {
                throw new AssertionFailedError("line <%s> is missing in <%s>".formatted(expectedLines[i], escapeBlank(text)));
            }
            if (currentIndex < 0) {
                throw new AssertionFailedError("line <%s> is missing in <%s>".formatted(expectedLines[i + 1], escapeBlank(text)));
            }
            if (currentIndex <= previousIndex) {
                throw new AssertionFailedError(
                        "line <%s> is before <%s> in <%s>".formatted(
                                actualLines.get(currentIndex),
                                actualLines.get(previousIndex),
                                escapeBlank(text)
                        )
                );
            }
        }
    }

    private static String escapeBlank(String string) {
        return string.replace("\n", "\\n").replace("\r", "\\r");
    }

    protected TestableProcess startJavaProcess(Class<?> klass, Object... args) throws IOException {
        Stream<String> command = Stream.of(
                new File(System.getProperty("java.home") + "/bin/java").getAbsolutePath(),
                "-classpath",
                System.getProperty("java.class.path"),
                klass.getName()
        );
        Stream<String> arguments = Stream.of(args).map(Object::toString);
        var commandLine = Stream.concat(command, arguments).collect(Collectors.toList());
        var prefix = this.getClass().getName() + "-" + klass.getName() + "#" + Objects.hash(args);
        var stdOut = File.createTempFile(prefix + "-stdout", ".txt");
        stdOut.deleteOnExit();
        var stdErr = File.createTempFile(prefix + "-stderr", ".txt");
        stdErr.deleteOnExit();
        var process = new ProcessBuilder(commandLine)
                .redirectOutput(ProcessBuilder.Redirect.to(stdOut))
                .redirectError(ProcessBuilder.Redirect.to(stdErr))
                .start();
        return new TestableProcess(process, stdOut, stdErr);
    }
}
