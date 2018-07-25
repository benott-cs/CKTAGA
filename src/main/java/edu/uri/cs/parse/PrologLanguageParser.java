package edu.uri.cs.parse;

import java.util.Objects;

/**
 * Created by Ben on 7/24/18.
 */
public class PrologLanguageParser {

    protected Language language;
    private String prologInformationFile;

    public PrologLanguageParser(String prologInformationFile) {
        language = null;
        this.prologInformationFile = prologInformationFile;
    }

    public void parseLanguage() {
        LanguageParser languageParser = new LanguageParser();
        language = languageParser.retrieveLanguageForPrologFile(prologInformationFile, language);
    }

    public Language retrieveLanguage() {
        if (Objects.isNull(language)) {
            parseLanguage();
        }
        return language;
    }
}
