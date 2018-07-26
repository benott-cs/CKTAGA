package edu.uri.cs.parse;

import com.igormaznitsa.prologparser.terms.AbstractPrologTerm;
import com.igormaznitsa.prologparser.terms.PrologAtom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ben on 7/24/18.
 */
public class Language {
    private List<PrologAtom> availableAtoms;
    private List<AbstractPrologTerm> availableTerms;

    public Language() {
        availableAtoms = new ArrayList<PrologAtom>();
        availableTerms = new ArrayList<AbstractPrologTerm>();
    }

    public boolean addAtom(PrologAtom atom) {
        boolean addAtom = true;
        for (PrologAtom e : availableAtoms) {
            if (e.getText().equals(atom.getText()) ||
                    e.equals(atom)) {
                addAtom = false;
            }
        }
        if (addAtom) {
            addAtom = availableAtoms.add(atom);
        }
        return addAtom;
    }

    public boolean addTerm(AbstractPrologTerm prologTerm) {
        boolean addTerm = true;
        for (AbstractPrologTerm e : availableTerms) {
            if (e.getText().equals(prologTerm.getText()) ||
                    e.equals(prologTerm)) {
                addTerm = false;
            }
        }
        if (addTerm) {
            addTerm = availableTerms.add(prologTerm);
        }
        return addTerm;
    }

    public List<PrologAtom> getAvailableAtoms() {
        return availableAtoms;
    }

    public List<AbstractPrologTerm> getAvailableTerms() {
        return availableTerms;
    }

    public void printAtomsAndTerms() {
        System.out.println("==============");
        System.out.println("= Atoms");
        System.out.println("==============");
        for (PrologAtom a : getAvailableAtoms()) {
            System.out.println(a);
        }
        System.out.println("==============");
        System.out.println("= Terms");
        System.out.println("==============");
        for (AbstractPrologTerm t : getAvailableTerms()) {
            System.out.println(t);
        }
    }
}
