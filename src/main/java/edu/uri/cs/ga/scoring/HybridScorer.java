package edu.uri.cs.ga.scoring;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.classifier.FeaturesAndTargets;
import edu.uri.cs.ga.scoring.kernel.KTACalculatorIF;
import edu.uri.cs.ga.scoring.kernel.KernelHelper;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.util.PropertyManager;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Ben on 12/17/18.
 */
@Slf4j
public class HybridScorer implements HypothesisScorerIF, KTACalculatorIF {

    private AlephAccuracyScorer alephAccuracyScorer;
    private CenteredKTAScorer centeredKTAScorer;

    public HybridScorer(HypothesisFactory hypothesisFactory, KernelHelper kernelHelper, boolean weighted) {
        this.alephAccuracyScorer = new AlephAccuracyScorer(hypothesisFactory, weighted);
        this.centeredKTAScorer = new CenteredKTAScorer(hypothesisFactory, kernelHelper);
    }

    @Override
    public double computeScore(Hypothesis h, int hypothesisNumber, String outputDir) {
        double centeredKTAScore = centeredKTAScorer.computeScore(h, hypothesisNumber, outputDir);
        double accuracyScore = alephAccuracyScorer.computeScore(h, hypothesisNumber, outputDir);
        h.setScore(centeredKTAScore * accuracyScore);
        log.info("Hypothesis {} from gen {} with filename {} had HybridScore, Accuracy of {}, {}", hypothesisNumber,
                outputDir, h.getHypothesisFile(), h.getScore(), accuracyScore);
        return h.getScore();
    }

    public synchronized double computerCKTABetween2CenteredMatrices(double [][] m1, double [][] m2) {
        return centeredKTAScorer.computerCKTABetween2CenteredMatrices(m1, m2);
    }

    public FeaturesAndTargets createFeatureVectorsForTestData(Hypothesis h, String outputDir,
                                                              PropertyManager propertyManager) {
        return centeredKTAScorer.createFeatureVectorsForTestData(h, outputDir, propertyManager);
    }
}
