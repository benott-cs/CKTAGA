package edu.uri.cs.hypothesis;

import com.igormaznitsa.prologparser.terms.AbstractPrologTerm;
import com.igormaznitsa.prologparser.terms.PrologStructure;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ben on 12/27/18.
 */
public class VariablesInLiteral {

    private boolean variableInHead = false;
    private boolean mostGeneral = false;
    private List<AbstractPrologTerm> variables;
    private PrologStructure literal;

    public VariablesInLiteral(List<LiteralContainingType> literalVariables, PrologStructure literal,
                              List<LiteralContainingType> headVariables, boolean mostGeneral) {
        this.variables = literalVariables.stream().
                map(m -> m.getAbstractPrologTerm()).collect(Collectors.toList());
        this.literal = literal;
        List<AbstractPrologTerm> headVars = headVariables.stream().
                map(m -> m.getAbstractPrologTerm()).collect(Collectors.toList());
        variableInHead = hasSharedVariable(headVars);
        this.mostGeneral = mostGeneral;
    }

    public boolean hasSharedVariable(List<AbstractPrologTerm> otherVars) {
        boolean ret = false;
        for (AbstractPrologTerm t : otherVars) {
            if(this.variables.contains(t)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariablesInLiteral that = (VariablesInLiteral) o;

        if (isVariableInHead() != that.isVariableInHead()) return false;
        if (isMostGeneral() != that.isMostGeneral()) return false;
        if (getVariables() != null ? !getVariables().equals(that.getVariables()) : that.getVariables() != null)
            return false;
        return getLiteral() != null ? getLiteral().equals(that.getLiteral()) : that.getLiteral() == null;
    }

    @Override
    public int hashCode() {
        int result = (isVariableInHead() ? 1 : 0);
        result = 31 * result + (isMostGeneral() ? 1 : 0);
        result = 31 * result + (getVariables() != null ? getVariables().hashCode() : 0);
        result = 31 * result + (getLiteral() != null ? getLiteral().hashCode() : 0);
        return result;
    }

    public boolean isMostGeneral() {
        return mostGeneral;
    }

    public void setMostGeneral(boolean mostGeneral) {
        this.mostGeneral = mostGeneral;
    }

    public boolean isVariableInHead() {
        return variableInHead;
    }

    public void setVariableInHead(boolean variableInHead) {
        this.variableInHead = variableInHead;
    }

    public List<AbstractPrologTerm> getVariables() {
        return variables;
    }

    public void setVariables(List<AbstractPrologTerm> variables) {
        this.variables = variables;
    }

    public PrologStructure getLiteral() {
        return literal;
    }

    public void setLiteral(PrologStructure literal) {
        this.literal = literal;
    }
}
