package edu.uri.cs.ga.ensemble;

import edu.uri.cs.classifier.FeaturesAndTargets;
import edu.uri.cs.classifier.SVM;
import edu.uri.cs.ga.PopulationManager;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.util.PropertyManager;
import libsvm.svm_problem;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Ben on 3/29/19.
 */
@Slf4j
public class EnsembleCreator {

    public class EnsemblePrediction {
        public double numPositiveVotes;
        public double numTotalVotes;
        public boolean isPositive;
    }

    private PopulationManager populationManager;
    private String rootDir;
    private boolean isNaive;
    private boolean penalizeInitialScoreRepeatedly = false;
    private int numEnsembleCandidates;
    private int numEnsembleMembers;
    private double diversityEncouragementFactor;
    // This is a total hack. I use 60 because I know that I never used more than
    // 60 hypotheses per generation in any experiment (this looks for 0-60 inclusive).
    private static final String HYPOTHESIS_REGEX = "hypothesis_([0-9]|[1-5]\\d|60)\\.pl";
    private List<Hypothesis> candidates = new ArrayList<>();
    private List<Hypothesis> members = new ArrayList<>();

    public EnsembleCreator(PopulationManager populationManager, PropertyManager propertyManager) {
        this.populationManager = populationManager;
        this.rootDir = propertyManager.getProperty(PropertyManager.CRKTAGA_PATH_TO_LAST_GEN);
        this.isNaive = propertyManager.getPropAsBoolean(PropertyManager.CRKTAGA_NAIVE_ENSEMBLE);
        this.penalizeInitialScoreRepeatedly =
                propertyManager.getPropAsBoolean(PropertyManager.CRKTAGA_PENALIZE_INITIAL_SCORE);
        this.numEnsembleCandidates = propertyManager.getPropAsInt(PropertyManager.CRKTAGA_NUM_ENSEMBLE_CANDIDATES);
        this.numEnsembleMembers = propertyManager.getPropAsInt(PropertyManager.CRKTAGA_NUM_ENSEMBLE_MEMBERS);
        this.diversityEncouragementFactor = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_DIVERSITY_ENCOURAGEMENT_FACTOR);
    }


    // TODO - thoughts (missing thoughts have been implemented)
    //   1) read in from multiple separate parameter selections? Not yet. First pass will just use best params.
    //   3) search over all kernel params for each candidate for ensemble? No. However, I think this will help (future work).
    //   6) implement means of creating combined kernels from base kernels

    public void createEnsemble() {
        loadHypotheses();
        retrieveEnsembleCandidates();
        getEnsembleMembers();
        HashMap<Hypothesis, SVM> ensembleSVMs = makeEnsembleClassifier();
        HashMap<Integer, EnsemblePrediction> preds = evaluateEnsemble(ensembleSVMs);
        computeEnsembleAccuracy(preds);
    }


    public void computeEnsembleAccuracy(HashMap<Integer, EnsemblePrediction> preds) {
        double totalSamples = preds.size();
        int totalCorrect = 0;
        for (Integer sample : preds.keySet()) {
            EnsemblePrediction pred = preds.get(sample);
            double finalVote = pred.numPositiveVotes / pred.numTotalVotes;
            boolean isPositive = (finalVote >= 0.5);
            totalCorrect += (isPositive == pred.isPositive) ? 1 : 0;
        }
        double accuracy = totalCorrect / totalSamples;
        log.info("SVM ensemble test accuracy is {}. {} out of {}", accuracy, totalCorrect, totalSamples);
    }

    public HashMap<Integer, EnsemblePrediction> evaluateEnsemble(HashMap<Hypothesis, SVM> ensemble) {
        HashMap<Integer, EnsemblePrediction> ret = new HashMap<>();
        for (Hypothesis key : ensemble.keySet()) {
            log.info("=========================");
            log.info("Hypothesis {} in ensemble", key.getHypothesisFile());
            key.getHypothesisDump().forEach(log::info);
            FeaturesAndTargets testFeatures =
                    populationManager.getTestFeatureVectors(key);
            printFeaturesAndTargets(testFeatures);
            svm_problem prob = SVM.createProblem(testFeatures);
            HashMap<Integer, SVM.SVMResult> res = ensemble.get(key).getPredicationForSVM(prob);
            for (Integer sample : res.keySet()) {
                EnsemblePrediction ensemblePrediction = ret.getOrDefault(sample, new EnsemblePrediction());
                ensemblePrediction.numPositiveVotes += ((res.get(sample).predicted > 0) ? 1.0 : 0.0);
                ensemblePrediction.numTotalVotes += 1.0;
                ensemblePrediction.isPositive = (res.get(sample).actual > 0);
                ret.put(sample, ensemblePrediction);
            }
        }
        return ret;
    }

    private void printFeaturesAndTargets(FeaturesAndTargets fat) {
        Map<String, ArrayList<Double>> features = fat.getFeatureVectors();
        Map<String, Double> targets = fat.getTargets();
        for (String key : features.keySet()) {
            if (targets.containsKey(key)) {
                log.info("test feature vec for {} is {} with target {}", key, features.get(key), targets.get(key));
            }
        }
    }

    private HashMap<Hypothesis, SVM> makeEnsembleClassifier() {
        HashMap<Hypothesis, SVM> bestSVMForHypothesis = new HashMap<>();
        for (Hypothesis member : members) {
            SVM svmWithBestCValue = getBestSVMForHypothesis(member);
            bestSVMForHypothesis.put(member, svmWithBestCValue);
        }
        return bestSVMForHypothesis;
    }

    private SVM getBestSVMForHypothesis(Hypothesis h) {
        SVM ret = null;
        double bestAccuracy = 0.0;
        List<SVM> svms = populationManager.createClassifierForHypothesis(h);
        if (!svms.isEmpty()) {
            for (SVM svm : svms) {
                double currAccuracy = svm.getAccuracyOnProvidedSample(svm.getSvm_problem());
                if (currAccuracy > bestAccuracy) {
                    bestAccuracy = currAccuracy;
                    ret = svm;
                }
            }
        }
        return ret;
    }

    private void getEnsembleMembers() {
        while(members.size() < numEnsembleMembers) {
            Hypothesis nextBest = getNextBestHypothesis();
            if (candidates.contains(nextBest)) {
                candidates.remove(nextBest);
            }
            members.add(nextBest);
            double adjustedScore = (Objects.isNull(nextBest.getAdjustedScore()) ? nextBest.getScore() : nextBest.getAdjustedScore());
            log.info("Hypothesis {} with score {} and adjusted score {} has been added to the ensemble",
                    nextBest.getHypothesisFile(), nextBest.getScore(), adjustedScore);
            adjustScoresDueToAddition(nextBest);
        }
    }

    private void adjustScoresDueToAddition(Hypothesis newHypothesisInEnsemble) {
        for (Hypothesis candidate : candidates) {
            double score = candidate.getScore();
            double currentAdjustedScore = (Objects.isNull(candidate.getAdjustedScore())
                    ? candidate.getScore() : candidate.getAdjustedScore());
            if (isNaive) {
                candidate.setAdjustedScore(score);
            } else {
                double alignmentBetweenHypotheses =
                        populationManager.computeCenteredCKTABetweenHypotheses(candidate, newHypothesisInEnsemble);
                double num = penalizeInitialScoreRepeatedly ? candidate.getAlignmentWithTarget() : 1.0;
                double adjustBy = num / Math.pow(alignmentBetweenHypotheses, diversityEncouragementFactor);
                double newAdjustedScore = currentAdjustedScore * adjustBy;
                candidate.setAdjustedScore(newAdjustedScore);
            }
        }
        candidates.sort((Comparator.comparing(Hypothesis::getAdjustedScore)
                .reversed()));
    }

    private Hypothesis getNextBestHypothesis() {
        Hypothesis nextBest = null;
        if (!candidates.isEmpty()) {
            nextBest = candidates.get(0);
        }
        return nextBest;
    }

    private void retrieveEnsembleCandidates() {
        populationManager.currentGeneration = System.currentTimeMillis();
        populationManager.scoreHypotheses();
        candidates = populationManager.getTopNHypotheses(numEnsembleCandidates);
    }

    private void loadHypotheses() {
        List<String> hypothesisFiles = getHypothesisFiles();
        for (String hypothesisFile : hypothesisFiles) {
            // Get all of the hypotheses
            populationManager.readHypothesisFromFile(hypothesisFile);
        }
    }

    private List<String> getHypothesisFiles() {
        List<String> ret = new ArrayList<>();
        File dir = new File(rootDir);
        File[] res = dir.listFiles((d, name) -> name.matches(HYPOTHESIS_REGEX));
        ret = (new ArrayList<File>(Arrays.asList(res))).
                stream().
                map(f -> f.getAbsolutePath().toString()).collect(Collectors.toList());
        return ret;
    }

}
