package io.homs.custache.parser.ast;

import io.homs.custache.eval.Context;

public class CommentAst extends Ast {

    public CommentAst(String templateId, int row, int col) {
        super(templateId, row, col);
    }

    @Override
    public String evaluate(Context context) {
        return "";
    }
}