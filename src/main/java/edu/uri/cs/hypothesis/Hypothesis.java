package edu.uri.cs.hypothesis;

import com.igormaznitsa.prologparser.terms.PrologStructure;
import edu.uri.cs.parse.HypothesisParser;
import edu.uri.cs.parse.Language;
import edu.uri.cs.tree.OrTree;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ben on 7/26/18.
 */
public class Hypothesis {

    private Language backgroundLanguage;
    private String hypothesisFile;
    private Language hypothesisLanguage;
    private HashMap<PrologStructure, OrTree> hypothesis;
    private double score = 0.0;
    private boolean isElite = false;

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

    public List<PrologStructure> getMValues() {
        return hypothesis.keySet().stream().collect(Collectors.toList());
    }

    public OrTree getValueForMthStructure(PrologStructure key) {
        return hypothesis.getOrDefault(key, null);
    }

    public boolean isElite() {
        return isElite;
    }

    public void setElite(boolean elite) {
        isElite = elite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hypothesis that = (Hypothesis) o;

        if (Double.compare(that.getScore(), getScore()) != 0) return false;
        if (isElite != that.isElite) return false;
        if (backgroundLanguage != null ? !backgroundLanguage.equals(that.backgroundLanguage) : that.backgroundLanguage != null)
            return false;
        if (hypothesisFile != null ? !hypothesisFile.equals(that.hypothesisFile) : that.hypothesisFile != null)
            return false;
        if (hypothesisLanguage != null ? !hypothesisLanguage.equals(that.hypothesisLanguage) : that.hypothesisLanguage != null)
            return false;
        return hypothesis != null ? hypothesis.equals(that.hypothesis) : that.hypothesis == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = backgroundLanguage != null ? backgroundLanguage.hashCode() : 0;
        result = 31 * result + (hypothesisFile != null ? hypothesisFile.hashCode() : 0);
        result = 31 * result + (hypothesisLanguage != null ? hypothesisLanguage.hashCode() : 0);
        result = 31 * result + (hypothesis != null ? hypothesis.hashCode() : 0);
        temp = Double.doubleToLongBits(getScore());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (isElite ? 1 : 0);
        return result;
    }
}
