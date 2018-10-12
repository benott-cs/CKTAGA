package edu.uri.cs.tree;

import com.igormaznitsa.prologparser.terms.PrologStructure;
import lombok.ToString;

import java.util.List;

/**
 * Created by Ben on 7/26/18.
 */
@ToString(callSuper = true)
public class AndTree extends EnumeratedTree<PrologStructure> {

    public AndTree(List<PrologStructure> objects) {
        super(objects);
    }

    public AndTree() {
        setGenericType();
    }
}
