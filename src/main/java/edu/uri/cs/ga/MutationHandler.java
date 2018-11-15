package edu.uri.cs.ga;

import com.igormaznitsa.prologparser.terms.PrologVariable;
import edu.uri.cs.hypothesis.Hypothesis;
import edu.uri.cs.tree.AndTree;
import edu.uri.cs.util.PropertyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public MutationHandler(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    public void initialize() {
        mutationProbability = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_PROB);
        downwardRefinementProbability = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_DOWNWARD_REFINEMENT_PROB);
        downwardRefinementAddLiteralPositiveProbability = propertyManager.getPropAsDouble(PropertyManager.CRKTAGA_MUTATION_DOWNWARD_LITERAL_ADD_POS);
        ignorePattern = propertyManager.getProperty(PropertyManager.CRKTAGA_MUTATION_IGNORE_PATTERN);

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

    private void performDownwardRefinement(Hypothesis h) {
        DownwardRefinementType refinementType =
                DownwardRefinementType.from(Utils.getIndexOfLeastExceedingNumber(Math.random(), downwardProbList));
        switch (refinementType) {
            case CONSTANT: {
                int i = 0;
                // get an or tree
                AndTree a = h.getValueForMthStructure(h.getRandomRule()).getRandomChildExpression();
                // get non-example name terms
                h.getHypothesisLanguage().getAvailableTerms().stream().
                        filter(t -> !Pattern.matches(ignorePattern, t.getText())).
                        collect(Collectors.toList());
                // get variables in language
                h.getHypothesisLanguage().getAvailableTerms().stream().
                        filter(t -> t instanceof PrologVariable).
                        collect(Collectors.toList());
                // Create a Pattern object
                break;
            }
            case VARIABLE: {
                int i = 0;
                break;
            }
            case LITERAL_ADDITION:
                break;
            default:
                break;
        }
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
