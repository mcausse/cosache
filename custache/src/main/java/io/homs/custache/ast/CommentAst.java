package io.homs.custache.ast;

import io.homs.custache.Context;

public class CommentAst extends Ast {

    final TextAst textOpt;

    public CommentAst(String templateId, int row, int col, TextAst textOpt) {
        super(templateId, row, col);
        this.textOpt = textOpt;
    }

    @Override
    public String evaluate(Context context) {
        return "";
    }
}