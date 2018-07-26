package edu.uri.cs;

import com.igormaznitsa.prologparser.terms.AbstractPrologTerm;
import com.igormaznitsa.prologparser.terms.PrologAtom;
import edu.uri.cs.hypothesis.HypothesisManager;
import edu.uri.cs.parse.HypothesisParser;
import edu.uri.cs.parse.Language;
import edu.uri.cs.parse.PrologLanguageParser;

/**
 * Created by Ben on 7/24/18.
 */
public class CKTAGA {

    public static void main(String[] args) {
        HypothesisManager hypothesisManager = new HypothesisManager("/home/Ben/Aleph/Mutagenesis/42/mutagenesis_42.b.sans_modes");
        hypothesisManager.readHypothesisFromFile("/home/Ben/Aleph/Mutagenesis/42/generated_theory_1.pl");
        hypothesisManager.dumpHypothesisLanguages();
    }
}
