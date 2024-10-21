package io.homs.custache;

import io.homs.custache.ast.Ast;
import io.homs.custache.ast.TextAst;
import io.homs.custache.ast.ValueAst;
import io.homs.custache.files.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.files.TemplateLoadingStrategy;
import lombok.Value;

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
        defaultContextConfiguration(ctx);
        String result = templateAst.evaluate(ctx);
        return result;
    }

    protected void defaultContextConfiguration(Context ctx) {
        ctx.def(TextAst.TRIM_TEXT_AST, true);
        ctx.def(ValueAst.CONTEXT_PARAM_AUTOTRIM_VALUES, false);
    }

    public Evaluation newEvaluation(Ast templateAst) {
        Context ctx = new Context();
        defaultContextConfiguration(ctx);
        return new Evaluation(templateAst, ctx);
    }

    @Value
    public static class Evaluation {

        Ast templateAst;
        Context ctx;

        public Evaluation with(String key, Object value) {
            this.ctx.def(key, value);
            return this;
        }

        public String evaluate() {
            return templateAst.evaluate(ctx);
        }
    }
}
