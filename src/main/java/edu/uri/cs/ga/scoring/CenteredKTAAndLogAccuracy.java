package edu.uri.cs.ga.scoring;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.ga.scoring.kernel.KTACalculatorIF;
import edu.uri.cs.ga.scoring.kernel.KernelHelper;
import edu.uri.cs.hypothesis.Hypothesis;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Ben on 12/27/18.
 */
@Slf4j
public class CenteredKTAAndLogAccuracy implements HypothesisScorerIF, KTACalculatorIF {

    private AlephAccuracyScorer alephAccuracyScorer;
    private CenteredKTAScorer centeredKTAScorer;

    public CenteredKTAAndLogAccuracy(HypothesisFactory hypothesisFactory, KernelHelper kernelHelper, boolean weighted) {
        this.alephAccuracyScorer = new AlephAccuracyScorer(hypothesisFactory, weighted);
        this.centeredKTAScorer = new CenteredKTAScorer(hypothesisFactory, kernelHelper, weighted);
    }

    @Override
    public double computeScore(Hypothesis h, int hypothesisNumber, String outputDir) {
        double accuracyScore = alephAccuracyScorer.computeScore(h, hypothesisNumber, outputDir);
        double centeredKTAScore = centeredKTAScorer.computeScore(h, hypothesisNumber, outputDir);
        log.debug("Hypothesis {} from gen {} had CKTA, Accuracy of {}, {}", hypothesisNumber,
                outputDir, centeredKTAScore, accuracyScore);
        h.setScore(centeredKTAScore);
        return h.getScore();
    }

    public synchronized double computerCKTABetween2CenteredMatrices(double[][] m1, double[][] m2) {
        return centeredKTAScorer.computerCKTABetween2CenteredMatrices(m1, m2);
    }
}