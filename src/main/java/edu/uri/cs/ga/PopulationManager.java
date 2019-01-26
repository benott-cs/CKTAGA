package edu.uri.cs.ga;

import com.igormaznitsa.prologparser.terms.PrologStructure;
import com.rits.cloning.Cloner;
import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.classifier.SVM;
import edu.uri.cs.ga.scoring.*;
import edu.uri.cs.ga.scoring.kernel.KTACalculatorIF;
import edu.uri.cs.ga.scoring.kernel.KernelHelper;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.parse.Language;
import edu.uri.cs.parse.PrologLanguageParser;
import edu.uri.cs.tree.AndTree;
import edu.uri.cs.tree.OrTree;
import edu.uri.cs.util.FileReaderUtils;
import edu.uri.cs.util.PropertyManager;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

/**
 * Created by Ben on 7/26/18.
 */
@Slf4j
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
    private Cloner cloner = new Cloner();
    private Random rand = new Random();
    private MutationHandler mutationHandler;
    private static int currentGeneration = 0;
    private Map<Integer, ScoresAndTotal> relScores = new HashMap<>();
    private HypothesisScorerFactory hypothesisScorerFactory = new HypothesisScorerFactory();
    private boolean diversityBoost = false;

    public PopulationManager(String backgroundFile, PropertyManager propertyManager) {
        this.backgroundFile = backgroundFile;
        this.propertyManager = propertyManager;
        hypothesisFactory = new HypothesisFactory(propertyManager);
        KernelHelper kernelHelper = new KernelHelper(propertyManager);
        boolean weightedAccuracy =
                propertyManager.getPropAsBoolean(PropertyManager.CRKTAGA_WEIGHTED_ACCURACY);
        hypothesisScorerIF = hypothesisScorerFactory.getHypothesisScorer(hypothesisFactory, kernelHelper, weightedAccuracy,
                propertyManager.getScoringType());
        mutationHandler = new MutationHandler(propertyManager);
    }

    public synchronized void initialize() {
        FileReaderUtils.readInStringLinesFromFile(propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_POSITIVE_EXAMPLE_FILE),
                positiveExamples);
        FileReaderUtils.readInStringLinesFromFile(propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_NEGATIVE_EXAMPLE_FILE),
                negativeExamples);
        numberOfGenerations = propertyManager.getPropAsInt(PropertyManager.CRKTAGA_NUMBER_OF_GENERATIONS);
        hypothesisOutputDirectory = propertyManager.getProperty(PropertyManager.CRKTAGA_HYPOTHESES_OUTPUT_DIRECTORY);
        diversityBoost = propertyManager.getPropAsBoolean(PropertyManager.CRKTAGA_DIVERSITY_BOOST);
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

        mutationHandler.initialize();

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
        scoreHypotheses();
    }

    public void runGA() {
        int numberOfEliteHypotheses = (int)Math.floor(hypotheses.size() * eliteSurvivalRate);
        if (eliteSurvivalRate > 0 && numberOfEliteHypotheses <= 0) {
            numberOfEliteHypotheses = 1;
        }
        printBestHypothesis("BEGIN");
        for (int i = 1; i < numberOfGenerations; i++) {
            relScores.clear();
            currentGeneration = i;
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
            scoreHypotheses();
        }
        Hypothesis bestHypothesis = printBestHypothesis("END");
        evaluateBestHypothesis(bestHypothesis);
    }

    private void evaluateBestHypothesis(Hypothesis bestHypothesis) {
        String outputDir = hypothesisOutputDirectory + "/GEN_" + currentGeneration;
        KTACalculatorIF centeredKTAScorer = centeredKTAScorer(hypothesisScorerIF);
        if (Objects.nonNull(centeredKTAScorer)) {
            SVM svm = createClassifierForHypothesis(bestHypothesis);
            log.debug("SVM training accuracy of best aligned solution is {}",
                    svm.getAccuracyOnProvidedSample(svm.getSvm_problem()));
            FeaturesAndTargets testFeatures =
                    centeredKTAScorer.createFeatureVectorsForTestData(bestHypothesis, outputDir, propertyManager);
            svm_problem prob = svm.createProblem(testFeatures);
            log.debug("SVM test accuracy of best aligned solution is {}",
                    svm.getAccuracyOnProvidedSample(svm.getSvm_problem()));
        }

        hypothesisScorerIF = new AlephAccuracyScorer(hypothesisFactory, false);

        double accuracy = hypothesisScorerIF.computeScore(bestHypothesis, hypotheses.size() + 100, outputDir);
        String bestHypothesisFile = hypothesisOutputDirectory + "/bestHypothesis.pl";
        FileReaderUtils.writeFile(bestHypothesisFile,
                bestHypothesis.getHypothesisDump(), false);
        log.debug("Logic hypothesis training accuracy of best aligned solution is {}", accuracy);

        hypothesisScorerIF = new TestDataAccuracyEvaluator(hypothesisFactory, propertyManager);
        accuracy = hypothesisScorerIF.computeScore(bestHypothesis, hypotheses.size() + 200, outputDir);
        log.debug("Logic hypothesis test accuracy of best aligned solution is {}", accuracy);
    }

    private KTACalculatorIF centeredKTAScorer(HypothesisScorerIF hypothesisScorerIF) {
        KTACalculatorIF centeredKTAScorer = null;
        if (hypothesisScorerIF instanceof CenteredKTAScorer) {
            centeredKTAScorer = (KTACalculatorIF)hypothesisScorerIF;
        } else if (hypothesisScorerIF instanceof HybridScorer) {
            centeredKTAScorer = (KTACalculatorIF)hypothesisScorerIF;
        }  else if (hypothesisScorerIF instanceof CenteredKTAAndLogAccuracy) {
            centeredKTAScorer = (KTACalculatorIF)hypothesisScorerIF;
        }
        return centeredKTAScorer;
    }

    private SVM createClassifierForHypothesis(Hypothesis h) {
        SVM svm = new SVM();
        svm_problem prob = svm.createProblem(h.getFeaturesAndTargets());
        svm_parameter params = h.getKernelHelper().getSvm_params();
        params.svm_type = 0;
        params.cache_size = 2048;
        params.C = 1;
        svm.trainClassifier(prob, h.getKernelHelper().getSvm_params());
        svm.setSvm_problem(prob);
        return svm;
    }

    private Hypothesis printBestHypothesis(String notes) {
        hypotheses.sort((Comparator.comparing(Hypothesis::getScore)
                .reversed()));
        Hypothesis bestHypothesis = hypotheses.get(0);
        System.out.println(notes + " -- the best hypothesis below had a score of: " + bestHypothesis.getScore());
        log.debug(notes + " -- the best hypothesis below had a score of: " + bestHypothesis.getScore());
        bestHypothesis.getHypothesisDump().forEach(System.out::println);
        bestHypothesis.getHypothesisDump().forEach(log::debug);
        return bestHypothesis;
    }

    private void addEliteMembersToNextGen(int numberOfEliteHypotheses, List<Hypothesis> nextGen) {
        for (int i = 0; i < numberOfEliteHypotheses; i++) {
            Hypothesis clone = cloner.deepClone(hypotheses.get(i));
            clone.setElite(true);
            nextGen.add(clone);
        }
    }

    private void writeHypothesesToFiles(String outputDir) {
        File directory = new File(outputDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        int i = 0;
        for (Hypothesis h : hypotheses) {
            String outFileName = outputDir + "/hypothesis_" + i + ".pl";
            FileReaderUtils.writeFile(outFileName,
                    h.getHypothesisDump(), false);
            i++;
        }
    }

    private boolean scoreHypotheses() {
        boolean ret = false;
        String outputDir = hypothesisOutputDirectory + "/GEN_" + currentGeneration;
        int i = 0;
        for (Hypothesis h : hypotheses) {
            hypothesisScorerIF.computeScore(h, i, outputDir);
            i++;
        }
        writeHypothesesToFiles(outputDir);
        return ret;
    }

    private List<Hypothesis> getOneSetOfChildren(double totalFitness, List<Double> partialSumsForSelection) {
        ScoresAndTotal defScoresAndTotal = new ScoresAndTotal();
        defScoresAndTotal.totalFitness = totalFitness;
        defScoresAndTotal.partialSumsForSelection = partialSumsForSelection;
        ScoresAndTotal scoresAndTotal = defScoresAndTotal;
        int index1 = Utils.getIndexOfLeastExceedingNumber(Math.random() * totalFitness, partialSumsForSelection);
        Hypothesis parent1 = hypotheses.get(index1);
        if (diversityBoost) {
            ScoresAndTotal tmp = getRelScores(index1);
            if (Objects.nonNull(tmp)) {
                scoresAndTotal = tmp;
            }
        }
        log.debug("Relative scores for index {} are: {}", index1,
                scoresAndTotal.partialSumsForSelection);
        // bias towards "different" solutions
        int index2 = Utils.getIndexOfLeastExceedingNumber(Math.random() * scoresAndTotal.totalFitness,
                scoresAndTotal.partialSumsForSelection);
        // make sure we have two different parents
        while (index2 == index1) {
            index2 = Utils.getIndexOfLeastExceedingNumber(Math.random() * scoresAndTotal.totalFitness,
                    scoresAndTotal.partialSumsForSelection);
        }
        Hypothesis parent2 = hypotheses.get(index2);
        List<Hypothesis> children = performCrossover(parent1, parent2);
        mutateChildren(children);
        return children;
    }

    private class ScoresAndTotal {
        public double totalFitness = 0.0;
        public List<Double> partialSumsForSelection = new ArrayList<>();
    }

    private ScoresAndTotal getRelScores(int excludedIndex) {
        if (relScores.containsKey(excludedIndex)) {
            return relScores.get(excludedIndex);
        }
        ScoresAndTotal scoresAndTotal = null;
        KTACalculatorIF centeredKTAScorer = centeredKTAScorer(hypothesisScorerIF);
        if (centeredKTAScorer != null) {
            scoresAndTotal = new ScoresAndTotal();
            Hypothesis excluded = hypotheses.get(excludedIndex);
            for (int i = 0; i < hypotheses.size(); i++) {
                if (i != excludedIndex) {
                    Hypothesis h = hypotheses.get(i);
                    Set<String> intersect = new HashSet<>(excluded.getExamples());
                    intersect.retainAll(h.getExamples());
                    boolean ok = (h.getExamples().size() == intersect.size());
                    if (ok) {
                        double alignmentBetweenHypotheses =
                                centeredKTAScorer.
                                        computerCKTABetween2CenteredMatrices(excluded.getCenteredKernelMatrix(),
                                                h.getCenteredKernelMatrix());
                        double adjustedScore = h.getScore() / alignmentBetweenHypotheses;
                        scoresAndTotal.totalFitness += adjustedScore;
                    } else {
                        throw new IllegalStateException("hypotheses had different example sets!");
                    }
                    scoresAndTotal.partialSumsForSelection.add(scoresAndTotal.totalFitness);
                } else {
                    scoresAndTotal.totalFitness += 1.0;
                    scoresAndTotal.partialSumsForSelection.add(scoresAndTotal.totalFitness);
                }
            }
            relScores.put(excludedIndex, scoresAndTotal);
        }
        return scoresAndTotal;
    }

    private void mutateChildren(List<Hypothesis> children) {
        for (Hypothesis h : children) {
            // check if we should mutate and do so if applicable
            if (!h.isElite()) {
                mutationHandler.mutateHypothesis(h);
            }
        }
    }

    private List<Hypothesis> performCrossover(Hypothesis parent1, Hypothesis parent2) {
        List<Hypothesis> children = new ArrayList<>();
        CrossoverType crossoverType =
                CrossoverType.from(Utils.getIndexOfLeastExceedingNumber(Math.random(), crossOverProbList));
        Hypothesis child1 = cloner.deepClone(parent1); child1.setElite(false);
        Hypothesis child2 = cloner.deepClone(parent2); child2.setElite(false);
        switch (crossoverType) {
            case RULE_SWAP: {
                // Note that if there is only one rule (i.e. one unique
                // head literal) in the hypothesis, this is the same as survival
                PrologStructure randomRuleKey1 = child1.getRandomRule();
                OrTree rule1 = child1.removeRule(randomRuleKey1);
                PrologStructure randomRuleKey2 = child2.getRandomRule();
                OrTree rule2 = child2.removeRule(randomRuleKey2);
                child1.addRule(randomRuleKey2, rule2);
                child2.addRule(randomRuleKey1, rule1);
                break;
            }
            case OR_SUBTREE_NODE_SWAP: {
                OrTree rule1 = child1.getValueForMthStructure(child1.getRandomRule());
                OrTree rule2 = child2.getValueForMthStructure(child2.getRandomRule());
                List<AndTree> rule1NthNode = rule1.getNthNode(rand.nextInt(rule1.getTreeSize()));
                List<AndTree> rule2NthNode = rule2.getNthNode(rand.nextInt(rule2.getTreeSize()));
                rule1.removeSomeChildExpressions(rule1NthNode);
                rule1.addIterms(rule2NthNode);
                rule1.generateTree();
                rule2.removeSomeChildExpressions(rule2NthNode);
                rule2.addIterms(rule1NthNode);
                rule2.generateTree();
                break;
            }
            case AND_SUBTREE_NODE_SWAP:
                AndTree andTree1 = child1.getValueForMthStructure(child1.getRandomRule()).getRandomChildExpression();
                AndTree andTree2 = child2.getValueForMthStructure(child2.getRandomRule()).getRandomChildExpression();
                if (Objects.isNull(andTree1) || Objects.isNull(andTree2)) {
                    throw new IllegalStateException("Received null and tree(s)");
                }
                if (andTree1.getTreeSize() == 0) {
                    andTree1.generateTree();
                }
                if (andTree2.getTreeSize() == 0) {
                    andTree2.generateTree();
                }
                List<PrologStructure> rule1NthNode = andTree1.getNthNode(rand.nextInt(andTree1.getTreeSize()));
                List<PrologStructure> rule2NthNode = andTree2.getNthNode(rand.nextInt(andTree2.getTreeSize()));
                andTree1.removeSomeChildExpressions(rule1NthNode);
                andTree1.addIterms(rule2NthNode);
                andTree1.generateTree();
                andTree2.removeSomeChildExpressions(rule2NthNode);
                andTree2.addIterms(rule1NthNode);
                andTree2.generateTree();
                break;
            case SURVIVAL:
            default:
                break;
        }
        children.add(child1);
        children.add(child2);
        return children;
    }

    public synchronized void readHypothesisFromFile(String hypothesisFile) {
        if (!initialized) {
            initialize();
        }
        if (!hypotheses.contains(hypothesisFile)) {
            Hypothesis h = new Hypothesis(backgroundLanguage, hypothesisFile);
            h.initialize();
            hypotheses.add(h);
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
