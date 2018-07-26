package edu.uri.cs.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ben on 7/26/18.
 */
public class GenericTree<T> {

    private List<T> childExpressions = new ArrayList<>();
    private EnumeratedTree<T> tree = null;

    public GenericTree() {

    }

    public void addIterm(T item) {
        childExpressions.add(item);
    }

    private void shuffle() {
        Collections.shuffle(childExpressions);
    }

    public void generateTree() {
        shuffle();
        tree = new EnumeratedTree<T>(childExpressions);
        tree.assignNumeralsToNodes();
    }

    public Object getNthNode(int n) {
        return tree.getNthNode(n);
    }
}
