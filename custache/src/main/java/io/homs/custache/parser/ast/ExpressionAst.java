package io.homs.custache.parser.ast;

import io.homs.custache.CustacheException;
import io.homs.custache.eval.Context;
import io.homs.custache.eval.Evaluation;
import lombok.Getter;

import java.util.List;

public class ExpressionAst extends Ast {

    final Evaluation evaluation = new Evaluation();

    @Getter
    final List<String> idents;
    final ExpressionAst expressionArg;

    public ExpressionAst(String templateId, int row, int col, List<String> idents, ExpressionAst expressionArg) {
        super(templateId, row, col);
        this.idents = idents;
        this.expressionArg = expressionArg;
    }

    @Override
    public String evaluate(Context context) {
        try {
            if (expressionArg == null) {
                return evaluation.evaluateToString(context, idents);
            } else {
                return evaluation.evaluateToString(context, idents, expressionArg.evaluateToObject(context));
            }
        } catch (Exception e) {
            throw new CustacheException("evaluating expression: " + this + ", at: ", getTemplateUrn(), getRow(), getCol(), e);
        }
    }

    public Object evaluateToObject(Context context) {
        try {
            if (expressionArg == null) {
                return evaluation.evaluateToObject(context, idents);
            } else {
                return evaluation.evaluateToObject(context, idents, expressionArg.evaluateToObject(context));
            }
        } catch (Exception e) {
            throw new CustacheException("evaluating expression: " + this + ", at: ", getTemplateUrn(), getRow(), getCol(), e);
        }
    }

    public boolean evaluateToBoolean(Context context) {
        try {
            if (expressionArg == null) {
                return evaluation.evaluateToBoolean(context, idents);
            } else {
                return evaluation.evaluateToBoolean(context, idents, expressionArg.evaluateToObject(context));
            }
        } catch (Exception e) {
            throw new CustacheException("evaluating expression: " + this + ", at: ", getTemplateUrn(), getRow(), getCol(), e);
        }
    }

    public Iterable<?> evaluateToIterable(Context context) {
        try {
            if (expressionArg == null) {
                return evaluation.evaluateToIterable(context, idents);
            } else {
                return evaluation.evaluateToIterable(context, idents, expressionArg.evaluateToObject(context));
            }
        } catch (Exception e) {
            throw new CustacheException("evaluating expression: " + this + ", at: ", getTemplateUrn(), getRow(), getCol(), e);
        }
    }

    @Override
    public String toString() {
        if (expressionArg == null) {
            return String.join(".", idents);
        } else {
            return String.join(".", idents) + "(" + expressionArg + ")";
        }
    }
}