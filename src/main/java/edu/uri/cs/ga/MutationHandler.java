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
                    h.refineVariable(variable, constant);
                    success = true;
                    break;
                }
                case VARIABLE: {
                    List<AndTree> andTrees = h.getListOfClausesWithAtLeastTwoUniqueVariables();
                    AndTree refineThis = andTrees.get(ThreadLocalRandom.current().nextInt(andTrees.size()));
                    if (andTrees.size() > 0) {
                        ClauseContainingType variable1 = h.getRandomVariableFromClause(refineThis);
                        ClauseContainingType variable2 = h.getRandomVariableFromClause(refineThis);
                        while(variable1.getAbstractPrologTerm().equals(variable2.getAbstractPrologTerm())) {
                            variable2 = h.getRandomVariableFromClause(refineThis);
                        }
                        h.refineVariable(variable1, variable2.getAbstractPrologTerm());
                        success = true;
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
            arguments[i] = new PrologVariable(CRKTAGA_CREATED_VARIABLE_NAME_START + variableCounter);
            variableCounter++;
        }
        PrologStructure prologStructure = new PrologStructure(prologAtom, arguments);
        return prologStructure;
    }

    private void performUpwardRefinement(Hypothesis h) {
        UpwardRefinementType refinementType =
                UpwardRefinementType.from(Utils.getIndexOfLeastExceedingNumber(Math.random(), upwardProbList));
        switch (refinementType) {
            case CONSTANT: {
                break;
            }
            case VARIABLE: {
                break;
            }
            case LITERAL_REMOVAL:
                break;
            default:
                break;
        }
    }
}
