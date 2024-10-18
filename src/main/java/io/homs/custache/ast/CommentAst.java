package io.homs.custache.ast;

import io.homs.custache.Context;

public class CommentAst extends Ast {

    final TextAst textOpt;

    public CommentAst(String templateId, int col, int row, TextAst textOpt) {
        super(templateId, col, row);
        this.textOpt = textOpt;
    }

    @Override
    public String evaluate(Context context) {
        return "";
    }

    @Override
    public String toString() {
        if (textOpt == null) {
            return "{{!}}{{/}}";
        }
        return "{{!}}" + textOpt + "{{/}}";
    }
}