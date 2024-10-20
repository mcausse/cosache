package io.homs.custache;

import io.homs.custache.ast.Ast;
import io.homs.custache.ast.TextAst;
import io.homs.custache.files.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.files.TemplateLoadingStrategy;

public class Custache {

    private final TemplateLoadingStrategy templateLoadingStrategy;

    public Custache(TemplateLoadingStrategy templateLoadingStrategy) {
        this.templateLoadingStrategy = templateLoadingStrategy;
    }

    public Custache() {
        this(new DefaultClasspathTemplateLoadingStrategy());
    }

    public Ast loadParseredTemplate(String templateUrn) {
        TemplateLoadingStrategy.Template loadedTemplate = templateLoadingStrategy.loadTemplate(templateUrn);
        Ast ast = new Parser(templateLoadingStrategy, loadedTemplate.getFullTemplateUrn(), loadedTemplate.getTemplateContent()).parse();
        return ast;
    }

    public String evaluate(Ast templateAst, String modelName, Object model) {
        Context ctx = new Context();
        ctx.def(modelName, model);
        ctx.def(TextAst.TRIM_TEXT_AST, true);
        String result = templateAst.evaluate(ctx);
        return result;
    }
}
