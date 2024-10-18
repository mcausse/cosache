package io.homs.custache.ast;

import io.homs.custache.Context;
import io.homs.custache.TemplateLoadingStrategy;

public class IncludeAst extends Ast {

    public static final String TEMPLATE_LOADING_STRATEGY = "TEMPLATE_LOADING_STRATEGY";

    final String templateUrn;

    public IncludeAst(String templateId, int col, int row, String templateUrn) {
        super(templateId, col, row);
        this.templateUrn = templateUrn;
    }

    @Override
    public String evaluate(Context context) {
        TemplateLoadingStrategy templateLoadingStrategy = (TemplateLoadingStrategy) context.get(TEMPLATE_LOADING_STRATEGY);
        Ast parseredTemplate = templateLoadingStrategy.loadParseredTemplate(templateUrn);
        return parseredTemplate.evaluate(context);
    }

    @Override
    public String toString() {
        return "{{>" + templateUrn + "}}";
    }
}
