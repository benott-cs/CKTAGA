package com.igormaznitsa.prologparser.terms;

import com.igormaznitsa.prologparser.utils.FastStringBuilder;
import com.igormaznitsa.prologparser.utils.StringUtils;

/**
 * Created by Ben on 7/25/18.
 */
public class AlephStringConstant extends AbstractPrologTerm {

    public AlephStringConstant(final String text) {
        super(text);
    }

    public AlephStringConstant(final AbstractPrologTerm term) {
        super(term.getText(), term.getStrPosition(), term.getLineNumber());
    }

    @Override
    public PrologTermType getType() {
        return PrologTermType.ALEPH_STRING;
    }

    @Override
    public String toString() {
        return new FastStringBuilder("\'").append(StringUtils.escapeString(text)).append('\'').toString();
    }
}
