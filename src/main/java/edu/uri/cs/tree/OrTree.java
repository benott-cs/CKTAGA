package edu.uri.cs.tree;

import com.igormaznitsa.prologparser.terms.PrologStructure;
import lombok.ToString;

import java.util.List;

/**
 * Created by Ben on 7/26/18.
 */
@ToString(callSuper = true)
public class OrTree extends EnumeratedTree<AndTree> {

    public OrTree(List<AndTree> objects) {
        super(objects);
    }

    public OrTree() {
        setGenericType();
    }
}
