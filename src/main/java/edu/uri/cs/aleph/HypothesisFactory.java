package edu.uri.cs.aleph;

import edu.uri.cs.ga.scoring.ConsumerWithEnd;
import edu.uri.cs.util.CommandLineRunner;
import edu.uri.cs.util.FileReaderUtils;
import edu.uri.cs.util.PropertyManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ben on 7/26/18.
 */
public class HypothesisFactory {

    private PropertyManager propertyManager;
    private String backgroundDataFileName;
    private String alephLocation;
    private String yapLocation;
    private String positiveExamplesFile;
    private String negativeExamplesFile;
    private static final String COMMAND_TOKEN = ":- ";
    private static final String THEORY_NUM_TOKEN = "<NUM>";
    private static String GENERATED_INPUT_BASE = "generated_theory_" + THEORY_NUM_TOKEN + ".pl";
    private static String CREATE_A_HYPOTHESIS_FILE = "create_a_hypothesis.pl";
    private static String OUTPUT_DIR = "";
    private CommandLineRunner commandLineRunner;

    public HypothesisFactory(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
        this.backgroundDataFileName = propertyManager.getProperty(PropertyManager.ALEPH_BACKGROUND_FILE);
        this.alephLocation = propertyManager.getProperty(PropertyManager.ALEPH_LOCATION);
        this.yapLocation = propertyManager.getProperty(PropertyManager.YAP_LOCATION);
        this.negativeExamplesFile = propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_NEGATIVE_EXAMPLE_FILE);
        this.positiveExamplesFile = propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_POSITIVE_EXAMPLE_FILE);
        this.commandLineRunner = new CommandLineRunner(propertyManager);
        this.OUTPUT_DIR = propertyManager.getProperty(PropertyManager.CRKTAGA_HYPOTHESES_OUTPUT_DIRECTORY);
        this.CREATE_A_HYPOTHESIS_FILE = OUTPUT_DIR + "/" + CREATE_A_HYPOTHESIS_FILE;
        this.GENERATED_INPUT_BASE = OUTPUT_DIR + "/" + GENERATED_INPUT_BASE;
    }

    public String createHypothesis(List<String> positiveExamples, List<String> negativeExamples, int hypNum) {
        createInputFiles(positiveExamples, negativeExamples);
        List<String> runnerScriptInputList = new ArrayList<>();
        String outputFileName = createRunnerScript(runnerScriptInputList, hypNum);
        String scriptName = CREATE_A_HYPOTHESIS_FILE;
        FileReaderUtils.writeFile(scriptName, runnerScriptInputList, true);
        commandLineRunner.runCommand(scriptName);
        return outputFileName;
    }

    public void evaluateFinal(String hypothesisFileToEvaluate, String testFile,
                                   ConsumerWithEnd<String> commandLineOutputParser) {
        List<String> runnerScriptInputList = new ArrayList<>();
        createTestEvaluationScript(runnerScriptInputList, hypothesisFileToEvaluate, testFile);
        String scriptName = OUTPUT_DIR + "/" + "logenProEvaluateTest.pl";
        FileReaderUtils.writeFile(scriptName, runnerScriptInputList, true);
        commandLineRunner.runCommand(scriptName, commandLineOutputParser);
    }

    public void evaluateHypothesis(String hypothesisFileToEvaluate, boolean testPositive,
                                     ConsumerWithEnd<String> commandLineOutputParser) {
        List<String> runnerScriptInputList = new ArrayList<>();
        createTrainingEvaluationScript(runnerScriptInputList, hypothesisFileToEvaluate, testPositive);
        String scriptName = OUTPUT_DIR + "/" + "logenProEvaluate.pl";
        FileReaderUtils.writeFile(scriptName, runnerScriptInputList, true);
        commandLineRunner.runCommand(scriptName, commandLineOutputParser);
    }

    // shuffle the data so that the inputs are in a randomized order
    private void createInputFiles(List<String> positiveExamples, List<String> negativeExamples) {
        Collections.shuffle(positiveExamples);
        Collections.shuffle(negativeExamples);
        FileReaderUtils.writeFile(positiveExamplesFile, positiveExamples, false);
        FileReaderUtils.writeFile(negativeExamplesFile, negativeExamples, false);
    }

    private String createRunnerScript(List<String> strings, int hypNum) {
        strings.add("#!" + yapLocation + " -L");
        strings.add(COMMAND_TOKEN + "['" + alephLocation + "'].");
        strings.add(COMMAND_TOKEN + "read_all('" + FileReaderUtils.getFilenameWithNoExtension(backgroundDataFileName) + "').");
        strings.add(COMMAND_TOKEN + "induce.");
        String outputFileName = GENERATED_INPUT_BASE.replaceAll(THEORY_NUM_TOKEN, Integer.toString(hypNum));
        strings.add(COMMAND_TOKEN + "write_rules('" + outputFileName + "').");
        return outputFileName;
    }

    private void createTestEvaluationScript(List<String> strings, String hypothesisFileToEvaluate,
                                                String testFile) {
        createEvaluationScriptLessTestClause(strings, hypothesisFileToEvaluate);
        strings.add(COMMAND_TOKEN + "test('" + testFile + "', show, Cov, Tot).");
    }

    private void createTrainingEvaluationScript(List<String> strings, String hypothesisFileToEvaluate,
                                          boolean testPositive) {
        createEvaluationScriptLessTestClause(strings, hypothesisFileToEvaluate);
        strings.add(COMMAND_TOKEN + "test('" +
                (testPositive ? positiveExamplesFile : negativeExamplesFile) +
                "', show, Cov, Tot).");
    }

    private void createEvaluationScriptLessTestClause(List<String> strings, String hypothesisFileToEvaluate) {
        strings.add("#!" + yapLocation + " -L");
        strings.add(COMMAND_TOKEN + "['" + alephLocation + "'].");
        strings.add(COMMAND_TOKEN + "['" + backgroundDataFileName + "'].");
        strings.add(COMMAND_TOKEN + "['" + hypothesisFileToEvaluate + "'].");
    }

    private String getBackgroundDataRootDir(String backgroundDataFileName) {
        return (FileReaderUtils.getParentDirectoryNameFromFilename(backgroundDataFileName) + "/");
    }
}
