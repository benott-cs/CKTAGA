package edu.uri.cs.ga.scoring;

import edu.uri.cs.ga.CrossoverType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ben on 12/27/18.
 */
public enum ScoringType {
    RANDOM(0),
    ACCURACY(1),
    CENTERED_KTA(2),
    ACCUR_TIMES_CKTA(3);

    private final int value;

    ScoringType(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    // Mapping difficulty to difficulty id
    private static final Map<Integer, ScoringType> _map = new HashMap<>();
    static
    {
        for (ScoringType scoringType : ScoringType.values())
            _map.put(scoringType.getValue(), scoringType);
    }

    public static ScoringType from(int value)
    {
        return _map.get(value);
    }
}
