package io.homs.custache.ast;

import io.homs.custache.Context;

public class IfAst extends Ast {

    final ExpressionAst expression;
    final TemplateAst body;

    public IfAst(String templateId, int col, int row, ExpressionAst expression, TemplateAst body) {
        super(templateId, col, row);
        this.expression = expression;
        this.body = body;
    }

    @Override
    public String evaluate(Context context) {
        boolean condition = expression.evaluateToBoolean(context);
        if (condition) {
            return body.evaluate(context);
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return "{{?" + expression + "}}" + body + "{{/}}";
    }
}