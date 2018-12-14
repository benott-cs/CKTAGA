package edu.uri.cs.ga.scoring;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.util.FileReaderUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Ben on 12/6/18.
 */
public class AlephAccuracyScorer implements HypothesisScorerIF {

    private HypothesisFactory hypothesisFactory;
    private boolean weighted;
    private CommandLineOutputParser positiveOutputParser;
    private CommandLineOutputParser negativeOutputParser;

    public AlephAccuracyScorer(HypothesisFactory hypothesisFactory, boolean weighted) {
        this.hypothesisFactory = hypothesisFactory;
        this.weighted = weighted;
    }

    @Override
    public double computeScore(Hypothesis h, int hypothesisNumber, String outputDir) {
        // 1 - write or tree to file "hypothesis_gen<x>_member<y>.pl"
        //     - write each highest level AndTree in the OrTree(s) as its (their) own clause
        List<String> hypothesisDump = h.getHypothesisDump();
        String hypothesisOutputFile = outputDir + "/hypothesis_" + hypothesisNumber + ".pl";
        FileReaderUtils.writeFile(hypothesisOutputFile,
                hypothesisDump, false);

        // 2 - evaluate positive and parse output
        positiveOutputParser = new CommandLineOutputParser(false);
        hypothesisFactory.evaluateHypothesis(hypothesisOutputFile, true, positiveOutputParser);

        // 3 - evaluate negative and parse output
        negativeOutputParser = new CommandLineOutputParser(true);
        hypothesisFactory.evaluateHypothesis(hypothesisOutputFile, false, negativeOutputParser);

        // Compute the totals and return
        h.setScore(computeAccuracy());
        return h.getScore();
    }

    private double computeAccuracy() {
        double totalExamples;
        int totalCorrect;
        if (weighted) {
            totalExamples = 2.0 * positiveOutputParser.getTotalCount() * negativeOutputParser.getTotalCount();
            totalCorrect = positiveOutputParser.getCorrectCount() * negativeOutputParser.getTotalCount() +
                    negativeOutputParser.getCorrectCount() * positiveOutputParser.getTotalCount();
        } else {
            totalExamples = 1.0 * positiveOutputParser.getTotalCount() + negativeOutputParser.getTotalCount();
            totalCorrect = positiveOutputParser.getCorrectCount() + negativeOutputParser.getCorrectCount();
        }
        return totalCorrect / totalExamples;
    }

    private class CommandLineOutputParser implements ConsumerWithEnd<String> {

        private int correctCount = 0;
        private int incorrectCount = 0;
        private boolean negate = false;
        private static final String COVERED_STRING = "covered]";
        private static final String NOT_COVERED = "not covered";

        public CommandLineOutputParser(boolean negateIt) {
            this.negate = negateIt;
        }

        @Override
        public void accept(String s) {
            if (s.contains(COVERED_STRING)) {
                if (s.contains(NOT_COVERED)) {
                    if(negate) {
                        correctCount++;
                    } else {
                        incorrectCount++;
                    }
                } else {
                    if(negate) {
                        incorrectCount++;
                    } else {
                        correctCount++;
                    }
                }
            }
        }

        @Override
        public Consumer<String> andThen(Consumer<? super String> after) {
            return null;
        }

        public int getCorrectCount() {
            return correctCount;
        }

        public int getTotalCount() {
            return correctCount + incorrectCount;
        }

        @Override
        public void finish() {

        }
    }
}
