package edu.uri.cs.tree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Ben on 7/26/18.
 */
public class EnumeratedTree<T> {

    private int numericAssignment;
    private int treeSize;

    /// Note that only two children are ever permitted
    // Internal nodes
    private List<EnumeratedTree<T>> children = new ArrayList<>();
    // Leaf nodes
    private Map<T, Integer> leafChildren = new HashMap<>();

    public EnumeratedTree(List<T> objects) {
        treeSize = objects.size();
        // create a balanced tree
        if (objects.size() > 3) {
            int partitionSize = Math.round(objects.size() / 2);
            List<T> firstHalf = objects.stream().limit(partitionSize).collect(Collectors.toList());
            objects.removeAll(firstHalf);
            children.add(new EnumeratedTree<T>(firstHalf));
            children.add(new EnumeratedTree<T>(objects));
        } else if (objects.size() == 3) {
            T leafNode = objects.remove(0);
            leafChildren.put(leafNode, 0);
            children.add(new EnumeratedTree<T>(objects));
        } else {
            for (T input : objects) {
                leafChildren.put(input, 0);
            }
        }
    }

    public void assignNumeralsToNodes() {
        Queue<EnumeratedTree<T>> queue = new LinkedList<EnumeratedTree<T>>();
        EnumeratedTree root = this;
        queue.clear();
        queue.add(root);
        int index = 0;
        while (!queue.isEmpty()) {
            EnumeratedTree<T> node = queue.remove();
            node.setNumericAssignment(index);
            index++;
            for (T leaf : node.leafChildren.keySet()) {
                node.leafChildren.put(leaf, index);
                index++;
            }
            queue.addAll(node.children);
        }
    }

    public Object getNthNode(int n) {
        if (n >= treeSize) {
            throw new IllegalArgumentException("N must be less than or equal to tree size: " + treeSize);
        }
        if (numericAssignment == n) {
            return this;
        }
        if (leafChildren.values().contains(n)) {
            for (T leaf : leafChildren.keySet()) {
                if (leafChildren.get(leaf) == n) {
                    return leaf;
                }
            }
        }
        Object ret = null;
        for (EnumeratedTree t : children) {
            ret = getNthNode(n);
            if (ret != null) {
                return ret;
            }
        }
        return ret;
    }

    public int getNumericAssignment() {
        return numericAssignment;
    }

    public void setNumericAssignment(int numericAssignment) {
        this.numericAssignment = numericAssignment;
    }
}
