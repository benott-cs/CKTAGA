package edu.uri.cs.ga;

import com.igormaznitsa.prologparser.terms.AbstractPrologTerm;
import com.igormaznitsa.prologparser.terms.PrologAtom;
import com.igormaznitsa.prologparser.terms.PrologStructure;
import com.igormaznitsa.prologparser.terms.PrologVariable;
import edu.uri.cs.hypothesis.ClauseContainingType;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.tree.AndTree;
import edu.uri.cs.util.PropertyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Ben on 11/5/18.
 */
public class MutationHandler {

    private PropertyManager propertyManager;
    private double mutationProbability;
    private double downwardRefinementProbability;
    private double downwardRefinementAddLiteralPositiveProbability;
    private List<Double> downwardProbList = new ArrayList<>();
    private List<Double> upwardProbList = new ArrayList<>();
    private String ignorePattern = "";
    private String atomIgnorePattern = "";
    private final int MAX_REFINEMENT_TRIES_ALLOWED = 5;
    private final int MAX_VARIABLE_SELECTION_RETRIES = 5;
    private static int variableCounter = 0;
    private static final String CRKTAGA_CREATED_VARIABLE_NAME_START = "NEW_VAR_";

    public MutationHandler(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    public void initialize() {
        mutationProbability = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_PROB);
        downwardRefinementProbability = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_DOWNWARD_REFINEMENT_PROB);
        downwardRefinementAddLiteralPositiveProbability = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_DOWNWARD_LITERAL_ADD_POS);
        ignorePattern = propertyManager.getProperty(PropertyManager.CRKTAGA_MUTATION_IGNORE_PATTERN);
        atomIgnorePattern = propertyManager.getProperty(PropertyManager.CRKTAGA_ATOM_MUTATION_IGNORE_PATTERN);

        double tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_DOWNWARD_CONSTANT);
        double sum = tempRate;
        downwardProbList.add(sum);
        tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_DOWNWARD_VARIABLE);
        sum += tempRate;
        downwardProbList.add(sum);
        tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_DOWNWARD_LITERAL_ADD);
        sum += tempRate;
        downwardProbList.add(sum);

        assert sum == 1.0 : "Invalid downward refinement mutation parameters - must sum to one";

        tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_UPWARD_CONSTANT);
        sum = tempRate;
        upwardProbList.add(sum);
        tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_UPWARD_VARIABLE);
        sum += tempRate;
        upwardProbList.add(sum);
        tempRate = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_UPWARD_LITERAL_REMOVE);
        sum += tempRate;
        upwardProbList.add(sum);

        assert sum == 1.0 : "Invalid upward refinement mutation parameters - must sum to one";
    }

    public void mutateHypothesis(Hypothesis h) {
        // If we should do a mutation
        if (Math.random() <= mutationProbability) {
            // pick up or down mutation (refinement) per provided setting
            if (Math.random() <= downwardRefinementProbability) {
                performDownwardRefinement(h);
            } else {
                performUpwardRefinement(h);
            }
        }
    }

    private boolean performDownwardRefinement(Hypothesis h) {
        boolean success = false;
        DownwardRefinementType refinementType;
        int tryCount = 0;
        while (!success && tryCount < MAX_REFINEMENT_TRIES_ALLOWED) {
            refinementType =
                    DownwardRefinementType.from(Utils.getIndexOfLeastExceedingNumber(Math.random(), downwardProbList));
            switch (refinementType) {
                case CONSTANT: {
                    AbstractPrologTerm constant = h.getRandomPrologConstant(ignorePattern);
                    ClauseContainingType variable = h.getClauseWithRandomVariable();
                    if (Objects.nonNull(variable)) {
                        h.refineVariable(variable, constant);
                        success = true;
                    }
                    break;
                }
                case VARIABLE: {
                    List<AndTree> andTrees = h.getListOfClausesWithAtLeastTwoUniqueVariables();
                    if (andTrees.size() > 0) {
                        AndTree refineThis = andTrees.get(ThreadLocalRandom.current().nextInt(andTrees.size()));
                        ClauseContainingType variable1 = h.getRandomVariableFromClause(refineThis);
                        ClauseContainingType variable2 = h.getRandomVariableFromClause(refineThis);
                        if (Objects.nonNull(variable1) && Objects.nonNull(variable2)) {
                            int i = 0;
                            while (variable1.getAbstractPrologTerm().equals(variable2.getAbstractPrologTerm()) &&
                                    i < MAX_VARIABLE_SELECTION_RETRIES) {
                                variable2 = h.getRandomVariableFromClause(refineThis); i++;
                            }
                            if (!variable1.getAbstractPrologTerm().equals(variable2.getAbstractPrologTerm())) {
                                h.refineVariable(variable1, variable2.getAbstractPrologTerm());
                                success = true;
                            }
                        }
                    }
                    break;
                }
                case LITERAL_ADDITION:
                    PrologAtom prologAtom = h.getRandomPrologAtom(atomIgnorePattern);
                    // Get an and tree
                    AndTree andTree = h.getValueForMthStructure(h.getRandomRule()).getRandomChildExpression();
                    // note that we only add negative literals because we only support
                    // Horn clauses; note that negative literals are equivalent to those
                    // appearing in the body of the clause
                    PrologStructure newLiteral = getPrologStructureFromAtom(prologAtom);
                    andTree.addIterm(newLiteral);
                    andTree.generateTree();
                    success = true;
                    break;
                default:
                    break;
            }
            tryCount++;
        }
        return success;
    }

    private PrologStructure getPrologStructureFromAtom(PrologAtom prologAtom) {
        AbstractPrologTerm[] arguments = new AbstractPrologTerm[prologAtom.getArity()];
        for (int i = 0; i < prologAtom.getArity(); i++) {
            arguments[i] = getNextNewVariable();
        }
        PrologStructure prologStructure = new PrologStructure(prologAtom, arguments);
        return prologStructure;
    }

    private PrologVariable getNextNewVariable() {
        String name = CRKTAGA_CREATED_VARIABLE_NAME_START + variableCounter;
        variableCounter++;
        return new PrologVariable(name);
    }

    private boolean performUpwardRefinement(Hypothesis h) {
        boolean success = false;
        UpwardRefinementType refinementType;
        int tryCount = 0;
        while (!success && tryCount < MAX_REFINEMENT_TRIES_ALLOWED) {
            refinementType =
                    UpwardRefinementType.from(Utils.getIndexOfLeastExceedingNumber(Math.random(), upwardProbList));
            switch (refinementType) {
                case CONSTANT: {
                    ClauseContainingType clauseWithConst = h.getClauseWithRandomConstant();
                    if (Objects.nonNull(clauseWithConst)) {
                        AndTree andTree = h.generateUpwardRefinementForVarOrConst(clauseWithConst,
                                getNextNewVariable());
                        andTree.generateTree();
                        success = true;
                    }
                    break;
                }
                case VARIABLE: {
                    ClauseContainingType clauseWithVariable = h.getClauseWithRandomVariable();
                    if (Objects.nonNull(clauseWithVariable)) {
                        AndTree andTree = h.generateUpwardRefinementForVarOrConst(clauseWithVariable,
                                getNextNewVariable());
                        andTree.generateTree();
                        success = true;
                    }
                    break;
                }
                case LITERAL_REMOVAL:
                    // Get an and tree
                    AndTree andTree = h.getValueForMthStructure(h.getRandomRule()).getRandomChildExpression();
                    if (andTree.getAllChildExpressions().size() > 0) {
                        PrologStructure literalToRemove = andTree.getRandomChildExpression();
                        andTree.removeTreeItem(literalToRemove);
                        andTree.generateTree();
                        success = true;
                    }
                    break;
                default:
                    break;
            }
            tryCount++;
        }
        return success;
    }
}
