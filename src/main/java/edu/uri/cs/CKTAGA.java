package edu.uri.cs;

import edu.uri.cs.ga.Logenpro;
import edu.uri.cs.util.PropertyManager;

/**
 * Created by Ben on 7/24/18.
 */
public class CKTAGA {

    public static void main(String[] args) {
        PropertyManager propertyManager = new PropertyManager(CKTAGA.class.getClassLoader().getResource("application.properties"));
        propertyManager.loadProperties();
        Logenpro logenpro = new Logenpro(propertyManager);
        logenpro.initialize();
        int i = 0;
    }
}
