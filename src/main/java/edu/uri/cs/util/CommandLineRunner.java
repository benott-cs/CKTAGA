package edu.uri.cs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Ben on 7/27/18.
 */
public class CommandLineRunner {

    private PropertyManager propertyManager;
    private String perlLocation;

    public CommandLineRunner(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
        this.perlLocation = propertyManager.getProperty(PropertyManager.PERL_LOCATION);
    }

    public void runPerlCommand(String perlScriptToRun) {
        String[] cmd = {perlLocation, perlScriptToRun};
        runCommand(cmd);
    }

    public void runCommand(String command) {
        String[] cmd = {command};
        runCommand(cmd);
    }

    private void runCommand(String[] cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            StreamGobbler streamGobbler =
                    new StreamGobbler(p.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = p.waitFor();
            assert exitCode == 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
}