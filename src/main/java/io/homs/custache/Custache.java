package io.homs.custache;

import io.homs.custache.ast.Ast;
import io.homs.custache.ast.IncludeAst;
import lombok.SneakyThrows;

public class Custache {

    private final TemplateLoadingStrategy templateLoadingStrategy;

    public Custache(TemplateLoadingStrategy templateLoadingStrategy) {
        this.templateLoadingStrategy = templateLoadingStrategy;
    }

    public Custache() {
        this(new DefaultClasspathTemplateLoadingStrategy());
    }

    @SneakyThrows
    public String templateFromClasspath(String templateUrn, String modelName, Object model) {
        Ast ast = templateLoadingStrategy.loadParseredTemplate(templateUrn);
        Context ctx = new Context();
        ctx.def(modelName, model);
        ctx.def(IncludeAst.TEMPLATE_LOADING_STRATEGY, templateLoadingStrategy);
        String result = ast.evaluate(ctx);
        return result;
    }
}
