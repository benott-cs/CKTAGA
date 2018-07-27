package edu.uri.cs.hypothesis;

import edu.uri.cs.parse.Language;
import edu.uri.cs.parse.PrologLanguageParser;

import java.util.HashMap;

/**
 * Created by Ben on 7/26/18.
 */
public class HypothesisManager {

    private String backgroundFile = null;
    private PrologLanguageParser backgroundParser = null;
    private Language backgroundLanguage = null;
    private HashMap<String, Hypothesis> hypothesisMap = new HashMap<>();
    private boolean initialized = false;

    public HypothesisManager(String backgroundFile) {
        this.backgroundFile = backgroundFile;
    }

    public synchronized void initialize() {
        backgroundParser = new PrologLanguageParser(backgroundFile);
        backgroundLanguage = backgroundParser.retrieveLanguage(false);
        initialized = true;
    }

    public synchronized void readHypothesisFromFile(String hypothesisFile) {
        if (!initialized) {
            initialize();
        }
        if (!hypothesisMap.containsKey(hypothesisFile)) {
            Hypothesis h = new Hypothesis(backgroundLanguage, hypothesisFile);
            h.initialize();
            hypothesisMap.put(hypothesisFile, h);
        } else {
            System.out.println(hypothesisFile + " has already been processed");
        }
    }

    public synchronized void dumpHypothesisLanguages() {
        for (Hypothesis h : hypothesisMap.values()) {
            h.printHypothesisLanguage();
        }
    }
}
