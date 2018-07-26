package edu.uri.cs.parse;

/**
 * Created by Ben on 7/24/18.
 */
public class HypothesisParser extends PrologLanguageParser {

    public HypothesisParser(String prologInformationFile, Language backgroundLanguage) {
        super(prologInformationFile);
        this.language = backgroundLanguage;
    }

}
