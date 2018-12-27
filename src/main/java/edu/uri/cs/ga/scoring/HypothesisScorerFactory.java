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
                ret = new AlephAccuracyScorer(hypothesisFactory, false);
                break;
            case CENTERED_KTA:
                ret = new CenteredKTAScorer(hypothesisFactory, kernelHelper,false);
                break;
            case ACCUR_TIMES_CKTA:
                ret = new HybridScorer(hypothesisFactory, kernelHelper,false);
                break;
        }
        return ret;
    }
}
