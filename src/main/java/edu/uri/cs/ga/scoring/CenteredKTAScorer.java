package edu.uri.cs.ga.scoring;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.classifier.FeaturesAndTargets;
import edu.uri.cs.ga.scoring.kernel.KTACalculatorIF;
import edu.uri.cs.ga.scoring.kernel.KernelHelper;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.util.FileReaderUtils;
import edu.uri.cs.util.PropertyManager;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Ben on 12/11/18.
 */
@Slf4j
public class CenteredKTAScorer implements HypothesisScorerIF, KTACalculatorIF {

    private HypothesisFactory hypothesisFactory;
    private CommandLineOutputParser outputParser;
    private KernelHelper kernelHelper;

    public CenteredKTAScorer(HypothesisFactory hypothesisFactory, KernelHelper kernelHelper) {
        this.hypothesisFactory = hypothesisFactory;
        this.kernelHelper = kernelHelper;
    }

    @Override
    public double computeScore(Hypothesis h, int hypothesisNumber, String outputDir) {
        // 1 - write or tree to file "hypothesis_gen<x>_member<y>.pl"
        //     - write each highest level AndTree in the OrTree(s) as its (their) own clause
        List<String> hypothesisDump = h.getHypothesisDump();
        if (hypothesisDump.isEmpty()) {
            // FOR DEBUG ONLY
            hypothesisDump = h.getHypothesisDump();
        } else {
            log.info("========================");
            log.info("= hypothesis {} with filename {}", hypothesisNumber, h.getHypothesisFile());
            log.info("========================");
            hypothesisDump.forEach(log::info);
        }
        List<String> tmp = new ArrayList<>();
        int i = 0;
        outputParser = new CommandLineOutputParser(false);
        for (String clause : hypothesisDump) {
            tmp.clear();
            tmp.add(clause);
            String hypothesisOutputFile = outputDir + "/hypothesis_" + hypothesisNumber + "_clause_" + i + ".pl";
            FileReaderUtils.writeFile(hypothesisOutputFile,
                    tmp, false);

            // 2 - evaluate positive and parse output
            outputParser.setNegate(false);
            outputParser.completed = false;
            hypothesisFactory.evaluateHypothesis(hypothesisOutputFile, true, outputParser);

            // 3 - evaluate negative and parse output
            outputParser.setNegate(true);
            outputParser.completed = false;
            hypothesisFactory.evaluateHypothesis(hypothesisOutputFile, false, outputParser);
            i++;
        }

        try {
            validateParser(outputParser, hypothesisDump.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!hypothesisDump.isEmpty()) {
            int size = outputParser.targets.keySet().size();
            double[][] targetMatrix = new double[size][size];
            double[][] kernelMatrix = new double[size][size];

            // Compute the totals and return
            h.setScore(computeAccuracy(size, hypothesisDump.size(), targetMatrix, kernelMatrix));
            h.setCenteredKernelMatrix(kernelMatrix);
            h.setExamples(outputParser.coveredClauses.keySet());
            h.setFeaturesAndTargets(new FeaturesAndTargets(outputParser.coveredClauses, outputParser.targets));
            h.setKernelHelper(kernelHelper);
        } else {
            // there were either no clauses or the clauses which existed had all most
            // general literals in them with no variables matching the head of the clause;
            // hence, there basically was no hypothesis - we will score this with a zero
            h.setScore(0.0);
        }
        h.setAlignmentWithTarget(h.getScore());
        return h.getScore();
    }

    public FeaturesAndTargets createFeatureVectorsForTestData(Hypothesis h, String outputDir,
                                                              PropertyManager propertyManager) {
        String testDataFile = propertyManager.getProperty(PropertyManager.CRKTAGA_TEST_DATA_FILE);
        if (Objects.nonNull(testDataFile)) {
            String posSampleToken = propertyManager.getProperty(PropertyManager.CRKTAGA_TEST_DATA_POS_TOKEN);
            String negSampleToken = propertyManager.getProperty(PropertyManager.CRKTAGA_TEST_DATA_NEG_TOKEN);
            List<String> allTestData = new ArrayList<>();
            FileReaderUtils.readInStringLinesFromFile(testDataFile, allTestData);
            List<String> positiveSamples = allTestData.stream().filter(t -> t.startsWith(posSampleToken))
                    .map(t -> t.replaceAll(posSampleToken, "")).collect(Collectors.toList());
            List<String> negativeSamples = allTestData.stream().filter(t -> t.startsWith(negSampleToken))
                    .map(t -> t.replaceAll(negSampleToken, "")).collect(Collectors.toList());
            List<String> hypothesisDump = h.getHypothesisDump();
            if (hypothesisDump.isEmpty()) {
                // FOR DEBUG ONLY
                hypothesisDump = h.getHypothesisDump();
            } else {
                log.info("========================");
                log.info("= create test data features - hypothesis with filename {}", h.getHypothesisFile());
                log.info("========================");
                hypothesisDump.forEach(log::info);
            }
            List<String> tmp = new ArrayList<>();
            int i = 0;
            outputParser = new CommandLineOutputParser(false);
            for (String clause : hypothesisDump) {
                tmp.clear();
                tmp.add(clause);
                String hypothesisOutputFile = outputDir + "/hypothesis_FINAL_clause_" + i + ".pl";
                FileReaderUtils.writeFile(hypothesisOutputFile,
                        tmp, false);

                // 2 - evaluate positive and parse output
                if (!positiveSamples.isEmpty()) {
                    outputParser.setNegate(false);
                    outputParser.completed = false;
                    String posFileName = outputDir + "/pos_samples.txt";
                    FileReaderUtils.writeFile(posFileName, positiveSamples, false);
                    hypothesisFactory.evaluateFinal(hypothesisOutputFile, posFileName, outputParser);
                }

                // 3 - evaluate negative and parse output
                if (!negativeSamples.isEmpty()) {
                    outputParser.setNegate(true);
                    outputParser.completed = false;
                    String negFileName = outputDir + "/neg_samples.txt";
                    FileReaderUtils.writeFile(negFileName, negativeSamples, false);
                    hypothesisFactory.evaluateFinal(hypothesisOutputFile, negFileName, outputParser);
                }
                i++;
            }

            try {
                validateParser(outputParser, hypothesisDump.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new FeaturesAndTargets(outputParser.coveredClauses, outputParser.targets);
        } else {
            return null;
        }
    }

    private synchronized double computeAccuracy(int size, int numClauses, double[][] targetMatrix, double[][] kernelMatrix) {

        computeMatrices(size, numClauses, targetMatrix, kernelMatrix);

        log.debug("Matrices... before centering");
        printMatrix(kernelMatrix, "Kernel Matrix");
        printMatrix(targetMatrix, "Target Matrix");

        center_kernel_matrix(size, kernelMatrix);
        center_kernel_matrix(size, targetMatrix);

        log.debug("Matrices... after centering");
        printMatrix(kernelMatrix, "Kernel Matrix");
        printMatrix(targetMatrix, "Target Matrix");

        return computerCKTABetween2CenteredMatrices(kernelMatrix, targetMatrix);
    }

    private static final double ZERO_EPSILON = 0.000001;

    public synchronized double computerCKTABetween2CenteredMatrices(double[][] m1, double[][] m2) {
        if (m1.length != m2.length || m1.length == 0 || m2.length == 0 || m1[0].length != m2[0].length ||
                m1.length != m1[0].length || m2.length != m2[0].length) {
            log.error("Invalid matrices provided for CKTA score");
            log.error("m1 is: {}", m1);
            log.error("m2 is: {}", m2);
            throw new IllegalArgumentException("Matrices did not have correct lengths!");
        }
        double numer = compute_frobenius_product(m1, m2);
        double denom = compute_frobenius_norm(m1, m2);
        double res;
        if (denom <= ZERO_EPSILON) {
            res = numer / ZERO_EPSILON;
        } else {
            res = numer / denom;
        }
        // if the numerator was zero, return the zero epsilon value
        res = (res == 0.0) ? ZERO_EPSILON : res;
        log.debug("CKTA is: {}", res);
        return res;
    }

    private double compute_frobenius_norm(double[][] m, double[][] n) {
        double res1 = compute_frobenius_product(m, m);
        double res2 = compute_frobenius_product(n, n);
        return Math.sqrt(res1 * res2);
    }

    private double compute_frobenius_product(double[][] m, double[][] n) {
        double res = 0.0;
        double mult = 1.0;
        // we cheat knowing that we are dealing with symmetric matrices
        for (int i = 0; i < m.length; i++) {
            for (int j = i; j < m[i].length; j++) {
                mult = 2.0;
                if (i == j) {
                    mult = 1.0;
                }
                // this is the cheat - we just add the off diagonal terms twice
                res += mult * m[i][j] * n[i][j];
            }
        }
        return res;
    }

    private void printMatrix(double[][] table, String printFirst) {
        log.debug(printFirst);
        for (int r = 0; r < table.length; r++) {
            String s = "";
            for (int c = 0; c < table[r].length; c++) {
                s += table[r][c] + "\t";
            }
            log.debug(s);
        }
    }

    private void center_kernel_matrix(int size, double[][] m) {
    /* compute normalizing values */
        float[] meanrow = new float[size];
        float correction = 0;
        for (int i = 0; i < size; i++) {
            meanrow[i] = 0;
        }

        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                meanrow[i] += m[i][j];
                correction += m[i][j];
                if (j != i) {
                    meanrow[j] += m[i][j];
                    correction += m[i][j];
                }
            }
        }
        for (int i = 0; i < size; i++) {
            meanrow[i] /= size;
            log.trace("meanrow[" + i + "] is: " + meanrow[i]);
        }
        correction /= (size * size);
        log.trace("correction is: " + correction);
        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                // Note that meanrow[j] equals meancol[j] since the matrix is symmetric
                // hence why it is not necessary to compute meancol;
                // this is from Cortes paper, formula (1)
                m[i][j] = m[i][j] - meanrow[i] - meanrow[j] + correction;
                if (i != j) {
                    m[j][i] = m[i][j];
                }
            }
        }
    }

    private double[][] computeMatrices(int size, int numClauses, double[][] targetMatrix, double[][] kernelMatrix) {
        double[] targetVec = new double[size];
        // the second dimension is the number of clauses in the hypothesis
        double[][] featureVecMatrix = new double[size][numClauses];
        int k = 0;
        log.debug("Key set is: " + outputParser.targets.keySet());
        for (String key : outputParser.targets.keySet()) {
            targetVec[k] = outputParser.targets.get(key);
            featureVecMatrix[k] = outputParser.coveredClauses.get(key).
                    stream().mapToDouble(Double::doubleValue).toArray();
            log.info("feature vec for {} is {} with target {}", key, featureVecMatrix[k], targetVec[k]);
            k++;
        }
        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                kernelMatrix[i][j] = computeKernelValue(featureVecMatrix[i], featureVecMatrix[j]);
                targetMatrix[i][j] = targetVec[i] * targetVec[j];
                if (i != j) {
                    targetMatrix[j][i] = targetMatrix[i][j];
                    kernelMatrix[j][i] = kernelMatrix[i][j];
                }
            }
        }
        return targetMatrix;
    }

    private double computeKernelValue(double[] v1, double[] v2) {
        return kernelHelper.computeKernel(v1, v2);
    }

    private boolean validateParser(CommandLineOutputParser outputParser, int hypothesisSize) throws IllegalStateException {
        Set<String> intersect = new HashSet<String>(outputParser.targets.keySet());
        intersect.retainAll(outputParser.coveredClauses.keySet());
        boolean ok = (outputParser.targets.size() == outputParser.coveredClauses.keySet().size()) &&
                (outputParser.targets.size() == intersect.size());
        if (!ok) {
            throw new IllegalStateException("Sizes of matrices do not align!");
        }
        boolean first = true;
        int vecLength = 0;
        ok = true;
        Iterator<ArrayList<Double>> iter = outputParser.coveredClauses.values().iterator();
        while (iter.hasNext()) {
            ArrayList<Double> vector = iter.next();
            if (first) {
                vecLength = vector.size();
                if (vecLength != hypothesisSize) {
                    throw new IllegalStateException("Feature vector doesn't have length equal to number of clauses!");
                }
                first = false;
            } else if (vector.size() != vecLength) {
                ok = false;
            }
        }
        if (!ok) {
            throw new IllegalStateException("Feature vectors don't all have the same length!");
        }
        return true;
    }


    private static double POSITIVE_TARGET = 1.0;
    private static double NEGATIVE_TARGET = -1.0;
    private static double COVERED = 1.0;
    private static double UNCOVERED = 0.0;
    private class CommandLineOutputParser implements ConsumerWithEnd<String> {

        // The coveredClauses variable contains the feature vectors
        TreeMap<String, ArrayList<Double>> coveredClauses = new TreeMap<>();
        TreeMap<String, Double> targets = new TreeMap<>();
        private static final String COVERED_STRING = "covered]";
        private static final String NOT_COVERED = "not covered";
        private boolean negate = false;
        boolean completed = false;

        public CommandLineOutputParser(boolean negate) {
            this.negate = negate;
        }

        public void setNegate(boolean negate) {
            this.negate = negate;
        }

        @Override
        public void accept(String s) {
            if (s.contains(COVERED_STRING)) {
                String key = s.split("[\\(\\)]")[1];
                if (!coveredClauses.containsKey(key)) {
                    coveredClauses.put(key, new ArrayList<>());
                }
                if (!targets.containsKey(key)) {
                    targets.put(key, negate ? NEGATIVE_TARGET : POSITIVE_TARGET);
                }
                // These are broken out by negate in case we want to handle
                // positive and negative samples differently in the future.
                // If negate is true, we are handling negative examples.
                if (s.contains(NOT_COVERED)) {
                    if (negate) {
                        coveredClauses.get(key).add(UNCOVERED);
                    } else {
                        coveredClauses.get(key).add(UNCOVERED);
                    }
                } else {
                    if (negate) {
                        coveredClauses.get(key).add(COVERED);
                    } else {
                        coveredClauses.get(key).add(COVERED);
                    }
                }
            }
        }

        @Override
        public void finish() {
            completed = true;
        }
    }
}
