package edu.uri.cs.hypothesis;

import edu.uri.cs.parse.HypothesisParser;
import edu.uri.cs.parse.Language;

/**
 * Created by Ben on 7/26/18.
 */
public class Hypothesis {

    private Language backgroundLanguage;
    private String hypothesisFile;
    private Language hypothesisLanguage;
    private double score = 0.0;

    public Hypothesis(Language backgroundLanguage, String hypothesisFile) {
        this.backgroundLanguage = backgroundLanguage;
        this.hypothesisFile = hypothesisFile;
    }

    public void initialize() {
        HypothesisParser hypothesisParser = new HypothesisParser(
                hypothesisFile,
                backgroundLanguage);
        hypothesisLanguage = hypothesisParser.retrieveLanguage(true);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void printHypothesisLanguage() {
        hypothesisLanguage.printAtomsAndTerms();
    }
}
