package edu.uri.cs.hypothesis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rits.cloning.Cloner;
import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.parse.Language;
import edu.uri.cs.parse.PrologLanguageParser;
import edu.uri.cs.util.FileReaderUtils;
import edu.uri.cs.util.PropertyManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    private int numberOfGenerations;
    private double eliteSurvivalRate = 0.0;
    private String hypothesisOutputDirectory;

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
        numberOfGenerations = propertyManager.getPropAsInt(PropertyManager.CRKTAGA_NUMBER_OF_GENERATIONS);
        hypothesisOutputDirectory = propertyManager.getProperty(PropertyManager.CRKTAGA_HYPOTHESES_OUTPUT_DIRECTORY);
        File directory = new File(hypothesisOutputDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        eliteSurvivalRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_ELITE_SURVIVAL_RATE);
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
    }

    public void runGA() {
        writeHypothesesToFiles(0);
        int numberOfEliteHypotheses = (int)Math.floor(hypotheses.size() * eliteSurvivalRate);
        if (eliteSurvivalRate > 0 && numberOfEliteHypotheses <= 0) {
            numberOfEliteHypotheses = 1;
        }
        for (int i = 1; i < numberOfGenerations; i++) {
            List<Hypothesis> nextGenHypotheses = new ArrayList<>();
            addEliteMembersToNextGen(numberOfEliteHypotheses, nextGenHypotheses);
            while (nextGenHypotheses.size() < hypotheses.size()) {
                List<Hypothesis> twoChildrenFromSelectionCrossoverAndMutation =
                        getOneSetOfChildren();
                if (nextGenHypotheses.size() - hypotheses.size() >= 2) {
                    nextGenHypotheses.addAll(twoChildrenFromSelectionCrossoverAndMutation);
                } else {
                    nextGenHypotheses.add(twoChildrenFromSelectionCrossoverAndMutation.get(0));
                }
            }
            hypotheses = nextGenHypotheses;
            writeHypothesesToFiles(i);
        }
    }

    private List<Hypothesis> getOneSetOfChildren() {
        return null;
    }

    private void addEliteMembersToNextGen(int numberOfEliteHypotheses, List<Hypothesis> nextGen) {
        hypotheses.sort((Comparator.comparing(Hypothesis::getScore)
                .reversed()));
        for (int i = 0; i < numberOfEliteHypotheses; i++) {
            Cloner cloner=new Cloner();
            Hypothesis clone=cloner.deepClone(hypotheses.get(i));
            clone.setElite(true);
            nextGen.add(clone);
        }
    }

    private void writeHypothesesToFiles(int generation) {
        String outputDir = hypothesisOutputDirectory + "/GEN_" + generation;
        File directory = new File(outputDir);
        if (!directory.exists()) {
            directory.mkdir();
        }
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.INDENT_OUTPUT, true);
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        int i = 0;
        for (Hypothesis h : hypotheses) {
            try {
                om.writeValue(new File(outputDir + "/hypothesis_" + i + ".json"), h);
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
