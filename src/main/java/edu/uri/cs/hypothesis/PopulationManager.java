package edu.uri.cs.hypothesis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.igormaznitsa.prologparser.terms.PrologStructure;
import com.rits.cloning.Cloner;
import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.parse.Language;
import edu.uri.cs.parse.PrologLanguageParser;
import edu.uri.cs.tree.OrTree;
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
    private List<Double> crossOverProbList = new ArrayList<>();
    private String hypothesisOutputDirectory;
    private Cloner cloner=new Cloner();

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

        double tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_CROSSOVER_PARAM_P0);
        double sum = tempRate;
        crossOverProbList.add(sum);
        tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_CROSSOVER_PARAM_P1);
        sum += tempRate;
        crossOverProbList.add(sum);
        tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_CROSSOVER_PARAM_P2);
        sum += tempRate;
        crossOverProbList.add(sum);
        tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_CROSSOVER_PARAM_P3);
        sum += tempRate;
        crossOverProbList.add(sum);

        assert sum == 1.0 : "Invalid crossover parameters - must sum to one";

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
            hypotheses.sort((Comparator.comparing(Hypothesis::getScore)
                    .reversed()));
            List<Double> partialSumsForSelection = new ArrayList<>();
            double totalFitness = 0.0;
            for (Hypothesis h : hypotheses) {
                totalFitness += h.getScore();
                partialSumsForSelection.add(totalFitness);
            }
            addEliteMembersToNextGen(numberOfEliteHypotheses, nextGenHypotheses);
            while (nextGenHypotheses.size() < hypotheses.size()) {
                List<Hypothesis> twoChildrenFromSelectionCrossoverAndMutation =
                        getOneSetOfChildren(totalFitness, partialSumsForSelection);
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

    private List<Hypothesis> getOneSetOfChildren(double totalFitness, List<Double> partialSumsForSelection) {
        int index1 = getIndexOfLeastExceedingNumber(Math.random() * totalFitness, partialSumsForSelection);
        int index2 = getIndexOfLeastExceedingNumber(Math.random() * totalFitness, partialSumsForSelection);;
        // make sure we have two different parents
        while (index2 == index1) {
            index2 = getIndexOfLeastExceedingNumber(Math.random() * totalFitness, partialSumsForSelection);
        }
        Hypothesis parent1 = hypotheses.get(index1);
        Hypothesis parent2 = hypotheses.get(index2);
        List<Hypothesis> children = performCrossover(parent1, parent2);
        return children;
    }

    private void mutateChildren(List<Hypothesis> children) {

    }

    private List<Hypothesis> performCrossover(Hypothesis parent1, Hypothesis parent2) {
        List<Hypothesis> children = new ArrayList<>();
        CrossoverType crossoverType =
                CrossoverType.from(getIndexOfLeastExceedingNumber(Math.random(), crossOverProbList));
        Hypothesis child1 = cloner.deepClone(parent1);
        Hypothesis child2 = cloner.deepClone(parent2);
        switch (crossoverType) {
            case RULE_SWAP:
                PrologStructure randomRuleKey1 = child1.getRandomRule();
                OrTree rule1 = child1.removeRule(randomRuleKey1);
                PrologStructure randomRuleKey2 = child2.getRandomRule();
                OrTree rule2 = child2.removeRule(randomRuleKey2);
                child1.addRule(randomRuleKey2, rule2);
                child2.addRule(randomRuleKey1, rule1);
                break;
            case OR_SUBTREE_NODE_SWAP:
            case AND_SUBTREE_NODE_SWAP:
            case SURVIVAL:
            default:
                break;
        }
        children.add(child1);
        children.add(child2);
        return children;
    }

    private int getIndexOfLeastExceedingNumber(double number, List<Double> listToCheck) {
        for (int i = 0; i < listToCheck.size(); i++) {
            if (listToCheck.get(i) >= number) {
                return i;
            }
        }
        return listToCheck.size() - 1;
    }

    private void addEliteMembersToNextGen(int numberOfEliteHypotheses, List<Hypothesis> nextGen) {
        for (int i = 0; i < numberOfEliteHypotheses; i++) {
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
