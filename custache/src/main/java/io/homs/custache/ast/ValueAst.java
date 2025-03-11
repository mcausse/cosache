package io.homs.custache.ast;

import io.homs.custache.Context;

public class ValueAst extends Ast {

    final ExpressionAst expressionAst;

    public ValueAst(String templateId, int col, int row, ExpressionAst expressionAst) {
        super(templateId, col, row);
        this.expressionAst = expressionAst;
    }

    @Override
    public String evaluate(Context context) {
        return expressionAst.evaluate(context);
    }

    @Override
    public String toString() {
        return "{{" + expressionAst + "}}";
    }
}