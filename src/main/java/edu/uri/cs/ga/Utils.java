package edu.uri.cs.ga;

import java.util.List;

/**
 * Created by Ben on 11/5/18.
 */
public class Utils {

    public static int getIndexOfLeastExceedingNumber(double number, List<Double> listToCheck) {
        for (int i = 0; i < listToCheck.size(); i++) {
            if (listToCheck.get(i) >= number) {
                return i;
            }
        }
        return listToCheck.size() - 1;
    }
}
