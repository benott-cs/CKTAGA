package edu.uri.cs.util;

import edu.uri.cs.ga.scoring.ScoringType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by Ben on 7/26/18.
 */
public class PropertyManager {

    public static final String CRKTAGA_NO_GA_READ_PREV_BEST = "crktaga.do.not.run.ga.read.in.prev.best";
    public static final String CRKTAGA_PREV_BEST_FILE = "crktaga.path.to.prev.best";
    public static final String CRKTAGA_PATH_TO_LAST_GEN = "crktaga.path.to.last.gen";
    public static final String CRKTAGA_SVM_C_VALUES = "crktaga.svm.c.values";
    public static final String CRKTAGA_SVM_DEGREE_VALUES = "crktaga.kernel.parameter.degrees";
    public static final String CRKTAGA_SVM_GAMMA_VALUES = "crktaga.kernel.parameter.gammas";
    public static final String CRKTAGA_SVM_COEFF_VALUES = "crktaga.kernel.parameter.coef0s";
    public static final String CRKTAGA_CREATE_ENSEMBLE = "crktaga.create.ensemble";
    public static final String CRKTAGA_NAIVE_ENSEMBLE = "crktaga.naive.ensemble";
    public static final String CRKTAGA_PENALIZE_INITIAL_SCORE = "crktaga.penalize.initial.score";
    public static final String CRKTAGA_NUM_ENSEMBLE_CANDIDATES = "crktaga.num.ensemble.candidates";
    public static final String CRKTAGA_NUM_ENSEMBLE_MEMBERS = "crktaga.num.ensemble.member";
    public static final String CRKTAGA_DIVERSITY_ENCOURAGEMENT_FACTOR = "crktaga.diversity.encouragement";

    public static final String CRKTAGA_BACKGROUND_FILE = "crktaga.background.file";
    public static final String ALEPH_BACKGROUND_FILE = "aleph.background.file";
    public static final String CRKTAGA_NUMBER_OF_GENERATIONS = "crktaga.number.of.generations";
    public static final String CRKTAGA_HYPOTHESES_OUTPUT_DIRECTORY = "crktaga.generation.output";
    public static final String CRKTAGA_ELITE_SURVIVAL_RATE = "crktaga.elite.survival.rate";
    public static final String CRKTAGA_CROSSOVER_PARAM_P0 = "crktaga.crossover.params.p0";
    public static final String CRKTAGA_CROSSOVER_PARAM_P1 = "crktaga.crossover.params.p1";
    public static final String CRKTAGA_CROSSOVER_PARAM_P2 = "crktaga.crossover.params.p2";
    public static final String CRKTAGA_CROSSOVER_PARAM_P3 = "crktaga.crossover.params.p3";

    public static final String CRKTAGA_WEIGHTED_ACCURACY = "crktaga.weighted.accuracy";
    public static final String CRKTAGA_DIVERSITY_BOOST = "crktaga.diversity.boost";
    public static final String CRKTAGA_MUTATION_PROB = "crktaga.mutation.prob";
    public static final String CRKTAGA_MUTATION_DOWNWARD_REFINEMENT_PROB = "crktaga.mutation.downward.refinement.prob";
    public static final String CRKTAGA_MUTATION_IGNORE_PATTERN = "crktaga.mutation.ignore.pattern";
    public static final String CRKTAGA_ATOM_MUTATION_IGNORE_PATTERN = "crktaga.mutation.atom.ignore.pattern";

    public static final String CRKTAGA_MUTATION_DOWNWARD_CONSTANT = "crktaga.mutation.downward.params.constant.sub";
    public static final String CRKTAGA_MUTATION_DOWNWARD_VARIABLE = "crktaga.mutation.downward.params.variable.sub";
    public static final String CRKTAGA_MUTATION_DOWNWARD_LITERAL_ADD = "crktaga.mutation.downward.params.literal.addition";
    public static final String CRKTAGA_MUTATION_DOWNWARD_LITERAL_ADD_POS = "crktaga.mutation.downward.params.literal.addition.positive";

