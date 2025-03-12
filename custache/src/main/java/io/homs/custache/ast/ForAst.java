package io.homs.custache.ast;

import io.homs.custache.Context;

public class ForAst extends Ast {

    final String ident;
    final ExpressionAst expression;
    final TemplateAst body;

    public ForAst(String templateId, int row, int col, String ident, ExpressionAst expression, TemplateAst body) {
        super(templateId, row, col);
        this.ident = ident;
        this.expression = expression;
        this.body = body;
    }

    @Override
    public String evaluate(Context context) {
        Iterable<?> iterable = expression.evaluateToIterable(context);
        var strb = new StringBuilder();

        int i = 0;
        for (var element : iterable) {
            Context forContext = new Context(context);
            forContext.def(ident, element);

            forContext.def(ident + "-odd", i % 2 == 1);
            forContext.def(ident + "-even", i % 2 == 0);
            forContext.def(ident + "-count", i);
            forContext.def(ident + "-num", i + 1);

            strb.append(body.evaluate(forContext));
            i++;
        }
        return strb.toString();
    }
}