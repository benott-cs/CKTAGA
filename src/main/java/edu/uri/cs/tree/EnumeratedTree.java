package edu.uri.cs.tree;

import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Ben on 7/26/18.
 */
@ToString
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
        // create a random tree
        if (objects.size() > 3) {
            int partitionSize = (int) Math.ceil(objects.size() * Math.random());
            if (partitionSize == 0) {
                partitionSize++;
            }
//            List<T> firstPortion = objects.stream().limit(partitionSize).collect(Collectors.toList());
            List<T> firstPortion = objects.subList(0,partitionSize - 1);
            List<T> secondPortion = objects.subList(partitionSize, objects.size());
            children.add(new EnumeratedTree<T>(firstPortion));
            children.add(new EnumeratedTree<T>(secondPortion));
        }
        else if (objects.size() == 3) {
//            T leafNode = objects.remove(0);
            leafChildren.put(objects.get(0), 0);
            children.add(new EnumeratedTree<T>(objects.subList(1,3)));
        }
        else {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnumeratedTree<?> that = (EnumeratedTree<?>) o;

        if (numericAssignment != that.numericAssignment) return false;
        if (treeSize != that.treeSize) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        return leafChildren != null ? leafChildren.equals(that.leafChildren) : that.leafChildren == null;
    }

    @Override
    public int hashCode() {
        int result = numericAssignment;
        result = 31 * result + treeSize;
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (leafChildren != null ? leafChildren.hashCode() : 0);
        return result;
    }
}
