package io.homs.custache;

import io.homs.custache.ast.Ast;
import io.homs.custache.ast.IncludeAst;
import io.homs.custache.ast.TextAst;

public class Custache {

    private final TemplateLoadingStrategy templateLoadingStrategy;

    public Custache(TemplateLoadingStrategy templateLoadingStrategy) {
        this.templateLoadingStrategy = templateLoadingStrategy;
    }

    public Custache() {
        this(new DefaultClasspathTemplateLoadingStrategy());
    }

    public Ast loadTemplate(String templateUrn) {
        Ast ast = templateLoadingStrategy.loadParseredTemplate(templateUrn);
        return ast;
    }

    public String evaluate(Ast templateAst, String modelName, Object model) {
        Context ctx = new Context();
        ctx.def(modelName, model);
        ctx.def(IncludeAst.TEMPLATE_LOADING_STRATEGY, templateLoadingStrategy);
        ctx.def(TextAst.TRIM_TEXT_AST, true);
        String result = templateAst.evaluate(ctx);
        return result;
    }
}
