package edu.uri.cs.ga;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ben on 11/5/18.
 */
public enum UpwardRefinementType {
    CONSTANT(0),
    VARIABLE(1),
    LITERAL_REMOVAL(2);

    private final int value;

    UpwardRefinementType(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    // Mapping difficulty to difficulty id
    private static final Map<Integer, UpwardRefinementType> _map = new HashMap<>();
    static
    {
        for (UpwardRefinementType refinementType : UpwardRefinementType.values())
            _map.put(refinementType.getValue(), refinementType);
    }

    public static UpwardRefinementType from(int value)
    {
        return _map.get(value);
    }
}
