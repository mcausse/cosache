package io.homs.custache.ast;

import io.homs.custache.Context;
import io.homs.custache.Evaluation;

public class TextAst extends Ast {

    final Evaluation evaluation = new Evaluation();

    final String text;
    final String textToRender;

    public TextAst(String templateId, int col, int row, String text) {
        super(templateId, col, row);
        this.text = text;
        this.textToRender = text.replaceAll("\\n\\s*$", "");
    }

    @Override
    public String evaluate(Context context) {
        return textToRender;
    }

    @Override
    public String toString() {
        return text;
    }
}