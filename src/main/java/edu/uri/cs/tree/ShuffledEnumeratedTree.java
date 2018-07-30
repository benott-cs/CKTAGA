package edu.uri.cs.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ben on 7/26/18.
 */
public class ShuffledEnumeratedTree<T> {

    private List<T> childExpressions = new ArrayList<>();
    private EnumeratedTree<T> tree = null;

    public ShuffledEnumeratedTree() {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShuffledEnumeratedTree<?> that = (ShuffledEnumeratedTree<?>) o;

        if (childExpressions != null ? !childExpressions.equals(that.childExpressions) : that.childExpressions != null)
            return false;
        return tree != null ? tree.equals(that.tree) : that.tree == null;
    }

    @Override
    public int hashCode() {
        int result = childExpressions != null ? childExpressions.hashCode() : 0;
        result = 31 * result + (tree != null ? tree.hashCode() : 0);
        return result;
    }
}
