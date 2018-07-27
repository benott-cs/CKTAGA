package edu.uri.cs.ga;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.hypothesis.HypothesisManager;
import edu.uri.cs.util.FileReaderUtils;
import edu.uri.cs.util.PropertyManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ben on 7/26/18.
 */
public class Logenpro {

    private PropertyManager propertyManager;
    private HypothesisManager hypothesisManager;
    private int populationSize = 0;
    private List<String> positiveExamples = new ArrayList<>();
    private List<String> negativeExamples = new ArrayList<>();
    private HypothesisFactory hypothesisFactory;

    public Logenpro(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
        hypothesisFactory = new HypothesisFactory(propertyManager);
    }

    public void initialize() {
        hypothesisManager = new HypothesisManager(propertyManager.getProperty(PropertyManager.CRKTAGA_BACKGROUND_FILE));
        setPopulationSize(propertyManager.getProperty(PropertyManager.CRKTAGA_POPULATION_SIZE));
        FileReaderUtils.readInStringLinesFromFile(propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_POSITIVE_EXAMPLE_FILE),
               positiveExamples);
        FileReaderUtils.readInStringLinesFromFile(propertyManager.getProperty(PropertyManager.ALEPH_HYPOTHESIS_NEGATIVE_EXAMPLE_FILE),
                negativeExamples);
        createInitialPopulation();
    }

    private void createInitialPopulation() {
        for (int i = 0; i < populationSize; i++) {
            // Create a hypothesis using Aleph. Add it to the hypothesis manager.
            hypothesisManager.readHypothesisFromFile(
                    hypothesisFactory.createHypothesis(positiveExamples, negativeExamples, i));
        }
    }

    private void setPopulationSize(String s) {
        try {
            populationSize = Integer.valueOf(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

}
