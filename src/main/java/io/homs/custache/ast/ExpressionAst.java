package io.homs.custache.ast;

import io.homs.custache.Context;
import io.homs.custache.Evaluation;

import java.util.List;

public class ExpressionAst extends Ast {

    final Evaluation evaluation = new Evaluation();

    final List<String> idents;

    public ExpressionAst(String templateId, int col, int row, List<String> idents) {
        super(templateId, col, row);
        this.idents = idents;
    }

    @Override
    public String evaluate(Context context) {
        try {
            return evaluation.evaluateToString(context, idents);
        } catch (Exception e) {
            throw new RuntimeException("evaluating expression: " + this + ", at: " + super.toString(), e);
        }
    }

    public Object evaluateToObject(Context context) {
        try {
            return evaluation.evaluateToObject(context, idents);
        } catch (Exception e) {
            throw new RuntimeException("evaluating expression: " + this + ", at: " + super.toString(), e);
        }
    }

    public boolean evaluateToBoolean(Context context) {
        try {
            return evaluation.evaluateToBoolean(context, idents);
        } catch (Exception e) {
            throw new RuntimeException("evaluating expression: " + this + ", at: " + super.toString(), e);
        }
    }

    public Iterable<?> evaluateToIterable(Context context) {
        try {
            return evaluation.evaluateToIterable(context, idents);
        } catch (Exception e) {
            throw new RuntimeException("evaluating expression: " + this + ", at: " + super.toString(), e);
        }
    }

    @Override
    public String toString() {
        return String.join(".", idents);
    }
}