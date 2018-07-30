package edu.uri.cs.hypothesis;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.parse.Language;
import edu.uri.cs.parse.PrologLanguageParser;
import edu.uri.cs.util.FileReaderUtils;
import edu.uri.cs.util.PropertyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ben on 7/26/18.
 */
public class PopulationManager {

    private String backgroundFile = null;
    private PrologLanguageParser backgroundParser = null;
    private Language backgroundLanguage = null;
    private List<Hypothesis> hypotheses = new ArrayList<>();
    private boolean initialized = false;
    private PropertyManager propertyManager;
    private List<String> positiveExamples = new ArrayList<>();
    private List<String> negativeExamples = new ArrayList<>();
    private HypothesisFactory hypothesisFactory;
    private HypothesisScorerIF hypothesisScorerIF;
    private int populationSize = 0;

    public PopulationManager(String backgroundFile, PropertyManager propertyManager) {
        this.backgroundFile = backgroundFile;
        this.propertyManager = propertyManager;
        hypothesisFactory = new HypothesisFactory(propertyManager);
        hypothesisScorerIF = new RandomScorer();
    }

    public synchronized void initialize() {
        FileReaderUtils.readInStringLinesFromFile(propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_POSITIVE_EXAMPLE_FILE),
                positiveExamples);
        FileReaderUtils.readInStringLinesFromFile(propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_NEGATIVE_EXAMPLE_FILE),
                negativeExamples);
        backgroundParser = new PrologLanguageParser(backgroundFile);
        backgroundLanguage = backgroundParser.retrieveLanguage(false);
        initialized = true;
    }

    public void createInitialPopulation(int populationSize) {
        // attempt to add population size unique hypotheses to the list. if we don'e find populationSize
        // hypotheses within 2*populationSize attempts, we just work with the ones we have (the
        // solution space may not be complex enough to find that many variants)
        for (int i = 0; i < populationSize || hypotheses.size() < populationSize || i > 2*populationSize; i++) {
            // Create a hypothesis using Aleph. Add it to the hypothesis manager.
            String hypothesisFileName = hypothesisFactory.createHypothesis(positiveExamples, negativeExamples, i);
            readHypothesisFromFile(hypothesisFileName);
        }
        populationSize = hypotheses.size();
    }

    public synchronized void readHypothesisFromFile(String hypothesisFile) {
        if (!initialized) {
            initialize();
        }
        if (!hypotheses.contains(hypothesisFile)) {
            Hypothesis h = new Hypothesis(backgroundLanguage, hypothesisFile);
            h.initialize();
            hypotheses.add(h);
            hypothesisScorerIF.computeScore(h);
        } else {
            System.out.println(hypothesisFile + " has already been processed");
        }
    }

    public synchronized void dumpHypothesisLanguages() {
        for (Hypothesis h : hypotheses) {
            h.printHypothesisLanguage();
        }
    }
}
