package io.homs.custache;

import io.homs.custache.ast.Ast;
import lombok.SneakyThrows;

public class DefaultClasspathTemplateLoadingStrategy implements TemplateLoadingStrategy {

    private final String prefix;
    private final String suffix;

    public DefaultClasspathTemplateLoadingStrategy(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public DefaultClasspathTemplateLoadingStrategy() {
        this("templates/", ".html");
    }

    @SneakyThrows
    @Override
    public Ast loadParseredTemplate(String templateUrn) {
        final String fullUrn = prefix + templateUrn + suffix;
        try {
            String templateContent = FileUtils.loadFromClasspath(fullUrn);
            return new Parser(fullUrn, templateContent).parse();
        } catch (Exception e) {
            throw new RuntimeException("problem loading: " + fullUrn, e);
        }
    }
}
