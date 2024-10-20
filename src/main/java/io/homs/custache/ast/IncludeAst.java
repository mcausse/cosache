package io.homs.custache.ast;

import io.homs.custache.Context;
import io.homs.custache.Parser;
import io.homs.custache.files.TemplateLoadingStrategy;
import lombok.Value;

import java.util.List;
import java.util.StringJoiner;

public class IncludeAst extends Ast {

    final TemplateLoadingStrategy templateLoadingStrategy;
    final String templateUrn;

    final Ast parseredTemplate;
    final List<MappingPair> mappingPairs;

    public IncludeAst(String templateId, int col, int row, TemplateLoadingStrategy templateLoadingStrategy, String templateUrn, List<MappingPair> mappingPairs) {
        super(templateId, col, row);
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

    @Override
    public String toString() {
        var r = new StringBuilder();
        r.append("{{>" + templateUrn);
        if (!mappingPairs.isEmpty()) {
            r.append("(");
            var j = new StringJoiner(", ");
            for (var m : mappingPairs) {
                j.add(m.getLeft() + "=" + m.getRight());
            }
            r.append(j);
            r.append(")");
        }
        r.append("}}");
        return r.toString();
    }

    @Value
    public static class MappingPair {
        String left;
        ExpressionAst right;
    }
}
