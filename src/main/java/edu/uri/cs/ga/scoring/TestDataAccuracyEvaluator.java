package edu.uri.cs.ga.scoring;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.util.FileReaderUtils;
import edu.uri.cs.util.PropertyManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Ben on 12/6/18.
 */
@Slf4j
public class TestDataAccuracyEvaluator implements HypothesisScorerIF {

    private HypothesisFactory hypothesisFactory;
    private CommandLineOutputParser positiveOutputParser;
    private CommandLineOutputParser negativeOutputParser;
    private String testDataFile;
    private String posSampleToken;
    private String negSampleToken;

    public TestDataAccuracyEvaluator(HypothesisFactory hypothesisFactory, PropertyManager propertyManager) {
        this.hypothesisFactory = hypothesisFactory;
        this.testDataFile = propertyManager.getProperty(PropertyManager.CRKTAGA_TEST_DATA_FILE);
        this.posSampleToken = propertyManager.getProperty(PropertyManager.CRKTAGA_TEST_DATA_POS_TOKEN);
        this.negSampleToken = propertyManager.getProperty(PropertyManager.CRKTAGA_TEST_DATA_NEG_TOKEN);
    }

    @Override
    public double computeScore(Hypothesis h, int hypothesisNumber, String outputDir) {
        if (Objects.nonNull(testDataFile)) {
            // 1 - write or tree to file "hypothesis_gen<x>_member<y>.pl"
            //     - write each highest level AndTree in the OrTree(s) as its (their) own clause
            List<String> hypothesisDump = h.getHypothesisDump();
            String hypothesisOutputFile = outputDir + "/hypothesis_" + hypothesisNumber + ".pl";
            FileReaderUtils.writeFile(hypothesisOutputFile,
                    hypothesisDump, false);

            List<String> allTestData = new ArrayList<>();
            FileReaderUtils.readInStringLinesFromFile(testDataFile, allTestData);
            List<String> positiveSamples = allTestData.stream().filter(t -> t.startsWith(posSampleToken))
                    .map(t -> t.replaceAll(posSampleToken, "")).collect(Collectors.toList());
            List<String> negativeSamples = allTestData.stream().filter(t -> t.startsWith(negSampleToken))
                    .map(t -> t.replaceAll(negSampleToken, "")).collect(Collectors.toList());

            // 2 - evaluate positive and parse output
            positiveOutputParser = new CommandLineOutputParser(false);
            if (!positiveSamples.isEmpty()) {
                String posFileName = outputDir + "/pos_samples.txt";
                FileReaderUtils.writeFile(posFileName, positiveSamples, false);
                hypothesisFactory.evaluateFinal(hypothesisOutputFile, posFileName, positiveOutputParser);
            }

            // 3 - evaluate negative and parse output
            negativeOutputParser = new CommandLineOutputParser(true);
            if (!negativeSamples.isEmpty()) {
                String negFileName = outputDir + "/neg_samples.txt";
                FileReaderUtils.writeFile(negFileName, negativeSamples, false);
                hypothesisFactory.evaluateFinal(hypothesisOutputFile, negFileName, negativeOutputParser);
            }
            // Compute the totals and return
            h.setScore(computeAccuracy());
        } else {
            log.debug("No test data provided - setting test accuracy to 0");
            h.setScore(0.0);
        }

        return h.getScore();
    }

    private double computeAccuracy() {
        double totalExamples = 1.0 * positiveOutputParser.getTotalCount() + negativeOutputParser.getTotalCount();;
        int totalCorrect = positiveOutputParser.getCorrectCount() + negativeOutputParser.getCorrectCount();
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
