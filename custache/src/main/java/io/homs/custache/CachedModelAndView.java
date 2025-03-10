package io.homs.custache;


import io.homs.custache.ast.Ast;
import io.homs.custache.files.TemplateLoadingStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedModelAndView {

    private final TemplateLoadingStrategy templateLoadingStrategy;

    private final Map<String, Ast> cachedTemplates;

    public CachedModelAndView(TemplateLoadingStrategy templateLoadingStrategy) {
        this.templateLoadingStrategy = templateLoadingStrategy;
        this.cachedTemplates = new ConcurrentHashMap<>();
    }

    public static class ModelAndView {

        private final Ast templateAst;
        private final Context ctx;

        public ModelAndView(Ast templateAst, Context ctx) {
            this.templateAst = templateAst;
            this.ctx = ctx;
        }

        public ModelAndView(Ast templateAst) {
            this(templateAst, new Context());
        }

        public ModelAndView with(String key, Object value) {
            this.ctx.def(key, value);
            return this;
        }

        public String evaluate() {
            return templateAst.evaluate(ctx);
        }
    }

    public ModelAndView getOrParse(String urnPart) {

        if (!cachedTemplates.containsKey(urnPart)) {

            TemplateLoadingStrategy.Template template = templateLoadingStrategy.loadTemplate(urnPart);
            Ast templateAst = new Parser(templateLoadingStrategy, template.getFullTemplateUrn(), template.getTemplateContent()).parse();
            cachedTemplates.put(urnPart, templateAst);
        }

        Ast templateAst = cachedTemplates.get(urnPart);
        return new ModelAndView(templateAst);
    }
}
