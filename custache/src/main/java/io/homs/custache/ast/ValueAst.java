package io.homs.custache.ast;

import io.homs.custache.Context;

public class ValueAst extends Ast {

    public static final String CONTEXT_PARAM_AUTOTRIM_VALUES = "CONTEXT_PARAM_AUTOTRIM_VALUES";

    final ExpressionAst expressionAst;

    public ValueAst(String templateId, int col, int row, ExpressionAst expressionAst) {
        super(templateId, col, row);
        this.expressionAst = expressionAst;
    }

    @Override
    public String evaluate(Context context) {
        String v = expressionAst.evaluate(context);
        if (context.find(CONTEXT_PARAM_AUTOTRIM_VALUES) != null && (Boolean) context.get(CONTEXT_PARAM_AUTOTRIM_VALUES)) {
            v = v.trim();
        }
        return v;
    }

    @Override
    public String toString() {
        return "{{" + expressionAst + "}}";
    }
}