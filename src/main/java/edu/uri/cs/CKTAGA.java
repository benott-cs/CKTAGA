package edu.uri.cs;

import edu.uri.cs.ga.Logenpro;
import edu.uri.cs.util.PropertyManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ben on 7/24/18.
 */
@Slf4j
public class CKTAGA {

    private static final String APPLICATION_PROPERTIES_LOCATION = "p";

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(APPLICATION_PROPERTIES_LOCATION, true, "application properties file location");
        Option help = new Option( "help", "print this message" );
        options.addOption(help);
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cmd.hasOption("help")) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "CKTAGA", options );
            System.exit(0);
        }
        URL appPropsFileURL = CKTAGA.class.getClassLoader().getResource("application.properties");
        if (cmd.hasOption(APPLICATION_PROPERTIES_LOCATION)) {
            // automatically generate the help statement
            File appPropFile = new File(cmd.getOptionValue( APPLICATION_PROPERTIES_LOCATION ));
            try {
                appPropsFileURL = appPropFile.toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        System.out.println("Using application.properties URL of " + appPropsFileURL.getPath());
        log.debug("Using application.properties URL of " + appPropsFileURL.getPath());
        PropertyManager propertyManager = new PropertyManager(appPropsFileURL);
        propertyManager.loadProperties();
        long timeBeforeStarting = System.currentTimeMillis();
        Logenpro logenpro = new Logenpro(propertyManager);
        logenpro.initialize();
        logenpro.evolve();
        long timeDelta = System.currentTimeMillis() - timeBeforeStarting;
        log.debug("Run took {} ms", timeDelta);
        System.exit(0);
    }
}
