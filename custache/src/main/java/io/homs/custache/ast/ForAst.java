package io.homs.custache.ast;

import io.homs.custache.Context;

public class ForAst extends Ast {

    final String ident;
    final ExpressionAst expression;
    final TemplateAst body;

    public ForAst(String templateId, int col, int row, String ident, ExpressionAst expression, TemplateAst body) {
        super(templateId, col, row);
        this.ident = ident;
        this.expression = expression;
        this.body = body;
    }

    @Override
    public String evaluate(Context context) {
        Iterable<?> iterable = expression.evaluateToIterable(context);
        var strb = new StringBuilder();
        for (var element : iterable) {
            Context forContext = new Context(context);
            forContext.def(ident, element);
            strb.append(body.evaluate(forContext));
        }
        return strb.toString();
    }

    @Override
    public String toString() {
        return "{{#" + ident + " " + expression + "}}" + body + "{{/}}";
    }
}