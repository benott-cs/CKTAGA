package edu.uri.cs.ga.scoring;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.util.FileReaderUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by Ben on 12/11/18.
 */
public class CenteredKTAScorer implements HypothesisScorerIF {
    private HypothesisFactory hypothesisFactory;
    private boolean weighted;
    private CommandLineOutputParser outputParser;

    public CenteredKTAScorer(HypothesisFactory hypothesisFactory, boolean weighted) {
        this.hypothesisFactory = hypothesisFactory;
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

        // Compute the totals and return
        h.setScore(computeAccuracy());
        return h.getScore();
    }

    private double computeAccuracy() {
        double totalExamples;
        int totalCorrect;
//        if (weighted) {
//            totalExamples = 2.0 * positiveOutputParser.getTotalCount() * negativeOutputParser.getTotalCount();
//            totalCorrect = positiveOutputParser.getCorrectCount() * negativeOutputParser.getTotalCount() +
//                    negativeOutputParser.getCorrectCount() * positiveOutputParser.getTotalCount();
//        } else {
//            totalExamples = 1.0 * positiveOutputParser.getTotalCount() + negativeOutputParser.getTotalCount();
//            totalCorrect = positiveOutputParser.getCorrectCount() + negativeOutputParser.getCorrectCount();
//        }
        return 0.0;
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

        HashMap<String, ArrayList<Double>> coveredClauses = new HashMap<>();
        HashMap<String, Double> targets = new HashMap<>();
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
