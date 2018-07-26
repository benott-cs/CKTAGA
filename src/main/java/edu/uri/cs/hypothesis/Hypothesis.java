package edu.uri.cs.hypothesis;

import com.igormaznitsa.prologparser.terms.PrologStructure;
import edu.uri.cs.parse.HypothesisParser;
import edu.uri.cs.parse.Language;
import edu.uri.cs.tree.OrTree;

import java.util.HashMap;

/**
 * Created by Ben on 7/26/18.
 */
public class Hypothesis {

    private Language backgroundLanguage;
    private String hypothesisFile;
    private Language hypothesisLanguage;
    private HashMap<PrologStructure, OrTree> hypothesis;
    private double score = 0.0;

    public Hypothesis(Language backgroundLanguage, String hypothesisFile) {
        this.backgroundLanguage = backgroundLanguage;
        this.hypothesisFile = hypothesisFile;
    }

    public void initialize() {
        HypothesisParser hypothesisParser = new HypothesisParser();
        hypothesisLanguage = hypothesisParser.retrieveLanguageForPrologFile(hypothesisFile, backgroundLanguage);
        hypothesis = hypothesisParser.getConcepts();
        for (OrTree orTree : hypothesis.values()) {
            orTree.generateTree();
        }
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
