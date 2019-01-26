package edu.uri.cs.ga.scoring.kernel;

import edu.uri.cs.ga.scoring.FeaturesAndTargets;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.util.PropertyManager;

/**
 * Created by Ben on 12/17/18.
 */
public interface KTACalculatorIF {
    double computerCKTABetween2CenteredMatrices(double [][] m1, double [][] m2);
    FeaturesAndTargets createFeatureVectorsForTestData(Hypothesis h, String outputDir,
                                                       PropertyManager propertyManager);
}
