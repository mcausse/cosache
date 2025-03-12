package io.homs.custache.ast;

import io.homs.custache.Context;
import io.homs.custache.Parser;
import io.homs.custache.files.TemplateLoadingStrategy;

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

        TemplateLoadingStrategy.Template loadedTemplate = templateLoadingStrategy.loadTemplate(templateUrn);
        Parser parser = new Parser(templateLoadingStrategy, loadedTemplate.getFullTemplateUrn(), loadedTemplate.getTemplateContent());
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
