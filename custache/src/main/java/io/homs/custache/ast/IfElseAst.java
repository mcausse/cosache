package io.homs.custache.ast;

import io.homs.custache.Context;

public class IfElseAst extends Ast {

    final ExpressionAst expression;
    final TemplateAst bodyIf;
    final TemplateAst bodyElse;

    public IfElseAst(String templateId, int col, int row, ExpressionAst expression, TemplateAst bodyIf, TemplateAst bodyElse) {
        super(templateId, col, row);
        this.expression = expression;
        this.bodyIf = bodyIf;
        this.bodyElse = bodyElse;
    }

    @Override
    public String evaluate(Context context) {
        boolean condition = expression.evaluateToBoolean(context);
        if (condition) {
            return bodyIf.evaluate(context);
        } else {
            if (bodyElse == null) {
                return "";
            }
            return bodyElse.evaluate(context);
        }
    }

    @Override
    public String toString() {
        if (bodyElse == null) {
            return "{{if" + expression + "}}" + bodyIf + "{{end}}";
        } else {
            return "{{if" + expression + "}}" + bodyIf + "{{else}}" + bodyElse + "{{end}}";
        }
    }
}