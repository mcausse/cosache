package io.homs.custache.ast;

import io.homs.custache.Context;

import java.util.List;
import java.util.stream.Collectors;

public class TemplateAst extends Ast {

    final List<Ast> astsList;

    public TemplateAst(String templateId, int col, int row, List<Ast> astsList) {
        super(templateId, col, row);
        this.astsList = astsList;
    }

    @Override
    public String evaluate(Context context) {
        var strb = new StringBuilder();
        for (var ast : astsList) {
            strb.append(ast.evaluate(context));
        }
        return strb.toString();
    }

    @Override
    public String toString() {
        return astsList.stream().map(Object::toString).collect(Collectors.joining());
    }
}