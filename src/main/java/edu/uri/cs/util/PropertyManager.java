package edu.uri.cs.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Created by Ben on 7/26/18.
 */
public class PropertyManager {

    public static final String CRKTAGA_BACKGROUND_FILE = "crktaga.background.file";
    public static final String ALEPH_BACKGROUND_FILE = "aleph.background.file";
    public static final String CRKTAGA_NUMBER_OF_GENERATIONS = "crktaga.number.of.generations";
    public static final String CRKTAGA_HYPOTHESES_OUTPUT_DIRECTORY = "crktaga.generation.output";
    public static final String CRKTAGA_ELITE_SURVIVAL_RATE = "crktaga.elite.survival.rate";
    public static final String ALEPH_HYPOTHESIS_OUTPUT_INITIAL_STRING = "aleph.hypothesis.output.initial.string";
    public static final String ALEPH_HYPOTHESIS_NEGATIVE_EXAMPLE_FILE = "aleph.hypothesis.negative.example.file";
    public static final String ALEPH_HYPOTHESIS_POSITIVE_EXAMPLE_FILE = "aleph.hypothesis.positive.example.file";
    public static final String CRKTAGA_POPULATION_SIZE = "crktaga.population.size";
    public static final String ALEPH_LOCATION = "aleph.location";
    public static final String YAP_LOCATION = "yap.location";
    public static final String PERL_LOCATION = "perl.location";

    private Properties properties;
    private URL propertyFileURL;

    public PropertyManager(URL propertyFileURL) {
        this.propertyFileURL = propertyFileURL;
        this.properties = new Properties();
    }

    public void loadProperties() {
        InputStream input = null;
        try {
            input = new FileInputStream(propertyFileURL.getFile());
            // load a properties file
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    public int getPropAsInt(String propertyName) {
        return Integer.valueOf(getProperty(propertyName));
    }

    public double getPropAsDouble(String propertyName) {
        return Double.valueOf(getProperty(propertyName));
    }
}
