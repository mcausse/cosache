//package io.homs.custache.ast;
//
//import io.homs.custache.Context;
//
//import java.util.List;
//
//public class DefnAst extends Ast {
//
//    final String functionName;
//    final List<String> argumentNames;
//    final TemplateAst body;
//
//    public DefnAst(String templateId, int col, int row, String functionName, List<String> argumentNames, TemplateAst body) {
//        super(templateId, col, row);
//        this.functionName = functionName;
//        this.argumentNames = argumentNames;
//        this.body = body;
//    }
//
//    @Override
//    public String evaluate(Context context) {
//        context.def(functionName, this);
//        return "";
//    }
//
//    public List<String> getArgumentNames() {
//        return argumentNames;
//    }
//
//    public TemplateAst getBody() {
//        return body;
//    }
//
//    @Override
//    public String toString() {
//        return "{{defn " + functionName + " " + argumentNames + "}}" + body + "{{end}}";
//    }
//}