package io.homs.custache.ast;

import io.homs.custache.Context;
import io.homs.custache.Evaluation;

public class TextAst extends Ast {

    @Deprecated
    public static final String TRIM_TEXT_AST = "TRIM_TEXT_AST";

    final Evaluation evaluation = new Evaluation();

    final String text;

    public TextAst(String templateId, int col, int row, String text) {
        super(templateId, col, row);
        this.text = text;

    }

    @Override
    public String evaluate(Context context) {
        // TODO que aquesta lògica la faci el parser, no cada vegada evaluant, tu
        if (context.find(TRIM_TEXT_AST) != null && Evaluation.isTrue(context.get(TRIM_TEXT_AST))) {
            return text.replaceAll("\\n\\s*$", "");
        } else {
            return text;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}