package io.homs.custache.ast;

import io.homs.custache.Context;


public abstract class Ast {
    final String templateId;
    final int col;
    final int row;

    protected Ast(String templateId, int col, int row) {
        this.templateId = templateId;
        this.col = col;
        this.row = row;
    }

    public abstract String evaluate(Context context);

    @Override
    public String toString() {
        return templateId + ":" + col + "," + row;
    }
}