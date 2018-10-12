package edu.uri.cs.tree;

import com.igormaznitsa.prologparser.terms.PrologStructure;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.ToString;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Ben on 7/26/18.
 */
@ToString
public class EnumeratedTree<T extends Object> {

    private int numericAssignment;
    private int treeSize;

    protected List<T> childExpressions = new ArrayList();
    /// Note that only two children are ever permitted
    // Internal nodes
    private List<EnumeratedTree<T>> children = new ArrayList<>();
    // Leaf nodes
    private Map<T, Integer> leafChildren = new HashMap<>();

    private Class<T> typeOfT;

    public EnumeratedTree(List<T> objects) {
        setGenericType();
        this.childExpressions = objects;
        initialize();
    }

    public EnumeratedTree() {
        setGenericType();
    }
    @SuppressWarnings("unchecked")
    protected void setGenericType() {
            this.typeOfT = (Class<T>)
                    ((ParameterizedType)getClass()
                            .getGenericSuperclass())
                            .getActualTypeArguments()[0];
    }

    public void addIterm(T item) {
        childExpressions.add(item);
    }

    public void setChildExpressions(List<T> childExpressions) {
        this.childExpressions = childExpressions;
    }

    private void shuffle() {
        Collections.shuffle(childExpressions);
    }

    public void generateTree() {
        initialize();
    }

    private OrTree getOrTree(List<T> items) {
        OrTree tree = new OrTree();
        for (T item : items) {
            tree.addIterm((AndTree)item);
        }
        tree.initialize();
        return tree;
    }

    private AndTree getAndTree(List<T> items) {
        AndTree tree = new AndTree();
        for (T item : items) {
            tree.addIterm((PrologStructure) item);
        }
        tree.initialize();
        return tree;
    }

    public void initialize() {
        shuffle();
        treeSize = childExpressions.size();
        // create a random tree
        if (childExpressions.size() > 3) {
            int partitionSize = (int) Math.ceil(childExpressions.size() * Math.random());
            if (partitionSize == 0) {
                partitionSize++;
            }
            List<T> firstPortion = childExpressions.subList(0, partitionSize - 1);
            List<T> secondPortion = childExpressions.subList(partitionSize, childExpressions.size());

            if (AndTree.class.getName().equals(typeOfT.getName())) {
                children.add((EnumeratedTree)getOrTree(firstPortion));
                children.add((EnumeratedTree)getOrTree(secondPortion));
            } else if (PrologStructure.class.getName().equals(typeOfT.getName())) {
                children.add((EnumeratedTree)getAndTree(firstPortion));
                children.add((EnumeratedTree)getAndTree(secondPortion));
            }

        } else if (childExpressions.size() == 3) {
            leafChildren.put(childExpressions.get(0), 0);
            if (AndTree.class.getName().equals(typeOfT.getName())) {
                children.add((EnumeratedTree)getOrTree(childExpressions.subList(1, 3)));
            } else if (PrologStructure.class.getName().equals(typeOfT.getName())) {
                children.add((EnumeratedTree)getAndTree(childExpressions.subList(1, 3)));
            }
        } else {
            for (T input : childExpressions) {
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

        if (getNumericAssignment() != that.getNumericAssignment()) return false;
        if (treeSize != that.treeSize) return false;
        if (childExpressions != null ? !childExpressions.equals(that.childExpressions) : that.childExpressions != null)
            return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        return leafChildren != null ? leafChildren.equals(that.leafChildren) : that.leafChildren == null;
    }

    @Override
    public int hashCode() {
        int result = getNumericAssignment();
        result = 31 * result + treeSize;
        result = 31 * result + (childExpressions != null ? childExpressions.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (leafChildren != null ? leafChildren.hashCode() : 0);
        return result;
    }
}
