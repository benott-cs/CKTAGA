package edu.uri.cs.ga.scoring;

import com.igormaznitsa.prologparser.terms.AlephStringConstant;
import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.ga.scoring.kernel.KTACalculatorIF;
import edu.uri.cs.ga.scoring.kernel.KernelHelper;
import edu.uri.cs.hypothesis.Hypothesis;

/**
 * Created by Ben on 12/17/18.
 */
public class HybridScorer implements HypothesisScorerIF, KTACalculatorIF {

    private AlephAccuracyScorer alephAccuracyScorer;
    private CenteredKTAScorer centeredKTAScorer;

    public HybridScorer(HypothesisFactory hypothesisFactory, KernelHelper kernelHelper, boolean weighted) {
        this.alephAccuracyScorer = new AlephAccuracyScorer(hypothesisFactory, weighted);
        this.centeredKTAScorer = new CenteredKTAScorer(hypothesisFactory, kernelHelper, weighted);
    }

    @Override
    public double computeScore(Hypothesis h, int hypothesisNumber, String outputDir) {
        double centeredKTAScore = centeredKTAScorer.computeScore(h, hypothesisNumber, outputDir);
        double accuracyScore = alephAccuracyScorer.computeScore(h, hypothesisNumber, outputDir);
        h.setScore(centeredKTAScore * accuracyScore);
        return h.getScore();
    }

    public synchronized double computerCKTABetween2CenteredMatrices(double [][] m1, double [][] m2) {
        return centeredKTAScorer.computerCKTABetween2CenteredMatrices(m1, m2);
    }
}
