package edu.uri.cs.hypothesis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.igormaznitsa.prologparser.terms.AbstractPrologTerm;
import com.igormaznitsa.prologparser.terms.PrologAtom;
import com.igormaznitsa.prologparser.terms.PrologStructure;
import com.igormaznitsa.prologparser.terms.PrologVariable;
import com.rits.cloning.Cloner;
import edu.uri.cs.parse.HypothesisParser;
import edu.uri.cs.parse.Language;
import edu.uri.cs.tree.AndTree;
import edu.uri.cs.tree.OrTree;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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
    @JsonIgnore
    private Random random = new Random();
    @JsonIgnore
    private Cloner cloner = new Cloner();

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

    public Language getHypothesisLanguage() {
        return hypothesisLanguage;
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

    public PrologStructure getRandomRule() {
        int index = random.nextInt(hypothesis.keySet().size());
        return getMValues().get(index);
    }

    public OrTree removeRule(PrologStructure prologStructure) {
        return hypothesis.remove(prologStructure);
    }

    public void addRule(PrologStructure prologStructure, OrTree rule) {
        hypothesis.put(prologStructure, rule);
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

    public void refineVariable(ClauseContainingType clauseContainingType, AbstractPrologTerm abstractPrologTerm) {
        PrologStructure head = null;
        AndTree treeToUpdate = null;
        if (Objects.nonNull(clauseContainingType.getHead())) {
            head = cloner.deepClone(clauseContainingType.getHead());
            treeToUpdate = cloner.deepClone(clauseContainingType.getClause());
        }
        if (Objects.isNull(treeToUpdate)) {
            treeToUpdate = clauseContainingType.getClause();
        }
        if (Objects.nonNull(head)) {
            for (OrTree o : hypothesis.values()) {
                o.removeTreeItem(treeToUpdate);
            }
            List<Integer> itemsToReplace = new ArrayList<>();
            for (int i = 0; i < head.getArity(); i++) {
                if (head.getElement(i).equals(clauseContainingType.getAbstractPrologTerm())) {
                    itemsToReplace.add(i);
                }
            }
            for (Integer i : itemsToReplace) {
                head.setElement(i, abstractPrologTerm);
            }
        }
        Iterator<PrologStructure> iter = treeToUpdate.getAllChildExpressions().iterator();
        while (iter.hasNext()) {
            PrologStructure prologStructure = iter.next();
            List<Integer> itemsToReplace = new ArrayList<>();
            for (int i = 0; i < prologStructure.getArity(); i++) {
                if (prologStructure.getElement(i).equals(clauseContainingType.getAbstractPrologTerm())) {
                    itemsToReplace.add(i);
                }
            }
            for (Integer i : itemsToReplace) {
                prologStructure.setElement(i, abstractPrologTerm);
            }
        }

        if (Objects.nonNull(head)) {
            List<AndTree> andTrees = new ArrayList<>();
            andTrees.add(treeToUpdate);
            OrTree orTree = new OrTree(andTrees);
            orTree.generateTree();
            hypothesis.put(head, orTree);
        }
    }

    public ClauseContainingType getClauseWithRandomVariable() {
        List<ClauseContainingType> allVariables = collectVariablesInHypothesis(null);
        return allVariables.get(ThreadLocalRandom.current().nextInt(allVariables.size()));
    }

    public ClauseContainingType getRandomVariableFromClause(AndTree andTree) {
        List<ClauseContainingType> allVariables = collectVariablesInHypothesis(andTree);
        return allVariables.get(ThreadLocalRandom.current().nextInt(allVariables.size()));
    }

    public List<AndTree> getListOfClausesWithAtLeastTwoUniqueVariables() {
        List<ClauseContainingType> variables = collectVariablesInHypothesis(null);
        Map<AndTree, Integer> counts = new HashMap<>();
        for (ClauseContainingType clauseContainingType : variables) {
            if (!counts.containsKey(clauseContainingType.getClause())) {
                counts.put(clauseContainingType.getClause(), 1);
            } else {
                counts.put(clauseContainingType.getClause(),
                        counts.get(clauseContainingType.getClause()) + 1);
            }
        }
        // get and trees with more than 1 variable (i.e. 2 or more)
        List<AndTree> ret = counts.entrySet().stream().
                filter(e -> e.getValue() > 1).
                map(e -> e.getKey()).
                collect(Collectors.toList());
        return ret;
    }

    public List<ClauseContainingType> collectVariablesInHypothesis(AndTree filterToThisClause) {
        List<ClauseContainingType> allVariables = new ArrayList<>();
        for (PrologStructure p : hypothesis.keySet()) {
            List<PrologVariable> variablesInHead = new ArrayList<>();
            for (int i = 0; i < p.getArity(); i++) {
                if (p.getElement(i) instanceof PrologVariable) {
                    variablesInHead.add((PrologVariable) p.getElement(i));
                }
            }
            for (AndTree a : hypothesis.get(p).getAllChildExpressions()) {
                if (Objects.isNull(filterToThisClause) || a.equals(filterToThisClause)) {
                    for (PrologStructure prologStructure : a.getAllChildExpressions()) {
                        for (int i = 0; i < prologStructure.getArity(); i++) {
                            AbstractPrologTerm abstractPrologTerm = prologStructure.getElement(i);
                            if (abstractPrologTerm instanceof PrologVariable) {
                                PrologVariable variable = (PrologVariable) abstractPrologTerm;
                                ClauseContainingType clauseContainingType =
                                        new ClauseContainingType(PrologVariable.class, a, abstractPrologTerm);
                                if (variablesInHead.contains(variable)) {
                                    clauseContainingType.setHead(p);
                                }
                                if (!allVariables.contains(clauseContainingType)) {
                                    allVariables.add(clauseContainingType);
                                }
                            }
                        }
                    }
                }
            }
        }
        return allVariables;
    }

    public AbstractPrologTerm getRandomPrologConstant(String ignorePattern) {
        return hypothesisLanguage.getRandomPrologConstant(ignorePattern);
    }

    public PrologVariable getRandomPrologVariable(String ignorePattern) {
        return hypothesisLanguage.getRandomPrologVariable(ignorePattern);
    }

    public PrologAtom getRandomPrologAtom(String ignorePattern) {
        return hypothesisLanguage.getRandomPrologAtom(ignorePattern);
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
