package io.homs.custache.parser.ast;

import io.homs.custache.eval.Context;

public class IfElseAst extends Ast {

    final ExpressionAst expression;
    final TemplateAst bodyIf;
    final TemplateAst bodyElse;
    final boolean negate;

    public IfElseAst(String templateId, int row, int col, ExpressionAst expression, TemplateAst bodyIf, TemplateAst bodyElse, boolean negate) {
        super(templateId, row, col);
        this.expression = expression;
        this.bodyIf = bodyIf;
        this.bodyElse = bodyElse;
        this.negate = negate;
    }

    @Override
    public String evaluate(Context context) {
        boolean condition = expression.evaluateToBoolean(context);

        if (negate) {
            condition = !condition;
        }

        if (condition) {
            return bodyIf.evaluate(context);
        } else {
            if (bodyElse == null) {
                return "";
            }
            return bodyElse.evaluate(context);
        }
    }
}