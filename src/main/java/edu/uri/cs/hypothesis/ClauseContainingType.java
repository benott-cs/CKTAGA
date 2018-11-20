package edu.uri.cs.hypothesis;

import com.igormaznitsa.prologparser.terms.AbstractPrologTerm;
import com.igormaznitsa.prologparser.terms.PrologStructure;
import edu.uri.cs.tree.AndTree;

/**
 * Created by Ben on 11/15/18.
 */
public class ClauseContainingType {

    private Class<?> type;
    private AbstractPrologTerm abstractPrologTerm;
    private AndTree clause;
    private PrologStructure head;

    public ClauseContainingType(Class<?> type, AndTree andTree, AbstractPrologTerm abstractPrologTerm) {
        this.type = type;
        this.clause = andTree;
        this.abstractPrologTerm = abstractPrologTerm;
    }

    public ClauseContainingType(Class<?> type, AndTree andTree, PrologStructure head, AbstractPrologTerm abstractPrologTerm) {
        this(type, andTree, head);
        this.abstractPrologTerm = abstractPrologTerm;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public AbstractPrologTerm getAbstractPrologTerm() {
        return abstractPrologTerm;
    }

    public void setAbstractPrologTerm(AbstractPrologTerm abstractPrologTerm) {
        this.abstractPrologTerm = abstractPrologTerm;
    }

    public AndTree getClause() {
        return clause;
    }

    public void setClause(AndTree clause) {
        this.clause = clause;
    }

    public PrologStructure getHead() {
        return head;
    }

    public void setHead(PrologStructure head) {
        this.head = head;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClauseContainingType that = (ClauseContainingType) o;

        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
        if (getAbstractPrologTerm() != null ? !getAbstractPrologTerm().equals(that.getAbstractPrologTerm()) : that.getAbstractPrologTerm() != null)
            return false;
        if (getClause() != null ? !getClause().equals(that.getClause()) : that.getClause() != null) return false;
        return getHead() != null ? getHead().equals(that.getHead()) : that.getHead() == null;
    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (getAbstractPrologTerm() != null ? getAbstractPrologTerm().hashCode() : 0);
        result = 31 * result + (getClause() != null ? getClause().hashCode() : 0);
        result = 31 * result + (getHead() != null ? getHead().hashCode() : 0);
        return result;
    }
}
