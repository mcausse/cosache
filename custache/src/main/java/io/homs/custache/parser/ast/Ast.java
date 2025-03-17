package io.homs.custache.parser.ast;

import io.homs.custache.eval.Context;
import lombok.Getter;

@Getter
public abstract class Ast {

    final String templateUrn;
    final int col;
    final int row;

    protected Ast(String templateUrn, int row, int col) {
        this.templateUrn = templateUrn;
        this.col = col;
        this.row = row;
    }

    public abstract String evaluate(Context context);
}