package edu.uri.cs.ga.scoring;

import edu.uri.cs.aleph.HypothesisFactory;
import edu.uri.cs.ga.scoring.kernel.KernelHelper;

/**
 * Created by Ben on 12/27/18.
 */
public class HypothesisScorerFactory {

    public HypothesisScorerFactory() {

    }

    public HypothesisScorerIF getHypothesisScorer(HypothesisFactory hypothesisFactory,
                                                  KernelHelper kernelHelper, boolean weighted,
                                                  ScoringType scoringType) {
        HypothesisScorerIF ret = null;
        switch (scoringType) {
            case RANDOM:
                ret = new RandomScorer();
                break;
            case ACCURACY:
                ret = new AlephAccuracyScorer(hypothesisFactory, weighted);
                break;
            case CENTERED_KTA:
                ret = new CenteredKTAScorer(hypothesisFactory, kernelHelper,weighted);
                break;
            case ACCUR_TIMES_CKTA:
                ret = new HybridScorer(hypothesisFactory, kernelHelper,weighted);
                break;
            case CENTERED_KTA_LOG_ACCURACY:
                ret = new CenteredKTAAndLogAccuracy(hypothesisFactory, kernelHelper, weighted);
                break;
        }
        return ret;
    }
}
