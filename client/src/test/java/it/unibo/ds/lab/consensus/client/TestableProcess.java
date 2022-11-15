package it.unibo.ds.lab.consensus.client;

import java.io.*;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public record TestableProcess(Process process, File stdout, File stderr) implements AutoCloseable {

    private <R> R readAll(File file, Collector<? super String, ?, R> f) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedWriter stdin() {
        return process.outputWriter();
    }

    public String stdoutAsText() {
        return readAll(stdout, Collectors.joining("\n"));
    }

    public String stderrAsText() {
        return readAll(stderr, Collectors.joining("\n"));
    }

    public List<String> stdoutAsLines() {
        return readAll(stdout, Collectors.toList());
    }

    public List<String> stderrAsLines() {
        return readAll(stderr, Collectors.toList());
    }

    @Override
    public void close() {
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        if (stdout.exists()) {
            stdout.delete();
        }
        if (stderr.exists()) {
            stderr.delete();
        }
    }

    public void printDebugInfo(String processName) {
        System.out.printf("Stdout of `%s`:\n> ", process.info().commandLine().orElse(processName));
        System.out.println(stdoutAsText().replace("\n", "\n> "));
        System.out.print("stderr of the same process:\n> ");
        System.out.println(stderrAsText().replace("\n", "\n> "));
        System.out.println();
    }
}
