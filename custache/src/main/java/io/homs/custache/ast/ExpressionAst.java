package io.homs.custache.ast;

import io.homs.custache.Context;
import io.homs.custache.CustacheException;
import io.homs.custache.Evaluation;

import java.util.List;

public class ExpressionAst extends Ast {

    final Evaluation evaluation = new Evaluation();

    final List<String> idents;

    public ExpressionAst(String templateId, int row, int col, List<String> idents) {
        super(templateId, row, col);
        this.idents = idents;
    }

    @Override
    public String evaluate(Context context) {
        try {
            return evaluation.evaluateToString(context, idents);
        } catch (Exception e) {
            throw new CustacheException("evaluating expression: " + String.join(".", idents) + ", at: ", getTemplateUrn(), getRow(), getCol(), e);
        }
    }

    public Object evaluateToObject(Context context) {
        try {
            return evaluation.evaluateToObject(context, idents);
        } catch (Exception e) {
            throw new CustacheException("evaluating expression: " + String.join(".", idents) + ", at: ", getTemplateUrn(), getRow(), getCol(), e);
        }
    }

    public boolean evaluateToBoolean(Context context) {
        try {
            return evaluation.evaluateToBoolean(context, idents);
        } catch (Exception e) {
            throw new CustacheException("evaluating expression: " + String.join(".", idents) + ", at: ", getTemplateUrn(), getRow(), getCol(), e);
        }
    }

    public Iterable<?> evaluateToIterable(Context context) {
        try {
            return evaluation.evaluateToIterable(context, idents);
        } catch (Exception e) {
            throw new CustacheException("evaluating expression: " + String.join(".", idents) + ", at: ", getTemplateUrn(), getRow(), getCol(), e);
        }
    }
}