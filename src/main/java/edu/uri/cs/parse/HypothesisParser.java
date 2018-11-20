package edu.uri.cs.parse;

import com.igormaznitsa.prologparser.terms.AbstractPrologTerm;
import com.igormaznitsa.prologparser.terms.PrologAtom;
import com.igormaznitsa.prologparser.terms.PrologStructure;
import edu.uri.cs.tree.AndTree;
import edu.uri.cs.tree.OrTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ben on 7/24/18.
 */
public class HypothesisParser extends LanguageParser {

    private HashMap<PrologStructure, OrTree> concepts = new HashMap<>();

    public HypothesisParser() {
    }

    @Override
    protected void processPrologStructure(AbstractPrologTerm term, Language language, int optionalArity) {
        super.processPrologStructure(term, language, optionalArity);
        switch (term.getType()) {
            case STRUCT:
            case LIST:
                if (term.getText().contains(":-")) {
                    PrologStructure s = (PrologStructure) term;
                    updateConceptFromClause(s, language);
                }
                break;
            default:
                break;
        }
    }

    private void updateConceptFromClause(PrologStructure prologStructure, Language language) {
        PrologStructure concept = (PrologStructure) prologStructure.getElement(0);
        // we have a valid clause here
        if (PrologAtom.class.isInstance(concept.getFunctor()) &&
                language.getAvailableAtoms().contains(concept.getFunctor())) {
            if (!concepts.containsKey(concept)) {
                concepts.put(concept, new OrTree());
            }
        }
        AndTree andTree = createAndTreeFromRHS((PrologStructure)prologStructure.getElement(1), language);
        OrTree orTree = concepts.getOrDefault(concept, null);
        if (orTree != null) {
            orTree.addIterm(andTree);
        }
    }

    private AndTree createAndTreeFromRHS(PrologStructure prologStructure, Language language) {
        List<PrologStructure> allAtomsStructure = getAllAtomsInStructure(prologStructure, language);
        AndTree andTree = new AndTree();
        for (PrologStructure s : allAtomsStructure) {
            andTree.addIterm(s);
        }
        andTree.generateTree();
        return andTree;
    }

    private List<PrologStructure> getAllAtomsInStructure(PrologStructure prologStructure, Language language) {
        List<PrologStructure> allAtomsStructure = new ArrayList<>();
        addStructureIfAtom(prologStructure, language, allAtomsStructure);
        for (int i = 0; i < prologStructure.getArity(); i++) {
            AbstractPrologTerm t = prologStructure.getElement(i);
            // we have an atom with values filled in
            if (PrologStructure.class.isInstance(t)) {
                PrologStructure s = (PrologStructure) t;
                if (!addStructureIfAtom(s, language, allAtomsStructure)) {
                    allAtomsStructure.addAll(getAllAtomsInStructure(s, language));
                }
            }
        }
        return allAtomsStructure;
    }

    private boolean addStructureIfAtom(PrologStructure prologStructure, Language language,
                                    List<PrologStructure> allAtomsStructure) {
        boolean ret = false;
        if (PrologAtom.class.isInstance(prologStructure.getFunctor()) &&
                language.getAvailableAtoms().contains(prologStructure.getFunctor())) {
            ret = allAtomsStructure.add(prologStructure);
        }
        return ret;
    }

    public HashMap<PrologStructure, OrTree> getConcepts() {
        return concepts;
    }
}
