package edu.uri.cs.aleph;

import edu.uri.cs.util.CommandLineRunner;
import edu.uri.cs.util.FileReaderUtils;
import edu.uri.cs.util.PropertyManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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
    private static final String GENERATED_INPUT_BASE = "generated_theory_" + THEORY_NUM_TOKEN + ".pl";
    private static final String CREATE_A_HYPOTHESIS_FILE = "create_a_hypothesis.pl";
    private CommandLineRunner commandLineRunner;

    public HypothesisFactory(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
        this.backgroundDataFileName = propertyManager.getProperty(PropertyManager.ALEPH_BACKGROUND_FILE);
        this.alephLocation = propertyManager.getProperty(PropertyManager.ALEPH_LOCATION);
        this.yapLocation = propertyManager.getProperty(PropertyManager.YAP_LOCATION);
        this.negativeExamplesFile = propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_NEGATIVE_EXAMPLE_FILE);
        this.positiveExamplesFile = propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_POSITIVE_EXAMPLE_FILE);
        this.commandLineRunner = new CommandLineRunner(propertyManager);
    }

    public String createHypothesis(List<String> positiveExamples, List<String> negativeExamples, int hypNum) {
        createInputFiles(positiveExamples, negativeExamples);
        List<String> runnerScriptInputList = new ArrayList<>();
        String outputFileName = createRunnerScript(runnerScriptInputList, hypNum);
        String scriptName = getBackgroundDataRootDir(backgroundDataFileName) + CREATE_A_HYPOTHESIS_FILE;
        FileReaderUtils.writeFile(scriptName, runnerScriptInputList, true);
        commandLineRunner.runCommand(scriptName);
        return outputFileName;
    }

    public void evaluateHypothesis(String hypothesisFileToEvaluate, boolean testPositive,
                                     Consumer<String> commandLineOutputParser) {
        List<String> runnerScriptInputList = new ArrayList<>();
        createEvaluationScript(runnerScriptInputList, hypothesisFileToEvaluate, testPositive);
        String scriptName = getBackgroundDataRootDir(backgroundDataFileName) + "logenProEvaluate.pl";
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
        outputFileName = getBackgroundDataRootDir(backgroundDataFileName) + outputFileName;
        strings.add(COMMAND_TOKEN + "write_rules('" + outputFileName + "').");
        return outputFileName;
    }

    private void createEvaluationScript(List<String> strings, String hypothesisFileToEvaluate,
                                          boolean testPositive) {
        strings.add("#!" + yapLocation + " -L");
        strings.add(COMMAND_TOKEN + "['" + alephLocation + "'].");
        strings.add(COMMAND_TOKEN + "['" + backgroundDataFileName + "'].");
        strings.add(COMMAND_TOKEN + "['" + hypothesisFileToEvaluate + "'].");
        strings.add(COMMAND_TOKEN + "test('" +
                (testPositive ? positiveExamplesFile : negativeExamplesFile) +
                "', show, cov, tot).");
    }

    private String getBackgroundDataRootDir(String backgroundDataFileName) {
        return (FileReaderUtils.getParentDirectoryNameFromFilename(backgroundDataFileName) + "/");
    }
}
