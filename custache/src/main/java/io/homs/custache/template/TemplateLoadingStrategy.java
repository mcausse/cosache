package io.homs.custache.template;

public interface TemplateLoadingStrategy {

    Template loadTemplate(String templateUrn);
}
