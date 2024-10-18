package io.homs.custache.ast;

import io.homs.custache.Context;

public class TextAst extends Ast {

    final String text;

    public TextAst(String templateId, int col, int row, String text) {
        super(templateId, col, row);
        this.text = text;

    }

    @Override
    public String evaluate(Context context) {
        return text
                .replaceAll("\\n\\s*$", ""); // TODO
    }

    @Override
    public String toString() {
        return text;
    }
}