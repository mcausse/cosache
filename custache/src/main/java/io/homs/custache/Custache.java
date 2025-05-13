package io.homs.custache;

import io.homs.custache.eval.Context;
import io.homs.custache.parser.Parser;
import io.homs.custache.parser.ast.Ast;
import io.homs.custache.template.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.template.Template;
import io.homs.custache.template.TemplateLoadingStrategy;

public class Custache {

    private final TemplateLoadingStrategy templateLoadingStrategy;

    public Custache(TemplateLoadingStrategy templateLoadingStrategy) {
        this.templateLoadingStrategy = templateLoadingStrategy;
    }

    public Custache() {
        this(new DefaultClasspathTemplateLoadingStrategy());
    }

    public Ast loadParseredTemplate(String templateUrn) {
        Template loadedTemplate = templateLoadingStrategy.loadTemplate(templateUrn);
        return loadParseredTemplate(loadedTemplate);
    }

    public Ast loadParseredTemplate(Template loadedTemplate) {
        return new Parser(templateLoadingStrategy, loadedTemplate).parse();
    }

    public String evaluate(Ast templateAst, String modelName, Object model) {
        Context ctx = new Context();
        ctx.def(modelName, model);
        return evaluate(templateAst, ctx);
    }

    public String evaluate(Ast templateAst, Context ctx) {
        String result = templateAst.evaluate(ctx);
        return result;
    }
}
