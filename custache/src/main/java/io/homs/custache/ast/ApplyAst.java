//package io.homs.custache.ast;
//
//import io.homs.custache.Context;
//
//import java.util.List;
//import java.util.StringJoiner;
//
//public class ApplyAst extends Ast {
//
//    final String ident;
//    final List<ExpressionAst> argumentExpressions;
//
//    public ApplyAst(String templateId, int col, int row, String ident, List<ExpressionAst> argumentExpressions) {
//        super(templateId, col, row);
//        this.ident = ident;
//        this.argumentExpressions = argumentExpressions;
//    }
//
//    @Override
//    public String evaluate(Context context) {
//        final DefnAst defnAst;
//        try {
//            Object value = context.get(ident);
//            if ((!(value instanceof DefnAst))) {
//                throw new RuntimeException("the variable '" + ident + "' is not defined as a function; is of type=" + value.getClass() + "; " + super.toString());
//            }
//            defnAst = (DefnAst) value;
//
//            List<String> argumentNames = defnAst.getArgumentNames();
//            if (argumentNames.size() != argumentExpressions.size()) {
//                throw new RuntimeException("the function '" + ident + "' expects " + argumentNames.size() + " arguments, but provided: " + argumentExpressions.size() + "; " + super.toString());
//            }
//
//            Context newCtx = new Context(context);
//            for (int i = 0; i < argumentNames.size(); i++) {
//                String argumentName = argumentNames.get(i);
//                Object argumentValue = argumentExpressions.get(i).evaluate(context);
//                newCtx.def(argumentName, argumentValue);
//            }
//
//            return defnAst.evaluate(newCtx);
//
//        } catch (Exception e) {
//            throw new RuntimeException("evaluation error at " + super.toString(), e);
//        }
//    }
//
//    @Override
//    public String toString() {
//        var r = new StringBuilder();
//        r.append("{{apply " + ident);
//        if (!argumentExpressions.isEmpty()) {
//            r.append("(");
//            StringJoiner sj = new StringJoiner(", ");
//            for (ExpressionAst argumentExpression : argumentExpressions) {
//                sj.add(argumentExpression.toString());
//            }
//            r.append(")");
//            r.append(sj);
//        }
//        r.append("}}");
//        return r.toString();
//    }
//}