package edu.uri.cs.hypothesis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.igormaznitsa.prologparser.terms.*;
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
    private double [][] centeredKernelMatrix;
    private Set<String> examples;
    @JsonIgnore
    private Random random = new Random();
    @JsonIgnore
    private Cloner cloner = new Cloner();

    public Hypothesis(Language backgroundLanguage, String hypothesisFile) {
        this.backgroundLanguage = backgroundLanguage;
        this.hypothesisFile = hypothesisFile;
    }

    public HashMap<PrologStructure, OrTree> getHypothesis() {
        return hypothesis;
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
        List<ClauseContainingType> allVariables = collectVariablesInHypothesis(null, PrologVariable.class);
        return getRandomClauseContainingTypeFromList(allVariables);
    }

    public ClauseContainingType getRandomVariableFromClause(AndTree andTree) {
        List<ClauseContainingType> allVariables = collectVariablesInHypothesis(andTree, PrologVariable.class);
        return getRandomClauseContainingTypeFromList(allVariables);
    }

    public ClauseContainingType getClauseWithRandomConstant() {
        List<ClauseContainingType> allConstants = collectVariablesInHypothesis(null, AlephStringConstant.class);
        allConstants.addAll(collectVariablesInHypothesis(null, PrologFloatNumber.class));
        allConstants.addAll(collectVariablesInHypothesis(null, PrologIntegerNumber.class));
        return getRandomClauseContainingTypeFromList(allConstants);
    }

    private ClauseContainingType getRandomClauseContainingTypeFromList(List<ClauseContainingType> allVariables) {
        ClauseContainingType ret = null;
        if (allVariables.size() > 0) {
            ret = allVariables.get(ThreadLocalRandom.current().nextInt(allVariables.size()));
        }
        return ret;
    }

    public List<AndTree> getListOfClausesWithAtLeastTwoUniqueVariables() {
        List<ClauseContainingType> variables = collectVariablesInHypothesis(null, PrologVariable.class);
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

    public AndTree generateUpwardRefinementForVarOrConst(AndTree a, AbstractPrologTerm providedTerm,
                                                         PrologVariable variableToReplaceProvidedTerm) {
        // first pass to see how many times to replicate each structure
        Iterator<PrologStructure> andTreeIter = a.getAllChildExpressions().iterator();
        Map<PrologStructure, Integer> countsForDuplication = new HashMap<>();
        while (andTreeIter.hasNext()) {
            PrologStructure prologStructure = andTreeIter.next();
            for (int i = 0; i < prologStructure.getArity(); i++) {
                AbstractPrologTerm abstractPrologTerm = prologStructure.getElement(i);
                if (abstractPrologTerm.equals(providedTerm)) {
                    countsForDuplication.merge(prologStructure, 1, (x,y) -> x + y);
                }
            }
        }
        // perform the required number of replications in the AndTree
        for (PrologStructure prologStructure : countsForDuplication.keySet()) {
            int count = countsForDuplication.get(prologStructure);
            // we subtract one because one is already in the AndTree
            int numberOfTimesToReplicate = ((int)Math.pow(2, count)) - 1;
            for (int i = 0; i < numberOfTimesToReplicate; i++) {
                a.addIterm(cloneThisObject(prologStructure));
            }
        }
        // now randomly replace the provided term with the new variable
        andTreeIter = a.getAllChildExpressions().iterator();
        while (andTreeIter.hasNext()) {
            PrologStructure prologStructure = andTreeIter.next();
            for (int i = 0; i < prologStructure.getArity(); i++) {
                AbstractPrologTerm abstractPrologTerm = prologStructure.getElement(i);
                if (abstractPrologTerm.equals(providedTerm) && Math.random() < 0.5) {
                    prologStructure.setElement(i, variableToReplaceProvidedTerm);
                }
            }
        }
        // get counts of identical clauses
        List<PrologStructure> allChildren = a.getAllChildExpressions();
        List<PrologStructure> distinctChildren = allChildren.stream().distinct().collect(Collectors.toList());
        if (allChildren.size() != distinctChildren.size()) {
            a.setChildExpressions(distinctChildren);
        }
        return a;
    }

    private <T> T cloneThisObject(T objectToClone) {
        return cloner.deepClone(objectToClone);
    }

    public <T extends AbstractPrologTerm> List<ClauseContainingType> collectVariablesInHypothesis(
            AndTree filterToThisClause, Class<T> cls) {
        List<ClauseContainingType> allVariables = new ArrayList<>();
        for (PrologStructure p : hypothesis.keySet()) {
            List<T> variablesInHead = new ArrayList<>();
            for (int i = 0; i < p.getArity(); i++) {
                if (cls.isInstance(p.getElement(i))) {
                    variablesInHead.add((T) p.getElement(i));
                }
            }
            for (AndTree a : hypothesis.get(p).getAllChildExpressions()) {
                if (Objects.isNull(filterToThisClause) || a.equals(filterToThisClause)) {
                    for (PrologStructure prologStructure : a.getAllChildExpressions()) {
                        for (int i = 0; i < prologStructure.getArity(); i++) {
                            AbstractPrologTerm abstractPrologTerm = prologStructure.getElement(i);
                            if (cls.isInstance(abstractPrologTerm)) {
                                T variable = (T) abstractPrologTerm;
                                ClauseContainingType clauseContainingType =
                                        new ClauseContainingType(cls, a, abstractPrologTerm);
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

    public List<String> getHypothesisDump() {
        List<String> hypothesisDump = new ArrayList<>();
        for (PrologStructure head : hypothesis.keySet()) {
            OrTree conceptDescription = hypothesis.get(head);
            for (AndTree clause : conceptDescription.getAllChildExpressions()) {
                String clauseString = "";
                for (PrologStructure prologStructure : clause.getAllChildExpressions()) {
                    clauseString += (clauseString.isEmpty() ? prologStructure.getAlephString() :
                            ", " + prologStructure.getAlephString());
                }
                hypothesisDump.add(head.getAlephString() + " :- " + clauseString + ".");
            }
        }
        return hypothesisDump;
    }

    public double[][] getCenteredKernelMatrix() {
        return centeredKernelMatrix;
    }

    public void setCenteredKernelMatrix(double[][] centeredKernelMatrix) {
        this.centeredKernelMatrix = centeredKernelMatrix;
    }

    public Set<String> getExamples() {
        return examples;
    }

    public void setExamples(Set<String> examples) {
        this.examples = examples;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hypothesis that = (Hypothesis) o;

        if (Double.compare(that.getScore(), getScore()) != 0) return false;
        if (isElite() != that.isElite()) return false;
        if (backgroundLanguage != null ? !backgroundLanguage.equals(that.backgroundLanguage) : that.backgroundLanguage != null)
            return false;
        if (hypothesisFile != null ? !hypothesisFile.equals(that.hypothesisFile) : that.hypothesisFile != null)
            return false;
        if (getHypothesisLanguage() != null ? !getHypothesisLanguage().equals(that.getHypothesisLanguage()) : that.getHypothesisLanguage() != null)
            return false;
        if (getHypothesis() != null ? !getHypothesis().equals(that.getHypothesis()) : that.getHypothesis() != null)
            return false;
        if (!Arrays.deepEquals(getCenteredKernelMatrix(), that.getCenteredKernelMatrix())) return false;
        if (getExamples() != null ? !getExamples().equals(that.getExamples()) : that.getExamples() != null)
            return false;
        if (random != null ? !random.equals(that.random) : that.random != null) return false;
        return cloner != null ? cloner.equals(that.cloner) : that.cloner == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = backgroundLanguage != null ? backgroundLanguage.hashCode() : 0;
        result = 31 * result + (hypothesisFile != null ? hypothesisFile.hashCode() : 0);
        result = 31 * result + (getHypothesisLanguage() != null ? getHypothesisLanguage().hashCode() : 0);
        result = 31 * result + (getHypothesis() != null ? getHypothesis().hashCode() : 0);
        temp = Double.doubleToLongBits(getScore());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (isElite() ? 1 : 0);
        result = 31 * result + Arrays.deepHashCode(getCenteredKernelMatrix());
        result = 31 * result + (getExamples() != null ? getExamples().hashCode() : 0);
        result = 31 * result + (random != null ? random.hashCode() : 0);
        result = 31 * result + (cloner != null ? cloner.hashCode() : 0);
        return result;
    }
}
