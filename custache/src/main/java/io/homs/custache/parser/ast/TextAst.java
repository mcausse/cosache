package io.homs.custache.parser.ast;

import io.homs.custache.eval.Context;
import io.homs.custache.eval.Evaluation;

public class TextAst extends Ast {

    final Evaluation evaluation = new Evaluation();

    final String text;
    final String textToRender;

    public TextAst(String templateId, int row, int col, String text) {
        super(templateId, row, col);
        this.text = text;
        this.textToRender = text.replaceAll("\\n\\s*$", "");
    }

    @Override
    public String evaluate(Context context) {
        return textToRender;
    }
}