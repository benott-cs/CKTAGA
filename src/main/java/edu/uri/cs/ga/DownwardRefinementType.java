package edu.uri.cs.ga;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ben on 11/5/18.
 */
public enum DownwardRefinementType {
    CONSTANT(0),
    VARIABLE(1),
    LITERAL_ADDITION(2);

    private final int value;

    DownwardRefinementType(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    // Mapping difficulty to difficulty id
    private static final Map<Integer, DownwardRefinementType> _map = new HashMap<>();
    static
    {
        for (DownwardRefinementType refinementType : DownwardRefinementType.values())
            _map.put(refinementType.getValue(), refinementType);
    }

    public static DownwardRefinementType from(int value)
    {
        return _map.get(value);
    }
}
