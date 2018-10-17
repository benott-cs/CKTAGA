package edu.uri.cs.hypothesis;

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
    private static final Map<Integer, CrossoverType> _map = new HashMap<Integer, CrossoverType>();
    static
    {
        for (CrossoverType difficulty : CrossoverType.values())
            _map.put(difficulty.getValue(), difficulty);
    }

    public static CrossoverType from(int value)
    {
        return _map.get(value);
    }
}
