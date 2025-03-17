package io.homs.custache.parser.ast;

import io.homs.custache.eval.Context;
import io.homs.custache.parser.Parser;
import io.homs.custache.template.Template;
import io.homs.custache.template.TemplateLoadingStrategy;

import java.util.List;

public class IncludeAst extends Ast {

    final TemplateLoadingStrategy templateLoadingStrategy;
    final String templateUrn;

    final Ast parseredTemplate;
    final List<MappingPair> mappingPairs;

    public IncludeAst(String templateId, int row, int col, TemplateLoadingStrategy templateLoadingStrategy, String templateUrn, List<MappingPair> mappingPairs) {
        super(templateId, row, col);
        this.templateLoadingStrategy = templateLoadingStrategy;
        this.templateUrn = templateUrn;
        this.mappingPairs = mappingPairs;

        Template loadedTemplate = templateLoadingStrategy.loadTemplate(templateUrn);
        Parser parser = new Parser(templateLoadingStrategy, loadedTemplate);
        this.parseredTemplate = parser.parse();
    }

    @Override
    public String evaluate(Context context) {
        Context contextWithMappings = new Context(context);
        for (MappingPair mappingPair : mappingPairs) {
            contextWithMappings.def(mappingPair.getLeft(), mappingPair.getRight().evaluateToObject(context));
        }
        return parseredTemplate.evaluate(contextWithMappings);
    }
}
