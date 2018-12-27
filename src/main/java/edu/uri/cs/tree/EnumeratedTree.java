package edu.uri.cs.tree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.igormaznitsa.prologparser.terms.PrologStructure;
import lombok.ToString;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
        generateTree();
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

    public boolean removeTreeItem(T item) {
        boolean ret = false;
        if (childExpressions.contains(item)) {
            childExpressions.remove(item);
            generateTree();
        }
        return ret;
    }

    public void addIterm(T item) {
        childExpressions.add(item);
    }

    public void addIterms(List<T> items) {
        childExpressions.addAll(items);
    }

    public void setChildExpressions(List<T> childExpressions) {
        this.childExpressions = childExpressions;
    }

    @JsonIgnore
    public T getRandomChildExpression() {
        if (childExpressions.size() > 0) {
            return childExpressions.get(ThreadLocalRandom.current().nextInt(childExpressions.size()));
        } else {
            try {
                return typeOfT.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<T> getAllChildExpressions() {
        return childExpressions;
    }

    private void shuffle() {
        Collections.shuffle(childExpressions);
    }

    public void generateTree() {
        initialize();
        assignNumeralsToNodes();
    }

    private OrTree getOrTree(List<T> items) {
        OrTree tree = new OrTree();
        for (T item : items) {
            tree.addIterm((AndTree)item);
        }
        tree.generateTree();
        return tree;
    }

    private AndTree getAndTree(List<T> items) {
        AndTree tree = new AndTree();
        for (T item : items) {
            tree.addIterm((PrologStructure) item);
        }
        tree.generateTree();
        return tree;
    }

    public void initialize() {
        leafChildren.clear();
        children.clear();
        shuffle();
        treeSize = childExpressions.size();
        // create a random tree
        if (childExpressions.size() > 3) {
            int partitionSize = (int) Math.ceil(childExpressions.size() * Math.random());
            if (partitionSize == 0) {
                partitionSize++;
            }
            List<T> firstPortion = childExpressions.subList(0, partitionSize);
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
        treeSize = index;
    }

    public List<T> getNthNode(int n) {
        ArrayList<T> ret = new ArrayList<>();
        if (numericAssignment == n) {
            return addChildrenToList(null);
        }
        if (leafChildren.values().contains(n)) {
            for (T leaf : leafChildren.keySet()) {
                if (leafChildren.get(leaf) == n) {
                    ret.add(leaf);
                    return ret;
                }
            }
        }
        List tmp = null;
        for (EnumeratedTree t : children) {
            tmp = t.getNthNode(n);
            if (tmp != null && !tmp.isEmpty()) {
                ret.addAll(tmp);
                return ret;
            }
        }
        return ret;
    }

    private List<T> addChildrenToList(List<T> expressions) {
        if (Objects.isNull(expressions)) {
            expressions = new ArrayList<>();
        }
        for (T leaf : leafChildren.keySet()) {
            expressions.add(leaf);
        }
        for (EnumeratedTree<T> child : children) {
            expressions = child.addChildrenToList(expressions);
        }
        return expressions;
    }

    public void removeSomeChildExpressions(List<T> expressionsToRemove) {
        for (T expression : expressionsToRemove) {
            if (childExpressions.contains(expression)) {
                childExpressions.remove(expression);
            }
        }
    }

    public int getTreeSize() {
        return treeSize;
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
