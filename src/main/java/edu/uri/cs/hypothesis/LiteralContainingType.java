package edu.uri.cs.hypothesis;

import com.igormaznitsa.prologparser.terms.AbstractPrologTerm;
import com.igormaznitsa.prologparser.terms.PrologStructure;
import edu.uri.cs.tree.AndTree;

/**
 * Created by Ben on 12/26/18.
 */
public class LiteralContainingType {
    private Class<?> type;
    private AbstractPrologTerm abstractPrologTerm;
    private PrologStructure literal;

    public LiteralContainingType(Class<?> type, PrologStructure literal, AbstractPrologTerm abstractPrologTerm) {
        this.type = type;
        this.literal = literal;
        this.abstractPrologTerm = abstractPrologTerm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LiteralContainingType that = (LiteralContainingType) o;

        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
        if (getAbstractPrologTerm() != null ? !getAbstractPrologTerm().equals(that.getAbstractPrologTerm()) : that.getAbstractPrologTerm() != null)
            return false;
        return getLiteral() != null ? getLiteral().equals(that.getLiteral()) : that.getLiteral() == null;
    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (getAbstractPrologTerm() != null ? getAbstractPrologTerm().hashCode() : 0);
        result = 31 * result + (getLiteral() != null ? getLiteral().hashCode() : 0);
        return result;
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

    public PrologStructure getLiteral() {
        return literal;
    }

    public void setLiteral(PrologStructure literal) {
        this.literal = literal;
    }
}
