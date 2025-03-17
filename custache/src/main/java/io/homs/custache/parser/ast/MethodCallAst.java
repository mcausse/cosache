package io.homs.custache.parser.ast;

import io.homs.custache.CustacheException;
import io.homs.custache.eval.Context;
import io.homs.custache.eval.Evaluation;

public class MethodCallAst extends Ast {

    final ExpressionAst objectExpression;
    final String methodName;
    final ExpressionAst argumentExpression;

    final Evaluation evaluation = new Evaluation();

    public MethodCallAst(String templateUrn, int row, int col, ExpressionAst objectExpression, String methodName, ExpressionAst argumentExpression) {
        super(templateUrn, row, col);
        this.objectExpression = objectExpression;
        this.methodName = methodName;
        this.argumentExpression = argumentExpression;
    }

    @Override
    public String evaluate(Context context) {
        Object targetObject = objectExpression.evaluateToObject(context);
        Object argumentValue = argumentExpression.evaluateToObject(context);
        try {
            Object r = evaluation.invokeMethod(targetObject, methodName, argumentValue);
            return String.valueOf(r);
        } catch (Exception e) {
            throw new CustacheException("evaluating expression: " + targetObject.getClass().getName() + "#" + methodName + "(" + argumentValue + ")" +
                    ", at: ", getTemplateUrn(), getRow(), getCol(), e);
        }
    }
}