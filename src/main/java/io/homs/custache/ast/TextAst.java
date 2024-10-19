package io.homs.custache.ast;

import io.homs.custache.Context;
import io.homs.custache.Evaluation;

public class TextAst extends Ast {

    public static final String TRIM_TEXT_AST = "TRIM_TEXT_AST";

    final Evaluation evaluation = new Evaluation();

    final String text;

    public TextAst(String templateId, int col, int row, String text) {
        super(templateId, col, row);
        this.text = text;

    }

    @Override
    public String evaluate(Context context) {

        if (context.find(TRIM_TEXT_AST) != null && Evaluation.isTrue(context.get(TRIM_TEXT_AST))) {
            return text
                    .replaceAll("\\n\\s*$", ""); // TODO
        } else {
            return text;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}