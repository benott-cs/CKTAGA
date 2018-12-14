package edu.uri.cs.ga.scoring;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.ga.kernel.KernelHelper;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.util.FileReaderUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Ben on 12/11/18.
 */
public class CenteredKTAScorer implements HypothesisScorerIF {
    private HypothesisFactory hypothesisFactory;
    private boolean weighted;
    private CommandLineOutputParser outputParser;
    private KernelHelper kernelHelper;

    public CenteredKTAScorer(HypothesisFactory hypothesisFactory, KernelHelper kernelHelper, boolean weighted) {
        this.hypothesisFactory = hypothesisFactory;
        this.kernelHelper = kernelHelper;
        this.weighted = weighted;
    }

    @Override
    public double computeScore(Hypothesis h, int hypothesisNumber, String outputDir) {
        // 1 - write or tree to file "hypothesis_gen<x>_member<y>.pl"
        //     - write each highest level AndTree in the OrTree(s) as its (their) own clause
        List<String> hypothesisDump = h.getHypothesisDump();
        List<String> tmp = new ArrayList<>();
        int i = 0;
        outputParser = new CommandLineOutputParser(false);
        for (String clause : hypothesisDump) {
            tmp.clear(); tmp.add(clause);
            String hypothesisOutputFile = outputDir + "/hypothesis_" + hypothesisNumber + "_clause_" + i + ".pl";
            FileReaderUtils.writeFile(hypothesisOutputFile,
                    tmp, false);

            // 2 - evaluate positive and parse output
            outputParser.setNegate(false);
            hypothesisFactory.evaluateHypothesis(hypothesisOutputFile, true, outputParser);

            // 3 - evaluate negative and parse output
            outputParser.setNegate(true);
            hypothesisFactory.evaluateHypothesis(hypothesisOutputFile, false, outputParser);
        }

        validateParser(outputParser);

        int size = outputParser.targets.keySet().size();
        double[][] targetMatrix = new double[size][size];
        double[][] kernelMatrix = new double[size][size];

        // Compute the totals and return
        h.setScore(computeAccuracy(size, targetMatrix, kernelMatrix));
        return h.getScore();
    }

    private double computeAccuracy(int size, double[][] targetMatrix, double[][] kernelMatrix) {

        computeMatrices(size, targetMatrix, kernelMatrix);

        System.out.println("Matrices... before centering");
        printMatrix(kernelMatrix, "Kernel Matrix"); printMatrix(targetMatrix, "Target Matrix");

        center_kernel_matrix(size, kernelMatrix);
        center_kernel_matrix(size, targetMatrix);

        System.out.println("Matrices... after centering");
        printMatrix(kernelMatrix, "Kernel Matrix"); printMatrix(targetMatrix, "Target Matrix");

        double numer = compute_frobenius_product(kernelMatrix, targetMatrix);
        double denom = compute_frobenius_norm(kernelMatrix, kernelMatrix);
        double res = (denom != 0.0) ? numer / denom : 0.0;
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
        System.out.println(printFirst);
        for(int r=0; r<table.length; r++) {
            for(int c=0; c<table[r].length; c++) {
                System.out.print(table[r][c] + "\t");
            }
            System.out.println();
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
            System.out.println("meanrow[" + i + "] is: " + meanrow[i]);
        }
        correction /= (size * size);
        System.out.println("correction is: " + correction);
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

    private double[][] computeMatrices(int size, double[][] targetMatrix, double[][] kernelMatrix) {
        double[] targetVec = new double[size];
        double[][] featureVecMatrix = new double[size][size];
        int k = 0;
        for (String key : outputParser.targets.keySet()) {
            targetVec[k] = outputParser.targets.get(key);
            featureVecMatrix[k] = outputParser.coveredClauses.get(key).
                    stream().mapToDouble(Double::doubleValue).toArray();
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

    private void validateParser(CommandLineOutputParser outputParser) {
        Set<String> intersect = new HashSet<String>(outputParser.targets.keySet());
        intersect.retainAll(outputParser.coveredClauses.keySet());
        boolean ok = (outputParser.targets.size() == outputParser.coveredClauses.keySet().size()) &&
                (outputParser.targets.size() == intersect.size());
        if (!ok) {
            throw new IllegalStateException("Sizes of matrices do not align!");
        }
        boolean first = true; int size = 0;
        ok = true;
        for (ArrayList<Double> vector : outputParser.coveredClauses.values()) {
            if (first) {
                size = vector.size();
                first = false;
            }
            else if (vector.size() != size) {
                ok = false;
            }
        }
        if (!ok) {
            throw new IllegalStateException("Feature for vectors don't all have the same length!");
        }
    }

    private class CommandLineOutputParser implements Consumer<String> {

        TreeMap<String, ArrayList<Double>> coveredClauses = new TreeMap<>();
        TreeMap<String, Double> targets = new TreeMap<>();
        private static final String COVERED_STRING = "covered]";
        private static final String NOT_COVERED = "not covered";
        private boolean negate = false;

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
                    targets.put(key, negate ? -1.0 : 1.0);
                }
                if (s.contains(NOT_COVERED)) {
                    if (negate) {
                        coveredClauses.get(key).add(1.0);
                    } else {
                        coveredClauses.get(key).add(0.0);
                    }
                } else {
                    if (negate) {
                        coveredClauses.get(key).add(0.0);
                    } else {
                        coveredClauses.get(key).add(1.0);
                    }
                }
            }
        }

        @Override
        public Consumer<String> andThen(Consumer<? super String> after) {
            return null;
        }

    }
}
