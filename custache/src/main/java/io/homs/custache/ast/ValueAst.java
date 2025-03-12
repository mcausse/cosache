package io.homs.custache.ast;

import io.homs.custache.Context;

public class ValueAst extends Ast {

    final ExpressionAst expressionAst;

    public ValueAst(String templateId, int row, int col, ExpressionAst expressionAst) {
        super(templateId, row, col);
        this.expressionAst = expressionAst;
    }

    @Override
    public String evaluate(Context context) {
        return expressionAst.evaluate(context);
    }
}