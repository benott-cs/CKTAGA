package edu.uri.cs;

import com.igormaznitsa.prologparser.terms.AbstractPrologTerm;
import com.igormaznitsa.prologparser.terms.PrologAtom;
import edu.uri.cs.parse.HypothesisParser;
import edu.uri.cs.parse.Language;
import edu.uri.cs.parse.PrologLanguageParser;

/**
 * Created by Ben on 7/24/18.
 */
public class CKTAGA {



    public static void main(String[] args) {
        PrologLanguageParser backgroundParser = new PrologLanguageParser("/home/Ben/Aleph/Mutagenesis/42/mutagenesis_42.b.sans_modes");
        HypothesisParser hypothesisParser = new HypothesisParser("/home/Ben/Aleph/Mutagenesis/42/generated_theory_1.pl", backgroundParser);
        Language language = hypothesisParser.retrieveLanguage();
        System.out.println("==============");
        System.out.println("= Atoms");
        System.out.println("==============");
        for (PrologAtom a : language.getAvailableAtoms()) {
            System.out.println(a);
        }
        System.out.println("==============");
        System.out.println("= Terms");
        System.out.println("==============");
        for (AbstractPrologTerm t : language.getAvailableTerms()) {
            System.out.println(t);
        }
    }
}
