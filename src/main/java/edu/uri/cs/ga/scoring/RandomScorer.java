package edu.uri.cs.ga.scoring;

import edu.uri.cs.hypothesis.Hypothesis;

/**
 * Created by Ben on 7/30/18.
 */
public class RandomScorer implements HypothesisScorerIF {
    @Override
    public double computeScore(Hypothesis h) {
        double score = 0.2 + 0.8*Math.random();
        h.setScore(score);
        return score;
    }
}
