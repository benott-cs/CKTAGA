package edu.uri.cs.parse;

import com.igormaznitsa.prologparser.PrologParser;
import com.igormaznitsa.prologparser.exceptions.PrologParserException;
import com.igormaznitsa.prologparser.terms.*;
import edu.uri.cs.util.FileReaderUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by Ben on 7/24/18.
 */
public class LanguageParser {

    private PrologParser parser = new PrologParser(null);

    public LanguageParser() {
    }

    public Language retrieveLanguageForPrologFile(String filename) {
        return retrieveLanguageForPrologFile(filename, null);
    }

    public Language retrieveLanguageForPrologFile(String filename, Language language) {
        if (Objects.isNull(language)) {
            language = new Language();
        }
        String hypothesis = null;
        try {
            hypothesis = FileReaderUtils.readFileAsString(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Objects.nonNull(hypothesis)) {
            try {
                retrieveLanguageFromString(hypothesis, language);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (PrologParserException e) {
                e.printStackTrace();
            }
        }
        return language;
    }

    protected void retrieveLanguageFromString(String theory, Language language) throws IOException, PrologParserException {
        AbstractPrologTerm term = parser.nextSentence(theory);
        // structure will be null when the end of the theory has been reached
        while (term != null) {
            processPrologStructure(term, language);
            term = parser.nextSentence();
        }
    }

    protected void processPrologStructure(AbstractPrologTerm term, Language language) {
        switch (term.getType()) {
            case VAR:
            case ALEPH_STRING:
                language.addTerm(term);
                break;
            case ATOM:
                if (PrologAtom.class.isInstance(term)) {
                    language.addAtom((PrologAtom) term);
                } else {
                    language.addTerm((AbstractPrologNumericTerm)term);
                }
                break;
            case STRUCT:
            case LIST:
                PrologStructure structure = (PrologStructure) term;
                processPrologStructure(structure.getFunctor(), language);
                for (int i = 0; i < structure.getArity(); i++) {
                    if (Objects.nonNull(structure.getElement(i))) {
                        processPrologStructure(structure.getElement(i), language);
                    }
                }
                break;
            case OPERATOR:
                System.out.println(term + " is an OPERATOR and is not supported");
                break;
            case OPERATORS:
                System.out.println(term + " is an OPERATORS and is not supported");
                break;
            default:
                break;
        }
    }
}
