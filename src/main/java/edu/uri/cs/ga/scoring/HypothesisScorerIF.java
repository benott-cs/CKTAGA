package edu.uri.cs.ga.scoring;

import edu.uri.cs.hypothesis.Hypothesis;

/**
 * Created by Ben on 7/30/18.
 */
public interface HypothesisScorerIF {
    double computeScore(Hypothesis h);
}
