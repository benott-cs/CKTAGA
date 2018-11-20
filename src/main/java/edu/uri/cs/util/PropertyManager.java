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
    public static final String CRKTAGA_CROSSOVER_PARAM_P0 = "crktaga.crossover.params.p0";
    public static final String CRKTAGA_CROSSOVER_PARAM_P1 = "crktaga.crossover.params.p1";
    public static final String CRKTAGA_CROSSOVER_PARAM_P2 = "crktaga.crossover.params.p2";
    public static final String CRKTAGA_CROSSOVER_PARAM_P3 = "crktaga.crossover.params.p3";

    public static final String CRKTAGA_MUTATION_PROB = "crktaga.mutation.prob";
    public static final String CRKTAGA_MUTATION_DOWNWARD_REFINEMENT_PROB = "crktaga.mutation.downward.refinement.prob";
    public static final String CRKTAGA_MUTATION_IGNORE_PATTERN = "crktaga.mutation.ignore.pattern";
    public static final String CRKTAGA_ATOM_MUTATION_IGNORE_PATTERN = "crktaga.mutation.atom.ignore.pattern";

    public static final String CRKTAGA_MUTATION_DOWNWARD_CONSTANT = "crktaga.mutation.downward.params.constant.sub";
    public static final String CRKTAGA_MUTATION_DOWNWARD_VARIABLE = "crktaga.mutation.downward.params.variable.sub";
    public static final String CRKTAGA_MUTATION_DOWNWARD_LITERAL_ADD = "crktaga.mutation.downward.params.literal.addition";
    public static final String CRKTAGA_MUTATION_DOWNWARD_LITERAL_ADD_POS = "crktaga.mutation.downward.params.literal.addition.positive";

    public static final String CRKTAGA_MUTATION_UPWARD_CONSTANT = "crktaga.mutation.upward.params.constant.sub";
    public static final String CRKTAGA_MUTATION_UPWARD_VARIABLE = "crktaga.mutation.downupwardward.params.variable.sub";
    public static final String CRKTAGA_MUTATION_UPWARD_LITERAL_REMOVE = "crktaga.mutation.upward.params.literal.removal";

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
