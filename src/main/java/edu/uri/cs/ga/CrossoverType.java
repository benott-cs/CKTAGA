package edu.uri.cs.ga;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ben on 10/16/18.
 */
public enum CrossoverType {
    SURVIVAL(0),
    RULE_SWAP(1),
    OR_SUBTREE_NODE_SWAP(2),
    AND_SUBTREE_NODE_SWAP(3);

    private final int value;

    CrossoverType(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    // Mapping difficulty to difficulty id
    private static final Map<Integer, CrossoverType> _map = new HashMap<>();
    static
    {
        for (CrossoverType crossoverType : CrossoverType.values())
            _map.put(crossoverType.getValue(), crossoverType);
    }

    public static CrossoverType from(int value)
    {
        return _map.get(value);
    }
}
