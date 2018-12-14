package edu.uri.cs.util;

import edu.uri.cs.ga.scoring.ConsumerWithEnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;
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
        runCommand(cmd, null);
    }

    public void runCommand(String command) {
        runCommand(command, null);
    }

    public void runCommand(String command, ConsumerWithEnd<String> commandLineOutputParser) {
        String[] cmd = {command};
        runCommand(cmd, commandLineOutputParser);
    }

    private void runCommand(String[] cmd, ConsumerWithEnd<String> commandLineOutputParser) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            StreamGobbler streamGobbler =
                    new StreamGobbler(p.getInputStream(),
                            Objects.isNull(commandLineOutputParser) ? new DefaultConsumerWithEnd() : commandLineOutputParser);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = p.waitFor();
            while (p.isAlive()) {
                exitCode = p.waitFor();
            }
            p.destroy();
            assert exitCode == 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private ConsumerWithEnd<String> consumer;

        public StreamGobbler(InputStream inputStream, ConsumerWithEnd<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
            consumer.finish();
        }
    }

    private class DefaultConsumerWithEnd implements ConsumerWithEnd<String> {

        public DefaultConsumerWithEnd() {

        }


        @Override
        public void accept(String s) {
            System.out.println(s);
        }

        @Override
        public Consumer<String> andThen(Consumer<? super String> after) {
            return null;
        }

        @Override
        public void finish() {
        }
    }
}
