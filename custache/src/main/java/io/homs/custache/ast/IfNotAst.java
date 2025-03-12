package io.homs.custache.ast;

import io.homs.custache.Context;

public class IfNotAst extends Ast {

    final ExpressionAst expression;
    final TemplateAst body;

    public IfNotAst(String templateId, int row, int col, ExpressionAst expression, TemplateAst body) {
        super(templateId, row, col);
        this.expression = expression;
        this.body = body;
    }

    @Override
    public String evaluate(Context context) {
        boolean condition = expression.evaluateToBoolean(context);
        if (condition) {
            return "";
        } else {
            return body.evaluate(context);
        }
    }
}