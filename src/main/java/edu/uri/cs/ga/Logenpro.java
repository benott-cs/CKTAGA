package edu.uri.cs.ga;

import edu.uri.cs.util.PropertyManager;

/**
 * Created by Ben on 7/26/18.
 */
public class Logenpro {

    private PropertyManager propertyManager;
    private PopulationManager populationManager;
    private int populationSize = 0;
    private boolean runGA = true;

    public Logenpro(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    public void initialize() {
        populationManager = new PopulationManager(propertyManager.getProperty(PropertyManager.CRKTAGA_BACKGROUND_FILE),
                propertyManager);
        setPopulationSize(propertyManager.getProperty(PropertyManager.CRKTAGA_POPULATION_SIZE));
        populationManager.initialize();
        runGA = !propertyManager.getPropAsBoolean(PropertyManager.CRKTAGA_NO_GA_READ_PREV_BEST);
        if (runGA) {
            populationManager.createInitialPopulation(populationSize);
        }
    }

    private void setPopulationSize(String s) {
        try {
            populationSize = Integer.valueOf(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void evolve() {
        if (runGA) {
            populationManager.runGA();
        } else {
            populationManager.evaluatePreviousBest(propertyManager.getProperty(PropertyManager.CRKTAGA_PREV_BEST_FILE));
        }
    }

}
