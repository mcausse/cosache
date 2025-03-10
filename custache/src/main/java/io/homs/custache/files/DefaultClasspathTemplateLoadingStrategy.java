package io.homs.custache.files;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URISyntaxException;

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
    public Template loadTemplate(String templateUrn) {
        final String fullUrn = prefix + templateUrn + suffix;
        try {
            return new Template(fullUrn, loadTemplateContent(fullUrn));
        } catch (Exception e) {
            throw new RuntimeException("problem loading: " + fullUrn, e);
        }
    }

    protected String loadTemplateContent(String fullUrn) throws URISyntaxException, IOException {
        return FileUtils.loadFromClasspath(fullUrn);
    }
}
