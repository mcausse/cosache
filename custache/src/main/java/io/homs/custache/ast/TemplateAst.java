package io.homs.custache.ast;

import io.homs.custache.Context;
import lombok.Getter;

import java.util.List;

@Getter
public class TemplateAst extends Ast {

    protected final List<Ast> astsList;

    public TemplateAst(String templateId, int row, int col, List<Ast> astsList) {
        super(templateId, row, col);
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
}