    public static final String CRKTAGA_MUTATION_UPWARD_CONSTANT = "crktaga.mutation.upward.params.constant.sub";
    public static final String CRKTAGA_MUTATION_UPWARD_VARIABLE = "crktaga.mutation.upward.params.variable.sub";
    public static final String CRKTAGA_MUTATION_UPWARD_LITERAL_REMOVE = "crktaga.mutation.upward.params.literal.removal";

    public static final String ALEPH_HYPOTHESIS_OUTPUT_INITIAL_STRING = "aleph.hypothesis.output.initial.string";
    public static final String ALEPH_HYPOTHESIS_NEGATIVE_EXAMPLE_FILE = "aleph.hypothesis.negative.example.file";
    public static final String ALEPH_HYPOTHESIS_POSITIVE_EXAMPLE_FILE = "aleph.hypothesis.positive.example.file";
    public static final String CRKTAGA_POPULATION_SIZE = "crktaga.population.size";
    public static final String ALEPH_LOCATION = "aleph.location";
    public static final String YAP_LOCATION = "yap.location";
    public static final String PERL_LOCATION = "perl.location";

    public static final String SCORING_TYPE = "crktaga.scoring.type";

    public static final String KERNEL_TYPE = "crktaga.kernel.parameter.type";
    public static final String KERNEL_DEGREE = "crktaga.kernel.parameter.degree";
    public static final String KERNEL_COEFFICIENT = "crktaga.kernel.parameter.coef0";
    public static final String KERNEL_GAMMA = "crktaga.kernel.parameter.gamma";

    public static final String CRKTAGA_TEST_DATA_FILE = "cktaga.test.data.file";
    public static final String CRKTAGA_TEST_DATA_POS_TOKEN = "cktaga.test.data.pos.token";
    public static final String CRKTAGA_TEST_DATA_NEG_TOKEN = "cktaga.test.data.neg.token";

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

    public boolean getPropAsBoolean(String propertyName) {
        return Boolean.parseBoolean(getProperty(propertyName).toLowerCase());
    }

    public ScoringType getScoringType() {
        return ScoringType.from(Integer.valueOf(getProperty(SCORING_TYPE)));
    }

    private List<Integer> getIntListForProperty(String property) {
        String[] stringCs = getProperty(property).split(",");
        List<Integer> ret;
        if (stringCs.length != 0) {
            ret = Arrays.stream(stringCs).map(s -> Integer.valueOf(s)).collect(Collectors.toList());
        } else {
            ret = new ArrayList<>();
        }
        return ret;
    }

    private List<Double> getDoubleListForProperty(String property) {
        String[] stringCs = getProperty(property).split(",");
        List<Double> ret;
        if (stringCs.length != 0) {
            ret = Arrays.stream(stringCs).map(s -> Double.valueOf(s)).collect(Collectors.toList());
        } else {
            ret = new ArrayList<>();
        }
        return ret;
    }

    public List<Double> getSVMCValues() {
        List<Double> ret = getDoubleListForProperty(CRKTAGA_SVM_C_VALUES);
        if (ret.isEmpty()) {
            ret.add(1.0);
        }
        return ret;
    }

    public List<Integer> getSVMDegreeValues() {
        List<Integer> ret = getIntListForProperty(CRKTAGA_SVM_DEGREE_VALUES);
        if (ret.isEmpty()) {
            ret.add(1);
        }
        return ret;
    }

    public List<Double> getSVMGammaValues() {
        List<Double> ret = getDoubleListForProperty(CRKTAGA_SVM_GAMMA_VALUES);
        if (ret.isEmpty()) {
            ret.add(1.0);
        }
        return ret;
    }

    public List<Double> getSVMCoeffValues() {
        List<Double> ret = getDoubleListForProperty(CRKTAGA_SVM_COEFF_VALUES);
        if (ret.isEmpty()) {
            ret.add(1.0);
        }
        return ret;
    }
}
