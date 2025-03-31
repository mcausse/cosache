package io.homs.custache.parser.ast;

import io.homs.custache.eval.Context;

public class CommentAst extends Ast {

    final TemplateAst ignoredBody;

    public CommentAst(String templateId, int row, int col, TemplateAst ignoredBody) {
        super(templateId, row, col);
        this.ignoredBody = ignoredBody;
    }

    @Override
    public String evaluate(Context context) {
        return "";
    }
}