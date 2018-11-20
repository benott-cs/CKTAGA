package edu.uri.cs.parse;

import com.igormaznitsa.prologparser.terms.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Ben on 7/24/18.
 */
public class Language {
    private List<PrologAtom> availableAtoms;
    private List<AbstractPrologTerm> availableTerms;
    private Random random = new Random();

    public Language() {
        availableAtoms = new ArrayList<PrologAtom>();
        availableTerms = new ArrayList<AbstractPrologTerm>();
    }

    public AbstractPrologTerm getRandomPrologConstant(String ignorePattern) {
        List<AbstractPrologTerm> constants =
                availableTerms.stream().
                filter(t -> !Pattern.matches(ignorePattern, t.getText())).
                filter(t -> t instanceof AlephStringConstant || t instanceof AbstractPrologNumericTerm).
                collect(Collectors.toList());
        int index = random.nextInt(constants.size());
        return constants.get(index);
    }

    public PrologVariable getRandomPrologVariable(String ignorePattern) {
        List<AbstractPrologTerm> variables =
                availableTerms.stream().
                filter(t -> !Pattern.matches(ignorePattern, t.getText())).
                filter(t -> t instanceof PrologVariable).
                collect(Collectors.toList());
        int index = random.nextInt(variables.size());
        return (PrologVariable)variables.get(index);
    }

    public PrologAtom getRandomPrologAtom(String ignorePattern) {
        List<PrologAtom> atoms =
                availableAtoms.stream().
                        filter(t -> !Pattern.matches(ignorePattern, t.getText())).
                        collect(Collectors.toList());
        int index = random.nextInt(atoms.size());
        return atoms.get(index);
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
